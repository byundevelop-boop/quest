package com.kurly.android.quest.core.domain.usecase

import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor() {
    operator fun invoke(currentFavoriteIds: Set<Long>, productId: Long): Set<Long> {
        val updated = currentFavoriteIds.toMutableSet()
        if (!updated.add(productId)) {
            updated.remove(productId)
        }
        return updated.toSet()
    }
}
