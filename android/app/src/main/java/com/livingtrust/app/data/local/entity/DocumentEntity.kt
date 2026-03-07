package com.livingtrust.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val size: Long,
    val localPath: String?,
    val remoteUrl: String?,
    val trustId: String?,
    val createdAt: Long
)
