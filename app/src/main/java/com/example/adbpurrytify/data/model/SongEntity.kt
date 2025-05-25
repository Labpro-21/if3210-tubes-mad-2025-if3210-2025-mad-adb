package com.example.adbpurrytify.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "songs",
    primaryKeys = ["id", "userId"],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["isLiked"]),
        Index(value = ["lastPlayedTimestamp"])
    ]
)
data class SongEntity(
    val id: Long,
    val title: String,
    val author: String,
    val artUri: String,
    val audioUri: String,
    val userId: Long,
    val isLiked: Boolean,
    val lastPlayedTimestamp: Long?,
    val lastPlayedPositionMs: Long?,
)