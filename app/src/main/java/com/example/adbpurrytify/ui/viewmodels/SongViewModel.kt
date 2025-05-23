package com.example.adbpurrytify.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adbpurrytify.data.AuthRepository
import com.example.adbpurrytify.data.SongRepository
import com.example.adbpurrytify.data.model.SongEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // All songs LiveData - used in the UI
    private val _allSongs = MutableLiveData<List<SongEntity>>()
    val allSongs: LiveData<List<SongEntity>> = _allSongs

    // Loading state LiveData
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // Error state LiveData
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // Current user ID
    private var currentUserId: Long? = null

    // Keep track of the last loaded tab index
    private var lastLoadedTabIndex: Int = 0

    // Function to get the current user ID (added for AddSong component)
    fun getCurrentUserId(): Long? {
        return currentUserId
    }

    // Load user data from the API
    fun loadUserData() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            val userResult = authRepository.getCurrentUser()
            if (userResult.isSuccess) {
                val userProfile = userResult.getOrThrow()
                currentUserId = userProfile.id
                loadSongsForTab(lastLoadedTabIndex)
            } else {
                _error.postValue("Failed to load user data")
                _isLoading.postValue(false)
            }
        }
    }

    suspend fun getSongById(songId: Long): SongEntity? {
        return songRepository.getSongById(songId)
    }

    // Set the current user ID and trigger the initial load
    fun setCurrentUser(userId: Long) {
        if (userId != currentUserId) {
            currentUserId = userId
            loadSongsForTab(lastLoadedTabIndex)
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
            _isLoading.postValue(false)
            _allSongs.postValue(emptyList())
            _error.postValue("User ID not available.")
        }
    }

    // Get liked songs for the current user
    fun loadLikedSongs(userId: Long) {
        _isLoading.postValue(true)
        _error.postValue(null)
        viewModelScope.launch {
            var isFirstEmission = true
            songRepository.getLikedSongs(userId)
                .catch { e ->
                    _error.postValue("Failed to load liked songs: ${e.message}")
                    _isLoading.postValue(false) // Stop loading on error
                }
                .collect { songs ->
                    _allSongs.postValue(songs)

                    if (isFirstEmission) {
                        _isLoading.postValue(false)
                        isFirstEmission = false
                    }
                }
        }
    }

    // Load all songs for the current user
    fun loadAllSongs(userId: Long) {
        _isLoading.postValue(true)
        _error.postValue(null)
        viewModelScope.launch {
            var isFirstEmission = true

            songRepository.getSongsByUser(userId)
                .catch { e ->
                    _error.postValue("Failed to load all songs: ${e.message}")
                    _isLoading.postValue(false)
                }
                .collect { songs ->
                    _allSongs.postValue(songs)
                    if (isFirstEmission) {
                        _isLoading.postValue(false)
                        isFirstEmission = false
                    }
                }
        }
    }

    // Insert a new song
    fun insert(song: SongEntity) {
        viewModelScope.launch {
            try {
                songRepository.insertSong(song)
                // No explicit refresh needed, Flow should update the list automatically
            } catch (e: Exception) {
                _error.postValue("Failed to add song: ${e.message}")
            }
        }
    }

    // Update a song (e.g., when liking/unliking)
    fun update(song: SongEntity) {
        viewModelScope.launch {
            try {
                songRepository.updateSong(song)
                // No explicit refresh needed, Flow should update the list automatically
            } catch (e: Exception) {
                _error.postValue("Failed to update song: ${e.message}")
            }
        }
    }

    suspend fun getPrevSongId(songId: Long): Long {
        currentUserId?.let {
            val prevSong = songRepository.getPreviousSong(it, songId)
            return prevSong?.id ?: -1L
        }
        return -1L
    }

    suspend fun getNextSongId(songId: Long): Long {
        currentUserId?.let {
            val nextSong = songRepository.getNextSong(it, songId)
            return nextSong?.id ?: -1L
        }
        return -1L
    }

    // Toggle like status of a song
    fun toggleLikeSong(song: SongEntity) {
        val updatedSong = song.copy(isLiked = !song.isLiked)
        update(updatedSong)
    }

    // Update LastPlayedTimestamp
    fun updateSongTimestamp(song: SongEntity) {
        val updatedSong = song.copy(lastPlayedTimestamp = System.currentTimeMillis())
        update(updatedSong)
    }
}