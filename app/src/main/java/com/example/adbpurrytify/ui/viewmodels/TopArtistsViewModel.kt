package com.example.adbpurrytify.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject



import com.example.adbpurrytify.data.AnalyticsRepository
import com.example.adbpurrytify.data.AuthRepository

data class ArtistListeningData(
    val id: Long,
    val name: String,
    val imageUrl: String,
    val minutesListened: Int,
    val songsCount: Int,
    val rank: Int
)

data class TopArtistsData(
    val month: String, // MM-YYYY format
    val displayMonth: String, // e.g., "April 2025"
    val totalArtists: Int,
    val artists: List<ArtistListeningData>
)




@HiltViewModel
class TopArtistsViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TopArtistsUiState>(TopArtistsUiState.Loading)
    val uiState: StateFlow<TopArtistsUiState> = _uiState.asStateFlow()

    sealed class TopArtistsUiState {
        object Loading : TopArtistsUiState()
        data class Success(val data: TopArtistsData) : TopArtistsUiState()
        data class Error(val message: String) : TopArtistsUiState()
    }

    fun loadTopArtistsData(monthYear: String) { // MM-YYYY format
        viewModelScope.launch {
            _uiState.value = TopArtistsUiState.Loading

            try {
                val userResult = authRepository.getCurrentUser()
                if (userResult.isSuccess) {
                    val userId = userResult.getOrThrow().id
                    val parts = monthYear.split("-")
                    val month = parts[0].toInt()
                    val year = parts[1].toInt()

                    val data = analyticsRepository.getTopArtistsData(userId, year, month)
                    _uiState.value = TopArtistsUiState.Success(data)
                } else {
                    _uiState.value = TopArtistsUiState.Error("Failed to get user data")
                }
            } catch (e: Exception) {
                _uiState.value = TopArtistsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
