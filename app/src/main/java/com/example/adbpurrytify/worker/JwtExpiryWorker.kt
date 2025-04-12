// service/JwtExpiryWorker.kt
package com.example.adbpurrytify.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
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
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "JwtExpiryWorker is doing some work...")

        val currentToken = TokenManager.getAuthToken()
        val refreshToken = TokenManager.getRefreshToken()

        if (currentToken == null) {
            Log.d(TAG, "No auth token found, stopping work.")
            return@withContext Result.success()
        }

        try {
            Log.d(TAG, "Verifying token...")
            val verifyResponse = RetrofitClient.instance.verifyTokenWithAuth("Bearer $currentToken")


            if (verifyResponse.isSuccessful) {
                Log.d(TAG, "Token is still valid.")
                scheduleNextWorker(applicationContext)
                return@withContext Result.success()
            } else {
                Log.w(TAG, "Token verification failed with code: ${verifyResponse.code()}")
            }

        } catch (e: HttpException) {
            if (e.code() == 401) {
                Log.w(TAG, "Token verification returned 403 (likely expired). Attempting refresh.")
                if (refreshToken != null) {
                    val result = attemptTokenRefresh(refreshToken)

                    scheduleNextWorker(applicationContext)

                    return@withContext result
                } else {
                    Log.e(TAG, "Token expired, but no refresh token available. Logging out.")
                    performLogout()
                    return@withContext Result.success()
                }
            } else {
                Log.e(TAG, "HTTP error during token verification: ${e.code()}", e)
                return@withContext Result.retry()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error during token verification", e)
            return@withContext Result.retry()
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during token verification", e)
            return@withContext Result.failure()
        }


        Log.w(TAG, "Token verification failed unexpectedly. Attempting refresh if possible.")
        if (refreshToken != null) {
            return@withContext attemptTokenRefresh(refreshToken)
        } else {
            Log.e(TAG, "Token invalid, no refresh token. Logging out.")
            performLogout()
            return@withContext Result.success()
        }
    }

    private suspend fun attemptTokenRefresh(refreshToken: String): Result {
        try {
            Log.d(TAG, "Attempting token refresh...")
            val refreshResponse = RetrofitClient.instance.refreshToken(
                RefreshTokenRequest(refreshToken)
            )

            if (refreshResponse.isSuccessful && refreshResponse.body() != null) {
                val newTokens = refreshResponse.body()!!
                TokenManager.saveAuthToken(newTokens.accessToken)
                TokenManager.saveRefreshToken(newTokens.refreshToken)

                Log.i(TAG, "Token refresh successful!")
                Log.i(TAG, "New access token is $newTokens")

                return Result.success()
            } else {
                Log.e(TAG, "Token refresh failed with code: ${refreshResponse.code()}. Logging out.")
                performLogout()
                return Result.success()
            }
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP error during token refresh: ${e.code()}. Logging out.", e)
            performLogout()
            return Result.success()
        } catch (e: IOException) {
            Log.e(TAG, "Network error during token refresh.", e)
            return Result.retry()
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during token refresh. Logging out.", e)
            performLogout()
            return Result.failure()
        }
    }

    private fun performLogout() {
        Log.i(TAG, "Performing logout: Clearing tokens.")
        TokenManager.clearTokens()
    }

    private fun scheduleNextWorker(context: Context) {
        val nextWork = OneTimeWorkRequestBuilder<JwtExpiryWorker>()
            .setInitialDelay(1, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueue(nextWork)
    }
}
