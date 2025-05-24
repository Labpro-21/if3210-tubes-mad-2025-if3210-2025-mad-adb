package com.example.adbpurrytify.api

import com.example.adbpurrytify.data.model.TrendingSongResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path


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

    @Multipart
    @PATCH("api/profile")
    suspend fun updateProfile(
        @Header("Authorization") authHeader: String,
        @PartMap parts: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part profilePhoto: MultipartBody.Part? = null
    ): Response<Unit>

    @POST("api/refresh-token")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<RefreshTokenResponse>

    @GET("api/verify-token")
    suspend fun verifyTokenWithAuth(
        @Header("Authorization") authHeader: String
    ): Response<Unit>

    @GET("api/top-songs/global")
    suspend fun getTopGlobalSongs(): Response<List<TrendingSongResponse>>

    @GET("api/top-songs/{country_code}")
    suspend fun getTopCountrySongs(@Path("country_code") countryCode: String): Response<List<TrendingSongResponse>>
}