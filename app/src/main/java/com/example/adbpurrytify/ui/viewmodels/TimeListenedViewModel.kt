package com.example.adbpurrytify.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

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
class TimeListenedViewModel @Inject constructor() : ViewModel() {

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
                // TODO: Replace with actual API call
                val mockData = generateMockTimeListenedData(monthYear)
                _uiState.value = TimeListenedUiState.Success(mockData)
            } catch (e: Exception) {
                _uiState.value = TimeListenedUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun generateMockTimeListenedData(monthYear: String): TimeListenedData {
        val displayMonth = formatMonthYearForDisplay(monthYear)
        val daysInMonth = getDaysInMonth(monthYear)
        val monthAbbr = getMonthAbbreviation(monthYear)

        // Generate daily data
        val dailyData = (1..daysInMonth).map { day ->
            DailyListeningData(
                day = day,
                minutes = (10..120).random(),
                date = "$monthAbbr $day"
            )
        }

        // Aggregate into weekly data
        val weeklyData = aggregateToWeeklyData(dailyData, monthAbbr)

        val totalMinutes = dailyData.sumOf { it.minutes }
        val dailyAverage = totalMinutes / dailyData.size
        val weeklyAverage = totalMinutes / weeklyData.size

        return TimeListenedData(
            month = monthYear,
            displayMonth = displayMonth,
            totalMinutes = totalMinutes,
            dailyAverage = dailyAverage,
            weeklyAverage = weeklyAverage,
            dailyData = dailyData,
            weeklyData = weeklyData
        )
    }

    private fun aggregateToWeeklyData(dailyData: List<DailyListeningData>, monthAbbr: String): List<WeeklyListeningData> {
        val weeks = mutableListOf<WeeklyListeningData>()
        val daysPerWeek = 7

        for (weekIndex in 0 until (dailyData.size + daysPerWeek - 1) / daysPerWeek) {
            val startDay = weekIndex * daysPerWeek
            val endDay = minOf(startDay + daysPerWeek - 1, dailyData.size - 1)

            val weekDays = dailyData.subList(startDay, endDay + 1)
            val weekMinutes = weekDays.sumOf { it.minutes }

            val startDate = dailyData[startDay].day
            val endDate = dailyData[endDay].day

            weeks.add(
                WeeklyListeningData(
                    weekNumber = weekIndex + 1,
                    minutes = weekMinutes,
                    weekLabel = "Week ${weekIndex + 1}",
                    dateRange = "$monthAbbr $startDate-$endDate"
                )
            )
        }

        return weeks
    }

    private fun formatMonthYearForDisplay(monthYear: String): String {
        return try {
            val parts = monthYear.split("-")
            if (parts.size == 2) {
                val month = parts[0].toInt()
                val year = parts[1]
                val monthNames = listOf(
                    "January", "February", "March", "April", "May", "June",
                    "July", "August", "September", "October", "November", "December"
                )
                "${monthNames[month - 1]} $year"
            } else {
                monthYear
            }
        } catch (e: Exception) {
            monthYear
        }
    }

    private fun getMonthAbbreviation(monthYear: String): String {
        return try {
            val parts = monthYear.split("-")
            if (parts.size == 2) {
                val month = parts[0].toInt()
                val monthAbbreviations = listOf(
                    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
                )
                monthAbbreviations[month - 1]
            } else {
                "Jan"
            }
        } catch (e: Exception) {
            "Jan"
        }
    }

    private fun getDaysInMonth(monthYear: String): Int {
        return try {
            val parts = monthYear.split("-")
            if (parts.size == 2) {
                val month = parts[0].toInt()
                val year = parts[1].toInt()

                when (month) {
                    2 -> if (isLeapYear(year)) 29 else 28
                    4, 6, 9, 11 -> 30
                    else -> 31
                }
            } else {
                30
            }
        } catch (e: Exception) {
            30
        }
    }

    private fun isLeapYear(year: Int): Boolean {
        return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
    }
}
