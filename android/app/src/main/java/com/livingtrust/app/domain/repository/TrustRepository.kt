package com.livingtrust.app.domain.repository

import com.livingtrust.app.domain.model.Trust
import com.livingtrust.app.util.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository interface for Trust CRUD operations.
 *
 * WHY an interface? (same principle as AuthRepository)
 * - Separates the "what" (domain contract) from the "how" (data layer implementation).
 *   UseCases and ViewModels only know about this interface, not TrustRepositoryImpl.
 *
 * WHY does getTrusts() return Flow instead of suspend fun?
 * - Flow is a reactive stream — it emits a new List<Trust> every time the database changes.
 * - This enables the offline-first pattern:
 *   1. UI subscribes to getTrusts() Flow from Room.
 *   2. refreshTrusts() fetches from the network and writes into Room.
 *   3. Room automatically emits the new list to all subscribers.
 * - The UI updates automatically without manual refresh calls.
 * - If getTrusts() were a regular suspend function, the UI would need to re-call it
 *   after every mutation — more complex and error-prone.
 *
 * WHY does getTrusts() NOT return Resource<Flow<...>>?
 * - Room queries never fail in a way that needs to be caught (the database is always
 *   available locally). Errors only happen during network sync, which is what
 *   refreshTrusts() handles separately.
 *
 * WHY does refreshTrusts() return Resource<Unit>?
 * - "Unit" means "no meaningful return value" — we just care whether it succeeded or failed.
 *   Resource<Unit> lets the caller show an error message if the network sync fails,
 *   while the UI continues to display the last locally-cached data.
 *
 * WHY does createTrust() return Resource<Trust>?
 * - The backend assigns the real ID to the trust after saving.
 *   Returning Resource<Trust> gives the caller the server-assigned trust (with its ID)
 *   so the local cache can be updated correctly.
 */
interface TrustRepository {

    /**
     * Returns a reactive stream of all trusts from the local Room database.
     * Emits a new list every time the database changes.
     * NOT a suspend function because Flow is already async.
     */
    fun getTrusts(): Flow<List<Trust>>

    /**
     * Fetches trusts from the remote API and updates the local Room database.
     * Returns Resource.Error if the network call fails.
     * The updated trusts are automatically emitted via the getTrusts() Flow.
     */
    suspend fun refreshTrusts(): Resource<Unit>

    /**
     * Sends the new trust to the backend and inserts it into the local database.
     * Returns the server-assigned trust (including the generated ID).
     */
    suspend fun createTrust(trust: Trust): Resource<Trust>

    /**
     * Deletes a trust by its ID on both the backend and local database.
     */
    suspend fun deleteTrust(id: String): Resource<Unit>
}
