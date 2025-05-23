package com.example.adbpurrytify

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import com.example.adbpurrytify.api.RetrofitClient
import com.example.adbpurrytify.data.TokenManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PurrytifyApplication : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()

        try {
            // Initialize legacy singletons for backward compatibility
            // These should eventually be removed once migration to DI is complete
            TokenManager.initialize(applicationContext)
            RetrofitClient.initialize(applicationContext)

            Log.d("PurrytifyApplication", "Application initialized successfully")
        } catch (e: Exception) {
            // Log any initialization errors
            Log.e("PurrytifyApplication", "Error initializing application", e)
        }
    }

    // Implementation of Configuration.Provider interface
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}