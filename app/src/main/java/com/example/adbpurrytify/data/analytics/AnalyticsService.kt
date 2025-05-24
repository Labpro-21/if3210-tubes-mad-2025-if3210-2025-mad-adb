package com.example.adbpurrytify.data.analytics

import android.util.Log
import com.example.adbpurrytify.data.local.AnalyticsDao
import com.example.adbpurrytify.data.model.SongEntity
import com.example.adbpurrytify.data.model.analytics.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsService @Inject constructor(
    private val analyticsDao: AnalyticsDao
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var currentSession: ListeningSessionEntity? = null

    // Improved tracking variables
    private var sessionStartTime: Long = 0
    private var lastActiveTime: Long = 0  // Last time music was actually playing
    private var isPaused: Boolean = false
    private var pauseStartTime: Long = 0
    private var totalPauseDuration: Long = 0  // Total time spent paused
    private var activeListeningTime: Long = 0  // Only time actually listening (no pauses)

    companion object {
        private const val TAG = "AnalyticsService"
        private const val MIN_LISTENING_DURATION = 5000L // 5 seconds minimum
        private const val POSITION_UPDATE_INTERVAL = 1000L // Update every second
    }

    // ===== SESSION MANAGEMENT =====



    suspend fun updateListeningProgress(currentPosition: Long, isPlaying: Boolean) {
        try {
            val session = currentSession ?: return
            val currentTime = System.currentTimeMillis()

            if (isPlaying && isPaused) {
                // Resuming from pause
                resumeFromPause(currentTime)
            } else if (!isPlaying && !isPaused) {
                // Starting to pause
                startPause(currentTime)
            } else if (isPlaying && !isPaused) {
                // Continuing to play - update active listening time
                if (currentTime > lastActiveTime + POSITION_UPDATE_INTERVAL) {
                    val timeDelta = currentTime - lastActiveTime

                    // Sanity check: ignore suspiciously large time gaps
                    if (timeDelta > 0 && timeDelta < 10000) { // Less than 10 seconds
                        activeListeningTime += timeDelta
                        lastActiveTime = currentTime

                        // Update session in database periodically
                        if (activeListeningTime % 5000 < 1000) { // Every ~5 seconds
                            updateSessionInDatabase(session, currentTime)
                        }
                    } else {
                        // Large time gap detected - probably phone was locked or app backgrounded
                        Log.w(TAG, "Large time gap detected: ${timeDelta}ms, treating as pause")
                        lastActiveTime = currentTime
                    }
                }
            }
            // If not playing and already paused, do nothing - pause duration is calculated on resume

        } catch (e: Exception) {
            Log.e(TAG, "Error updating listening progress", e)
        }
    }

    private suspend fun startPause(currentTime: Long) {
        if (!isPaused) {
            // Add any remaining active time since last update
            if (currentTime > lastActiveTime) {
                val finalActiveDelta = currentTime - lastActiveTime
                if (finalActiveDelta > 0 && finalActiveDelta < 10000) {
                    activeListeningTime += finalActiveDelta
                }
            }

            isPaused = true
            pauseStartTime = currentTime

            Log.d(TAG, "Started pause. Active listening time so far: ${activeListeningTime}ms")
        }
    }

    suspend fun startListeningSession(userId: Long, song: SongEntity) {
        try {
            // End any existing active session first
            endActiveSession(userId)

            val currentTime = System.currentTimeMillis()
            val calendar = Calendar.getInstance()

            val session = ListeningSessionEntity(
                userId = userId,
                songId = song.id,
                songTitle = song.title,
                artistName = song.author,
                startTime = currentTime,
                year = calendar.get(Calendar.YEAR),
                month = calendar.get(Calendar.MONTH) + 1,
                dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            )

            val sessionId = analyticsDao.insertListeningSession(session)
            currentSession = session.copy(id = sessionId)

            // Reset all tracking variables
            sessionStartTime = currentTime
            lastActiveTime = currentTime
            isPaused = false
            pauseStartTime = 0
            totalPauseDuration = 0
            activeListeningTime = 0

            // ===== NEW: UPDATE STREAKS ON SESSION START =====
            updateStreaksOnStart(userId, session)

            Log.d(TAG, "Started listening session for: ${song.title} by ${song.author}")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting listening session", e)
        }
    }

    // New method: Update streaks when user STARTS listening (not when they finish)
    private suspend fun updateStreaksOnStart(userId: Long, session: ListeningSessionEntity) {
        try {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Date(session.startTime))

            Log.d(TAG, "Checking streaks for song ${session.songTitle} on $today")

            // Check for existing active streaks for this song
            val activeStreaks = analyticsDao.getActiveStreaks(userId)
                .filter { it.songId == session.songId }

            if (activeStreaks.isNotEmpty()) {
                val streak = activeStreaks.first()
                Log.d(TAG, "Found existing streak for ${session.songTitle}: ${streak.streakLength} days, last date: ${streak.endDate}")

                val calendar = Calendar.getInstance().apply {
                    time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(streak.endDate)!!
                    add(Calendar.DAY_OF_MONTH, 1)
                }
                val expectedNextDay = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(calendar.time)

                when {
                    today == expectedNextDay -> {
                        // Consecutive day - extend streak
                        val updatedStreak = streak.copy(
                            endDate = today,
                            streakLength = streak.streakLength + 1,
                            totalPlayCount = streak.totalPlayCount + 1
                        )
                        analyticsDao.insertOrUpdateStreak(updatedStreak)
                        Log.d(TAG, "Extended streak for ${session.songTitle} to ${updatedStreak.streakLength} days")
                    }
                    today == streak.endDate -> {
                        // Same day - just increment play count
                        val updatedStreak = streak.copy(
                            totalPlayCount = streak.totalPlayCount + 1
                        )
                        analyticsDao.insertOrUpdateStreak(updatedStreak)
                        Log.d(TAG, "Same day listen for ${session.songTitle}, incremented play count")
                    }
                    else -> {
                        // Gap in streak - end current and start new
                        analyticsDao.deactivateStreaksForSong(userId, session.songId)
                        createNewStreak(userId, session, today)
                        Log.d(TAG, "Gap detected, ended old streak and started new one for ${session.songTitle}")
                    }
                }
            } else {
                // No active streak - start a new one
                createNewStreak(userId, session, today)
                Log.d(TAG, "No existing streak, started new one for ${session.songTitle}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating streaks on start", e)
        }
    }

    // Remove the old updateStreaks call from endListeningSession:
    suspend fun endListeningSession(userId: Long, completed: Boolean = false) {
        try {
            val session = currentSession ?: return
            val currentTime = System.currentTimeMillis()

            // Handle any ongoing pause
            if (isPaused && pauseStartTime > 0) {
                val finalPauseDuration = currentTime - pauseStartTime
                if (finalPauseDuration > 0) {
                    totalPauseDuration += finalPauseDuration
                }
            } else if (!isPaused && lastActiveTime > 0) {
                // Add any final active listening time
                val finalActiveDuration = currentTime - lastActiveTime
                if (finalActiveDuration > 0 && finalActiveDuration < 10000) {
                    activeListeningTime += finalActiveDuration
                }
            }

            // Final duration is ONLY the active listening time (excluding pauses)
            val finalDuration = activeListeningTime

            Log.d(TAG, "Session summary:")
            Log.d(TAG, "  Total session time: ${currentTime - sessionStartTime}ms")
            Log.d(TAG, "  Active listening time: ${finalDuration}ms")
            Log.d(TAG, "  Total pause time: ${totalPauseDuration}ms")
            Log.d(TAG, "  Pause count: ${session.pauseCount}")

            // Only save sessions that meet minimum duration
            if (finalDuration >= MIN_LISTENING_DURATION) {
                val completedSession = session.copy(
                    endTime = currentTime,
                    duration = finalDuration,
                    isCompleted = completed
                )

                analyticsDao.updateListeningSession(completedSession)

                // ===== REMOVED: No longer update streaks on session end =====
                // updateStreaks(userId, completedSession) // <-- REMOVED

                Log.i(TAG, "Ended listening session: ${finalDuration}ms active listening for ${session.songTitle}")
            } else {
                Log.d(TAG, "Session too short (${finalDuration}ms active), not saving")
            }

            // Reset all tracking variables
            currentSession = null
            sessionStartTime = 0
            lastActiveTime = 0
            isPaused = false
            pauseStartTime = 0
            totalPauseDuration = 0
            activeListeningTime = 0

        } catch (e: Exception) {
            Log.e(TAG, "Error ending listening session", e)
        }
    }

    private suspend fun resumeFromPause(currentTime: Long) {
        if (isPaused && pauseStartTime > 0) {
            // Calculate pause duration and add to total
            val pauseDuration = currentTime - pauseStartTime
            if (pauseDuration > 0) {
                totalPauseDuration += pauseDuration

                // Update pause count in session
                currentSession?.let { session ->
                    val updatedSession = session.copy(pauseCount = session.pauseCount + 1)
                    currentSession = updatedSession
                }

                Log.d(TAG, "Resumed from pause. Pause duration: ${pauseDuration}ms, Total pause time: ${totalPauseDuration}ms")
            }

            isPaused = false
            pauseStartTime = 0
            lastActiveTime = currentTime
        }
    }

    private suspend fun updateSessionInDatabase(session: ListeningSessionEntity, currentTime: Long) {
        val updatedSession = session.copy(
            duration = activeListeningTime, // Only active listening time, no pauses
            pauseCount = session.pauseCount
        )

        analyticsDao.updateListeningSession(updatedSession)
        currentSession = updatedSession
    }

    suspend fun pauseListeningSession() {
        try {
            val currentTime = System.currentTimeMillis()
            startPause(currentTime)

            // Immediately update database with current progress
            currentSession?.let { session ->
                updateSessionInDatabase(session, currentTime)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing listening session", e)
        }
    }

    suspend fun resumeListeningSession() {
        try {
            val currentTime = System.currentTimeMillis()
            resumeFromPause(currentTime)

            Log.d(TAG, "Resumed listening session")
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming listening session", e)
        }
    }

    suspend fun skipTrack() {
        try {
            val session = currentSession ?: return
            val updatedSession = session.copy(skipCount = session.skipCount + 1)
            currentSession = updatedSession
            analyticsDao.updateListeningSession(updatedSession)

            Log.d(TAG, "Recorded skip for session")
        } catch (e: Exception) {
            Log.e(TAG, "Error recording skip", e)
        }
    }


    private suspend fun endActiveSession(userId: Long) {
        try {
            val activeSession = analyticsDao.getActiveListeningSession(userId)
            if (activeSession != null) {
                val endTime = System.currentTimeMillis()
                // Use existing duration if it's reasonable, otherwise calculate minimum duration
                val finalDuration = if (activeSession.duration > 0) {
                    activeSession.duration
                } else {
                    maxOf(0L, endTime - activeSession.startTime - 60000) // Assume max 1 min pause
                }

                val updatedSession = activeSession.copy(
                    endTime = endTime,
                    duration = finalDuration
                )
                analyticsDao.updateListeningSession(updatedSession)

                Log.d(TAG, "Ended previous active session with duration: ${finalDuration}ms")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error ending active session", e)
        }
    }

    // ===== STREAK MANAGEMENT (unchanged) =====

    private suspend fun updateStreaks(userId: Long, session: ListeningSessionEntity) {
        try {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Date(session.startTime))

            // Check for existing active streaks for this song
            val activeStreaks = analyticsDao.getActiveStreaks(userId)
                .filter { it.songId == session.songId }

            if (activeStreaks.isNotEmpty()) {
                val streak = activeStreaks.first()
                val calendar = Calendar.getInstance().apply {
                    time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(streak.endDate)!!
                    add(Calendar.DAY_OF_MONTH, 1)
                }
                val expectedNextDay = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(calendar.time)

                if (today == expectedNextDay) {
                    // Consecutive day - extend streak
                    val updatedStreak = streak.copy(
                        endDate = today,
                        streakLength = streak.streakLength + 1,
                        totalPlayCount = streak.totalPlayCount + 1
                    )
                    analyticsDao.insertOrUpdateStreak(updatedStreak)
                } else if (today != streak.endDate) {
                    // Gap in streak - end current and start new if applicable
                    analyticsDao.deactivateStreaksForSong(userId, session.songId)
                    createNewStreak(userId, session, today)
                }
            } else {
                // No active streak - start a new one
                createNewStreak(userId, session, today)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating streaks", e)
        }
    }

    private suspend fun createNewStreak(userId: Long, session: ListeningSessionEntity, dateStr: String) {
        val newStreak = StreakEntity(
            userId = userId,
            songId = session.songId,
            songTitle = session.songTitle,
            artistName = session.artistName,
            streakLength = 1,
            startDate = dateStr,
            endDate = dateStr,
            isActive = true,
            totalPlayCount = 1
        )
        analyticsDao.insertOrUpdateStreak(newStreak)
    }

    // ===== REAL-TIME DATA ACCESS =====

    fun getCurrentSessionDuration(): Long {
        return if (currentSession != null) {
            activeListeningTime + if (!isPaused && lastActiveTime > 0) {
                maxOf(0, System.currentTimeMillis() - lastActiveTime)
            } else 0
        } else 0
    }

    fun getCurrentSession(): ListeningSessionEntity? = currentSession

    fun getRealTimeListeningStats(userId: Long, year: Int, month: Int): Flow<RealTimeStats> {
        return analyticsDao.getTotalListeningTimeForMonthFlow(userId, year, month)
            .map { totalTime ->
                val currentSessionTime = if (currentSession?.year == year && currentSession?.month == month) {
                    getCurrentSessionDuration()
                } else 0

                RealTimeStats(
                    totalListeningTime = (totalTime ?: 0) + currentSessionTime,
                    isCurrentlyListening = currentSession != null && !isPaused
                )
            }.distinctUntilChanged()
    }

    // ===== CLEANUP =====

    suspend fun cleanup(userId: Long) {
        try {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val cutoffYear = currentYear - 2 // Keep 2 years of data
            val cutoffDate = "${cutoffYear}-01-01"

            analyticsDao.deleteOldListeningSessions(userId, cutoffYear)
            analyticsDao.deleteOldStreaks(userId, cutoffDate)

            Log.d(TAG, "Cleaned up analytics data older than $cutoffYear")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }

    fun release() {
        scope.cancel()
    }
}

data class RealTimeStats(
    val totalListeningTime: Long,
    val isCurrentlyListening: Boolean
)