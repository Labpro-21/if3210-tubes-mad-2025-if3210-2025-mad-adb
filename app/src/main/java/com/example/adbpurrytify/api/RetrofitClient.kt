package com.example.adbpurrytify.api

import android.content.Context
import com.example.adbpurrytify.data.TokenManager
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * RetrofitClient for API calls
 *
 * Note: This is a transitional class that will eventually be replaced completely by NetworkModule
 * For new code, prefer dependency injection through NetworkModule
 */
@Singleton
class RetrofitClient @Inject constructor(@ApplicationContext context: Context) {

    private val BASE_URL = "http://34.101.226.132:3000/"

    // Initialize TokenManager with context
    private val tokenManager = TokenManager(context)

    private val interceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(AuthInterceptor(tokenManager))
        // Add interceptor to check http response body
        .addInterceptor(interceptor)
        .build()

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(ApiService::class.java)
    }

    companion object {
        // Static instance for backward compatibility
        // This will be removed once migration to DI is complete
        @Volatile
        private var INSTANCE: RetrofitClient? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                synchronized(this) {
                    if (INSTANCE == null) {
                        INSTANCE = RetrofitClient(context.applicationContext)
                    }
                }
            }
        }

        // Accessor for the instance during transition
        val instance: ApiService
            get() = INSTANCE?.instance
                ?: throw IllegalStateException("RetrofitClient not initialized. Call initialize() first.")
    }
}