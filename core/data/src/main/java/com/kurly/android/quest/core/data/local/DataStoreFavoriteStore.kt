package com.kurly.android.quest.core.data.local

/**
 * Data 계층의 로컬 데이터 소스: 앱의 찜 상품 ID 목록을 DataStore로 보관한다.
 */

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kurly.android.quest.core.domain.repository.FavoriteStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

private val Context.favoriteDataStore by preferencesDataStore(name = "favorite_store")

class DataStoreFavoriteStore @Inject constructor(
    @ApplicationContext private val context: Context
) : FavoriteStore {

    override suspend fun getFavoriteIds(): Set<Long> {
        return context.favoriteDataStore.data
            .catch { throwable ->
                if (throwable is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw throwable
                }
            }
            .map { preferences ->
                preferences[FAVORITE_IDS_KEY].orEmpty()
            }
            .first()
            .mapNotNull { value -> value.toLongOrNull() }
            .toSet()
    }

    override suspend fun saveFavoriteIds(ids: Set<Long>) {
        val values = ids.map { it.toString() }.toSet()
        context.favoriteDataStore.edit { preferences ->
            preferences[FAVORITE_IDS_KEY] = values
        }
    }

    private companion object {
        val FAVORITE_IDS_KEY = stringSetPreferencesKey("favorite_ids")
    }
}
