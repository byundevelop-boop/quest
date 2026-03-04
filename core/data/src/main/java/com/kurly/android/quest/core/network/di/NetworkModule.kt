package com.kurly.android.quest.core.network.di

/**
 * Data 계층 DI: ViewModel에서 요구하는 MainRepository를 네트워크 구현으로 제공합니다.
 */

import android.content.Context
import com.kurly.android.quest.core.domain.repository.MainRepository
import com.kurly.android.quest.core.network.NetworkProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped

/** 네트워크 계층 의존성을 제공하는 Hilt 모듈 */
@Module
@InstallIn(ViewModelComponent::class)
object NetworkModule {

    /** 메인 화면의 ViewModel에서만 쓰는 메인 저장소 구현체를 제공 */
    @Provides
    @ViewModelScoped
    fun provideMainRepository(
        @ApplicationContext context: Context
    ): MainRepository {
        return NetworkProvider.provideMainRepository(context)
    }
}
