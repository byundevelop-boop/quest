package com.kurly.android.quest.core.network.dto

import com.google.gson.annotations.SerializedName

// 화면에 표시할 섹션 하나의 네트워크 응답 구조
internal data class SectionDto(
    // 서버에서 받은 섹션 고유 ID
    @SerializedName("id")
    val id: Int = -1,
    // 섹션 제목
    @SerializedName("title")
    val title: String = "",
    // 섹션 타입 문자열(예: "vertical")
    @SerializedName("type")
    val type: String = "vertical"
)
