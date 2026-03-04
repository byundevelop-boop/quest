package com.kurly.android.quest.core.model

/** 섹션 내에 표시되는 상품 정보 */
data class Product(
    val id: Long,
    val name: String,
    val image: String,
    val originalPrice: Int,
    val discountedPrice: Int?,
    val isSoldOut: Boolean
)
