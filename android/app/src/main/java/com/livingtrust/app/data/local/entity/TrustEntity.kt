package com.livingtrust.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trusts")
data class TrustEntity(
    @PrimaryKey val id: String,
    val trustName: String,
    val grantor: String,
    val trustee: String,
    val successorTrustee: String,
    val beneficiaries: String,   // JSON array stored as string
    val assets: String,          // JSON array stored as string
    val status: String,          // draft | review | complete
    val createdAt: Long,
    val updatedAt: Long
)
