package com.example.adbpurrytify.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adbpurrytify.data.AuthRepository
import com.example.adbpurrytify.data.RecommendationRepository
import com.example.adbpurrytify.data.SongRepository
import com.example.adbpurrytify.data.model.SongEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val authRepository: AuthRepository,
    private val recommendationRepository: RecommendationRepository
) : ViewModel() {
    // New songs LiveData
    private val _newSongs = MutableLiveData<List<SongEntity>>()
    val newSongs: LiveData<List<SongEntity>> = _newSongs

    // Recently played songs LiveData
    private val _recentlyPlayed = MutableLiveData<List<SongEntity>>()
    val recentlyPlayed: LiveData<List<SongEntity>> = _recentlyPlayed

    // Trending global songs LiveData
    private val _trendingGlobalSongs = MutableLiveData<List<SongEntity>>()
    val trendingGlobalSongs: LiveData<List<SongEntity>> = _trendingGlobalSongs

    // Trending local songs LiveData
    private val _trendingCountrySongs = MutableLiveData<List<SongEntity>>()
    val trendingCountrySongs: LiveData<List<SongEntity>> = _trendingCountrySongs

    // Recommendations LiveData
    private val _recommendedSongs = MutableLiveData<List<SongEntity>>()
    val recommendedSongs: LiveData<List<SongEntity>> = _recommendedSongs

    // Loading states
    private val _isNewSongsLoading = MutableLiveData<Boolean>(false)
    val isNewSongsLoading: LiveData<Boolean> = _isNewSongsLoading

    private val _isRecentlyPlayedLoading = MutableLiveData<Boolean>(false)
    val isRecentlyPlayedLoading: LiveData<Boolean> = _isRecentlyPlayedLoading

    private val _isTrendingGlobalLoading = MutableLiveData<Boolean>(false)
    val isTrendingGlobalLoading: LiveData<Boolean> = _isTrendingGlobalLoading

    private val _isTrendingCountryLoading = MutableLiveData<Boolean>(false)
    val isTrendingCountryLoading: LiveData<Boolean> = _isTrendingCountryLoading

    // Recommendations loading state
    private val _isRecommendationsLoading = MutableLiveData<Boolean>(false)
    val isRecommendationsLoading: LiveData<Boolean> = _isRecommendationsLoading

    // Error state
    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    // Current user ID
    private var currentUserId: Long? = null

    // Current user location
    private var currentLocation: String? = null

    // Currently playing song
    private val _currentlyPlayingSong = MutableLiveData<SongEntity?>(null)
    val currentlyPlayingSong: LiveData<SongEntity?> = _currentlyPlayingSong

    // Load user data and trigger data loading
    fun loadUserData() {
        viewModelScope.launch {
            val userResult = authRepository.getCurrentUser()
            if (userResult.isSuccess) {
                val userProfile = userResult.getOrThrow()
                val userId = userProfile.id
                val userLocation = userProfile.location

                if (userId != currentUserId) {
                    currentUserId = userId
                    loadNewSongs(userId)
                    loadRecentlyPlayedSongs(userId)
                    loadRecommendations(userId, userLocation)
                }

                if (userLocation != currentLocation) {
                    currentLocation = userLocation
                    loadTrendingSongs()
                    // Reload recommendations if location changed
                    if (currentUserId != null) {
                        loadRecommendations(currentUserId!!, userLocation)
                    }
                }
            }
        }
    }

    // Getter for currentUserId
    fun getUserId(): Long? {
        return this.currentUserId
    }

    // Getter for currentLocation
    fun getUserLocation(): String? {
        return this.currentLocation
    }

    // Get song by ID
    suspend fun getSongById(songId: Long): SongEntity? {
        return songRepository.getSongById(songId)
    }

    // Play a song
    fun playSong(song: SongEntity) {
        _currentlyPlayingSong.value = song

        // Also update last played info in database
        viewModelScope.launch {
            songRepository.updateSongPlaybackTime(song.id, 0)
        }
    }

    // Load new songs
    fun loadNewSongs(userId: Long) {
        _isNewSongsLoading.postValue(true)
        _error.postValue(null)
        viewModelScope.launch {
            var isFirstEmission = true
            songRepository.getAllSongs(userId)
                .catch { e ->
                    _error.postValue("Failed to load new songs: ${e.message}")
                    _isNewSongsLoading.postValue(false)
                    _newSongs.postValue(emptyList()) // Ensure empty list on error
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
            songRepository.getRecentlyPlayedSongs(userId)
                .catch { e ->
                    _error.postValue("Failed to load recently played songs: ${e.message}")
                    _isRecentlyPlayedLoading.postValue(false)
                    _recentlyPlayed.postValue(emptyList()) // Ensure empty list on error
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

    // Load recommendations
    fun loadRecommendations(userId: Long, userCountry: String?) {
        _isRecommendationsLoading.postValue(true)
        _error.postValue(null)
        viewModelScope.launch {
            try {
                val recommendations = recommendationRepository.getPersonalizedRecommendations(userId, userCountry)
                _recommendedSongs.postValue(recommendations)
            } catch (e: Exception) {
                _error.postValue("Failed to load recommendations: ${e.message}")
                _recommendedSongs.postValue(emptyList())
            } finally {
                _isRecommendationsLoading.postValue(false)
            }
        }
    }

    // Load online trending songs (global and local if it is supported)
    fun loadTrendingSongs() {
        viewModelScope.launch {
            _isTrendingGlobalLoading.postValue(true)
            try {
                val songs = songRepository.getTopGlobalSongs()
                _trendingGlobalSongs.postValue(songs)
            } catch (e: Exception) {
                _trendingGlobalSongs.postValue(emptyList())
            } finally {
                _isTrendingGlobalLoading.postValue(false)
            }
        }

        val supportedCountries = listOf("ID", "MY", "US", "GB", "CH", "DE", "BR")
        viewModelScope.launch {
            _isTrendingCountryLoading.postValue(true)
            try {
                val country = currentLocation?.takeIf { it.uppercase() in supportedCountries }?.uppercase()
                if (country != null) {
                    val songs = songRepository.getTopCountrySongs(country)
                    _trendingCountrySongs.postValue(songs)
                } else {
                    _trendingCountrySongs.postValue(emptyList())
                }
            } catch (e: Exception) {
                _trendingCountrySongs.postValue(emptyList())
            } finally {
                _isTrendingCountryLoading.postValue(false)
            }
        }
    }
}
