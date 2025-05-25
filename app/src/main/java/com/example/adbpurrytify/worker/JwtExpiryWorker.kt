// service/JwtExpiryWorker.kt
package com.example.adbpurrytify.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.adbpurrytify.api.RefreshTokenRequest
import com.example.adbpurrytify.api.RetrofitClient
import com.example.adbpurrytify.data.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit

class JwtExpiryWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "JwtExpiryCheckWork"
        private const val TAG = "JwtExpiryWorker"
        private const val REFRESH_INTERVAL_MINUTES = 1L
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "JwtExpiryWorker is performing periodic token refresh...")

        val refreshToken = TokenManager.getRefreshToken()

        if (refreshToken == null) {
            Log.d(TAG, "No refresh token found, stopping work.")
            return@withContext Result.success()
        }

        try {
            val result = attemptTokenRefresh(refreshToken)

            if (result == Result.success()) {
                scheduleNextWorker(applicationContext)
            }

            return@withContext result

        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during periodic token refresh", e)
            // ❌ DON'T schedule next worker on unexpected errors
            return@withContext Result.failure()
        }
    }

    private suspend fun attemptTokenRefresh(refreshToken: String): Result {
        try {
            Log.d(TAG, "Attempting periodic token refresh...")
            val refreshResponse = RetrofitClient.instance.refreshToken(
                RefreshTokenRequest(refreshToken)
            )

            if (refreshResponse.isSuccessful && refreshResponse.body() != null) {
                val newTokens = refreshResponse.body()!!
                TokenManager.saveAuthToken(newTokens.accessToken)
                TokenManager.saveRefreshToken(newTokens.refreshToken)

                Log.i(TAG, "Periodic token refresh successful!")
                return Result.success()
            } else {
                Log.e(TAG, "Periodic token refresh failed with code: ${refreshResponse.code()}. Logging out.")
                performLogout()
                // ❌ DON'T schedule next worker when tokens are invalid
                return Result.success() // Success means we handled the error properly
            }
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP error during periodic token refresh: ${e.code()}. Logging out.", e)
            performLogout()
            return Result.success()
        } catch (e: IOException) {
            Log.e(TAG, "Network error during periodic token refresh.", e)
            return Result.retry() // Retry network errors
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during periodic token refresh. Logging out.", e)
            performLogout()
            return Result.failure()
        }
    }

    private fun performLogout() {
        Log.i(TAG, "Performing logout: Clearing tokens.")
        TokenManager.clearTokens()

        // ✅ Cancel all future work when logging out
        WorkManager.getInstance(applicationContext).cancelUniqueWork(WORK_NAME)
    }

    private fun scheduleNextWorker(context: Context) {
        val nextWork = OneTimeWorkRequestBuilder<JwtExpiryWorker>()
            .setInitialDelay(REFRESH_INTERVAL_MINUTES, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        // ✅ Use unique work to prevent multiple workers
        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE, // Replace any existing work
            nextWork
        )
        Log.d(TAG, "Scheduled next token refresh in $REFRESH_INTERVAL_MINUTES minutes")
    }
}
