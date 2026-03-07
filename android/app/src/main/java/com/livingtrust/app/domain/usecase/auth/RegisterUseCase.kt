package com.livingtrust.app.domain.usecase.auth

import com.livingtrust.app.domain.model.User
import com.livingtrust.app.domain.repository.AuthRepository
import com.livingtrust.app.util.Resource
import javax.inject.Inject

/**
 * Use case that handles the business logic for new user registration.
 *
 * WHY a separate RegisterUseCase instead of adding register() to LoginUseCase?
 * - Single Responsibility Principle: each class has exactly one reason to change.
 *   Login validation rules (e.g., no password length check on login) differ from
 *   registration rules (password must meet minimum requirements).
 * - Separate classes are easier to test individually.
 *
 * See LoginUseCase for full explanation of why we use Use Cases and `operator fun invoke`.
 */
class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * Validates registration inputs and delegates to the repository if valid.
     *
     * WHY check password.length < 6?
     * - This is a common minimum password security requirement.
     *   The same rule should be enforced on the backend too (defence in depth),
     *   but checking it client-side gives the user immediate feedback without a round trip.
     *
     * WHY NOT validate password complexity (uppercase, symbols, etc.)?
     * - Over-strict password rules frustrate users and lead to worse passwords
     *   (e.g., "Password1!" is technically complex but easy to guess).
     *   6-character minimum is a simple, widely-understood baseline for v1.
     *
     * WHY validate email format AFTER checking blank and password length?
     * - Validation errors are shown one at a time (the first failure short-circuits via `return`).
     * - Checking blank fields first catches the most common mistakes (forgetting to fill a field)
     *   before running the more expensive regex check.
     *
     * WHY trim name and email but not password?
     * - Names and emails typed on mobile keyboards often pick up accidental leading/trailing spaces.
     * - Passwords should be sent exactly as typed — a space could be intentional.
     */
    suspend operator fun invoke(name: String, email: String, password: String): Resource<User> {
        if (name.isBlank()) return Resource.Error("Name is required")
        if (email.isBlank()) return Resource.Error("Email is required")
        if (password.length < 6) return Resource.Error("Password must be at least 6 characters")
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Resource.Error("Invalid email format")
        }
        return authRepository.register(name.trim(), email.trim(), password)
    }
}
