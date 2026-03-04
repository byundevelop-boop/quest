package com.kurly.android.quest.core.network.dto

import com.google.gson.annotations.SerializedName
import com.kurly.android.quest.core.model.Product

// 상품 목록 응답 DTO
internal data class ProductsResponseDto(
    // 응답 본문 안의 상품 목록
    @SerializedName("data")
    val data: List<ProductDto> = emptyList()
)

// DTO 리스트를 Product 모델 리스트로 변환
internal fun ProductsResponseDto.toModel(): List<Product> {
    return data
        .asSequence()
        // 상품 데이터 유효성 검사(ID/이름/이미지/가격)
        .filter { product ->
            product.id > 0 &&
                product.name.isNotBlank() &&
                product.image.isNotBlank() &&
                product.originalPrice > 0
        }
        .map { product -> product.toModel() }
        .toList()
}
