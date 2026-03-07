package com.livingtrust.app.data.local.dao

import androidx.room.*
import com.livingtrust.app.data.local.entity.TrustEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrustDao {

    @Query("SELECT * FROM trusts ORDER BY updatedAt DESC")
    fun getAllTrusts(): Flow<List<TrustEntity>>

    @Query("SELECT * FROM trusts WHERE id = :id")
    suspend fun getTrustById(id: String): TrustEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrust(trust: TrustEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrusts(trusts: List<TrustEntity>)

    @Update
    suspend fun updateTrust(trust: TrustEntity)

    @Delete
    suspend fun deleteTrust(trust: TrustEntity)

    @Query("DELETE FROM trusts WHERE id = :id")
    suspend fun deleteTrustById(id: String)
}
