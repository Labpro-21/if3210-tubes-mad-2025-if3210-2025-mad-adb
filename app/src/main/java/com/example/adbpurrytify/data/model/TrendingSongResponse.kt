package com.example.adbpurrytify.data.model

data class TrendingSongResponse(
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

fun TrendingSongResponse.toSongEntity(): SongEntity {
    return SongEntity(
        id = this.id,
        title = this.title,
        author = this.artist,
        artUri = this.artwork,
        audioUri = this.url,
        userId = -1,
        isLiked = false,
        lastPlayedTimestamp = null,
        lastPlayedPositionMs = null
    )
}
