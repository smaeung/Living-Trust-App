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

/**
 * TrustRepositoryImpl — implements the offline-first data strategy for trusts.
 *
 * OFFLINE-FIRST PATTERN:
 * The UI always reads from the local Room database, never directly from the network.
 * The network is only used to SYNC data into Room. This means:
 *   - The app works without internet (shows cached data)
 *   - The UI updates instantly without waiting for network round trips
 *   - Room's Flow automatically pushes new data to the UI after sync completes
 *
 * Data flow diagram:
 *
 *   Server (Express API) ──refreshTrusts()──► Room DB ──Flow──► ViewModel ──► UI
 *                                            (source of truth)
 *
 * WHY three constructor parameters (TrustApi, TrustDao, Gson)?
 *   - TrustApi: makes network calls to the Express backend
 *   - TrustDao: reads/writes to the local SQLite database via Room
 *   - Gson: serializes/deserializes List<String> to/from JSON strings for Room storage
 *           (Room can only store primitive types and strings, not lists directly)
 */
class TrustRepositoryImpl @Inject constructor(
    private val trustApi: TrustApi,
    private val trustDao: TrustDao,
    private val gson: Gson
) : TrustRepository {

    /**
     * Returns a live stream of trusts from the LOCAL database.
     *
     * WHY not fetch from network here?
     * Reading from the database is instant and works offline. The caller
     * (HomeViewModel.init) also calls refreshTrusts() separately to sync from
     * the server. This two-step approach means the UI shows data immediately
     * from cache, then updates automatically when the sync completes.
     *
     * The .map { } transforms each TrustEntity (database format) into a
     * Trust domain model (UI-friendly format with proper List<String> types).
     */
    override fun getTrusts(): Flow<List<Trust>> {
        return trustDao.getAllTrusts().map { entities ->
            entities.map { it.toDomain(gson) }
        }
    }

    /**
     * Downloads all trusts from the server and saves them to Room.
     *
     * Called on: app startup, pull-to-refresh, after creating a trust.
     * Returns Resource.Success(Unit) — the actual data comes through the
     * Flow from getTrusts(), not from this function's return value.
     *
     * WHY return Resource<Unit> and not the list?
     * The UI subscribes to getTrusts() Flow for data. refreshTrusts() just
     * triggers a sync — its only job is to tell the caller if the sync
     * succeeded or failed.
     */
    override suspend fun refreshTrusts(): Resource<Unit> {
        return try {
            val response = trustApi.getTrusts()
            // Convert each TrustDto → TrustEntity (serialize lists to JSON strings)
            val entities = response.trusts.map { it.toEntity(gson) }
            // insertTrusts uses OnConflictStrategy.REPLACE — safely handles duplicates
            trustDao.insertTrusts(entities)
            Resource.Success(Unit)  // Unit = Kotlin's "void" equivalent
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to refresh trusts")
        }
    }

    /**
     * Creates a new trust on the server AND saves it locally.
     *
     * Write-through pattern: write to server first, then cache locally.
     * This ensures the server is always the authority. If the server call fails,
     * we don't save to Room — so the local DB never has phantom records.
     *
     * @param trust The domain Trust model from the use case (already validated)
     * @return Resource.Success with the saved Trust (now has a server-assigned id)
     */
    override suspend fun createTrust(trust: Trust): Resource<Trust> {
        return try {
            // Convert domain Trust → CreateTrustRequest DTO for the API
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
            // Save the server's response to Room (it has the final id assigned by server)
            trustDao.insertTrust(response.trust.toEntity(gson))
            // Return the domain model version (not the DTO) back to the ViewModel
            Resource.Success(response.trust.toDomain())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create trust")
        }
    }

    /**
     * Deletes a trust from both the server and the local database.
     *
     * Delete-through pattern: delete from server first, then from local DB.
     * If the server delete fails, the local record is kept — consistent with
     * the server being the source of truth.
     *
     * @param id The trust's server-assigned unique identifier
     */
    override suspend fun deleteTrust(id: String): Resource<Unit> {
        return try {
            trustApi.deleteTrust(id)       // delete from server first
            trustDao.deleteTrustById(id)   // then remove from local cache
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete trust")
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private Mapper Functions
    //
    // WHY private extension functions instead of a separate Mapper class?
    // These mappers are only needed inside this repository. Keeping them here
    // as private extensions makes them easy to find and avoids creating extra
    // files for simple field-to-field conversions.
    //
    // Kotlin extension functions: `fun TrustDto.toEntity()` means you can call
    // myDto.toEntity() — it looks like a method on TrustDto but is defined here.
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Converts a server DTO to a Room Entity.
     * Lists are serialized to JSON strings because Room (SQLite) cannot store List<String>.
     * gson.toJson(["Alice", "Bob"]) → '["Alice","Bob"]'
     */
    private fun TrustDto.toEntity(gson: Gson) = TrustEntity(
        id = id,
        trustName = trustName,
        grantor = grantor,
        trustee = trustee,
        successorTrustee = successorTrustee,
        beneficiaries = gson.toJson(beneficiaries),  // List<String> → JSON string
        assets = gson.toJson(assets),                // List<String> → JSON string
        status = status,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    /**
     * Converts a server DTO directly to a domain model (no database involved).
     * Used when the create/update API response needs to be returned to the ViewModel.
     */
    private fun TrustDto.toDomain() = Trust(
        id = id,
        trustName = trustName,
        grantor = grantor,
        trustee = trustee,
        successorTrustee = successorTrustee,
        beneficiaries = beneficiaries,   // already a List<String> from JSON parsing
        assets = assets,
        status = status
    )

    /**
     * Converts a Room Entity back to a domain model for the UI.
     * JSON strings are deserialized back to List<String>.
     * gson.fromJson('["Alice","Bob"]', Array<String>::class.java).toList()
     *   → listOf("Alice", "Bob")
     */
    private fun TrustEntity.toDomain(gson: Gson): Trust {
        // fromJson parses the JSON string back into an Array, then we convert to List
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
