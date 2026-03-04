package com.kurly.android.quest.core.domain.policy

import org.junit.Assert.assertEquals
import org.junit.Test

class DiscountPolicyTest {

    @Test
    fun `returns rounded discount rate percent`() {
        assertEquals(25, calculateDiscountRatePercent(originalPrice = 10000, discountedPrice = 7500))
    }

    @Test
    fun `returns zero when discount is missing`() {
        assertEquals(0, calculateDiscountRatePercent(originalPrice = 10000, discountedPrice = null))
    }

    @Test
    fun `returns zero when discounted price is not lower than original`() {
        assertEquals(0, calculateDiscountRatePercent(originalPrice = 5000, discountedPrice = 5000))
    }
}
