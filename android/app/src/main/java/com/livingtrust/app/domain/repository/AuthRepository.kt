package com.livingtrust.app.domain.repository

import com.livingtrust.app.domain.model.User
import com.livingtrust.app.util.Resource

/**
 * Domain repository interface for authentication operations.
 *
 * WHY an interface instead of a concrete class?
 * - This is the cornerstone of Clean Architecture's Dependency Inversion Principle.
 *   The domain layer (UseCases, ViewModels) depends on this interface, not on
 *   AuthRepositoryImpl. This means:
 *   1. The domain layer has zero knowledge of Retrofit, Room, or DataStore.
 *   2. You can swap implementations (e.g., use Firebase instead of JWT) by changing
 *      only AppModule — no changes to the domain layer or ViewModels.
 *   3. Unit tests can use a FakeAuthRepository that doesn't touch the network.
 *
 * WHY `suspend` on all functions?
 * - Authentication always involves async I/O (network call + DataStore write).
 *   Marking functions `suspend` forces callers to run them inside a coroutine scope,
 *   keeping the main thread free and preventing ANR (Application Not Responding) errors.
 *
 * WHY return `Resource<User>` instead of just `User`?
 * - Network calls can fail. Resource<T> is a sealed class that wraps the result as
 *   either Success(data), Error(message), or Loading — preventing null pointer errors
 *   and making error handling explicit at every call site.
 *
 * WHY return `Resource<User>` for login/register specifically?
 * - On success, the ViewModel needs the user's data (name, email) to show in the UI.
 * - On failure, the ViewModel needs an error message to display.
 * - Resource<User> covers both cases in a single return type.
 *
 * WHY does logout NOT return Resource?
 * - Logout is a local operation (clear the token from DataStore). It cannot fail
 *   in a meaningful way. No network call is needed for a JWT-based auth system.
 */
interface AuthRepository {

    /** Authenticates the user and saves the returned JWT token locally. */
    suspend fun login(email: String, password: String): Resource<User>

    /** Creates a new account and saves the returned JWT token locally. */
    suspend fun register(name: String, email: String, password: String): Resource<User>

    /** Clears the locally stored JWT token, effectively logging the user out. */
    suspend fun logout()

    /** Returns true if a JWT token is currently stored (user is logged in). */
    suspend fun isLoggedIn(): Boolean
}
