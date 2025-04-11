package com.example.adbpurrytify.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adbpurrytify.api.ApiService
import com.example.adbpurrytify.api.UserProfile
import com.example.adbpurrytify.data.TokenManager
import com.example.adbpurrytify.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val apiService: ApiService
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

                val response = apiService.getProfile("Bearer $token")
                if (response.isSuccessful) {
                    val userProfile = response.body()
                    if (userProfile != null) {
                        val user = mapToUserModel(userProfile)
                        _uiState.value = ProfileUiState.Success(user)
                    } else {
                        _uiState.value = ProfileUiState.Error("Empty response")
                    }
                } else {
                    _uiState.value = ProfileUiState.Error("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "Unknown error")
            }
        }
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
        data class Success(val user: User) : ProfileUiState()
        data class Error(val message: String) : ProfileUiState()
    }
}
