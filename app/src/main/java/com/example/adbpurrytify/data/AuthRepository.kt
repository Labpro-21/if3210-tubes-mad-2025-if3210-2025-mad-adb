package com.example.adbpurrytify.data

import android.util.Log
import com.example.adbpurrytify.api.ApiService
import com.example.adbpurrytify.api.RefreshTokenRequest
import com.example.adbpurrytify.api.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class AuthRepository(
    private val apiService: ApiService
) {

    suspend fun isTokenValid(): Boolean {
        val token = TokenManager.getAuthToken() ?: return false

        return try {
            val response = apiService.verifyTokenWithAuth("Bearer $token")
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error validating token", e)
            false
        }
    }

    // Try to refresh the token and return whether it succeeded
    suspend fun refreshToken(): Boolean = withContext(Dispatchers.IO) {
        try {
            val refreshToken = TokenManager.getRefreshToken() ?: return@withContext false
            Log.d("AuthRepository", "Attempting to refresh token")

            val request = RefreshTokenRequest(refreshToken)
            val response = apiService.refreshToken(request)

            if (response.isSuccessful && response.body() != null) {
                val tokenResponse = response.body()!!
                TokenManager.saveAuthToken(tokenResponse.accessToken)
                TokenManager.saveRefreshToken(tokenResponse.refreshToken)
                Log.d("AuthRepository", "Token refreshed successfully")
                return@withContext true
            } else {
                Log.e("AuthRepository", "Failed to refresh token: ${response.code()}")
                return@withContext false
            }
        } catch (e: HttpException) {
            Log.e("AuthRepository", "HTTP error during token refresh: ${e.code()}", e)
            return@withContext false
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error refreshing token", e)
            return@withContext false
        }
    }

    // Validate and refresh token if needed
    suspend fun validateAndRefreshTokenIfNeeded(): Boolean {
        // First check if current token is valid
        if (isTokenValid()) {
            return true
        }

        // If not valid, try to refresh
        return refreshToken()
    }


    suspend fun currentUser(): UserProfile? {
        val token = TokenManager.getAuthToken() ?: return null
        return try {
            apiService.getProfile("Bearer $token").body()
        } catch (e: Exception) {
            Log.e("Profile Request", e.toString())
            null
        }
    }
}
