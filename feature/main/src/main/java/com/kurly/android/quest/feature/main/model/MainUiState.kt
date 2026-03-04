package com.kurly.android.quest.feature.main.model

data class MainUiState(
    val sections: List<SectionUiModel> = emptyList(),
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingNextPage: Boolean = false,
    val nextPage: Int? = 1,
    val blockingError: MainError? = null,
    val inlineError: MainError? = null
)
