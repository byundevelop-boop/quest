package com.kurly.android.quest.feature.main.ui.formatter

import org.junit.Assert.assertEquals
import org.junit.Test

class PriceFormatterTest {

    @Test
    fun `price format adds comma and won`() {
        assertEquals("6,200원", PriceFormatter.price(6200))
    }

    @Test
    fun `discount rate formats percent value`() {
        assertEquals("25%", PriceFormatter.discountRate(25))
    }

    @Test
    fun `discount rate clamps negative value to zero`() {
        assertEquals("0%", PriceFormatter.discountRate(-3))
    }
}
