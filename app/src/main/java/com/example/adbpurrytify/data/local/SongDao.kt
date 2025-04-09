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
    @Query("SELECT * FROM songs")
    fun getAllSongs(): Flow<List<SongEntity>>
}