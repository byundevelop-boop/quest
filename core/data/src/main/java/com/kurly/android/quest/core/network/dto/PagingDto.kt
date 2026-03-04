package com.kurly.android.quest.core.network.dto

import com.google.gson.annotations.SerializedName

// 페이징 정보 DTO
internal data class PagingDto(
    // JSON의 next_page를 Kotlin 속성 nextPage로 매핑
    @SerializedName("next_page")
    val nextPage: Int? = null
)
