package com.kurly.android.quest.core.network

/**
 * Data 계층 구현체: Network API 응답을 도메인 모델(MainRepository)로 매핑한다.
 */

import com.kurly.android.quest.core.domain.repository.MainRepository
import com.kurly.android.quest.core.model.Product
import com.kurly.android.quest.core.model.SectionsPage
import com.kurly.android.quest.core.network.dto.toModel

internal class MainRepositoryImpl(
    private val api: KurlyApi
) : MainRepository {

    override suspend fun getSections(page: Int): Result<SectionsPage> {
        // 네트워크 요청+매핑 실패를 Result로 묶어 상위에서 공통 처리할 수 있게 한다.
        return runCatching {
            api.getSections(page).toModel()
        }
    }

    override suspend fun getSectionProducts(sectionId: Int): Result<List<Product>> {
        // 네트워크 요청+매핑 실패를 Result로 묶어 상위에서 공통 처리할 수 있게 한다.
        return runCatching {
            api.getSectionProducts(sectionId).toModel()
        }
    }
}
