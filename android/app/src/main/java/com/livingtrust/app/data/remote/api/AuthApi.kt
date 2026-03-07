package com.livingtrust.app.data.remote.api

import com.livingtrust.app.data.remote.dto.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * AuthApi — Retrofit interface for authentication endpoints.
 *
 * WHY an interface instead of a class?
 * Retrofit reads the annotations (@POST, @GET, @Body, etc.) and generates the
 * full network implementation at runtime. We only declare WHAT we want to call —
 * Retrofit handles all the HTTP boilerplate: building URLs, serializing JSON,
 * handling response codes, running on a background thread, etc.
 *
 * WHY `suspend`?
 * These are coroutine-compatible functions. Calling them from a coroutine
 * (e.g. inside viewModelScope.launch { }) suspends the coroutine while the
 * network request is in flight, without blocking the main thread. The result
 * is returned when the server responds.
 *
 * WHY no BASE_URL here?
 * The base URL (http://10.0.2.2:3001/) is configured once in NetworkModule
 * and injected into Retrofit. Each function here only specifies the path
 * that gets appended to the base URL.
 *
 * If the server returns an HTTP error (4xx, 5xx), Retrofit throws an
 * HttpException which our repository catches and converts to Resource.Error.
 */
interface AuthApi {

    /**
     * Creates a new user account.
     * Maps to: POST http://<base>/api/auth/register
     *
     * @Body — Retrofit serializes the RegisterRequest object to JSON automatically:
     * { "email": "...", "password": "...", "name": "..." }
     *
     * @return AuthResponse containing the JWT token and user info on success.
     * @throws HttpException if the email is already registered (HTTP 400)
     */
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    /**
     * Authenticates an existing user with email and password.
     * Maps to: POST http://<base>/api/auth/login
     *
     * @return AuthResponse with a fresh JWT token on success.
     * @throws HttpException if credentials are invalid (HTTP 401)
     */
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    /**
     * Fetches the current user's profile using their JWT token.
     * Maps to: GET http://<base>/api/auth/me
     *
     * Note: Most calls use the automatic auth interceptor in NetworkModule.
     * This function has an explicit @Header parameter as an alternative approach,
     * showing how to manually pass a header when needed.
     *
     * @param token The full "Bearer <jwt>" string.
     * @return MeResponse with basic user info.
     */
    @GET("api/auth/me")
    suspend fun getMe(@Header("Authorization") token: String): MeResponse
}
