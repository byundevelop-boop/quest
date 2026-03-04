package com.kurly.android.quest.core.network

/**
 * Network 서비스 인터페이스: sections/products API 엔드포인트 정의.
 */

import com.kurly.android.quest.core.network.dto.ProductsResponseDto
import com.kurly.android.quest.core.network.dto.SectionsResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

internal interface KurlyApi {
    @GET("sections")
    suspend fun getSections(
        @Query("page") page: Int
    ): SectionsResponseDto

    @GET("section/products")
    suspend fun getSectionProducts(
        @Query("sectionId") sectionId: Int
    ): ProductsResponseDto
}
