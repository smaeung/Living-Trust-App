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

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LivingTrustDatabase {
        return Room.databaseBuilder(
            context,
            LivingTrustDatabase::class.java,
            LivingTrustDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    fun provideTrustDao(database: LivingTrustDatabase): TrustDao = database.trustDao()

    @Provides
    fun provideDocumentDao(database: LivingTrustDatabase): DocumentDao = database.documentDao()
}
