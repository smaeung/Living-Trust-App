package com.livingtrust.app.util

/**
 * Resource — a generic wrapper for any async operation result.
 *
 * WHY do we need this?
 * Every network call or database write can produce one of three outcomes:
 *   1. It worked and we have data  → Success
 *   2. It failed and we have a message → Error
 *   3. It is still running → Loading
 *
 * Without Resource, ViewModels would need multiple separate state fields:
 *   var isLoading = false
 *   var data: Trust? = null
 *   var errorMessage: String? = null
 * This allows impossible states like isLoading=true AND data!=null at the same time.
 *
 * With Resource, there is exactly ONE state at a time — impossible states
 * cannot be represented, making bugs easier to catch.
 *
 * WHY sealed class?
 * A sealed class is like an enum but each variant can carry its own data.
 * It forces the compiler to check every case in a `when` expression, so you
 * can never forget to handle the Error case.
 *
 * WHY <out T> (covariant generic)?
 * The `out` keyword means Resource<Dog> can be used where Resource<Animal>
 * is expected (since Dog is a subtype of Animal). This is safe here because
 * Resource only ever PRODUCES a T, never consumes one.
 *
 * Usage example:
 * ```kotlin
 * when (val result = loginUseCase(email, password)) {
 *     is Resource.Success -> showHome(result.data)   // result.data is typed as User
 *     is Resource.Error   -> showError(result.message)
 *     Resource.Loading    -> showSpinner()
 * }
 * ```
 */
sealed class Resource<out T> {

    /**
     * The operation succeeded.
     * @param data The result value. Its type matches whatever T was specified as
     *             (e.g. Resource<User> gives data: User).
     */
    data class Success<out T>(val data: T) : Resource<T>()

    /**
     * The operation failed.
     * Uses Resource<Nothing> because an error carries no data payload.
     * @param message Human-readable description of what went wrong.
     */
    data class Error(val message: String) : Resource<Nothing>()

    /**
     * The operation is still in progress.
     * `data object` (Kotlin 1.9+) creates a singleton — there is only ever one
     * Loading instance, which is memory-efficient since it carries no data.
     */
    data object Loading : Resource<Nothing>()
}
