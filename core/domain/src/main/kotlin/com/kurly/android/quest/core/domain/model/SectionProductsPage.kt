package com.kurly.android.quest.core.domain.model

import com.kurly.android.quest.core.model.Product
import com.kurly.android.quest.core.model.SectionType

data class SectionProductsPage(
    val sections: List<SectionProducts>,
    val nextPage: Int?
)

data class SectionProducts(
    val id: Int,
    val title: String,
    val type: SectionType,
    val products: List<Product>
)
