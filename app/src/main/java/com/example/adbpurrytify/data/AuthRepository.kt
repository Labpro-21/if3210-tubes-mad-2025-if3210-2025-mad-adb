package com.example.adbpurrytify.data

import android.util.Log
import com.example.adbpurrytify.api.ApiService
import com.example.adbpurrytify.api.LoginRequest
import com.example.adbpurrytify.api.RefreshTokenRequest
import com.example.adbpurrytify.api.UserProfile
import com.example.adbpurrytify.data.local.AnalyticsDao
import com.example.adbpurrytify.data.model.SongEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for authentication-related operations.
 * Handles login, token validation, profile information, and profile updates.
 */
@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager,
    private val analyticsDao: AnalyticsDao,
    private val songRepository: SongRepository
) {
    private val TAG = "AuthRepository"

    suspend fun login(email: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = LoginRequest(email, password)
            val response = apiService.login(request)

            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                tokenManager.saveAuthToken(loginResponse.accessToken)
                tokenManager.saveRefreshToken(loginResponse.refreshToken)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Login failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Login error", e)
            Result.failure(e)
        }
    }

    suspend fun isTokenValid(): Boolean {
        val token = tokenManager.getAuthToken() ?: return false

        return try {
            val response = apiService.verifyTokenWithAuth("Bearer $token")
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(TAG, "Error validating token", e)
            false
        }
    }

    suspend fun refreshToken(): Boolean = withContext(Dispatchers.IO) {
        try {
            val refreshToken = tokenManager.getRefreshToken() ?: return@withContext false
            Log.d(TAG, "Attempting to refresh token")

            val request = RefreshTokenRequest(refreshToken)
            val response = apiService.refreshToken(request)

            if (response.isSuccessful && response.body() != null) {
                val tokenResponse = response.body()!!
                tokenManager.saveAuthToken(tokenResponse.accessToken)
                tokenManager.saveRefreshToken(tokenResponse.refreshToken)
                Log.d(TAG, "Token refreshed successfully")
                return@withContext true
            } else {
                Log.e(TAG, "Failed to refresh token: ${response.code()}")
                return@withContext false
            }
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP error during token refresh: ${e.code()}", e)
            return@withContext false
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing token", e)
            return@withContext false
        }
    }

    suspend fun validateAndRefreshTokenIfNeeded(): Boolean {
        // First check if current token is valid
        if (isTokenValid()) {
            return true
        }

        // If not valid, try to refresh
        return refreshToken()
    }

    suspend fun getCurrentUser(): Result<UserProfile> = withContext(Dispatchers.IO) {
        val token = tokenManager.getAuthToken() ?: return@withContext Result.failure(Exception("No auth token"))

        try {
            val response = apiService.getProfile("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get profile: ${response.code()}"))
            }
        } catch (e: HttpException) {
            if (e.code() == 401) {
                // Try token refresh if unauthorized
                if (refreshToken()) {
                    // Retry with new token
                    val newToken = tokenManager.getAuthToken()!!
                    try {
                        val retryResponse = apiService.getProfile("Bearer $newToken")
                        if (retryResponse.isSuccessful && retryResponse.body() != null) {
                            return@withContext Result.success(retryResponse.body()!!)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error getting profile after token refresh", e)
                    }
                }
            }
            Log.e(TAG, "HTTP error getting profile", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting profile", e)
            Result.failure(e)
        }
    }

    suspend fun updateProfile(
        parts: Map<String, RequestBody>,
        imagePart: MultipartBody.Part? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val token = tokenManager.getAuthToken() ?: return@withContext Result.failure(Exception("No auth token"))

        try {
            Log.d(TAG, "Updating profile with ${parts.size} text parts and image: ${imagePart != null}")

            val response = apiService.updateProfile("Bearer $token", parts, imagePart)

            if (response.isSuccessful) {
                Log.d(TAG, "Profile updated successfully")
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Profile update failed: ${response.code()} - $errorBody")
                Result.failure(Exception("Failed to update profile: ${response.code()} - $errorBody"))
            }
        } catch (e: HttpException) {
            if (e.code() == 401) {
                // Try token refresh if unauthorized
                if (refreshToken()) {
                    // Retry with new token
                    val newToken = tokenManager.getAuthToken()!!
                    try {
                        val retryResponse = apiService.updateProfile("Bearer $newToken", parts, imagePart)
                        if (retryResponse.isSuccessful) {
                            Log.d(TAG, "Profile updated successfully after token refresh")
                            return@withContext Result.success(Unit)
                        } else {
                            val errorBody = retryResponse.errorBody()?.string() ?: "Unknown error"
                            Log.e(TAG, "Profile update failed after retry: ${retryResponse.code()} - $errorBody")
                            return@withContext Result.failure(Exception("Failed to update profile: ${retryResponse.code()} - $errorBody"))
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating profile after token refresh", e)
                    }
                }
            }
            Log.e(TAG, "HTTP error updating profile", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating profile", e)
            Result.failure(e)
        }
    }

    fun logout() {
        tokenManager.clearTokens()
    }

    suspend fun checkForAutoResume(userId: Long): AutoResumeData? {
        return try {
            val incompleteSession = analyticsDao.getActiveListeningSession(userId)
            if (incompleteSession != null) {
                val song = songRepository.getSongById(incompleteSession.songId)
                if (song != null) {
                    // Mark the session as completed since we're resuming
                    val completedSession = incompleteSession.copy(
                        endTime = System.currentTimeMillis(),
                        duration = incompleteSession.duration
                    )
                    analyticsDao.updateListeningSession(completedSession)

                    AutoResumeData(
                        song = song,
                        lastPosition = incompleteSession.duration,
                        sessionStartTime = incompleteSession.startTime
                    )
                } else null
            } else null
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error checking auto-resume", e)
            null
        }
    }
}

data class AutoResumeData(
    val song: SongEntity,
    val lastPosition: Long,
    val sessionStartTime: Long
)