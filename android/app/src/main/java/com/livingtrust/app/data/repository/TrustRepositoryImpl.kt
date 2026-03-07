package com.livingtrust.app.data.repository

import com.google.gson.Gson
import com.livingtrust.app.data.local.dao.TrustDao
import com.livingtrust.app.data.local.entity.TrustEntity
import com.livingtrust.app.data.remote.api.TrustApi
import com.livingtrust.app.data.remote.dto.CreateTrustRequest
import com.livingtrust.app.data.remote.dto.TrustDto
import com.livingtrust.app.domain.model.Trust
import com.livingtrust.app.domain.repository.TrustRepository
import com.livingtrust.app.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TrustRepositoryImpl @Inject constructor(
    private val trustApi: TrustApi,
    private val trustDao: TrustDao,
    private val gson: Gson
) : TrustRepository {

    override fun getTrusts(): Flow<List<Trust>> {
        return trustDao.getAllTrusts().map { entities ->
            entities.map { it.toDomain(gson) }
        }
    }

    override suspend fun refreshTrusts(): Resource<Unit> {
        return try {
            val response = trustApi.getTrusts()
            val entities = response.trusts.map { it.toEntity(gson) }
            trustDao.insertTrusts(entities)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to refresh trusts")
        }
    }

    override suspend fun createTrust(trust: Trust): Resource<Trust> {
        return try {
            val response = trustApi.createTrust(
                CreateTrustRequest(
                    trustName = trust.trustName,
                    grantor = trust.grantor,
                    trustee = trust.trustee,
                    successorTrustee = trust.successorTrustee,
                    beneficiaries = trust.beneficiaries,
                    assets = trust.assets
                )
            )
            trustDao.insertTrust(response.trust.toEntity(gson))
            Resource.Success(response.trust.toDomain())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create trust")
        }
    }

    override suspend fun deleteTrust(id: String): Resource<Unit> {
        return try {
            trustApi.deleteTrust(id)
            trustDao.deleteTrustById(id)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete trust")
        }
    }

    // --- Mappers ---

    private fun TrustDto.toEntity(gson: Gson) = TrustEntity(
        id = id,
        trustName = trustName,
        grantor = grantor,
        trustee = trustee,
        successorTrustee = successorTrustee,
        beneficiaries = gson.toJson(beneficiaries),
        assets = gson.toJson(assets),
        status = status,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    private fun TrustDto.toDomain() = Trust(
        id = id,
        trustName = trustName,
        grantor = grantor,
        trustee = trustee,
        successorTrustee = successorTrustee,
        beneficiaries = beneficiaries,
        assets = assets,
        status = status
    )

    private fun TrustEntity.toDomain(gson: Gson): Trust {
        val beneficiaryList = gson.fromJson(beneficiaries, Array<String>::class.java).toList()
        val assetList = gson.fromJson(assets, Array<String>::class.java).toList()
        return Trust(
            id = id,
            trustName = trustName,
            grantor = grantor,
            trustee = trustee,
            successorTrustee = successorTrustee,
            beneficiaries = beneficiaryList,
            assets = assetList,
            status = status
        )
    }
}
