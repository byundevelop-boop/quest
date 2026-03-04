package com.kurly.android.quest.core.domain.repository

import com.kurly.android.quest.core.model.Product
import com.kurly.android.quest.core.model.SectionsPage

interface MainRepository {
    /**
     * 섹션 목록 페이지를 조회한다.(mock은 페이지 당 섹션 5개씩 존재하는 것으로 확인, 총 4페이지)
     */
    suspend fun getSections(page: Int): Result<SectionsPage>

    /**
     * 특정 섹션에 속한 상품 목록을 조회한다.
     */
    suspend fun getSectionProducts(sectionId: Int): Result<List<Product>>
}
