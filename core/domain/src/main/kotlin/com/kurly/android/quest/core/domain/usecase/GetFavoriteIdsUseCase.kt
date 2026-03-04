package com.kurly.android.quest.core.domain.usecase

import com.kurly.android.quest.core.domain.repository.FavoriteStore
import javax.inject.Inject

class GetFavoriteIdsUseCase @Inject constructor(
    private val favoriteStore: FavoriteStore
) {
    suspend operator fun invoke(): Result<Set<Long>> {
        return runCatching { favoriteStore.getFavoriteIds() }
    }
}
