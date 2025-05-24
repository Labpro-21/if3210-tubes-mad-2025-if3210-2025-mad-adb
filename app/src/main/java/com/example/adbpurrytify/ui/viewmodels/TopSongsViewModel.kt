package com.example.adbpurrytify.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class TopSongsViewModel @Inject constructor() : ViewModel() {

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
                // TODO: Replace with actual API call
                val mockData = generateMockTopSongsData(monthYear)
                _uiState.value = TopSongsUiState.Success(mockData)
            } catch (e: Exception) {
                _uiState.value = TopSongsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun generateMockTopSongsData(monthYear: String): TopSongsData {
        val displayMonth = formatMonthYearForDisplay(monthYear)

        val songs = listOf(
            "Starboy" to "The Weeknd",
            "Loose" to "Daniel Caesar",
            "Nights" to "Frank Ocean",
            "Doomsday" to "MF DOOM",
            "Good 4 U" to "Olivia Rodrigo",
            "HUMBLE." to "Kendrick Lamar",
            "Blinding Lights" to "The Weeknd",
            "Come Through and Chill" to "Miguel",
            "Golden" to "Harry Styles",
            "Levitating" to "Dua Lipa"
        ).mapIndexed { index, (title, artist) ->
            SongListeningData(
                id = index.toLong(),
                title = title,
                artist = artist,
                imageUrl = "",
                playsCount = (50 - index * 3),
                minutesListened = (150 - index * 10),
                rank = index + 1
            )
        }

        return TopSongsData(
            month = monthYear,
            displayMonth = displayMonth,
            totalSongs = 203,
            songs = songs
        )
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
}
