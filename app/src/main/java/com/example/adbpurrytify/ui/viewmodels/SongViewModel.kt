package com.example.adbpurrytify.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.adbpurrytify.data.local.SongDao
import com.example.adbpurrytify.data.model.SongEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class SongViewModel(private val songDao: SongDao) : ViewModel() {

    // All songs LiveData - used in the UI
    private val _allSongs = MutableLiveData<List<SongEntity>>()
    val allSongs: LiveData<List<SongEntity>> = _allSongs

    // Loading state LiveData - Initialize it!
    private val _isLoading = MutableLiveData<Boolean>(false) // Default to not loading
    val isLoading: LiveData<Boolean> = _isLoading

    // Error state LiveData
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // Current user ID
    private var currentUserId: Long? = null

    // Keep track of the last loaded tab index
    private var lastLoadedTabIndex: Int = 0

    suspend fun getSongById(songId: Long): SongEntity? {
        return songDao.getSongById(songId)
    }

    // Set the current user ID and trigger the initial load
    fun setCurrentUser(userId: Long) {
        if (userId != currentUserId) {
            currentUserId = userId
            loadSongsForTab(lastLoadedTabIndex) // Load based on current/last tab
        }
    }

    // Unified function to load songs based on tab index
    fun loadSongsForTab(tabIndex: Int) {
        currentUserId?.let { userId ->
            lastLoadedTabIndex = tabIndex
            when (tabIndex) {
                0 -> loadAllSongs(userId)
                1 -> loadLikedSongs(userId)
            }
        } ?: run {
            _isLoading.postValue(false) // Ensure loading stops if no user ID
            _allSongs.postValue(emptyList()) // Clear songs if no user
            _error.postValue("User ID not available.") // Optional error
        }
    }

    // Get liked songs for the current user
    fun loadLikedSongs(userId: Long) {
        _isLoading.postValue(true) // Set loading true *before* starting collection
        _error.postValue(null) // Clear previous errors
        viewModelScope.launch {
            var isFirstEmission = true // Flag to handle only the first load
            songDao.getLikedSongs(userId)
                .catch { e ->
                    _error.postValue("Failed to load liked songs: ${e.message}")
                    _isLoading.postValue(false) // Stop loading on error
                    // Optionally post an empty list or keep the old data
                    // _allSongs.postValue(emptyList())
                }
                .collect { songs ->
                    _allSongs.postValue(songs)
                    // Only set loading to false after the *first* emission
                    if (isFirstEmission) {
                        _isLoading.postValue(false)
                        isFirstEmission = false
                    }
                }
            // Note: If the flow completes without emitting (empty table initially),
            // isLoading might stay true. Add a finally block if that's a concern.
            // However, for Room, it usually emits an empty list immediately.
        }
    }

    // Load all songs for the current user
    fun loadAllSongs(userId: Long) {
        _isLoading.postValue(true) // Set loading true *before* starting collection
        _error.postValue(null) // Clear previous errors
        viewModelScope.launch {
            var isFirstEmission = true // Flag to handle only the first load
            // Assuming getAllSongs means songs *by* this user based on previous context
            songDao.getSongsByUser(userId) // Make sure this is the correct DAO method
                .catch { e ->
                    _error.postValue("Failed to load all songs: ${e.message}")
                    _isLoading.postValue(false) // Stop loading on error
                    // Optionally post an empty list or keep the old data
                    // _allSongs.postValue(emptyList())
                }
                .collect { songs ->
                    _allSongs.postValue(songs)
                    // Only set loading to false after the *first* emission
                    if (isFirstEmission) {
                        _isLoading.postValue(false)
                        isFirstEmission = false
                    }
                }
        }
    }


    // Insert a new song
    fun insert(song: SongEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                songDao.insert(song)
                // No explicit refresh needed, Flow should update the list automatically
            } catch (e: Exception) {
                _error.postValue("Failed to add song: ${e.message}")
            }
        }
    }

    // Update a song (e.g., when liking/unliking)
    fun update(song: SongEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                songDao.update(song)
                // No explicit refresh needed, Flow should update the list automatically
            } catch (e: Exception) {
                _error.postValue("Failed to update song: ${e.message}")
            }
        }
    }

    suspend fun getPrevSongId(songId: Long): Long {
        var prevSong = songDao.getPreviousSong(currentUserId!!, songId)
        if (prevSong == null) {
            return -1L
        }
        return prevSong.id
    }
    suspend fun getNextSongId(songId: Long): Long {
        var nextSong = songDao.getNextSong(currentUserId!!, songId)
        if (nextSong == null) {
            return -1L
        }
        return nextSong.id
    }

    // Toggle like status of a song
    fun toggleLikeSong(song: SongEntity) {
        val updatedSong = song.copy(isLiked = !song.isLiked)
        update(updatedSong)
    }

    // --- Factory ---
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
