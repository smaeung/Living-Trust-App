package com.livingtrust.app.domain.repository

import com.livingtrust.app.domain.model.Trust
import com.livingtrust.app.util.Resource
import kotlinx.coroutines.flow.Flow

interface TrustRepository {
    fun getTrusts(): Flow<List<Trust>>
    suspend fun refreshTrusts(): Resource<Unit>
    suspend fun createTrust(trust: Trust): Resource<Trust>
    suspend fun deleteTrust(id: String): Resource<Unit>
}
