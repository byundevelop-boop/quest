package com.kurly.android.quest.core.domain.repository

interface FavoriteStore {
    suspend fun getFavoriteIds(): Set<Long>
    suspend fun saveFavoriteIds(ids: Set<Long>)
}
