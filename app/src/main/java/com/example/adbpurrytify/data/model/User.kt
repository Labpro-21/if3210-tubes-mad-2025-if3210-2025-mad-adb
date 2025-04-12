package com.example.adbpurrytify.data.model

data class User(
    val id: Long = 0,
    val userName: String = "",
    val email: String = "",
    val image: String = "",
    val location: String = "",
    val createdAt: String = "",
    val updatedAt: String = "",
)

data class UserStats(
    val songCount: Int = 0,
    val likedCount: Int = 0,
    val listenedCount: Int = 0
)

