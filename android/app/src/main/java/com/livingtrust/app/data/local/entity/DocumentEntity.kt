package com.livingtrust.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * DocumentEntity — a row in the "documents" table representing a file
 * (PDF, Word, etc.) associated with a Living Trust.
 *
 * WHY track both localPath and remoteUrl?
 * Documents can exist in two places:
 *   - localPath: a file the user picked from their device (not yet uploaded)
 *   - remoteUrl: a file already stored on the server
 * Both fields are nullable (String?) because a document might only have one
 * location at a time. The `?` in Kotlin means the value can be null.
 *
 * WHY trustId is nullable (String?)?
 * A document might not be linked to a specific trust yet (e.g. it was just
 * uploaded but not yet categorised). Allowing null here gives flexibility.
 *
 * WHY store size as Long instead of Int?
 * File sizes can exceed 2 GB on modern devices. Int can only hold values up to
 * ~2.1 billion (about 2 GB), while Long handles up to ~9.2 exabytes safely.
 */
@Entity(tableName = "documents")
data class DocumentEntity(
    /** Unique identifier. Same ID as the server record, enabling upsert sync. */
    @PrimaryKey val id: String,

    /** Display name of the file, e.g. "Smith_Family_Trust_Draft.pdf". */
    val name: String,

    /** MIME type or file extension, e.g. "application/pdf" or "pdf". */
    val type: String,

    /** File size in bytes. Long to safely handle files larger than 2 GB. */
    val size: Long,

    /** Absolute path on the device's local storage, or null if not downloaded. */
    val localPath: String?,

    /** URL on the remote server, or null if the file is only stored locally. */
    val remoteUrl: String?,

    /** Foreign key to TrustEntity.id — which trust this document belongs to.
     *  Null if the document is not yet linked to any trust. */
    val trustId: String?,

    /** Unix timestamp (milliseconds) when this document was added to the app. */
    val createdAt: Long
)
