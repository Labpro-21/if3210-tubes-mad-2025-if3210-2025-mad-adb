// SoundCapsuleModels.kt
package com.example.adbpurrytify.data.model

data class SoundCapsule(
    val month: String, // e.g., "04-2025" (MM-YYYY format)
    val displayMonth: String, // e.g., "April 2025" (for display purposes)
    val timeListened: Int, // in minutes
    val topArtist: Artist?,
    val topSong: Song?,
    val dayStreak: DayStreak?,
    val hasData: Boolean = true
)

data class Artist(
    val id: Long,
    val name: String,
    val imageUrl: String
)

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val imageUrl: String
)

data class DayStreak(
    val songTitle: String,
    val artist: String,
    val imageUrl: String,
    val streakDays: Int,
    val dateRange: String // e.g., "Mar 21-26, 2025"
)
