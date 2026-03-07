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

/**
 * Hilt DI module that binds domain repository interfaces to their concrete implementations.
 *
 * WHY do we need this module?
 * - In Clean Architecture, the domain layer defines interfaces like [AuthRepository].
 *   The actual implementation (AuthRepositoryImpl) lives in the data layer.
 * - Hilt needs to be told: "when something asks for AuthRepository, give it AuthRepositoryImpl".
 *   That's what this module does.
 *
 * This is the Dependency Inversion Principle (DIP) in practice:
 *   - High-level code (ViewModels, UseCases) depends on the abstract interface.
 *   - The concrete class is swapped in here, at the DI level.
 *   - To swap the implementation (e.g., for testing), you only change this file.
 *
 * WHY use @Provides instead of @Binds?
 * - @Binds is slightly more efficient but requires an abstract class module.
 *   @Provides works in object modules and is more explicit — you can see exactly
 *   which constructor arguments are passed to each implementation.
 *
 * WHY @InstallIn(SingletonComponent::class)?
 * - Repositories are singletons: they hold state (e.g., the database DAO, network client)
 *   and should be shared across the entire app rather than recreated per screen.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides AuthRepositoryImpl as the concrete implementation of [AuthRepository].
     *
     * WHY AuthRepositoryImpl needs AuthApi + TokenManager?
     * - AuthApi: makes HTTP calls to the /auth endpoints (login, register).
     * - TokenManager: saves/clears the JWT token after login/logout.
     * Both are already provided by NetworkModule and are injected automatically by Hilt.
     */
    @Provides
    @Singleton
    fun provideAuthRepository(
        authApi: AuthApi,
        tokenManager: TokenManager
    ): AuthRepository = AuthRepositoryImpl(authApi, tokenManager)

    /**
     * Provides TrustRepositoryImpl as the concrete implementation of [TrustRepository].
     *
     * WHY TrustRepositoryImpl needs TrustApi + TrustDao + Gson?
     * - TrustApi: fetches trusts from the remote server.
     * - TrustDao: reads/writes trusts in the local Room database (offline-first).
     * - Gson: serializes List<String> (beneficiaries, assets) to/from JSON strings
     *   for storage in Room (SQLite doesn't support array columns natively).
     */
    @Provides
    @Singleton
    fun provideTrustRepository(
        trustApi: TrustApi,
        trustDao: TrustDao,
        gson: Gson
    ): TrustRepository = TrustRepositoryImpl(trustApi, trustDao, gson)

    /**
     * Provides AiRepositoryImpl as the concrete implementation of [AiRepository].
     *
     * WHY AiRepositoryImpl only needs AiApi (no local DAO)?
     * - AI chat responses are not cached — every message is a fresh call to the backend.
     * - Storing AI conversation history would require additional schema design.
     *   For v1, real-time responses without caching are sufficient.
     */
    @Provides
    @Singleton
    fun provideAiRepository(aiApi: AiApi): AiRepository = AiRepositoryImpl(aiApi)
}
