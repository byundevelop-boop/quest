package com.kurly.android.quest.core.domain.policy

import kotlin.math.roundToInt

fun calculateDiscountRatePercent(
    originalPrice: Int,
    discountedPrice: Int?
): Int {
    if (originalPrice <= 0 || discountedPrice == null || discountedPrice >= originalPrice) {
        return 0
    }

    return (((originalPrice - discountedPrice).toFloat() / originalPrice.toFloat()) * 100f)
        .roundToInt()
}
