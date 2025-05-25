package com.example.adbpurrytify.data.model

data class OnlineSong(
    val id: Long,
    val title: String,
    val artist: String,
    val artwork: String,
    val url: String,
    val duration: String, // Note: in "mm:ss" format
    val country: String,
    val rank: Int,
    val createdAt: String,
    val updatedAt: String
)

// For display-only purposes (trending lists, search results, etc.)
fun OnlineSong.toDisplaySongEntity(): SongEntity {
    return SongEntity(
        id = this.id,
        title = this.title,
        author = this.artist,
        artUri = this.artwork,
        audioUri = this.url,
        userId = -1, // This is OK for display-only
        isLiked = false,
        lastPlayedTimestamp = null,
        lastPlayedPositionMs = null
    )
}

// For saving to database with proper user ID
fun OnlineSong.toSongEntity(userId: Long): SongEntity {
    return SongEntity(
        id = this.id,
        title = this.title,
        author = this.artist,
        artUri = this.artwork,
        audioUri = this.url,
        userId = userId, // Use the provided userId
        isLiked = false,
        lastPlayedTimestamp = System.currentTimeMillis(),
        lastPlayedPositionMs = 0
    )
}