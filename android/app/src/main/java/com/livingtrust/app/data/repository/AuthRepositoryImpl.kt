package com.livingtrust.app.data.repository

import com.livingtrust.app.data.remote.api.AuthApi
import com.livingtrust.app.data.remote.dto.LoginRequest
import com.livingtrust.app.data.remote.dto.RegisterRequest
import com.livingtrust.app.domain.model.User
import com.livingtrust.app.domain.repository.AuthRepository
import com.livingtrust.app.util.Resource
import com.livingtrust.app.util.TokenManager
import javax.inject.Inject

/**
 * AuthRepositoryImpl — concrete implementation of AuthRepository.
 *
 * WHY a separate interface (AuthRepository) and implementation (AuthRepositoryImpl)?
 * This is the Dependency Inversion Principle:
 *   - ViewModels and use cases depend on the AuthRepository INTERFACE (in domain/)
 *   - The interface has zero Android/network dependencies — it is pure Kotlin
 *   - This implementation lives in the data/ layer with the network code
 *
 * In unit tests we can create a FakeAuthRepository that returns whatever we want,
 * without touching the network at all. ViewModels don't care which implementation
 * they get — they just know the interface contract.
 *
 * @Inject constructor: tells Hilt how to build this class automatically.
 * Hilt sees it needs an AuthApi and TokenManager, which it already knows how to
 * provide (from NetworkModule and the @Singleton TokenManager).
 *
 * Responsibilities of this class:
 *   1. Call the network API
 *   2. On success: save the token and convert the DTO to a domain model
 *   3. On failure: wrap the exception message in Resource.Error
 *   4. Never let exceptions propagate to the ViewModel — always return Resource
 */
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,         // handles HTTP requests
    private val tokenManager: TokenManager // handles JWT storage
) : AuthRepository {

    /**
     * Logs in a user with email and password.
     *
     * Step by step:
     * 1. Build a LoginRequest DTO and send it to the server via Retrofit
     * 2. On success: save the JWT token → convert UserDto to domain User → wrap in Resource.Success
     * 3. On failure: catch any exception (network error, wrong password, etc.)
     *               and return Resource.Error with the message
     *
     * The `try/catch` here ensures the ViewModel never sees a raw exception —
     * it only ever receives a Resource.
     */
    override suspend fun login(email: String, password: String): Resource<User> {
        return try {
            val response = authApi.login(LoginRequest(email, password))
            // Immediately persist the token so future API calls are authenticated
            tokenManager.saveToken(response.token)
            Resource.Success(
                // Convert UserDto (data layer) → User (domain layer)
                // The domain User has no network-specific fields
                User(
                    id = response.user.id,
                    email = response.user.email,
                    name = response.user.name
                )
            )
        } catch (e: Exception) {
            // e.message can be null (e.g. NullPointerException with no message)
            // The ?: operator provides a fallback string in that case
            Resource.Error(e.message ?: "Login failed")
        }
    }

    /**
     * Creates a new account and logs the user in.
     *
     * Same pattern as login: call API → save token → convert to domain model.
     * The server creates the account and returns a JWT immediately, so the user
     * is logged in right after registering without a separate login step.
     */
    override suspend fun register(name: String, email: String, password: String): Resource<User> {
        return try {
            val response = authApi.register(RegisterRequest(email, password, name))
            tokenManager.saveToken(response.token)
            Resource.Success(
                User(
                    id = response.user.id,
                    email = response.user.email,
                    name = response.user.name
                )
            )
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Registration failed")
        }
    }

    /**
     * Logs out by deleting the stored JWT token.
     *
     * There is no server-side logout call here because the Express backend
     * uses stateless JWT tokens. Clearing the token locally is sufficient —
     * the next app launch will see no token and show the Login screen.
     */
    override suspend fun logout() {
        tokenManager.clearToken()
    }

    /**
     * Checks whether the user is currently logged in.
     *
     * A non-null token means the user has previously authenticated.
     * Note: this does NOT verify the token is still valid on the server —
     * expired tokens will be caught as HTTP 401 errors on the next API call.
     *
     * @return true if a token exists in storage, false otherwise.
     */
    override suspend fun isLoggedIn(): Boolean {
        return tokenManager.getToken() != null
    }
}
