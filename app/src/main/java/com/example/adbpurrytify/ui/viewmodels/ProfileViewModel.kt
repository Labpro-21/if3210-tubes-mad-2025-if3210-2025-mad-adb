package com.example.adbpurrytify.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adbpurrytify.data.AuthRepository
import com.example.adbpurrytify.data.SongRepository
import com.example.adbpurrytify.data.model.User
import com.example.adbpurrytify.data.model.UserStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val songRepository: SongRepository
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
                val userResult = authRepository.getCurrentUser()

                if (userResult.isSuccess) {
                    val userProfile = userResult.getOrThrow()
                    val user = User(
                        id = userProfile.id,
                        userName = userProfile.username,
                        email = userProfile.email,
                        image = userProfile.profilePhoto,
                        location = userProfile.location,
                        createdAt = userProfile.createdAt,
                        updatedAt = userProfile.updatedAt
                    )

                    // Get song statistics
                    val stats = getUserStats(userProfile.id)

                    _uiState.value = ProfileUiState.Success(user, stats)
                } else {
                    val exception = userResult.exceptionOrNull()
                    if (exception is HttpException && exception.code() == 401) {
                        _uiState.value = ProfileUiState.Error("Session expired. Please log in again.")
                    } else {
                        _uiState.value = ProfileUiState.Error(exception?.message ?: "Unknown error")
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error loading profile", e)
                _uiState.value = ProfileUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private suspend fun getUserStats(userId: Long): UserStats {
        val allSongs = songRepository.getAllSongs(userId).first()
        val likedSongs = songRepository.getLikedSongs(userId).first()
        val listenedSongs = songRepository.getRecentlyPlayedSongs(userId).first()

        return UserStats(
            songCount = allSongs.size,
            likedCount = likedSongs.size,
            listenedCount = listenedSongs.size
        )
    }

    sealed class ProfileUiState {
        data object Loading : ProfileUiState()
        data class Success(val user: User, val stats: UserStats) : ProfileUiState()
        data class Error(val message: String) : ProfileUiState()
    }
}