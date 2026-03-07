package com.livingtrust.app.domain.usecase.auth

import com.livingtrust.app.domain.model.User
import com.livingtrust.app.domain.repository.AuthRepository
import com.livingtrust.app.util.Resource
import javax.inject.Inject

/**
 * Use case that handles the business logic for user login.
 *
 * WHY Use Cases?
 * - In Clean Architecture, Use Cases (also called Interactors) contain business rules.
 * - Without Use Cases, ViewModels grow large with validation logic mixed in.
 *   A ViewModel's job is to hold UI state, not validate email format.
 * - Use Cases are pure Kotlin classes — easy to unit test without Android or UI dependencies.
 * - If multiple screens need to log in (unlikely but possible), they share this Use Case
 *   rather than duplicating validation.
 *
 * WHY `operator fun invoke()`?
 * - Defining `invoke` as an operator lets us call the use case like a function:
 *   `loginUseCase(email, password)` instead of `loginUseCase.login(email, password)`.
 * - This is a Kotlin convention for single-responsibility classes.
 *   It reinforces that a Use Case does exactly one thing.
 *
 * WHY validate here instead of in the ViewModel?
 * - Validation is BUSINESS logic, not UI logic.
 *   Business rules belong in the domain layer, not in the presentation layer.
 * - If we switch from Compose to XML views (or add a web admin panel), the same
 *   validation rules apply without copying code.
 *
 * WHY `@Inject constructor`?
 * - Tells Hilt: "when this class is needed, create it with AuthRepository injected".
 *   Hilt automatically provides the correct AuthRepository implementation (from AppModule).
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * Validates the credentials and delegates to the repository if valid.
     *
     * Validation order:
     * 1. Empty email check — catches the most common mistake first
     * 2. Empty password check
     * 3. Email format check — uses Android's built-in regex pattern
     *
     * WHY not validate password length/complexity here?
     * - Login should accept whatever password the user set at registration.
     *   Enforcing complexity rules on login would lock out users with old passwords.
     *   Complexity is only validated in RegisterUseCase.
     *
     * WHY `email.trim()`?
     * - Users often accidentally add spaces when typing on mobile keyboards.
     *   Trimming whitespace before sending to the server prevents "user not found" errors
     *   that would confuse the user.
     */
    suspend operator fun invoke(email: String, password: String): Resource<User> {
        if (email.isBlank()) return Resource.Error("Email is required")
        if (password.isBlank()) return Resource.Error("Password is required")
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Resource.Error("Invalid email format")
        }
        // Trim email whitespace before sending, but NOT the password
        // (password might legitimately start/end with a space, though unlikely)
        return authRepository.login(email.trim(), password)
    }
}
