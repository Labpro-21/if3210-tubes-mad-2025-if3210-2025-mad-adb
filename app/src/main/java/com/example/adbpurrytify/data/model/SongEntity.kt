package com.example.adbpurrytify.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val author: String,
    val artUri: String,
    val audioUri: String,
    val userId: Long,
    val isLiked: Boolean,
    val lastPlayedTimestamp: Long?,
    val lastPlayedPositionMs: Long?
)
