package com.example.adbpurrytify.ui.viewmodels

import androidx.lifecycle.*
import com.example.adbpurrytify.data.local.SongDao
import com.example.adbpurrytify.data.model.SongEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SongViewModel(private val songDao: SongDao) : ViewModel() {

    // All songs LiveData - this is used in your UI
    private val _allSongs = MutableLiveData<List<SongEntity>>()
    val allSongs: LiveData<List<SongEntity>> = _allSongs

    // Current user LiveData to track current user
    private val _currentUserId = MutableLiveData<Long>()

    // Initialize with all songs
    init {
        viewModelScope.launch {
            songDao.getAllSongs().collect {
                _allSongs.postValue(it)
            }
        }
    }

    // Set the current user ID and update relevant song lists
    fun setCurrentUser(userId: Long) {
        if (userId != _currentUserId.value) {
            _currentUserId.value = userId
            loadUserSongs(userId)
        }
    }

    // Load songs for a specific user
    private fun loadUserSongs(userId: Long) {
        viewModelScope.launch {
            songDao.getSongsByUser(userId).collect {
                _allSongs.postValue(it)
            }
        }
    }

    // Get liked songs
    fun loadLikedSongs(userId: Long) {
        viewModelScope.launch {
            songDao.getLikedSongs(userId).collect {
                _allSongs.postValue(it)
            }
        }
    }

    // Load all songs (no filtering)
    fun loadAllSongs() {
        viewModelScope.launch {
            songDao.getAllSongs().collect {
                _allSongs.postValue(it)
            }
        }
    }

    // Insert a new song
    fun insert(song: SongEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            songDao.insert(song)
        }
    }

    // Update a song (e.g., when liking/unliking)
    fun update(song: SongEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            songDao.update(song)
        }
    }

    // Toggle like status of a song
    fun toggleLikeSong(song: SongEntity) {
        val updatedSong = song.copy(isLiked = !song.isLiked)
        update(updatedSong)
    }

    // Factory to create the ViewModel with dependencies
    class Factory(private val songDao: SongDao) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SongViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SongViewModel(songDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
