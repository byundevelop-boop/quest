package com.kurly.android.quest.feature.main.model

import androidx.compose.runtime.Immutable
import com.kurly.android.quest.core.model.SectionType

@Immutable
data class SectionUiModel(
    val id: Int,
    val title: String,
    val type: SectionType,
    val products: List<ProductUiModel>
)

@Immutable
data class ProductUiModel(
    val id: Long,
    val name: String,
    val image: String,
    val originalPrice: Int,
    val discountedPrice: Int?,
    val discountRatePercent: Int,
    val isSoldOut: Boolean,
    val isFavorite: Boolean
) {
    val hasDiscount: Boolean
        get() = discountedPrice != null && discountedPrice < originalPrice
}
