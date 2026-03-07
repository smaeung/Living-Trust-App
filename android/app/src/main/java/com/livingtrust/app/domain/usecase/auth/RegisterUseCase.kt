package com.livingtrust.app.domain.usecase.auth

import com.livingtrust.app.domain.model.User
import com.livingtrust.app.domain.repository.AuthRepository
import com.livingtrust.app.util.Resource
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
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
