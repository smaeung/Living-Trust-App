package com.livingtrust.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.livingtrust.app.data.local.dao.DocumentDao
import com.livingtrust.app.data.local.dao.TrustDao
import com.livingtrust.app.data.local.entity.DocumentEntity
import com.livingtrust.app.data.local.entity.TrustEntity

@Database(
    entities = [TrustEntity::class, DocumentEntity::class],
    version = 1,
    exportSchema = false
)
abstract class LivingTrustDatabase : RoomDatabase() {
    abstract fun trustDao(): TrustDao
    abstract fun documentDao(): DocumentDao

    companion object {
        const val DATABASE_NAME = "living_trust_db"
    }
}
