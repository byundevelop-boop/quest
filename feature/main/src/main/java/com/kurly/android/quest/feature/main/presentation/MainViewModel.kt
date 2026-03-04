package com.kurly.android.quest.feature.main.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kurly.android.quest.core.model.Product
import com.kurly.android.quest.core.domain.model.SectionProducts
import com.kurly.android.quest.core.domain.policy.calculateDiscountRatePercent
import com.kurly.android.quest.core.domain.usecase.GetFavoriteIdsUseCase
import com.kurly.android.quest.core.domain.usecase.LoadSectionsPageUseCase
import com.kurly.android.quest.core.domain.usecase.SaveFavoriteIdsUseCase
import com.kurly.android.quest.core.domain.usecase.ToggleFavoriteUseCase
import com.kurly.android.quest.feature.main.model.MainError
import com.kurly.android.quest.feature.main.model.MainUiState
import com.kurly.android.quest.feature.main.model.ProductUiModel
import com.kurly.android.quest.feature.main.model.SectionUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
    private val loadSectionsPageUseCase: LoadSectionsPageUseCase,
    private val getFavoriteIdsUseCase: GetFavoriteIdsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val saveFavoriteIdsUseCase: SaveFavoriteIdsUseCase
) : ViewModel() {

    private var favoriteIds: Set<Long> = emptySet()
    private var activeLoadJob: Job? = null
    private var latestRequestToken: Long = 0L
    private var lastFailedRequest: LoadRequest? = null

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            favoriteIds = getFavoriteIdsUseCase()
                .getOrDefault(emptySet())
            loadInitialPage()
        }
    }

    fun refresh() {
        enqueueLoad(
            request = LoadRequest(page = FIRST_PAGE, type = LoadType.REFRESH),
            cancelInFlight = true
        )
    }

    fun retry() {
        lastFailedRequest?.let { failedRequest ->
            enqueueLoad(
                request = failedRequest,
                cancelInFlight = failedRequest.type != LoadType.APPEND
            )
            return
        }

        if (_uiState.value.sections.isEmpty()) loadInitialPage() else loadNextPage()
    }

    fun loadNextPage() {
        val state = _uiState.value
        val nextPage = state.nextPage ?: return
        if (state.isInitialLoading || state.isRefreshing || state.isLoadingNextPage) {
            return
        }

        enqueueLoad(
            request = LoadRequest(page = nextPage, type = LoadType.APPEND),
            cancelInFlight = false
        )
    }

    fun toggleFavorite(productId: Long) {
        favoriteIds = toggleFavoriteUseCase(favoriteIds, productId)
        val isFavoriteNow = productId in favoriteIds

        _uiState.update { current ->
            current.copy(
                sections = current.sections.map { section ->
                    section.copy(
                        products = section.products.map { product ->
                            if (product.id == productId) {
                                product.copy(isFavorite = isFavoriteNow)
                            } else {
                                product
                            }
                        }
                    )
                }
            )
        }

        persistFavorites()
    }

    fun clearInlineError() {
        _uiState.update { current ->
            current.copy(inlineError = null)
        }
    }

    fun retryInlineError() {
        when (_uiState.value.inlineError) {
            MainError.LOAD_FAILED -> retry()
            MainError.FAVORITE_SAVE_FAILED -> persistFavorites()
            null -> Unit
        }
    }

    private fun loadInitialPage() {
        enqueueLoad(
            request = LoadRequest(page = FIRST_PAGE, type = LoadType.INITIAL),
            cancelInFlight = true
        )
    }

    private fun enqueueLoad(
        request: LoadRequest,
        cancelInFlight: Boolean
    ) {
        viewModelScope.launch {
            if (cancelInFlight) {
                activeLoadJob?.cancelAndJoin()
            } else if (activeLoadJob?.isActive == true) {
                return@launch
            }

            val requestToken = ++latestRequestToken
            activeLoadJob = launch {
                _uiState.update { current ->
                    current.copy(
                        isInitialLoading = request.type == LoadType.INITIAL,
                        isRefreshing = request.type == LoadType.REFRESH,
                        isLoadingNextPage = request.type == LoadType.APPEND,
                        blockingError = null,
                        inlineError = null
                    )
                }

                runCatching { fetchPage(request.page) }
                    .onSuccess { (sections, nextPage) ->
                        if (requestToken != latestRequestToken) {
                            return@onSuccess
                        }

                        lastFailedRequest = null
                        _uiState.update { current ->
                            current.copy(
                                sections = if (request.type == LoadType.APPEND) {
                                    mergeSections(current.sections, sections)
                                } else {
                                    sections
                                },
                                isInitialLoading = false,
                                isRefreshing = false,
                                isLoadingNextPage = false,
                                nextPage = nextPage,
                                blockingError = null,
                                inlineError = null
                            )
                        }
                    }
                    .onFailure { throwable ->
                        if (throwable is CancellationException) {
                            return@onFailure
                        }

                        if (requestToken != latestRequestToken) {
                            return@onFailure
                        }

                        lastFailedRequest = request
                        _uiState.update { current ->
                            val hasContent = current.sections.isNotEmpty()
                            current.copy(
                                isInitialLoading = false,
                                isRefreshing = false,
                                isLoadingNextPage = false,
                                blockingError = if (hasContent) null else MainError.LOAD_FAILED,
                                inlineError = if (hasContent) MainError.LOAD_FAILED else null
                            )
                        }
                    }
            }
        }
    }

    private suspend fun fetchPage(page: Int): Pair<List<SectionUiModel>, Int?> {
        val loadedPage = loadSectionsPageUseCase(page).getOrThrow()
        val sectionModels = loadedPage.sections.map { section ->
            section.toUiModel(favoriteIds)
        }
        return sectionModels to loadedPage.nextPage
    }

    private fun mergeSections(
        currentSections: List<SectionUiModel>,
        incomingSections: List<SectionUiModel>
    ): List<SectionUiModel> {
        if (currentSections.isEmpty()) return incomingSections

        val existingIds = currentSections.mapTo(hashSetOf()) { it.id }
        val deduplicatedIncoming = incomingSections.filter { section ->
            existingIds.add(section.id)
        }
        return currentSections + deduplicatedIncoming
    }

    private fun SectionProducts.toUiModel(favoriteIds: Set<Long>): SectionUiModel {
        return SectionUiModel(
            id = id,
            title = title,
            type = type,
            products = products.map { product -> product.toUiModel(favoriteIds) }
        )
    }

    private fun Product.toUiModel(favoriteIds: Set<Long>): ProductUiModel {
        return ProductUiModel(
            id = id,
            name = name,
            image = image,
            originalPrice = originalPrice,
            discountedPrice = discountedPrice,
            discountRatePercent = calculateDiscountRatePercent(originalPrice, discountedPrice),
            isSoldOut = isSoldOut,
            isFavorite = favoriteIds.contains(id)
        )
    }

    private fun persistFavorites() {
        val snapshot = favoriteIds.toSet()
        viewModelScope.launch {
            saveFavoriteIdsUseCase(snapshot)
                .onSuccess {
                    _uiState.update { current ->
                        if (current.inlineError == MainError.FAVORITE_SAVE_FAILED) {
                            current.copy(inlineError = null)
                        } else {
                            current
                        }
                    }
                }
                .onFailure {
                    _uiState.update { current ->
                        if (current.sections.isEmpty()) {
                            current
                        } else {
                            current.copy(inlineError = MainError.FAVORITE_SAVE_FAILED)
                        }
                    }
                }
        }
    }

    private data class LoadRequest(
        val page: Int,
        val type: LoadType
    )

    private enum class LoadType {
        INITIAL,
        REFRESH,
        APPEND
    }

    private companion object {
        const val FIRST_PAGE = 1
    }
}
