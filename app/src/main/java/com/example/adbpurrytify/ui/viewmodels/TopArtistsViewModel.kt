package com.example.adbpurrytify.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

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
class TopArtistsViewModel @Inject constructor() : ViewModel() {

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
                // TODO: Replace with actual API call
                val mockData = generateMockTopArtistsData(monthYear)
                _uiState.value = TopArtistsUiState.Success(mockData)
            } catch (e: Exception) {
                _uiState.value = TopArtistsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun generateMockTopArtistsData(monthYear: String): TopArtistsData {
        val displayMonth = formatMonthYearForDisplay(monthYear)

        val artists = listOf(
            "The Beatles", "The Weeknd", "Kanye West", "Doechii", "Frank Ocean",
            "Daniel Caesar", "Tyler, The Creator", "Kendrick Lamar", "SZA", "Billie Eilish"
        ).mapIndexed { index, name ->
            ArtistListeningData(
                id = index.toLong(),
                name = name,
                imageUrl = "",
                minutesListened = (200 - index * 15),
                songsCount = (20 - index * 2),
                rank = index + 1
            )
        }

        return TopArtistsData(
            month = monthYear,
            displayMonth = displayMonth,
            totalArtists = 137,
            artists = artists
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
