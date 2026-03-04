package com.kurly.android.quest.feature.main.presentation

import com.kurly.android.quest.core.domain.repository.MainRepository
import com.kurly.android.quest.core.model.Product
import com.kurly.android.quest.core.model.Section
import com.kurly.android.quest.core.model.SectionType
import com.kurly.android.quest.core.model.SectionsPage
import com.kurly.android.quest.core.domain.repository.FavoriteStore
import com.kurly.android.quest.core.domain.usecase.GetFavoriteIdsUseCase
import com.kurly.android.quest.core.domain.usecase.LoadSectionsPageUseCase
import com.kurly.android.quest.core.domain.usecase.SaveFavoriteIdsUseCase
import com.kurly.android.quest.core.domain.usecase.ToggleFavoriteUseCase
import com.kurly.android.quest.feature.main.model.MainError
import com.kurly.android.quest.feature.main.testutil.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial load brings first page sections`() = runTest {
        val viewModel = createViewModel(FakeRepository(), InMemoryFavoriteStore())
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isInitialLoading)
        assertEquals(2, state.sections.size)
        assertEquals(2, state.nextPage)
        assertNull(state.blockingError)
    }

    @Test
    fun `toggle favorite updates duplicated product ids across sections`() = runTest {
        val favoriteStore = InMemoryFavoriteStore()
        val viewModel = createViewModel(FakeRepository(), favoriteStore)
        advanceUntilIdle()

        viewModel.toggleFavorite(100L)

        val products = viewModel.uiState.value.sections.flatMap { it.products }
        val targetProducts = products.filter { it.id == 100L }
        assertTrue(targetProducts.isNotEmpty())
        assertTrue(targetProducts.all { it.isFavorite })
        assertTrue(100L in favoriteStore.ids)
    }

    @Test
    fun `load next page appends and stops when next page is null`() = runTest {
        val viewModel = createViewModel(FakeRepository(), InMemoryFavoriteStore())
        advanceUntilIdle()

        viewModel.loadNextPage()
        advanceUntilIdle()

        val afterPage2 = viewModel.uiState.value
        assertEquals(3, afterPage2.sections.size)
        assertEquals(null, afterPage2.nextPage)

        viewModel.loadNextPage()
        advanceUntilIdle()

        val afterSecondTry = viewModel.uiState.value
        assertEquals(3, afterSecondTry.sections.size)
    }

    @Test
    fun `initial failure exposes blocking error`() = runTest {
        val viewModel = createViewModel(FailingRepository(), InMemoryFavoriteStore())
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.sections.isEmpty())
        assertEquals(MainError.LOAD_FAILED, state.blockingError)
        assertNull(state.inlineError)
    }

    @Test
    fun `refresh failure keeps sections and shows inline error`() = runTest {
        val viewModel = createViewModel(RefreshFailRepository(), InMemoryFavoriteStore())
        advanceUntilIdle()

        viewModel.refresh()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.sections.isNotEmpty())
        assertEquals(MainError.LOAD_FAILED, state.inlineError)
        assertNull(state.blockingError)
    }

    @Test
    fun `retry repeats last failed append request`() = runTest {
        val repository = AppendFailOnceRepository()
        val viewModel = createViewModel(repository, InMemoryFavoriteStore())
        advanceUntilIdle()

        viewModel.loadNextPage()
        advanceUntilIdle()

        val failedState = viewModel.uiState.value
        assertEquals(2, failedState.sections.size)
        assertEquals(MainError.LOAD_FAILED, failedState.inlineError)

        viewModel.retry()
        advanceUntilIdle()

        val recoveredState = viewModel.uiState.value
        assertEquals(3, recoveredState.sections.size)
        assertNull(recoveredState.inlineError)
        assertEquals(2, repository.page2RequestCount)
    }

    @Test
    fun `concurrent load next page calls trigger only one request`() = runTest {
        val repository = SlowPagingRepository()
        val viewModel = createViewModel(repository, InMemoryFavoriteStore())
        advanceUntilIdle()

        viewModel.loadNextPage()
        viewModel.loadNextPage()

        advanceUntilIdle()
        assertEquals(1, repository.page2RequestCount)

        repository.releasePage2()
        advanceUntilIdle()

        assertEquals(3, viewModel.uiState.value.sections.size)
    }

    @Test
    fun `toggle favorite shows inline error when persistence fails`() = runTest {
        val viewModel = createViewModel(FakeRepository(), FailingFavoriteStore())
        advanceUntilIdle()

        viewModel.toggleFavorite(100L)
        advanceUntilIdle()

        assertEquals(MainError.FAVORITE_SAVE_FAILED, viewModel.uiState.value.inlineError)
    }

    @Test
    fun `retry inline error retries favorite save and clears error on success`() = runTest {
        val favoriteStore = FailOnceFavoriteStore()
        val viewModel = createViewModel(FakeRepository(), favoriteStore)
        advanceUntilIdle()

        viewModel.toggleFavorite(100L)
        advanceUntilIdle()
        assertEquals(MainError.FAVORITE_SAVE_FAILED, viewModel.uiState.value.inlineError)

        viewModel.retryInlineError()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.inlineError)
        assertTrue(100L in favoriteStore.ids)
        assertEquals(2, favoriteStore.saveAttemptCount)
    }

    @Test
    fun `initial favorites from store are reflected in ui`() = runTest {
        val favoriteStore = InMemoryFavoriteStore(mutableSetOf(100L))
        val viewModel = createViewModel(FakeRepository(), favoriteStore)
        advanceUntilIdle()

        val targetProducts = viewModel.uiState.value.sections
            .flatMap { section -> section.products }
            .filter { product -> product.id == 100L }

        assertTrue(targetProducts.isNotEmpty())
        assertTrue(targetProducts.all { product -> product.isFavorite })
    }

    private fun createViewModel(
        repository: MainRepository,
        favoriteStore: FavoriteStore
    ): MainViewModel {
        return MainViewModel(
            loadSectionsPageUseCase = LoadSectionsPageUseCase(repository),
            getFavoriteIdsUseCase = GetFavoriteIdsUseCase(favoriteStore),
            toggleFavoriteUseCase = ToggleFavoriteUseCase(),
            saveFavoriteIdsUseCase = SaveFavoriteIdsUseCase(favoriteStore)
        )
    }

    private class FakeRepository : MainRepository {
        override suspend fun getSections(page: Int): Result<SectionsPage> {
            return when (page) {
                1 -> Result.success(
                    SectionsPage(
                        sections = listOf(
                            Section(id = 1, title = "A", type = SectionType.HORIZONTAL),
                            Section(id = 2, title = "B", type = SectionType.VERTICAL)
                        ),
                        nextPage = 2
                    )
                )

                2 -> Result.success(
                    SectionsPage(
                        sections = listOf(
                            Section(id = 3, title = "C", type = SectionType.GRID)
                        ),
                        nextPage = null
                    )
                )

                else -> Result.failure(IllegalArgumentException("invalid page"))
            }
        }

        override suspend fun getSectionProducts(sectionId: Int): Result<List<Product>> {
            return Result.success(productsForSection(sectionId))
        }
    }

    private class FailingRepository : MainRepository {
        override suspend fun getSections(page: Int): Result<SectionsPage> {
            return Result.failure(IllegalStateException("forced failure"))
        }

        override suspend fun getSectionProducts(sectionId: Int): Result<List<Product>> {
            return Result.success(emptyList())
        }
    }

    private class RefreshFailRepository : MainRepository {
        private var page1RequestCount = 0

        override suspend fun getSections(page: Int): Result<SectionsPage> {
            if (page != 1) {
                return Result.failure(IllegalArgumentException("invalid page"))
            }

            page1RequestCount += 1
            return if (page1RequestCount == 1) {
                Result.success(
                    SectionsPage(
                        sections = listOf(
                            Section(id = 1, title = "A", type = SectionType.HORIZONTAL),
                            Section(id = 2, title = "B", type = SectionType.VERTICAL)
                        ),
                        nextPage = 2
                    )
                )
            } else {
                Result.failure(IllegalStateException("refresh failed"))
            }
        }

        override suspend fun getSectionProducts(sectionId: Int): Result<List<Product>> {
            return Result.success(productsForSection(sectionId))
        }
    }

    private class AppendFailOnceRepository : MainRepository {
        var page2RequestCount: Int = 0
            private set

        override suspend fun getSections(page: Int): Result<SectionsPage> {
            return when (page) {
                1 -> Result.success(
                    SectionsPage(
                        sections = listOf(
                            Section(id = 1, title = "A", type = SectionType.HORIZONTAL),
                            Section(id = 2, title = "B", type = SectionType.VERTICAL)
                        ),
                        nextPage = 2
                    )
                )

                2 -> {
                    page2RequestCount += 1
                    if (page2RequestCount == 1) {
                        Result.failure(IllegalStateException("append failed"))
                    } else {
                        Result.success(
                            SectionsPage(
                                sections = listOf(
                                    Section(id = 3, title = "C", type = SectionType.GRID)
                                ),
                                nextPage = null
                            )
                        )
                    }
                }

                else -> Result.failure(IllegalArgumentException("invalid page"))
            }
        }

        override suspend fun getSectionProducts(sectionId: Int): Result<List<Product>> {
            return Result.success(productsForSection(sectionId))
        }
    }

    private class SlowPagingRepository : MainRepository {
        private val page2Gate = CompletableDeferred<Unit>()
        var page2RequestCount: Int = 0
            private set

        override suspend fun getSections(page: Int): Result<SectionsPage> {
            return when (page) {
                1 -> Result.success(
                    SectionsPage(
                        sections = listOf(
                            Section(id = 1, title = "A", type = SectionType.HORIZONTAL),
                            Section(id = 2, title = "B", type = SectionType.VERTICAL)
                        ),
                        nextPage = 2
                    )
                )

                2 -> {
                    page2RequestCount += 1
                    page2Gate.await()
                    Result.success(
                        SectionsPage(
                            sections = listOf(
                                Section(id = 3, title = "C", type = SectionType.GRID)
                            ),
                            nextPage = null
                        )
                    )
                }

                else -> Result.failure(IllegalArgumentException("invalid page"))
            }
        }

        override suspend fun getSectionProducts(sectionId: Int): Result<List<Product>> {
            return Result.success(productsForSection(sectionId))
        }

        fun releasePage2() {
            page2Gate.complete(Unit)
        }
    }

    private class InMemoryFavoriteStore(
        val ids: MutableSet<Long> = mutableSetOf()
    ) : FavoriteStore {
        override suspend fun getFavoriteIds(): Set<Long> = ids.toSet()

        override suspend fun saveFavoriteIds(ids: Set<Long>) {
            this.ids.clear()
            this.ids.addAll(ids)
        }
    }

    private class FailingFavoriteStore : FavoriteStore {
        override suspend fun getFavoriteIds(): Set<Long> = emptySet()

        override suspend fun saveFavoriteIds(ids: Set<Long>) {
            error("failed to save")
        }
    }

    private class FailOnceFavoriteStore : FavoriteStore {
        val ids: MutableSet<Long> = mutableSetOf()
        var saveAttemptCount: Int = 0
            private set

        override suspend fun getFavoriteIds(): Set<Long> = ids.toSet()

        override suspend fun saveFavoriteIds(ids: Set<Long>) {
            saveAttemptCount += 1
            if (saveAttemptCount == 1) {
                error("save failed once")
            }
            this.ids.clear()
            this.ids.addAll(ids)
        }
    }
}

private fun productsForSection(sectionId: Int): List<Product> {
    return when (sectionId) {
        1 -> listOf(
            Product(
                id = 100,
                name = "P1",
                image = "",
                originalPrice = 10000,
                discountedPrice = 9000,
                isSoldOut = false
            )
        )

        2 -> listOf(
            Product(
                id = 100,
                name = "P1 duplicated",
                image = "",
                originalPrice = 10000,
                discountedPrice = 9000,
                isSoldOut = false
            ),
            Product(
                id = 200,
                name = "P2",
                image = "",
                originalPrice = 5000,
                discountedPrice = null,
                isSoldOut = false
            )
        )

        3 -> listOf(
            Product(
                id = 300,
                name = "P3",
                image = "",
                originalPrice = 3000,
                discountedPrice = null,
                isSoldOut = false
            )
        )

        else -> emptyList()
    }
}
