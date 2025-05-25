package com.example.adbpurrytify.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.adbpurrytify.data.model.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    @Insert
    suspend fun insert(song: SongEntity)

    @Update
    suspend fun update(song: SongEntity)

    // Get a specific song by ID
    @Query("SELECT * FROM songs WHERE id = :songId")
    suspend fun getSongById(songId: Long): SongEntity?

    // Get next available ID with offset
    @Query("SELECT COALESCE(MAX(id), 999999) + 1 FROM songs WHERE id >= 1000000")
    suspend fun getNextAvailableId(): Long

    // 0. All songs
    @Query("SELECT * FROM songs WHERE userId = :userId")
    fun getAllSongs(userId: Long): Flow<List<SongEntity>>

    // 1. Get all songs for a specific user
    @Query("SELECT * FROM songs WHERE userId = :userId")
    fun getSongsByUser(userId: Long): Flow<List<SongEntity>>

    // 2. Get liked songs for a specific user
    @Query("SELECT * FROM songs WHERE userId = :userId AND isLiked = 1")
    fun getLikedSongs(userId: Long): Flow<List<SongEntity>>

    // 3. Get recently played songs (non-null lastPlayedTimestamp) for a user
    @Query("SELECT * FROM songs WHERE userId = :userId AND lastPlayedTimestamp IS NOT NULL ORDER BY lastPlayedTimestamp DESC LIMIT 20")
    fun getRecentlyPlayedSongs(userId: Long): Flow<List<SongEntity>>

    // 4. Get the latest recently played song for a user (limit 1)
    @Query("SELECT * FROM songs WHERE userId = :userId AND lastPlayedTimestamp IS NOT NULL ORDER BY lastPlayedTimestamp DESC LIMIT 1")
    suspend fun getLastPlayedSong(userId: Long): SongEntity?

    // 5. Get downloaded songs (artUri starts with http AND audioUri doesn't start with http)
    @Query("SELECT * FROM songs WHERE userId = :userId AND artUri LIKE 'http%' AND audioUri NOT LIKE 'http%'")
    fun getDownloadedSongs(userId: Long): Flow<List<SongEntity>>

    // 6. Get local songs (audioUri doesn't start with http)
    @Query("SELECT * FROM songs WHERE userId = :userId AND audioUri NOT LIKE 'http%'")
    fun getLocalSongs(userId: Long): Flow<List<SongEntity>>

    // prev
    @Query("""
        SELECT * FROM songs 
        WHERE userId = :userId 
          AND id < :currentSongId 
        ORDER BY id DESC 
    LIMIT 1
    """)
    suspend fun getPreviousSong(userId: Long, currentSongId: Long): SongEntity?

    // next
    @Query("""
        SELECT * FROM songs 
        WHERE userId = :userId 
          AND id > :currentSongId 
        ORDER BY id ASC 
    LIMIT 1
    """)
    suspend fun getNextSong(userId: Long, currentSongId: Long): SongEntity?

}