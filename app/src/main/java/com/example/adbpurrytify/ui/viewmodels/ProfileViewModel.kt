// app/src/main/java/com/example/adbpurrytify/ui/viewmodels/ProfileViewModel.kt
package com.example.adbpurrytify.ui.viewmodels

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adbpurrytify.data.AnalyticsRepository
import com.example.adbpurrytify.data.AuthRepository
import com.example.adbpurrytify.data.SongRepository
import com.example.adbpurrytify.data.export.ExportManager
import com.example.adbpurrytify.data.export.exportAsCSV
import com.example.adbpurrytify.data.export.exportAsPDF
import com.example.adbpurrytify.data.model.SoundCapsule
import com.example.adbpurrytify.data.model.User
import com.example.adbpurrytify.data.model.UserStats
import com.example.adbpurrytify.utils.PermissionHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val songRepository: SongRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val exportManager: ExportManager // Move to constructor
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState

    private val _selectedMonth = MutableStateFlow(getCurrentMonthKey())
    val selectedMonth: StateFlow<String> = _selectedMonth

    // Add these state flows for export status
    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState

    private var currentUserId: Long? = null

    init {
        loadProfile()
        startRealTimeUpdates()
    }


    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                val userResult = authRepository.getCurrentUser()

                if (userResult.isSuccess) {
                    val userProfile = userResult.getOrThrow()
                    currentUserId = userProfile.id

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

                    // Get sound capsules from real analytics data
                    val soundCapsules = getSoundCapsules(userProfile.id)

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

    private fun startRealTimeUpdates() {
        viewModelScope.launch {
            // Wait for user to be loaded
            while (currentUserId == null) {
                kotlinx.coroutines.delay(100)
            }

            val userId = currentUserId!!

            // Combine real-time stats with selected month to trigger updates
            combine(
                selectedMonth,
                analyticsRepository.getRealTimeStats(userId, getCurrentYear(), getCurrentMonth())
            ) { month, realTimeStats ->
                month to realTimeStats
            }.distinctUntilChanged()
                .collect { (selectedMonth, _) ->
                    // Update sound capsules when real-time data changes
                    updateSoundCapsulesRealTime(userId)
                }
        }
    }

    private suspend fun updateSoundCapsulesRealTime(userId: Long) {
        try {
            val currentState = _uiState.value
            if (currentState is ProfileUiState.Success) {
                // Reload sound capsules with fresh data
                val updatedCapsules = getSoundCapsules(userId)

                _uiState.value = currentState.copy(
                    soundCapsules = updatedCapsules
                )
            }
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Error updating real-time sound capsules", e)
        }
    }

    fun selectMonth(month: String) {
        _selectedMonth.value = month
    }

    private fun getCurrentMonthKey(): String {
        val calendar = Calendar.getInstance()
        return String.format("%02d-%04d", calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR))
    }

    private fun getCurrentYear(): Int {
        return Calendar.getInstance().get(Calendar.YEAR)
    }

    private fun getCurrentMonth(): Int {
        return Calendar.getInstance().get(Calendar.MONTH) + 1
    }

    private suspend fun getSoundCapsules(userId: Long): Map<String, SoundCapsule> {
        val capsules = mutableMapOf<String, SoundCapsule>()

        try {
            // Get available months from analytics
            val availableMonths = analyticsRepository.getAvailableMonths(userId)

            // Always include current month for real-time updates
            val currentMonthKey = getCurrentMonthKey()
            val monthsToLoad = (availableMonths + currentMonthKey).distinct().take(6)

            for (monthKey in monthsToLoad) {
                val parts = monthKey.split("-")
                val month = parts[0].toInt()
                val year = parts[1].toInt()

                val capsule = analyticsRepository.getSoundCapsule(userId, year, month)
                capsules[monthKey] = capsule
            }

            // If no months at all, ensure current month exists
            if (capsules.isEmpty()) {
                val parts = currentMonthKey.split("-")
                val month = parts[0].toInt()
                val year = parts[1].toInt()

                capsules[currentMonthKey] = SoundCapsule(
                    month = currentMonthKey,
                    displayMonth = formatMonthForDisplay(currentMonthKey),
                    timeListened = 0,
                    topArtist = null,
                    topSong = null,
                    dayStreak = null,
                    hasData = false
                )
            }
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Error loading sound capsules", e)
            // Fallback to current month with no data
            val currentMonthKey = getCurrentMonthKey()
            capsules[currentMonthKey] = SoundCapsule(
                month = currentMonthKey,
                displayMonth = formatMonthForDisplay(currentMonthKey),
                timeListened = 0,
                topArtist = null,
                topSong = null,
                dayStreak = null,
                hasData = false
            )
        }

        return capsules
    }

    private suspend fun getUserStats(userId: Long): UserStats {
        return try {
            val allSongs = songRepository.getAllSongs(userId).first()
            val likedSongs = songRepository.getLikedSongs(userId).first()
            val listenedSongs = songRepository.getRecentlyPlayedSongs(userId).first()

            UserStats(
                songCount = allSongs.size,
                likedCount = likedSongs.size,
                listenedCount = listenedSongs.size
            )
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Error loading user stats", e)
            UserStats(songCount = 0, likedCount = 0, listenedCount = 0)
        }
    }

    fun exportSoundCapsuleWithPermission(context: Context, month: String, format: ExportFormat) {
        viewModelScope.launch {
            // Check if we have storage permission
            if (PermissionHandler.hasStoragePermission(context)) {
                // We have permission, proceed with export
                exportSoundCapsule(context, month, format)
            } else {
                // Show error message asking user to grant permission
                _exportState.value = ExportState.Error("Storage permission required. Please grant permission in app settings to download files.")

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Please grant storage permission to download files",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    fun shareSoundCapsuleWithPermission(context: Context, month: String, format: ExportFormat) {
        viewModelScope.launch {
            // For sharing, we don't need external storage permission since we can use app cache
            shareSoundCapsule(context, month, format)
        }
    }

    private fun formatMonthForDisplay(monthYear: String): String {
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

    sealed class ProfileUiState {
        data object Loading : ProfileUiState()
        data class Success(
            val user: User,
            val stats: UserStats,
            val soundCapsules: Map<String, SoundCapsule>
        ) : ProfileUiState()
        data class Error(val message: String) : ProfileUiState()
    }


    sealed class ExportState {
        object Idle : ExportState()
        object Loading : ExportState()
        data class Success(val uri: Uri, val fileName: String) : ExportState()
        data class Error(val message: String) : ExportState()
    }

    fun exportSoundCapsule(context: Context, month: String, format: ExportFormat) {
        viewModelScope.launch {
            _exportState.value = ExportState.Loading

            try {
                val currentState = _uiState.value
                if (currentState is ProfileUiState.Success) {
                    val soundCapsule = currentState.soundCapsules[month]
                    if (soundCapsule == null) {
                        _exportState.value = ExportState.Error("Sound capsule not found for $month")
                        return@launch
                    }

                    // Get detailed data for export
                    val userId = currentState.user.id
                    val parts = month.split("-")
                    val monthInt = parts[0].toInt()
                    val year = parts[1].toInt()

                    val topArtistsData = try {
                        analyticsRepository.getTopArtistsData(userId, year, monthInt)
                    } catch (e: Exception) {
                        null
                    }

                    val topSongsData = try {
                        analyticsRepository.getTopSongsData(userId, year, monthInt)
                    } catch (e: Exception) {
                        null
                    }

                    val result = when (format) {
                        ExportFormat.CSV -> soundCapsule.exportAsCSV(
                            context, exportManager, topArtistsData, topSongsData
                        )
                        ExportFormat.PDF -> soundCapsule.exportAsPDF(
                            context, exportManager, topArtistsData, topSongsData
                        )
                    }

                    if (result.isSuccess) {
                        val uri = result.getOrThrow()
                        val fileName = "sound_capsule_${month}.${format.extension}"
                        _exportState.value = ExportState.Success(uri, fileName)

                        // Show success message
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Export completed: $fileName", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val error = result.exceptionOrNull()
                        _exportState.value = ExportState.Error(error?.message ?: "Export failed")
                    }
                } else {
                    _exportState.value = ExportState.Error("Profile data not loaded")
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Export error", e)
                _exportState.value = ExportState.Error(e.message ?: "Unknown export error")
            }
        }
    }

    fun shareSoundCapsule(context: Context, month: String, format: ExportFormat) {
        viewModelScope.launch {
            exportSoundCapsule(context, month, format)

            // Wait for export to complete, then share
            exportState.collect { state ->
                if (state is ExportState.Success) {
                    try {
                        val mimeType = when (format) {
                            ExportFormat.CSV -> "text/csv"
                            ExportFormat.PDF -> "application/pdf"
                        }
                        exportManager.shareFile(context, state.uri, mimeType)
                    } catch (e: Exception) {
                        _exportState.value = ExportState.Error("Failed to share: ${e.message}")
                    }
                    return@collect
                }
            }
        }
    }

    fun clearExportState() {
        _exportState.value = ExportState.Idle
    }

    enum class ExportFormat(val extension: String) {
        CSV("csv"),
        PDF("pdf")
    }
}


