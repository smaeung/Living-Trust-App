package com.livingtrust.app.di

import android.content.Context
import androidx.room.Room
import com.livingtrust.app.data.local.LivingTrustDatabase
import com.livingtrust.app.data.local.dao.DocumentDao
import com.livingtrust.app.data.local.dao.TrustDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt DI module that provides the Room database and its DAO (Data Access Object) instances.
 *
 * WHY a separate DatabaseModule?
 * - Keeping database setup separate from network or repository setup makes the code
 *   easier to read, test, and maintain. Each module has a single responsibility.
 *
 * WHY @InstallIn(SingletonComponent::class)?
 * - The database must be a singleton — creating multiple Room instances pointing to
 *   the same file can cause data corruption and wastes memory.
 *   SingletonComponent ensures these objects live for the entire lifetime of the app.
 *
 * Dependency chain:
 *   Context → LivingTrustDatabase → TrustDao / DocumentDao
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Creates and provides the Room database instance.
     *
     * WHY Room.databaseBuilder?
     * - Room.databaseBuilder is the standard way to create a Room database.
     *   It needs three things:
     *   1. Context: to know where to store the database file on the device.
     *   2. The database class (LivingTrustDatabase): so Room knows the schema.
     *   3. The database file name: the file saved in the app's private storage.
     *
     * WHY @ApplicationContext?
     * - There are two kinds of Context in Android: Application context and Activity context.
     *   Activity context is tied to a specific screen and gets destroyed when the screen closes.
     *   Application context lives as long as the app — perfect for a singleton database.
     *   @ApplicationContext is a Hilt qualifier that injects the correct app-level context.
     *
     * WHY @Singleton on the database but NOT on DAOs?
     * - The database itself must be a single instance (see above).
     * - DAOs are just interfaces into the same database; creating a new DAO object
     *   doesn't open a new database connection — it's just a lightweight wrapper.
     *   So DAOs don't need @Singleton (though it wouldn't hurt to add it).
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LivingTrustDatabase {
        return Room.databaseBuilder(
            context,
            LivingTrustDatabase::class.java,
            LivingTrustDatabase.DATABASE_NAME
        ).build()
    }

    /**
     * Provides the TrustDao by asking the database for it.
     *
     * WHY this pattern instead of injecting LivingTrustDatabase everywhere?
     * - Code that needs to query trusts should depend on the narrow TrustDao interface,
     *   not the entire database class. This is the Interface Segregation principle —
     *   depend on only what you need.
     * - It also makes testing easier: you can mock TrustDao in unit tests without
     *   setting up the entire Room database.
     */
    @Provides
    fun provideTrustDao(database: LivingTrustDatabase): TrustDao = database.trustDao()

    /** Provides DocumentDao. See provideTrustDao for the WHY explanation. */
    @Provides
    fun provideDocumentDao(database: LivingTrustDatabase): DocumentDao = database.documentDao()
}
