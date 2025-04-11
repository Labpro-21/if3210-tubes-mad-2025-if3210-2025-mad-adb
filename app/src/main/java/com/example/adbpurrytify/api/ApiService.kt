package com.example.adbpurrytify.api

import com.example.adbpurrytify.data.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST


data class LoginRequest(val email: String, val password: String)
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String
)

data class RefreshTokenRequest(val refreshToken: String)
data class RefreshTokenResponse(val accessToken: String, val refreshToken: String)
data class UserProfile(
    val id: Long,
    val username: String,
    val email: String,
    val profilePhoto: String,
    val location: String,
    val createdAt: String,
    val updatedAt: String
    )


interface ApiService {
    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/profile")
    suspend fun getProfile(
        @Header("Authorization") authHeader: String
    ): Response<UserProfile>

    @POST("api/refresh-token")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<RefreshTokenResponse>


    @GET("api/verify-token")
    suspend fun verifyTokenWithAuth(
        @Header("Authorization") authHeader: String
    ): Response<Unit>
}
