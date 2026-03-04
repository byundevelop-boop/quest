package com.kurly.android.quest.feature.main.ui.formatter

import java.text.NumberFormat
import java.util.Locale

internal object PriceFormatter {
    private val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)

    fun price(value: Int): String {
        return "${numberFormat.format(value)}원"
    }

    fun discountRate(percent: Int): String {
        return "${percent.coerceAtLeast(0)}%"
    }
}
