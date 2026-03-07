package com.livingtrust.app.data.repository

import com.livingtrust.app.data.remote.api.AuthApi
import com.livingtrust.app.data.remote.dto.LoginRequest
import com.livingtrust.app.data.remote.dto.RegisterRequest
import com.livingtrust.app.domain.model.User
import com.livingtrust.app.domain.repository.AuthRepository
import com.livingtrust.app.util.Resource
import com.livingtrust.app.util.TokenManager
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(email: String, password: String): Resource<User> {
        return try {
            val response = authApi.login(LoginRequest(email, password))
            tokenManager.saveToken(response.token)
            Resource.Success(
                User(
                    id = response.user.id,
                    email = response.user.email,
                    name = response.user.name
                )
            )
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Login failed")
        }
    }

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

    override suspend fun logout() {
        tokenManager.clearToken()
    }

    override suspend fun isLoggedIn(): Boolean {
        return tokenManager.getToken() != null
    }
}
