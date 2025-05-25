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
        // First check local database
        val localSong = songRepository.getSongById(songId)
        if (localSong != null) {
            return localSong
        }

        // If not found locally, get from online for display purposes
        return songRepository.getOnlineSongForDisplay(songId)
    }

    // Save online song to user's library when they interact with it
    suspend fun saveOnlineSongForUser(songId: Long): SongEntity? {
        val userId = currentUserId ?: return null
        return songRepository.saveOnlineSongForUser(songId, userId)
    }

    // Set the current user ID and trigger the initial load
    fun setCurrentUser(userId: Long) {
        if (userId != currentUserId) {
            currentUserId = userId
            loadSongsForTab(lastLoadedTabIndex)
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


    // Load recently played songs for the current user
    fun loadRecentlyPlayedSongs(userId: Long) {
        _isLoading.postValue(true)
        _error.postValue(null)
        viewModelScope.launch {
            var isFirstEmission = true
            songRepository.getRecentlyPlayedSongs(userId)
                .catch { e ->
                    _error.postValue("Failed to load recently played songs: ${e.message}")
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

    // Load downloaded songs for the current user
    fun loadDownloadedSongs(userId: Long) {
        _isLoading.postValue(true)
        _error.postValue(null)
        viewModelScope.launch {
            var isFirstEmission = true
            songRepository.getDownloadedSongs(userId)
                .catch { e ->
                    _error.postValue("Failed to load downloaded songs: ${e.message}")
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

    // Load local songs for the current user
    fun loadLocalSongs(userId: Long) {
        _isLoading.postValue(true)
        _error.postValue(null)
        viewModelScope.launch {
            var isFirstEmission = true
            songRepository.getLocalSongs(userId)
                .catch { e ->
                    _error.postValue("Failed to load local songs: ${e.message}")
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

    // Unified function to load songs based on tab index
    fun loadSongsForTab(tabIndex: Int) {
        currentUserId?.let { userId ->
            lastLoadedTabIndex = tabIndex
            when (tabIndex) {
                0 -> loadRecentlyPlayedSongs(userId)
                1 -> loadLikedSongs(userId)
                2 -> loadDownloadedSongs(userId)
                3 -> loadLocalSongs(userId)
            }
        } ?: run {
            _isLoading.postValue(false)
            _allSongs.postValue(emptyList())
            _error.postValue("User ID not available.")
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

    suspend fun getPrevSong(songId: Long): SongEntity? {
        currentUserId?.let {
            val prevSong = songRepository.getPreviousSong(it, songId)
            return prevSong
        }
        return null
    }

    suspend fun getNextSong(songId: Long): SongEntity? {
        currentUserId?.let {
            val nextSong = songRepository.getNextSong(it, songId)
            return nextSong
        }
        return null
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