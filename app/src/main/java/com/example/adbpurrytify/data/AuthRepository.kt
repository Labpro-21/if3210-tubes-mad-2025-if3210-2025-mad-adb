package com.example.adbpurrytify.data

import android.util.Log
import com.example.adbpurrytify.api.ApiService
import com.example.adbpurrytify.api.UserProfile
import com.example.adbpurrytify.data.model.User
import retrofit2.HttpException

class AuthRepository constructor(
    private val apiService: ApiService
) {

    suspend fun isTokenValid(): Boolean {
        val token = TokenManager.getAuthToken() ?: return false
        return try {
            val response = apiService.verifyTokenWithAuth("Bearer $token")
            response.isSuccessful
        } catch (e: HttpException) {
            e.code() != 401
        } catch (e: Exception) {
            false
        }
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
