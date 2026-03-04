package com.kurly.android.quest.core.network

import com.kurly.android.quest.core.model.SectionType
import com.kurly.android.quest.core.network.dto.PagingDto
import com.kurly.android.quest.core.network.dto.ProductDto
import com.kurly.android.quest.core.network.dto.ProductsResponseDto
import com.kurly.android.quest.core.network.dto.SectionDto
import com.kurly.android.quest.core.network.dto.SectionsResponseDto
import com.kurly.android.quest.core.network.dto.toModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DtoMappingTest {

    @Test
    fun `sections mapping filters invalid sections and maps paging`() {
        val dto = SectionsResponseDto(
            data = listOf(
                SectionDto(id = 1, title = "Section A", type = "horizontal"),
                SectionDto(id = -1, title = "Invalid", type = "grid"),
                SectionDto(id = 2, title = "", type = "vertical")
            ),
            paging = PagingDto(nextPage = 3)
        )

        val model = dto.toModel()

        assertEquals(1, model.sections.size)
        assertEquals(1, model.sections.first().id)
        assertEquals(SectionType.HORIZONTAL, model.sections.first().type)
        assertEquals(3, model.nextPage)
    }

    @Test
    fun `products mapping filters invalid records`() {
        val dto = ProductsResponseDto(
            data = listOf(
                ProductDto(
                    id = 100L,
                    name = "Valid Product",
                    image = "https://example.com/1.jpg",
                    originalPrice = 10000,
                    discountedPrice = 9000,
                    isSoldOut = false
                ),
                ProductDto(
                    id = 0L,
                    name = "Invalid Id",
                    image = "https://example.com/2.jpg",
                    originalPrice = 5000,
                    discountedPrice = null,
                    isSoldOut = false
                ),
                ProductDto(
                    id = 200L,
                    name = "",
                    image = "https://example.com/3.jpg",
                    originalPrice = 7000,
                    discountedPrice = null,
                    isSoldOut = false
                )
            )
        )

        val products = dto.toModel()

        assertEquals(1, products.size)
        assertEquals(100L, products.first().id)
        assertEquals(9000, products.first().discountedPrice)
    }

    @Test
    fun `missing paging maps to null next page`() {
        val dto = SectionsResponseDto(
            data = listOf(SectionDto(id = 1, title = "A", type = "grid")),
            paging = null
        )

        val model = dto.toModel()

        assertNull(model.nextPage)
    }
}
