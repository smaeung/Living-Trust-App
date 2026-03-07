package com.livingtrust.app.data.local.dao

import androidx.room.*
import com.livingtrust.app.data.local.entity.DocumentEntity
import kotlinx.coroutines.flow.Flow

/**
 * DocumentDao — Data Access Object for the "documents" table.
 *
 * Same principles as TrustDao: Room generates the SQL implementation at compile
 * time from the annotations. We declare what we want, Room handles the how.
 *
 * Documents have two query patterns:
 *   1. Get ALL documents (for the Documents screen list)
 *   2. Get documents belonging to a SPECIFIC trust (for the trust detail view)
 *
 * Both return Flow so the UI automatically updates when documents are added
 * or deleted.
 */
@Dao
interface DocumentDao {

    /**
     * Returns a live stream of all documents, newest first.
     *
     * Used by DocumentsScreen to show the complete document library.
     * The Flow automatically emits a new list whenever a document is
     * inserted, updated, or deleted — no manual refresh needed.
     */
    @Query("SELECT * FROM documents ORDER BY createdAt DESC")
    fun getAllDocuments(): Flow<List<DocumentEntity>>

    /**
     * Returns a live stream of documents belonging to a specific trust.
     *
     * Used when viewing a single trust's detail page to show only the
     * files attached to that trust.
     *
     * @param trustId The ID of the trust whose documents we want.
     */
    @Query("SELECT * FROM documents WHERE trustId = :trustId")
    fun getDocumentsByTrust(trustId: String): Flow<List<DocumentEntity>>

    /**
     * Saves a document to the local database.
     * OnConflictStrategy.REPLACE handles the upsert case: if the server
     * sends the same document again (same id), we update it in place.
     *
     * @param document The document entity to persist.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity)

    /**
     * Deletes the given document. Room matches by the @PrimaryKey (id).
     * Use this when you have the full entity object available (e.g. from
     * a swipe-to-delete action in the list).
     *
     * @param document The document entity to remove.
     */
    @Delete
    suspend fun deleteDocument(document: DocumentEntity)

    /**
     * Deletes a document when you only have its ID.
     * Avoids the extra database read that would be needed to get the full
     * entity object just for deletion.
     *
     * @param id The unique ID of the document to delete.
     */
    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocumentById(id: String)
}
