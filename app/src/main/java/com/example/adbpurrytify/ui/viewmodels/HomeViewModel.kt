package com.example.adbpurrytify.ui.viewmodels

import androidx.lifecycle.*
import com.example.adbpurrytify.data.AuthRepository
import com.example.adbpurrytify.data.local.SongDao
import com.example.adbpurrytify.data.model.SongEntity
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class HomeViewModel(
    private val songDao: SongDao,
    private val authRepository: AuthRepository // Needed to get user ID initially
) : ViewModel() {

    // --- LiveData for UI ---
    private val _newSongs = MutableLiveData<List<SongEntity>>()
    val newSongs: LiveData<List<SongEntity>> = _newSongs

    private val _recentlyPlayedSongs = MutableLiveData<List<SongEntity>>()
    val recentlyPlayedSongs: LiveData<List<SongEntity>> = _recentlyPlayedSongs

    private val _currentlyPlayingSong = MutableLiveData<SongEntity?>()
    val currentlyPlayingSong: LiveData<SongEntity?> = _currentlyPlayingSong

    private val _isLoadingNew = MutableLiveData<Boolean>(false)
    val isLoadingNew: LiveData<Boolean> = _isLoadingNew

    private val _isLoadingRecent = MutableLiveData<Boolean>(false)
    val isLoadingRecent: LiveData<Boolean> = _isLoadingRecent

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // --- Internal State ---
    private var currentUserId: Long? = null

    // --- Initialization ---
    // Fetch user ID and load initial data
    init {
        viewModelScope.launch {
            val userProfile = authRepository.currentUser()
            userProfile?.id?.let { userId ->
                setCurrentUser(userId)
            } ?: run {
                _error.postValue("Could not retrieve user profile.")
                // Post empty lists and stop loading if no user
                _newSongs.postValue(emptyList())
                _recentlyPlayedSongs.postValue(emptyList())
                _isLoadingNew.postValue(false)
                _isLoadingRecent.postValue(false)
            }
        }
    }

    // Set user and trigger data loading
    private fun setCurrentUser(userId: Long) {
        if (userId != currentUserId) {
            currentUserId = userId
            loadNewSongs(userId)
            loadRecentlyPlayedSongs(userId)
        }
    }

    // --- Data Loading Functions ---

    // Load "New Songs" (using getAllSongs for now)
    fun loadNewSongs(userId: Long) {
        _isLoadingNew.postValue(true)
        _error.postValue(null) // Clear previous errors for this section
        viewModelScope.launch {
            var isFirstEmission = true
            songDao.getAllSongs(userId) // Using getAllSongs as "New Songs"
                .catch { e ->
                    _error.postValue("Failed to load new songs: ${e.message}")
                    _isLoadingNew.postValue(false)
                }
                .collect { songs ->
                    _newSongs.postValue(songs)
                    if (isFirstEmission) {
                        _isLoadingNew.postValue(false)
                        isFirstEmission = false
                    }
                }
        }
    }

    // Load Recently Played Songs
    fun loadRecentlyPlayedSongs(userId: Long) {
        _isLoadingRecent.postValue(true)
        _error.postValue(null) // Clear previous errors for this section
        viewModelScope.launch {
            var isFirstEmission = true
            songDao.getRecentlyPlayedSongs(userId)
                .catch { e ->
                    _error.postValue("Failed to load recently played songs: ${e.message}")
                    _isLoadingRecent.postValue(false)
                }
                .collect { songs ->
                    _recentlyPlayedSongs.postValue(songs)
                    if (isFirstEmission) {
                        _isLoadingRecent.postValue(false)
                        isFirstEmission = false
                    }
                }
        }
    }

    // --- Playback / Interaction Functions ---

    // Call this when a song item is clicked
    fun playSong(song: SongEntity) {
        _currentlyPlayingSong.postValue(song)
        // TODO: Add actual playback logic (e.g., update lastPlayedTimestamp)
        // Maybe update the song in the DB here?
        // updateLastPlayed(song)
    }

    // Example: Update last played timestamp (call this from playback service)
    fun updateLastPlayed(song: SongEntity, positionMs: Long? = null) {
        currentUserId?.let { userId ->
            val updatedSong = song.copy(
                lastPlayedTimestamp = System.currentTimeMillis(),
                lastPlayedPositionMs = positionMs
                // Ensure userId is correct if needed, though it should be
            )
            viewModelScope.launch {
                try {
                    songDao.update(updatedSong)
                    // Flow for recently played should update automatically
                } catch (e: Exception) {
                    _error.postValue("Failed to update last played: ${e.message}")
                }
            }
        }
    }

    // Toggle like status (can reuse from SongViewModel or add here)
    fun toggleLikeSong(song: SongEntity) {
        val updatedSong = song.copy(isLiked = !song.isLiked)
        viewModelScope.launch {
            try {
                songDao.update(updatedSong)
                // Flows for new/recent will update if the song is present
            } catch (e: Exception) {
                _error.postValue("Failed to update like status: ${e.message}")
            }
        }
    }

    // --- Factory ---
    class Factory(
        private val songDao: SongDao,
        private val authRepository: AuthRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(songDao, authRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
