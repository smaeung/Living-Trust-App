package com.livingtrust.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * TrustEntity — the database table row for a Living Trust document.
 *
 * WHY @Entity?
 * Room (Android's official database library) uses annotations to understand your
 * data structure. @Entity tells Room: "create a table for this class". The
 * `tableName` parameter sets the actual SQL table name.
 *
 * WHY a separate Entity class instead of using the domain Trust model directly?
 * This is the "separation of concerns" principle. The Entity is designed for
 * efficient database storage, which means:
 *   - Lists (beneficiaries, assets) are serialized to JSON strings because
 *     SQLite (the underlying database) doesn't natively support arrays.
 *   - Timestamps are stored as Long (milliseconds since epoch) which is compact
 *     and sortable.
 * The domain Trust model (in domain/model/) is designed for business logic and
 * uses proper Kotlin types like List<String>. The repository converts between them.
 *
 * WHY `data class`?
 * Kotlin data classes automatically generate equals(), hashCode(), toString(),
 * and copy() methods. This makes comparing and updating records easy without
 * writing boilerplate code.
 *
 * Database schema (SQL equivalent):
 * CREATE TABLE trusts (
 *   id TEXT PRIMARY KEY,
 *   trustName TEXT NOT NULL,
 *   grantor TEXT NOT NULL,
 *   trustee TEXT NOT NULL,
 *   successorTrustee TEXT NOT NULL,
 *   beneficiaries TEXT NOT NULL,   -- stored as JSON string e.g. '["Alice","Bob"]'
 *   assets TEXT NOT NULL,          -- stored as JSON string e.g. '["Home at 123 Main St"]'
 *   status TEXT NOT NULL,          -- "draft" | "review" | "complete"
 *   createdAt INTEGER NOT NULL,    -- Unix timestamp in milliseconds
 *   updatedAt INTEGER NOT NULL
 * )
 */
@Entity(tableName = "trusts")
data class TrustEntity(
    /** Unique identifier from the server. Used as primary key so we can
     *  upsert (insert-or-replace) when syncing from the API. */
    @PrimaryKey val id: String,

    /** The legal name of the trust, e.g. "The Smith Family Trust". */
    val trustName: String,

    /** The person creating and funding the trust (the owner). */
    val grantor: String,

    /** The person or institution managing the trust assets. Often same as grantor. */
    val trustee: String,

    /** Who takes over as trustee if the primary trustee cannot serve. */
    val successorTrustee: String,

    /** JSON-encoded list of beneficiary names. e.g. '["Alice Smith","Bob Smith"]'
     *  Stored as String because SQLite doesn't support array columns natively. */
    val beneficiaries: String,

    /** JSON-encoded list of asset descriptions. e.g. '["House at 123 Main St"]'
     *  Converted back to List<String> by TrustRepositoryImpl when reading. */
    val assets: String,

    /** Lifecycle stage of the trust document: "draft" | "review" | "complete" */
    val status: String,

    /** Unix timestamp (milliseconds) when this record was first created locally. */
    val createdAt: Long,

    /** Unix timestamp (milliseconds) when this record was last modified.
     *  Used for ORDER BY updatedAt DESC in TrustDao queries. */
    val updatedAt: Long
)
