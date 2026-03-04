package com.kurly.android.quest.core.domain.usecase

import com.kurly.android.quest.core.domain.repository.FavoriteStore
import javax.inject.Inject

class SaveFavoriteIdsUseCase @Inject constructor(
    private val favoriteStore: FavoriteStore
) {
    suspend operator fun invoke(ids: Set<Long>): Result<Unit> {
        return runCatching { favoriteStore.saveFavoriteIds(ids) }
    }
}
