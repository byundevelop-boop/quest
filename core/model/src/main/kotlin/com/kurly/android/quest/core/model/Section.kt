package com.kurly.android.quest.core.model

/** 홈 화면에 노출되는 섹션 메타 정보 */
data class Section(
    val id: Int,
    val title: String,
    val type: SectionType
)
