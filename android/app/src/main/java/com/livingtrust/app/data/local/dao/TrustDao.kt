package com.livingtrust.app.data.local.dao

import androidx.room.*
import com.livingtrust.app.data.local.entity.TrustEntity
import kotlinx.coroutines.flow.Flow

/**
 * TrustDao — Data Access Object for the "trusts" table.
 *
 * WHY a DAO?
 * DAO is a design pattern that hides all SQL details behind a clean Kotlin
 * interface. Instead of writing raw SQL throughout the app, callers just call
 * trustDao.getAllTrusts() and get typed Kotlin objects back. Room generates
 * the actual SQL implementation at compile time from the annotations.
 *
 * WHY an interface (not a class)?
 * Room reads the annotations and generates a concrete implementation class
 * automatically. We only need to declare what we want, not how to do it.
 *
 * WHY @Dao?
 * This tells Room that this interface contains database operations. Without it,
 * Room would ignore the annotated functions.
 *
 * Key concepts used here:
 *   - Flow<T>: a reactive stream. When the database changes, any collector
 *     automatically receives the new data. Perfect for keeping UI in sync.
 *   - suspend: these functions are asynchronous and must run in a coroutine
 *     (background thread). They will not block the UI thread.
 *   - OnConflictStrategy.REPLACE: if a row with the same primary key already
 *     exists, delete the old one and insert the new one. This is the "upsert"
 *     pattern used when syncing data from the server.
 */
@Dao
interface TrustDao {

    /**
     * Returns a live stream of ALL trusts, sorted newest-first.
     *
     * WHY Flow (not suspend)?
     * `Flow<List<TrustEntity>>` is a hot observable. The UI collects it once
     * and automatically receives new emissions whenever the database changes —
     * no need to manually re-query after creating or deleting a trust.
     *
     * The `ORDER BY updatedAt DESC` ensures the most recently modified trust
     * appears at the top of the list.
     */
    @Query("SELECT * FROM trusts ORDER BY updatedAt DESC")
    fun getAllTrusts(): Flow<List<TrustEntity>>

    /**
     * Finds a single trust by its ID, or returns null if it doesn't exist.
     *
     * WHY suspend (not Flow)?
     * We only need the value once (not ongoing updates), so a suspend function
     * that returns a nullable result is the right choice here.
     *
     * @param id The trust's unique identifier (from the server).
     * @return The trust entity, or null if not found in local database.
     */
    @Query("SELECT * FROM trusts WHERE id = :id")
    suspend fun getTrustById(id: String): TrustEntity?

    /**
     * Inserts or updates a single trust in the database.
     *
     * OnConflictStrategy.REPLACE: if a trust with the same id already exists,
     * it is deleted and replaced with this new one. This lets us call insert
     * for both new records and updates — simpler than separate insert/update logic.
     *
     * @param trust The trust entity to save.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrust(trust: TrustEntity)

    /**
     * Bulk insert for syncing a list of trusts received from the server.
     * Using a list is more efficient than calling insertTrust() in a loop
     * because Room can wrap all inserts in a single database transaction.
     *
     * @param trusts List of trust entities to save.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrusts(trusts: List<TrustEntity>)

    /**
     * Updates an existing trust. Room matches the record by its @PrimaryKey (id).
     * If no record with that id exists, nothing happens.
     *
     * @param trust The updated trust entity.
     */
    @Update
    suspend fun updateTrust(trust: TrustEntity)

    /**
     * Deletes a trust by passing the full entity object.
     * Room uses the @PrimaryKey field to identify which row to delete.
     * Use this when you already have the entity object in memory.
     *
     * @param trust The trust entity to remove from the database.
     */
    @Delete
    suspend fun deleteTrust(trust: TrustEntity)

    /**
     * Deletes a trust when you only have its ID (not the full entity object).
     * More convenient than loading the entity first just to delete it.
     *
     * @param id The unique ID of the trust to delete.
     */
    @Query("DELETE FROM trusts WHERE id = :id")
    suspend fun deleteTrustById(id: String)
}
