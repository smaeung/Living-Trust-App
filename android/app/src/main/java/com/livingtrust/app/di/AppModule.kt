package com.livingtrust.app.di

import com.google.gson.Gson
import com.livingtrust.app.data.remote.api.AiApi
import com.livingtrust.app.data.remote.api.AuthApi
import com.livingtrust.app.data.remote.api.TrustApi
import com.livingtrust.app.data.local.dao.TrustDao
import com.livingtrust.app.data.repository.AiRepositoryImpl
import com.livingtrust.app.data.repository.AuthRepositoryImpl
import com.livingtrust.app.data.repository.TrustRepositoryImpl
import com.livingtrust.app.domain.repository.AiRepository
import com.livingtrust.app.domain.repository.AuthRepository
import com.livingtrust.app.domain.repository.TrustRepository
import com.livingtrust.app.util.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        authApi: AuthApi,
        tokenManager: TokenManager
    ): AuthRepository = AuthRepositoryImpl(authApi, tokenManager)

    @Provides
    @Singleton
    fun provideTrustRepository(
        trustApi: TrustApi,
        trustDao: TrustDao,
        gson: Gson
    ): TrustRepository = TrustRepositoryImpl(trustApi, trustDao, gson)

    @Provides
    @Singleton
    fun provideAiRepository(aiApi: AiApi): AiRepository = AiRepositoryImpl(aiApi)
}
