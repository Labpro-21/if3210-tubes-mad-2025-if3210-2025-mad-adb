package com.example.adbpurrytify.data.model.analytics

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "streaks",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["songId"]),
        Index(value = ["isActive"]),
        Index(value = ["streakLength"])
    ]
)
data class StreakEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val songId: Long,
    val songTitle: String,
    val artistName: String,
    val streakLength: Int, // number of consecutive days
    val startDate: String, // YYYY-MM-DD
    val endDate: String, // YYYY-MM-DD
    val isActive: Boolean = false, // whether the streak is still ongoing
    val totalPlayCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)