package com.example.adbpurrytify.ui.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adbpurrytify.data.AnalyticsRepository
import com.example.adbpurrytify.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


data class SongListeningData(
    val id: Long,
    val title: String,
    val artist: String,
    val imageUrl: String,
    val playsCount: Int,
    val minutesListened: Int,
    val rank: Int
)

data class TopSongsData(
    val month: String, // MM-YYYY format
    val displayMonth: String, // e.g., "April 2025"
    val totalSongs: Int,
    val songs: List<SongListeningData>
)

@HiltViewModel
class TopSongsViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TopSongsUiState>(TopSongsUiState.Loading)
    val uiState: StateFlow<TopSongsUiState> = _uiState.asStateFlow()

    sealed class TopSongsUiState {
        object Loading : TopSongsUiState()
        data class Success(val data: TopSongsData) : TopSongsUiState()
        data class Error(val message: String) : TopSongsUiState()
    }

    fun loadTopSongsData(monthYear: String) { // MM-YYYY format
        viewModelScope.launch {
            _uiState.value = TopSongsUiState.Loading

            try {
                val userResult = authRepository.getCurrentUser()
                if (userResult.isSuccess) {
                    val userId = userResult.getOrThrow().id
                    val parts = monthYear.split("-")
                    val month = parts[0].toInt()
                    val year = parts[1].toInt()

                    val data = analyticsRepository.getTopSongsData(userId, year, month)
                    _uiState.value = TopSongsUiState.Success(data)
                } else {
                    _uiState.value = TopSongsUiState.Error("Failed to get user data")
                }
            } catch (e: Exception) {
                _uiState.value = TopSongsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}