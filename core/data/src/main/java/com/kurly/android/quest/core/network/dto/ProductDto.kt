package com.kurly.android.quest.core.network.dto

import com.google.gson.annotations.SerializedName
import com.kurly.android.quest.core.model.Product

// 상품 하나의 네트워크 응답 DTO
internal data class ProductDto(
    // 상품 고유 번호
    @SerializedName("id")
    val id: Long = -1L,
    // 상품명
    @SerializedName("name")
    val name: String = "",
    // 썸네일/대표 이미지 URL
    @SerializedName("image")
    val image: String = "",
    // 원가
    @SerializedName("originalPrice")
    val originalPrice: Int = 0,
    // 할인 가격(할인이 없으면 null)
    @SerializedName("discountedPrice")
    val discountedPrice: Int? = null,
    // 품절 여부
    @SerializedName("isSoldOut")
    val isSoldOut: Boolean = false
)

// 단일 ProductDto를 Product 모델로 변환
internal fun ProductDto.toModel(): Product {
    return Product(
        id = id,
        name = name,
        image = image,
        originalPrice = originalPrice,
        discountedPrice = discountedPrice,
        isSoldOut = isSoldOut
    )
}
