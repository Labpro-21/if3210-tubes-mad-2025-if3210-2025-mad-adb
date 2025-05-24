// app/src/main/java/com/example/adbpurrytify/ui/viewmodels/AnalyticsViewModel.kt
package com.example.adbpurrytify.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adbpurrytify.data.AnalyticsRepository
import com.example.adbpurrytify.data.AuthRepository
import com.example.adbpurrytify.data.analytics.RealTimeStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private var currentUserId: Long? = null

    init {
        initializeUser()
    }

    private fun initializeUser() {
        viewModelScope.launch {
            try {
                val userResult = authRepository.getCurrentUser()
                if (userResult.isSuccess) {
                    currentUserId = userResult.getOrThrow().id
                }
            } catch (e: Exception) {
                // Handle error silently for now
            }
        }
    }

    fun getRealTimeStats(monthYear: String): Flow<RealTimeStats?> {
        return currentUserId?.let { userId ->
            try {
                val parts = monthYear.split("-")
                if (parts.size == 2) {
                    val month = parts[0].toInt()
                    val year = parts[1].toInt()
                    analyticsRepository.getRealTimeStats(userId, year, month)
                } else {
                    flowOf(null)
                }
            } catch (e: Exception) {
                flowOf(null)
            }
        } ?: flowOf(null)
    }

    suspend fun exportData(monthYear: String): String {
        return currentUserId?.let { userId ->
            try {
                val parts = monthYear.split("-")
                if (parts.size == 2) {
                    val month = parts[0].toInt()
                    val year = parts[1].toInt()
                    analyticsRepository.exportToCsv(userId, year, month)
                } else {
                    "Error: Invalid month format"
                }
            } catch (e: Exception) {
                "Error exporting data: ${e.message}"
            }
        } ?: "User not found"
    }
}