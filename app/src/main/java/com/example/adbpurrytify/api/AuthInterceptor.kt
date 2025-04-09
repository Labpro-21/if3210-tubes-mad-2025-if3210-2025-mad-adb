package com.example.adbpurrytify.api

import com.example.adbpurrytify.data.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = TokenManager.getAuthToken() // Retrieve token
        val originalRequest = chain.request()

        if (token != null && originalRequest.header("Authorization") == null) {
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            return chain.proceed(newRequest)
        }

        return chain.proceed(originalRequest)
    }
}
