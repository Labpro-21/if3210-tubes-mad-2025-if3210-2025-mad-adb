package com.example.adbpurrytify.data

import com.example.adbpurrytify.api.ApiService
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
}
