// ShareDayStreakViewModel.kt
package com.example.adbpurrytify.ui.viewmodels

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adbpurrytify.data.AnalyticsRepository
import com.example.adbpurrytify.data.AuthRepository
import com.example.adbpurrytify.data.model.DayStreak
import com.example.adbpurrytify.utils.SoundCapsuleImageGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShareDayStreakViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ShareUiState>(ShareUiState.Idle)
    val uiState: StateFlow<ShareUiState> = _uiState.asStateFlow()

    private val _dayStreak = MutableStateFlow<DayStreak?>(null)
    val dayStreak: StateFlow<DayStreak?> = _dayStreak.asStateFlow()

    sealed class ShareUiState {
        object Idle : ShareUiState()
        object Loading : ShareUiState()
        data class Success(val imageUri: Uri, val dayStreak: DayStreak) : ShareUiState()
        data class Error(val message: String) : ShareUiState()
    }

    fun loadAndGenerateImage(context: Context, month: String) {
        viewModelScope.launch {
            _uiState.value = ShareUiState.Loading

            try {
                // Get current user
                val userResult = authRepository.getCurrentUser()
                if (userResult.isFailure) {
                    _uiState.value = ShareUiState.Error("Failed to get user data")
                    return@launch
                }

                val userId = userResult.getOrThrow().id
                val parts = month.split("-")
                val monthInt = parts[0].toInt()
                val year = parts[1].toInt()

                // Load real sound capsule data to get the day streak
                val soundCapsule = analyticsRepository.getSoundCapsule(userId, year, monthInt)
                val realDayStreak = soundCapsule.dayStreak

                if (realDayStreak == null) {
                    _uiState.value = ShareUiState.Error("No day streak data available for this month")
                    return@launch
                }

                _dayStreak.value = realDayStreak

                // Generate image with real data and actual album art
                val imageUri = SoundCapsuleImageGenerator.generateDayStreakImage(
                    context = context,
                    dayStreak = realDayStreak
                )

                if (imageUri != null) {
                    _uiState.value = ShareUiState.Success(imageUri, realDayStreak)
                } else {
                    _uiState.value = ShareUiState.Error("Failed to generate image")
                }
            } catch (e: Exception) {
                _uiState.value = ShareUiState.Error("Error loading data: ${e.message}")
            }
        }
    }

    fun shareUniversal(context: Context, imageUri: Uri, platform: SharePlatform? = null) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                putExtra(Intent.EXTRA_TEXT, "Check out my ${dayStreak.value?.streakDays}-day streak on Purrytify! 🔥🎵")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                // Set specific package if platform is specified
                platform?.packageName?.let { packageName ->
                    setPackage(packageName)
                }
            }

            when (platform) {
                SharePlatform.LINE -> {
                    if (shareIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(shareIntent)
                    } else {
                        shareGeneral(context, imageUri)
                    }
                }
                SharePlatform.INSTAGRAM -> {
                    if (shareIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(shareIntent)
                    } else {
                        shareGeneral(context, imageUri)
                    }
                }
                SharePlatform.TWITTER -> {
                    if (shareIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(shareIntent)
                    } else {
                        shareGeneral(context, imageUri)
                    }
                }
                SharePlatform.WHATSAPP -> {
                    if (shareIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(shareIntent)
                    } else {
                        shareGeneral(context, imageUri)
                    }
                }
                null -> {
                    // General share with all apps
                    shareGeneral(context, imageUri)
                }
            }
        } catch (e: Exception) {
            _uiState.value = ShareUiState.Error("Failed to share: ${e.message}")
        }
    }

    private fun shareGeneral(context: Context, imageUri: Uri) {
        val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, "Check out my ${dayStreak.value?.streakDays}-day streak on Purrytify! 🔥🎵")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(fallbackIntent, "Share Day Streak"))
    }
}