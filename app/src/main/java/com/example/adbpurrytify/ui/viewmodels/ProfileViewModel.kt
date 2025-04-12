package com.example.adbpurrytify.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adbpurrytify.api.ApiService
import com.example.adbpurrytify.api.UserProfile
import com.example.adbpurrytify.data.AuthRepository
import com.example.adbpurrytify.data.TokenManager
import com.example.adbpurrytify.data.local.SongDao
import com.example.adbpurrytify.data.model.User
import com.example.adbpurrytify.data.model.UserStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ProfileViewModel(
    private val apiService: ApiService,
    private val songDao: SongDao, // Inject the SongDao
    private val authRepository: AuthRepository // Add AuthRepository dependency
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                val token = TokenManager.getAuthToken()
                if (token == null) {
                    _uiState.value = ProfileUiState.Error("Not logged in")
                    return@launch
                }

                try {
                    fetchUserProfile(token)
                } catch (e: HttpException) {
                    if (e.code() == 401) {
                        Log.d("ProfileViewModel", "Token expired, attempting to refresh")

                        val refreshed = authRepository.refreshToken()

                        if (refreshed) {
                            val newToken = TokenManager.getAuthToken()
                            if (newToken != null) {
                                Log.d("ProfileViewModel", "Token refreshed, retrying request")
                                fetchUserProfile(newToken)
                            } else {
                                _uiState.value = ProfileUiState.Error("Failed to get new token after refresh")
                            }
                        } else {
                            _uiState.value = ProfileUiState.Error("Session expired. Please log in again.")
                        }
                    } else {
                        _uiState.value = ProfileUiState.Error("Error: ${e.code()}")
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error loading profile", e)
                _uiState.value = ProfileUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private suspend fun fetchUserProfile(token: String) {
        val response = apiService.getProfile("Bearer $token")
        if (response.isSuccessful) {
            val userProfile = response.body()
            if (userProfile != null) {
                val user = mapToUserModel(userProfile)

                // Get song statistics from the database
                val userId = userProfile.id
                val stats = getUserStats(userId)

                _uiState.value = ProfileUiState.Success(user, stats)
            } else {
                _uiState.value = ProfileUiState.Error("Empty response")
            }
        } else {
            throw HttpException(response)
        }
    }

    private suspend fun getUserStats(userId: Long): UserStats {
        // Use Flow.first() to get the current value of the Flow
        val allSongs = songDao.getAllSongs(userId).first()
        val likedSongs = songDao.getLikedSongs(userId).first()
        val listenedSongs = songDao.getRecentlyPlayedSongs(userId).first()

        return UserStats(
            songCount = allSongs.size,
            likedCount = likedSongs.size,
            listenedCount = listenedSongs.size
        )
    }

    private fun mapToUserModel(userProfile: UserProfile): User {
        return User(
            id = userProfile.id,
            userName = userProfile.username,
            email = userProfile.email,
            image = userProfile.profilePhoto,
            location = userProfile.location,
            createdAt = userProfile.createdAt,
            updatedAt = userProfile.updatedAt
        )
    }

    sealed class ProfileUiState {
        data object Loading : ProfileUiState()
        data class Success(val user: User, val stats: UserStats) : ProfileUiState()
        data class Error(val message: String) : ProfileUiState()
    }
}