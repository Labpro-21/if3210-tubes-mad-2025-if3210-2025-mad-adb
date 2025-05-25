package com.example.adbpurrytify.data.model.analytics

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "listening_sessions",
    indices = [
        Index(value = ["songId"]),
        Index(value = ["userId"]),
        Index(value = ["startTime"]),
        Index(value = ["year", "month"]),
        Index(value = ["year", "month", "dayOfMonth"])
    ]
)
data class ListeningSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val songId: Long,
    val songTitle: String,
    val artistName: String,
    val startTime: Long, // timestamp when started listening
    val endTime: Long? = null, // timestamp when stopped (null if still listening)

    /**
     * IMPORTANT: This is ACTIVE listening duration in milliseconds
     * This does NOT include pause time - only time actually spent listening
     * Formula: duration = total_time_music_was_playing (excludes pauses)
     *
     * For example:
     * - User starts song at 10:00:00
     * - Pauses at 10:01:00 (1 minute of listening)
     * - Resumes at 10:05:00 (4 minutes of pause - NOT counted)
     * - Stops at 10:06:00 (1 more minute of listening)
     * - Total duration = 2 minutes (not 6 minutes)
     */
    val duration: Long = 0, // ACTIVE listening duration in milliseconds (excludes pause time)

    val year: Int,
    val month: Int, // 1-12
    val dayOfMonth: Int,
    val isCompleted: Boolean = false, // whether the song was listened to completion
    val skipCount: Int = 0, // how many times user skipped within this session
    val pauseCount: Int = 0, // how many times user paused during this session
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Get the total session time (including pauses)
     * This is different from duration which excludes pauses
     */
    fun getTotalSessionTime(): Long? {
        return if (endTime != null) {
            endTime - startTime
        } else null
    }

    /**
     * Get the estimated pause time during this session
     * This is an approximation: totalSessionTime - activeDuration
     */
    fun getEstimatedPauseTime(): Long? {
        val totalTime = getTotalSessionTime()
        return if (totalTime != null && totalTime > duration) {
            totalTime - duration
        } else null
    }

    /**
     * Get listening efficiency (what percentage of session time was actually listening)
     */
    fun getListeningEfficiency(): Float? {
        val totalTime = getTotalSessionTime()
        return if (totalTime != null && totalTime > 0) {
            duration.toFloat() / totalTime.toFloat()
        } else null
    }
}