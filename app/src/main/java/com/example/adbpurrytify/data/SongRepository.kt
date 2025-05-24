package com.example.adbpurrytify.data

import com.example.adbpurrytify.api.ApiService
import com.example.adbpurrytify.data.local.SongDao
import com.example.adbpurrytify.data.model.SongEntity
import com.example.adbpurrytify.data.model.toSongEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for song-related operations.
 * Provides a clean API for ViewModel to interact with data from various sources.
 */
@Singleton
class SongRepository @Inject constructor(
    private val songDao: SongDao,
    private val apiService: ApiService
) {
    // Local data operations
    fun getAllSongs(userId: Long): Flow<List<SongEntity>> {
        return songDao.getAllSongs(userId)
    }

    fun getSongsByUser(userId: Long): Flow<List<SongEntity>> {
        return songDao.getSongsByUser(userId)
    }

    fun getLikedSongs(userId: Long): Flow<List<SongEntity>> {
        return songDao.getLikedSongs(userId)
    }

    fun getRecentlyPlayedSongs(userId: Long): Flow<List<SongEntity>> {
        return songDao.getRecentlyPlayedSongs(userId)
    }

    suspend fun getLastPlayedSong(userId: Long): SongEntity? {
        return songDao.getLastPlayedSong(userId)
    }

    suspend fun getPreviousSong(userId: Long, currentSongId: Long): SongEntity? {
        return songDao.getPreviousSong(userId, currentSongId)
    }

    suspend fun getNextSong(userId: Long, currentSongId: Long): SongEntity? {
        return songDao.getNextSong(userId, currentSongId)
    }

    suspend fun getSongById(songId: Long): SongEntity? {
        return songDao.getSongById(songId)
    }

    suspend fun insertSong(song: SongEntity) {
        songDao.insert(song)
    }

    suspend fun updateSong(song: SongEntity) {
        songDao.update(song)
    }

    // Remote data operations
    suspend fun getTopGlobalSongs(): List<SongEntity> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTopGlobalSongs()
            if (response.isSuccessful) {
                response.body()?.map { it.toSongEntity() } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getTopCountrySongs(countryCode: String): List<SongEntity> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTopCountrySongs(countryCode)
            if (response.isSuccessful) {
                response.body()?.map { it.toSongEntity() } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Business logic
    suspend fun toggleLikeSong(songId: Long, userId: Long) {
        val song = getSongById(songId) ?: return
        updateSong(song.copy(isLiked = !song.isLiked))
    }

    suspend fun updateSongPlaybackTime(songId: Long, position: Long) {
        val song = getSongById(songId) ?: return
        updateSong(song.copy(
            lastPlayedTimestamp = System.currentTimeMillis(),
            lastPlayedPositionMs = position
        ))
    }

    suspend fun markSongAsPlayed(songId: Long) {
        val song = getSongById(songId) ?: return
        updateSong(song.copy(lastPlayedTimestamp = System.currentTimeMillis()))
    }
}