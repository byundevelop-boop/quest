package com.kurly.android.quest.core.network.dto

import com.google.gson.annotations.SerializedName
import com.kurly.android.quest.core.model.Section
import com.kurly.android.quest.core.model.SectionType
import com.kurly.android.quest.core.model.SectionsPage

// 네트워크 응답 JSON을 받을 때 사용하는 DTO 모델
internal data class SectionsResponseDto(
    // 응답 본문 안의 섹션 목록
    @SerializedName("data")
    val data: List<SectionDto> = emptyList(),
    // 다음 페이지 정보(없으면 null)
    @SerializedName("paging")
    val paging: PagingDto? = null
)

// DTO -> 앱 내부 도메인 모델 변환
internal fun SectionsResponseDto.toModel(): SectionsPage {
    return SectionsPage(
        sections = data
            .asSequence()
            .filter { section -> section.id > 0 && section.title.isNotBlank() }
            .map { section ->
                Section(
                    id = section.id,
                    title = section.title,
                    type = SectionType.from(section.type)
                )
            }
            .toList(),
        nextPage = paging?.nextPage
    )
}
