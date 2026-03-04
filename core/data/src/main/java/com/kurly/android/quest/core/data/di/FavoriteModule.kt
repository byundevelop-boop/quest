package com.kurly.android.quest.core.data.di

/**
 * Data 계층의 DI 바인딩 모듈: Domain 인터페이스(FavoriteStore)와 구현체를 연결한다.
 */

import com.kurly.android.quest.core.data.local.DataStoreFavoriteStore
import com.kurly.android.quest.core.domain.repository.FavoriteStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FavoriteModule {

    @Binds
    @Singleton
    abstract fun bindFavoriteStore(
        dataStoreFavoriteStore: DataStoreFavoriteStore
    ): FavoriteStore
}
