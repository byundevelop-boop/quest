package com.kurly.android.quest.core.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class ToggleFavoriteUseCaseTest {

    private val useCase = ToggleFavoriteUseCase()

    @Test
    fun `adds id when not exists`() {
        val result = useCase(setOf(1L, 2L), 3L)
        assertEquals(setOf(1L, 2L, 3L), result)
    }

    @Test
    fun `removes id when already exists`() {
        val result = useCase(setOf(1L, 2L, 3L), 2L)
        assertEquals(setOf(1L, 3L), result)
    }
}
