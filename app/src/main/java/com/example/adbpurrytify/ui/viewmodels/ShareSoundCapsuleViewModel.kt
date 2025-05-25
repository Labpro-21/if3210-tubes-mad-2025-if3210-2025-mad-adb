package com.example.adbpurrytify.ui.viewmodels

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adbpurrytify.data.AnalyticsRepository
import com.example.adbpurrytify.data.AuthRepository
import com.example.adbpurrytify.data.model.SoundCapsule
import com.example.adbpurrytify.utils.SoundCapsuleImageGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShareSoundCapsuleViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ShareUiState>(ShareUiState.Idle)
    val uiState: StateFlow<ShareUiState> = _uiState.asStateFlow()

    private val _soundCapsule = MutableStateFlow<SoundCapsule?>(null)
    val soundCapsule: StateFlow<SoundCapsule?> = _soundCapsule.asStateFlow()

    sealed class ShareUiState {
        object Idle : ShareUiState()
        object Loading : ShareUiState()
        data class Success(val imageUri: Uri, val soundCapsule: SoundCapsule) : ShareUiState()
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

                // Load real sound capsule data
                val realSoundCapsule = analyticsRepository.getSoundCapsule(userId, year, monthInt)
                _soundCapsule.value = realSoundCapsule

                // Generate image with real data and actual album art
                val imageUri = SoundCapsuleImageGenerator.generateSoundCapsuleImage(
                    context = context,
                    soundCapsule = realSoundCapsule
                )

                if (imageUri != null) {
                    _uiState.value = ShareUiState.Success(imageUri, realSoundCapsule)
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
                putExtra(Intent.EXTRA_TEXT, "Check out my Sound Capsule from Purrytify! 🎵")
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
                        // Fallback to general share if LINE is not installed
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
            putExtra(Intent.EXTRA_TEXT, "Check out my Sound Capsule from Purrytify! 🎵")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(fallbackIntent, "Share Sound Capsule"))
    }
}

enum class SharePlatform(val displayName: String, val packageName: String) {
    LINE("LINE", "jp.naver.line.android"),
    INSTAGRAM("Instagram", "com.instagram.android"),
    TWITTER("Twitter", "com.twitter.android"),
    WHATSAPP("WhatsApp", "com.whatsapp")
}