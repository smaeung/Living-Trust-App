package com.livingtrust.app.domain.repository

import com.livingtrust.app.domain.model.User
import com.livingtrust.app.util.Resource

interface AuthRepository {
    suspend fun login(email: String, password: String): Resource<User>
    suspend fun register(name: String, email: String, password: String): Resource<User>
    suspend fun logout()
    suspend fun isLoggedIn(): Boolean
}
