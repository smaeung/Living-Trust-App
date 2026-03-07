package com.livingtrust.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.livingtrust.app.data.local.dao.DocumentDao
import com.livingtrust.app.data.local.dao.TrustDao
import com.livingtrust.app.data.local.entity.DocumentEntity
import com.livingtrust.app.data.local.entity.TrustEntity

/**
 * LivingTrustDatabase — the central Room database for offline data storage.
 *
 * WHY Room (and not raw SQLite)?
 * SQLite is Android's built-in relational database, but using it directly
 * requires writing error-prone boilerplate: creating Cursor objects, managing
 * column indices, handling type conversions, etc. Room is an abstraction layer
 * that:
 *   - Validates SQL queries at compile time (typos become build errors)
 *   - Converts database rows to Kotlin data classes automatically
 *   - Works natively with Kotlin coroutines and Flow
 *   - Enforces type safety throughout
 *
 * WHY abstract class?
 * Room generates a concrete implementation class at compile time. We declare
 * the abstract functions and Room fills in all the SQL code. We never instantiate
 * this class directly — we always use Room.databaseBuilder() (in DatabaseModule).
 *
 * @Database annotation parameters:
 *   - entities: the list of @Entity classes that become database tables.
 *     Each entity = one table. Adding a new entity here AND listing it in the
 *     @Database annotation is all that's needed to create a new table.
 *   - version: starts at 1 and must be incremented whenever the schema changes
 *     (e.g. you add a column). Room uses this number to decide whether to run
 *     migration code on existing user devices.
 *   - exportSchema: when true, Room writes schema JSON files to disk for version
 *     control. We set it false here to keep the build simple during development;
 *     set it true in production to track schema history.
 *
 * Current tables:
 *   - trusts     (defined by TrustEntity)
 *   - documents  (defined by DocumentEntity)
 */
@Database(
    entities = [TrustEntity::class, DocumentEntity::class],
    version = 1,
    exportSchema = false
)
abstract class LivingTrustDatabase : RoomDatabase() {

    /**
     * Provides access to trust CRUD operations.
     * Room generates the implementation — we just call database.trustDao()
     * to get an instance. Managed as a singleton by DatabaseModule so there
     * is only ever one instance per database connection.
     */
    abstract fun trustDao(): TrustDao

    /**
     * Provides access to document CRUD operations.
     */
    abstract fun documentDao(): DocumentDao

    companion object {
        /**
         * The filename for the SQLite database file on the device.
         * Stored in the app's private data directory: /data/data/<package>/databases/
         * Users cannot access this file without root; it is also backed up
         * with Android's Auto Backup feature by default.
         */
        const val DATABASE_NAME = "living_trust_db"
    }
}
