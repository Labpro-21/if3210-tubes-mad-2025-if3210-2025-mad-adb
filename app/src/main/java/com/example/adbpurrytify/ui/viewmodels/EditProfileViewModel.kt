package com.example.adbpurrytify.ui.viewmodels

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adbpurrytify.data.AuthRepository
import com.example.adbpurrytify.data.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = mutableStateOf<EditProfileUiState>(EditProfileUiState.Loading)
    val uiState: State<EditProfileUiState> = _uiState

    private val TAG = "EditProfileViewModel"

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = EditProfileUiState.Loading
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

                    _uiState.value = EditProfileUiState.Success(user)
                } else {
                    val exception = userResult.exceptionOrNull()
                    if (exception is HttpException && exception.code() == 401) {
                        _uiState.value = EditProfileUiState.Error("Session expired. Please log in again.")
                    } else {
                        _uiState.value = EditProfileUiState.Error(exception?.message ?: "Failed to load profile")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading profile", e)
                _uiState.value = EditProfileUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun validateLocation(location: String): LocationValidationResult {
        if (location.isBlank()) {
            return LocationValidationResult.Valid
        }

        val trimmedLocation = location.trim().uppercase()

        // Check if it's a valid supported country code (2 letters)
        if (trimmedLocation.length == 2 && trimmedLocation.all { it.isLetter() }) {
            if (isSupportedCountryCode(trimmedLocation)) {
                return LocationValidationResult.Valid
            } else {
                return LocationValidationResult.Invalid("Country not supported. Available countries: Indonesia (ID), Malaysia (MY), USA (US), UK (GB), Switzerland (CH), Germany (DE), Brazil (BR)")
            }
        }

        // Check if it's a valid supported country name
        if (trimmedLocation.length > 2) {
            val countryCode = getCountryCodeFromName(trimmedLocation)
            if (countryCode != null) {
                return LocationValidationResult.Valid
            } else {
                return LocationValidationResult.Invalid("Country not supported. Available countries: Indonesia, Malaysia, USA, UK, Switzerland, Germany, Brazil")
            }
        }

        return LocationValidationResult.Invalid("Please enter a valid country name or 2-letter country code")
    }

    private fun isSupportedCountryCode(code: String): Boolean {
        val supportedCountryCodes = setOf("ID", "MY", "US", "GB", "CH", "DE", "BR")
        return supportedCountryCodes.contains(code)
    }

    private fun getCountryCodeFromName(name: String): String? {
        val countryMap = mapOf(
            "INDONESIA" to "ID",
            "MALAYSIA" to "MY",
            "USA" to "US",
            "UNITED STATES" to "US",
            "UNITED STATES OF AMERICA" to "US",
            "AMERICA" to "US",
            "UK" to "GB",
            "UNITED KINGDOM" to "GB",
            "GREAT BRITAIN" to "GB",
            "BRITAIN" to "GB",
            "ENGLAND" to "GB",
            "SWITZERLAND" to "CH",
            "GERMANY" to "DE",
            "DEUTSCHLAND" to "DE",
            "BRAZIL" to "BR",
            "BRASIL" to "BR"
        )
        return countryMap[name]
    }

    private fun normalizeLocationInput(location: String): String {
        val trimmed = location.trim().uppercase()

        // If it's a country name, convert to country code
        val countryCode = getCountryCodeFromName(trimmed)
        return countryCode ?: trimmed
    }

    suspend fun updateProfile(
        location: String? = null,
        profileImageUri: Uri? = null,
        context: Context
    ): ProfileUpdateResult {
        return try {
            Log.d(TAG, "Starting profile update - Location: $location, Image: ${profileImageUri != null}")

            // Validate location first
            location?.let { loc ->
                if (loc.isNotBlank()) {
                    val validationResult = validateLocation(loc)
                    if (validationResult is LocationValidationResult.Invalid) {
                        return ProfileUpdateResult.Error(validationResult.message)
                    }
                }
            }

            // Prepare multipart request parts
            val parts = mutableMapOf<String, okhttp3.RequestBody>()
            var imagePart: MultipartBody.Part? = null

            // Add location if provided (normalize it first)
            location?.let {
                if (it.isNotBlank()) {
                    val normalizedLocation = normalizeLocationInput(it)
                    parts["location"] = normalizedLocation.toRequestBody("text/plain".toMediaTypeOrNull())
                    Log.d(TAG, "Added location to request: $normalizedLocation")
                }
            }

            // Add profile image if provided
            profileImageUri?.let { uri ->
                try {
                    val imageFile = createFileFromUri(context, uri)
                    val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                    imagePart = MultipartBody.Part.createFormData("profilePhoto", imageFile.name, requestFile)
                    Log.d(TAG, "Added image to request: ${imageFile.name}, size: ${imageFile.length()} bytes")
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing image file", e)
                    return ProfileUpdateResult.Error("Failed to process image file: ${e.message}")
                }
            }

            // First attempt
            Log.d(TAG, "Attempting profile update (1st try)")
            var result = authRepository.updateProfile(parts, imagePart)

            if (result.isSuccess) {
                Log.i(TAG, "Profile updated successfully on first attempt")
                // Invalidate profile cache and reload
                loadProfile()
                return ProfileUpdateResult.Success
            }

            // Check if it's a timeout or network error
            val firstError = result.exceptionOrNull()
            val isRetryableError = isRetryableError(firstError)

            if (isRetryableError) {
                Log.w(TAG, "First attempt failed with retryable error, trying again...", firstError)

                // Wait a bit before retry
                kotlinx.coroutines.delay(1000)

                // Second attempt
                Log.d(TAG, "Attempting profile update (2nd try)")
                result = authRepository.updateProfile(parts, imagePart)

                if (result.isSuccess) {
                    Log.i(TAG, "Profile updated successfully on second attempt")
                    // Invalidate profile cache and reload
                    loadProfile()
                    return ProfileUpdateResult.Success
                } else {
                    val secondError = result.exceptionOrNull()
                    Log.e(TAG, "Profile update failed on both attempts", secondError)
                    return ProfileUpdateResult.Error(
                        "Update failed after 2 attempts. Please check your connection and try again."
                    )
                }
            } else {
                Log.e(TAG, "Profile update failed with non-retryable error", firstError)
                return ProfileUpdateResult.Error(
                    firstError?.message ?: "Update failed. Please try again."
                )
            }

        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during profile update", e)
            ProfileUpdateResult.Error("Unexpected error: ${e.message}")
        }
    }

    private fun isRetryableError(error: Throwable?): Boolean {
        return when {
            error == null -> false
            error is java.net.SocketTimeoutException -> true
            error is java.net.ConnectException -> true
            error is java.net.UnknownHostException -> true
            error is java.io.IOException -> true
            error is HttpException -> {
                // Retry on server errors (5xx) but not client errors (4xx)
                error.code() >= 500
            }
            error.message?.contains("timeout", ignoreCase = true) == true -> true
            error.message?.contains("network", ignoreCase = true) == true -> true
            else -> false
        }
    }

    private fun createFileFromUri(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Cannot open input stream for URI")

        // Create a temporary file
        val tempFile = File(context.cacheDir, "profile_image_${System.currentTimeMillis()}.jpg")

        inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return tempFile
    }

    sealed class EditProfileUiState {
        data object Loading : EditProfileUiState()
        data class Success(val user: User) : EditProfileUiState()
        data class Error(val message: String) : EditProfileUiState()
    }

    sealed class ProfileUpdateResult {
        data object Success : ProfileUpdateResult()
        data class Error(val message: String) : ProfileUpdateResult()
    }

    sealed class LocationValidationResult {
        data object Valid : LocationValidationResult()
        data class Invalid(val message: String) : LocationValidationResult()
    }
}
