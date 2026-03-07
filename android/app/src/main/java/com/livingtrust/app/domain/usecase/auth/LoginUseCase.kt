package com.livingtrust.app.domain.usecase.auth

import com.livingtrust.app.domain.model.User
import com.livingtrust.app.domain.repository.AuthRepository
import com.livingtrust.app.util.Resource
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Resource<User> {
        if (email.isBlank()) return Resource.Error("Email is required")
        if (password.isBlank()) return Resource.Error("Password is required")
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Resource.Error("Invalid email format")
        }
        return authRepository.login(email.trim(), password)
    }
}
