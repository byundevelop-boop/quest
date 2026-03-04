package com.kurly.android.quest.core.domain.usecase

import com.kurly.android.quest.core.domain.repository.MainRepository
import com.kurly.android.quest.core.model.Product
import com.kurly.android.quest.core.model.Section
import com.kurly.android.quest.core.model.SectionType
import com.kurly.android.quest.core.model.SectionsPage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoadSectionsPageUseCaseTest {

    @Test
    fun `loads sections and products into combined page model`() = runTest {
        val useCase = LoadSectionsPageUseCase(FakeRepository())

        val result = useCase(1)

        assertTrue(result.isSuccess)
        val model = result.getOrThrow()
        assertEquals(2, model.sections.size)
        assertEquals(2, model.nextPage)
        assertEquals(1, model.sections.first().products.size)
    }

    @Test
    fun `returns failure when section call fails`() = runTest {
        val useCase = LoadSectionsPageUseCase(FailingRepository())

        val result = useCase(1)

        assertTrue(result.isFailure)
    }

    private class FakeRepository : MainRepository {
        override suspend fun getSections(page: Int): Result<SectionsPage> {
            return Result.success(
                SectionsPage(
                    sections = listOf(
                        Section(id = 1, title = "A", type = SectionType.HORIZONTAL),
                        Section(id = 2, title = "B", type = SectionType.VERTICAL)
                    ),
                    nextPage = 2
                )
            )
        }

        override suspend fun getSectionProducts(sectionId: Int): Result<List<Product>> {
            return Result.success(
                when (sectionId) {
                    1 -> listOf(
                        Product(
                            id = 101,
                            name = "A1",
                            image = "",
                            originalPrice = 1000,
                            discountedPrice = null,
                            isSoldOut = false
                        )
                    )

                    2 -> listOf(
                        Product(
                            id = 201,
                            name = "B1",
                            image = "",
                            originalPrice = 2000,
                            discountedPrice = 1500,
                            isSoldOut = false
                        )
                    )

                    else -> emptyList()
                }
            )
        }
    }

    private class FailingRepository : MainRepository {
        override suspend fun getSections(page: Int): Result<SectionsPage> {
            return Result.failure(IllegalStateException("fail"))
        }

        override suspend fun getSectionProducts(sectionId: Int): Result<List<Product>> {
            return Result.success(emptyList())
        }
    }
}
