package com.livingtrust.app.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.livingtrust.app.BuildConfig
import com.livingtrust.app.data.remote.api.AiApi
import com.livingtrust.app.data.remote.api.AuthApi
import com.livingtrust.app.data.remote.api.TrustApi
import com.livingtrust.app.util.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt DI module that provides all network-related dependencies.
 *
 * WHY @Module + @InstallIn(SingletonComponent::class)?
 * - @Module tells Hilt "this object contains provider functions".
 * - @InstallIn(SingletonComponent) means all dependencies created here live as long
 *   as the entire app — they are created once and reused everywhere.
 *   Network clients are expensive to create, so sharing a single instance is efficient.
 *
 * WHY separate NetworkModule from AppModule?
 * - Separation of concerns: networking config (base URL, timeouts, interceptors) is
 *   logically distinct from repository bindings or database setup.
 *   Keeping them in separate files makes each file smaller and easier to understand.
 *
 * The dependency graph here is:
 *   Gson → AuthInterceptor(TokenManager) → OkHttpClient → Retrofit → Api interfaces
 *
 * Hilt automatically wires this chain — you never call `new Retrofit(...)` manually.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Provides a Gson instance for JSON serialization / deserialization.
     *
     * WHY Gson?
     * - Retrofit needs a converter to turn JSON responses (strings) into Kotlin data classes.
     *   GsonConverterFactory bridges that gap.
     * - GsonBuilder() lets us add custom type adapters later (e.g., Date formatting)
     *   without changing Retrofit setup.
     *
     * WHY @Singleton?
     * - Gson is stateless and thread-safe, so one instance shared app-wide saves memory.
     */
    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    /**
     * Provides an OkHttp Interceptor that automatically attaches the JWT token to every request.
     *
     * WHY an Interceptor?
     * - Without an interceptor, every API call would need to manually add
     *   `Authorization: Bearer <token>` — that's repetitive and error-prone.
     * - An interceptor sits in the middle of every HTTP request and can modify it
     *   before it goes out. This is the standard pattern for auth headers in OkHttp.
     *
     * WHY runBlocking?
     * - OkHttp's Interceptor interface is synchronous (not suspend-friendly).
     *   DataStore's getToken() is a suspend function (async).
     * - runBlocking bridges the gap: it blocks the current thread until getToken()
     *   completes. This is acceptable here because OkHttp already runs on a background
     *   thread — we never block the main (UI) thread.
     *
     * WHY check `if (token != null)`?
     * - The user might not be logged in (e.g., the Login/Register screens).
     *   In that case we send the request as-is without an Authorization header.
     *   Those endpoints don't require auth, so they will still work.
     */
    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenManager: TokenManager): Interceptor {
        return Interceptor { chain ->
            // Retrieve the stored JWT token synchronously (see WHY above)
            val token = runBlocking { tokenManager.getToken() }
            val request = if (token != null) {
                // Add the Authorization header only when a token exists
                chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } else {
                // No token: send the original request unchanged
                chain.request()
            }
            chain.proceed(request)
        }
    }

    /**
     * Provides the OkHttpClient — the actual HTTP engine that sends requests.
     *
     * WHY OkHttp?
     * - Retrofit uses OkHttp under the hood. By providing our own OkHttpClient we can
     *   attach interceptors, configure timeouts, and enable logging.
     *
     * WHY two interceptors?
     * 1. authInterceptor: attaches JWT (see above)
     * 2. HttpLoggingInterceptor: prints request/response details to Logcat
     *
     * WHY only log in DEBUG?
     * - Logging full request/response bodies in a production build is a security risk
     *   (tokens, personal data would appear in logs accessible by other apps).
     *   BuildConfig.DEBUG is automatically `true` in debug builds and `false` in release.
     *
     * WHY 30-second timeouts?
     * - AI endpoints can take a few seconds to respond (GPT-4 processing time).
     *   The default OkHttp timeout is 10 seconds, which might cut off AI responses.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: Interceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)   // auth header first
            .addInterceptor(logging)            // then log the final request (with auth header)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Provides the Retrofit instance — the type-safe HTTP client builder.
     *
     * WHY Retrofit?
     * - Retrofit lets us define API endpoints as Kotlin interfaces with annotations
     *   (@GET, @POST, etc.) instead of writing raw HTTP code.
     * - It handles serialization, deserialization, and threading automatically.
     *
     * WHY BuildConfig.BASE_URL?
     * - The base URL is defined in app/build.gradle.kts as a BuildConfig field.
     *   This lets us change the server URL for different build variants (debug/staging/release)
     *   without touching source code.
     *
     * WHY GsonConverterFactory?
     * - Tells Retrofit to use Gson to convert JSON ↔ Kotlin data classes automatically.
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    /**
     * Creates an implementation of AuthApi from the Retrofit interface definition.
     *
     * WHY retrofit.create(AuthApi::class.java)?
     * - AuthApi is an interface — it has no implementation code.
     * - Retrofit reads the @GET/@POST annotations at runtime and generates a real
     *   HTTP client class that implements the interface. This is called a "dynamic proxy".
     * - We never write the actual HTTP code; Retrofit generates it for us.
     *
     * The same pattern applies to TrustApi and AiApi below.
     */
    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    /** Provides the Trust API client. See provideAuthApi for the WHY explanation. */
    @Provides
    @Singleton
    fun provideTrustApi(retrofit: Retrofit): TrustApi = retrofit.create(TrustApi::class.java)

    /** Provides the AI API client. See provideAuthApi for the WHY explanation. */
    @Provides
    @Singleton
    fun provideAiApi(retrofit: Retrofit): AiApi = retrofit.create(AiApi::class.java)
}
