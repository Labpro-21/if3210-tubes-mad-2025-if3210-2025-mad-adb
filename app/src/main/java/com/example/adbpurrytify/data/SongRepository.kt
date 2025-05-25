package com.example.adbpurrytify.data

import android.util.Log
import com.example.adbpurrytify.api.ApiService
import com.example.adbpurrytify.data.local.AnalyticsDao
import com.example.adbpurrytify.data.local.SongDao
import com.example.adbpurrytify.data.model.SongEntity
import com.example.adbpurrytify.data.model.toDisplaySongEntity
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
    private val apiService: ApiService,
    private val analyticsDao: AnalyticsDao
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

    // Remote data operations - FOR DISPLAY ONLY
    suspend fun getTopGlobalSongs(): List<SongEntity> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTopGlobalSongs()
            if (response.isSuccessful) {
                response.body()?.map { it.toDisplaySongEntity() } ?: emptyList()  // For display only
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getLastIncompleteSong(userId: Long): SongEntity? {
        return try {
            val incompleteSession = analyticsDao.getActiveListeningSession(userId)
            if (incompleteSession != null) {
                getSongById(incompleteSession.songId)
            } else null
        } catch (e: Exception) {
            Log.e("SongRepository", "Error getting incomplete song", e)
            null
        }
    }

    suspend fun getTopCountrySongs(countryCode: String): List<SongEntity> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTopCountrySongs(countryCode)
            if (response.isSuccessful) {
                response.body()?.map { it.toDisplaySongEntity() } ?: emptyList()  // For display only
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Get online song for display purposes
    suspend fun getOnlineSongForDisplay(songId: Long): SongEntity? = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getOnlineSong(songId)
            if (response.isSuccessful) {
                response.body()?.toDisplaySongEntity()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // Save online song to user's library
    suspend fun saveOnlineSongForUser(songId: Long, userId: Long): SongEntity? = withContext(Dispatchers.IO) {
        try {
            // Check if song already exists for this user
            val existingSong = getSongById(songId)
            if (existingSong != null && existingSong.userId == userId) {
                // Song already exists for this user, just update timestamp
                val updatedSong = existingSong.copy(
                    lastPlayedTimestamp = System.currentTimeMillis()
                )
                updateSong(updatedSong)
                return@withContext updatedSong
            }

            // Fetch from API and save with proper user ID
            val response = apiService.getOnlineSong(songId)
            if (response.isSuccessful) {
                val onlineSong = response.body()
                if (onlineSong != null) {
                    val songEntity = onlineSong.toSongEntity(userId)
                    insertSong(songEntity)
                    return@withContext songEntity
                }
            }
            null
        } catch (e: Exception) {
            Log.e("SongRepository", "Error saving online song for user", e)
            null
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