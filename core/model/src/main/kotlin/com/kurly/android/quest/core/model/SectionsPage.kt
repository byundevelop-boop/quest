package com.kurly.android.quest.core.model

/** 섹션 목록 페이징 응답 모델 */
data class SectionsPage(
    val sections: List<Section>,
    // null이면 더 불러올 페이지가 없음(마지막 페이지)
    val nextPage: Int?
)
