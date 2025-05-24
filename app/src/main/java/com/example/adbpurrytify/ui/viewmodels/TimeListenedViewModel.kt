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

data class DailyListeningData(
    val day: Int,
    val minutes: Int,
    val date: String // e.g., "Apr 1"
)

data class WeeklyListeningData(
    val weekNumber: Int, // 1, 2, 3, 4, 5
    val minutes: Int,
    val weekLabel: String, // e.g., "Week 1", "Week 2"
    val dateRange: String // e.g., "Apr 1-7"
)

data class TimeListenedData(
    val month: String, // MM-YYYY format
    val displayMonth: String, // e.g., "April 2025"
    val totalMinutes: Int,
    val dailyAverage: Int,
    val weeklyAverage: Int,
    val dailyData: List<DailyListeningData>,
    val weeklyData: List<WeeklyListeningData>
)





@HiltViewModel
class TimeListenedViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TimeListenedUiState>(TimeListenedUiState.Loading)
    val uiState: StateFlow<TimeListenedUiState> = _uiState.asStateFlow()

    sealed class TimeListenedUiState {
        object Loading : TimeListenedUiState()
        data class Success(val data: TimeListenedData) : TimeListenedUiState()
        data class Error(val message: String) : TimeListenedUiState()
    }

    fun loadTimeListenedData(monthYear: String) { // MM-YYYY format
        viewModelScope.launch {
            _uiState.value = TimeListenedUiState.Loading

            try {
                val userResult = authRepository.getCurrentUser()
                if (userResult.isSuccess) {
                    val userId = userResult.getOrThrow().id
                    val parts = monthYear.split("-")
                    val month = parts[0].toInt()
                    val year = parts[1].toInt()

                    val data = analyticsRepository.getTimeListenedData(userId, year, month)
                    _uiState.value = TimeListenedUiState.Success(data)
                } else {
                    _uiState.value = TimeListenedUiState.Error("Failed to get user data")
                }
            } catch (e: Exception) {
                _uiState.value = TimeListenedUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
