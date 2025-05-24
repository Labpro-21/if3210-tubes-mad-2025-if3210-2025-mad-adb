package com.example.adbpurrytify.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adbpurrytify.data.AuthRepository
import com.example.adbpurrytify.data.SongRepository
import com.example.adbpurrytify.data.model.User
import com.example.adbpurrytify.data.model.UserStats
import com.example.adbpurrytify.data.model.SoundCapsule
import com.example.adbpurrytify.data.model.Artist
import com.example.adbpurrytify.data.model.Song
import com.example.adbpurrytify.data.model.DayStreak
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val songRepository: SongRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState

    private val _selectedMonth = MutableStateFlow(getCurrentMonth())
    val selectedMonth: StateFlow<String> = _selectedMonth

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

                    // Get sound capsules for the last 4 months
                    val soundCapsules = getSoundCapsules()

                    _uiState.value = ProfileUiState.Success(user, stats, soundCapsules)
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

    fun selectMonth(month: String) {
        _selectedMonth.value = month
    }

    private fun getCurrentMonth(): String {
        val calendar = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return monthFormat.format(calendar.time)
    }

    private fun getSoundCapsules(): Map<String, SoundCapsule> {
        val calendar = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val capsules = mutableMapOf<String, SoundCapsule>()

        // Generate last 4 months including current month
        for (i in 0..3) {
            val monthKey = monthFormat.format(calendar.time)

            when (i) {
                0 -> { // Current month - April 2025
                    capsules[monthKey] = SoundCapsule(
                        month = monthKey,
                        timeListened = 862,
                        topArtist = Artist(
                            id = 1,
                            name = "The Beatles",
                            imageUrl = "https://example.com/beatles.jpg"
                        ),
                        topSong = Song(
                            id = 1,
                            title = "Starboy",
                            artist = "The Weeknd",
                            imageUrl = "https://example.com/starboy.jpg"
                        ),
                        dayStreak = DayStreak(
                            songTitle = "Loose",
                            artist = "Daniel Caesar",
                            imageUrl = "https://example.com/loose.jpg",
                            streakDays = 5,
                            dateRange = "Mar 21-26, 2025"
                        )
                    )
                }
                1 -> { // March 2025
                    capsules[monthKey] = SoundCapsule(
                        month = monthKey,
                        timeListened = 1240,
                        topArtist = Artist(
                            id = 2,
                            name = "Taylor Swift",
                            imageUrl = "https://example.com/taylor.jpg"
                        ),
                        topSong = Song(
                            id = 2,
                            title = "Anti-Hero",
                            artist = "Taylor Swift",
                            imageUrl = "https://example.com/antihero.jpg"
                        ),
                        dayStreak = DayStreak(
                            songTitle = "Lavender Haze",
                            artist = "Taylor Swift",
                            imageUrl = "https://example.com/lavender.jpg",
                            streakDays = 7,
                            dateRange = "Mar 15-21, 2025"
                        )
                    )
                }
                2 -> { // February 2025 - No data
                    capsules[monthKey] = SoundCapsule(
                        month = monthKey,
                        timeListened = 0,
                        topArtist = null,
                        topSong = null,
                        dayStreak = null,
                        hasData = false
                    )
                }
                3 -> { // January 2025
                    capsules[monthKey] = SoundCapsule(
                        month = monthKey,
                        timeListened = 956,
                        topArtist = Artist(
                            id = 3,
                            name = "Dua Lipa",
                            imageUrl = "https://example.com/dualipa.jpg"
                        ),
                        topSong = Song(
                            id = 3,
                            title = "Levitating",
                            artist = "Dua Lipa",
                            imageUrl = "https://example.com/levitating.jpg"
                        ),
                        dayStreak = DayStreak(
                            songTitle = "Physical",
                            artist = "Dua Lipa",
                            imageUrl = "https://example.com/physical.jpg",
                            streakDays = 3,
                            dateRange = "Jan 10-12, 2025"
                        )
                    )
                }
            }

            calendar.add(Calendar.MONTH, -1)
        }

        return capsules
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
        data class Success(
            val user: User,
            val stats: UserStats,
            val soundCapsules: Map<String, SoundCapsule>
        ) : ProfileUiState()
        data class Error(val message: String) : ProfileUiState()
    }
}
