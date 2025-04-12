package com.example.adbpurrytify.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.adbpurrytify.data.local.SongDao
import com.example.adbpurrytify.data.model.SongEntity
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class HomeViewModel(private val songDao: SongDao) : ViewModel() {
    // New songs LiveData
    private val _newSongs = MutableLiveData<List<SongEntity>>()
    val newSongs: LiveData<List<SongEntity>> = _newSongs

    // Recently played songs LiveData
    private val _recentlyPlayed = MutableLiveData<List<SongEntity>>()
    val recentlyPlayed: LiveData<List<SongEntity>> = _recentlyPlayed

    // Loading states
    private val _isNewSongsLoading = MutableLiveData<Boolean>(false)
    val isNewSongsLoading: LiveData<Boolean> = _isNewSongsLoading

    private val _isRecentlyPlayedLoading = MutableLiveData<Boolean>(false)
    val isRecentlyPlayedLoading: LiveData<Boolean> = _isRecentlyPlayedLoading

    // Error state
    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    // Current user ID
    private var currentUserId: Long? = null

    // Currently playing song
    private val _currentlyPlayingSong = MutableLiveData<SongEntity?>(null)
    val currentlyPlayingSong: LiveData<SongEntity?> = _currentlyPlayingSong

    // Set the current user ID and trigger data loading
    fun setCurrentUser(userId: Long) {
        if (userId != currentUserId) {
            currentUserId = userId
            loadNewSongs(userId)
            loadRecentlyPlayedSongs(userId)
        }
    }

    // Get song by ID
    suspend fun getSongById(songId: Long): SongEntity? {
        return songDao.getSongById(songId)
    }

    // Play a song
    fun playSong(song: SongEntity) {
        _currentlyPlayingSong.value = song

        // Also update last played info in database
        viewModelScope.launch {
            val updatedSong = song.copy(
                lastPlayedTimestamp = System.currentTimeMillis(),
                lastPlayedPositionMs = 0 // Start from beginning
            )
            songDao.update(updatedSong)
        }
    }

    // Load new songs
    fun loadNewSongs(userId: Long) {
        _isNewSongsLoading.postValue(true)
        _error.postValue(null)
        viewModelScope.launch {
            var isFirstEmission = true
            songDao.getAllSongs(userId)
                .catch { e ->
                    _error.postValue("Failed to load new songs: ${e.message}")
                    _isNewSongsLoading.postValue(false)
                }
                .collect { songs ->
                    _newSongs.postValue(songs)
                    if (isFirstEmission) {
                        _isNewSongsLoading.postValue(false)
                        isFirstEmission = false
                    }
                }
        }
    }

    // Load recently played songs
    fun loadRecentlyPlayedSongs(userId: Long) {
        _isRecentlyPlayedLoading.postValue(true)
        _error.postValue(null)
        viewModelScope.launch {
            var isFirstEmission = true
            songDao.getRecentlyPlayedSongs(userId)
                .catch { e ->
                    _error.postValue("Failed to load recently played songs: ${e.message}")
                    _isRecentlyPlayedLoading.postValue(false)
                }
                .collect { songs ->
                    _recentlyPlayed.postValue(songs)
                    if (isFirstEmission) {
                        _isRecentlyPlayedLoading.postValue(false)
                        isFirstEmission = false
                    }
                }
        }
    }

    // Factory for creating the ViewModel with dependencies
    class Factory(private val songDao: SongDao) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(songDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
