package com.kurly.android.quest.core.domain.usecase

import com.kurly.android.quest.core.domain.repository.MainRepository
import com.kurly.android.quest.core.domain.model.SectionProducts
import com.kurly.android.quest.core.domain.model.SectionProductsPage
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class LoadSectionsPageUseCase @Inject constructor(
    private val repository: MainRepository
) {
    suspend operator fun invoke(page: Int): Result<SectionProductsPage> {
        return runCatching {
            val sectionsPage = repository.getSections(page).getOrThrow()

            val productsBySection = coroutineScope {
                sectionsPage.sections
                    .map { section ->
                        async {
                            val products = repository.getSectionProducts(section.id)
                                .getOrElse { emptyList() }
                            section.id to products
                        }
                    }
                    .awaitAll()
                    .toMap()
            }

            SectionProductsPage(
                sections = sectionsPage.sections.map { section ->
                    SectionProducts(
                        id = section.id,
                        title = section.title,
                        type = section.type,
                        products = productsBySection[section.id].orEmpty()
                    )
                },
                nextPage = sectionsPage.nextPage
            )
        }
    }
}
