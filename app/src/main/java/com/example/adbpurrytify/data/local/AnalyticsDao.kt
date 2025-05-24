package com.example.adbpurrytify.data.local

import androidx.room.*
import com.example.adbpurrytify.data.model.analytics.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalyticsDao {

    // ===== LISTENING SESSIONS =====

    @Insert
    suspend fun insertListeningSession(session: ListeningSessionEntity): Long

    @Update
    suspend fun updateListeningSession(session: ListeningSessionEntity)

    @Query("SELECT * FROM listening_sessions WHERE id = :sessionId")
    suspend fun getListeningSession(sessionId: Long): ListeningSessionEntity?

    @Query("SELECT * FROM listening_sessions WHERE userId = :userId AND endTime IS NULL LIMIT 1")
    suspend fun getActiveListeningSession(userId: Long): ListeningSessionEntity?

    // ===== MONTHLY ANALYTICS =====

    @Query("""
        SELECT SUM(duration) as totalTime 
        FROM listening_sessions 
        WHERE userId = :userId AND year = :year AND month = :month
    """)
    suspend fun getTotalListeningTimeForMonth(userId: Long, year: Int, month: Int): Long?

    @Query("""
        SELECT SUM(duration) as totalTime 
        FROM listening_sessions 
        WHERE userId = :userId AND year = :year AND month = :month
    """)
    fun getTotalListeningTimeForMonthFlow(userId: Long, year: Int, month: Int): Flow<Long?>

    @Query("""
        SELECT COUNT(DISTINCT songId) as uniqueSongs 
        FROM listening_sessions 
        WHERE userId = :userId AND year = :year AND month = :month
    """)
    suspend fun getUniqueSongsForMonth(userId: Long, year: Int, month: Int): Int

    @Query("""
        SELECT COUNT(DISTINCT artistName) as uniqueArtists 
        FROM listening_sessions 
        WHERE userId = :userId AND year = :year AND month = :month
    """)
    suspend fun getUniqueArtistsForMonth(userId: Long, year: Int, month: Int): Int

    // FIXED: Top Songs query - Use duration field directly
    @Query("""
        SELECT songTitle, artistName, songId, 
               COUNT(*) as playCount, 
               SUM(duration) as totalTime
        FROM listening_sessions 
        WHERE userId = :userId AND year = :year AND month = :month
        GROUP BY songId 
        ORDER BY totalTime DESC, playCount DESC
        LIMIT :limit
    """)
    suspend fun getTopSongsForMonth(userId: Long, year: Int, month: Int, limit: Int = 10): List<TopSongResult>

    // FIXED: Top Artists query - Use duration field directly
    @Query("""
        SELECT artistName, 
               COUNT(*) as playCount, 
               SUM(duration) as totalTime,
               COUNT(DISTINCT songId) as uniqueSongs
        FROM listening_sessions 
        WHERE userId = :userId AND year = :year AND month = :month
        GROUP BY artistName 
        ORDER BY totalTime DESC, uniqueSongs DESC
        LIMIT :limit
    """)
    suspend fun getTopArtistsForMonth(userId: Long, year: Int, month: Int, limit: Int = 10): List<TopArtistResult>

    // ===== DAILY ANALYTICS =====

    @Query("""
        SELECT dayOfMonth, 
               SUM(duration) as totalTime, 
               COUNT(*) as sessionCount,
               COUNT(DISTINCT songId) as uniqueSongs
        FROM listening_sessions 
        WHERE userId = :userId AND year = :year AND month = :month
        GROUP BY dayOfMonth
        ORDER BY dayOfMonth
    """)
    suspend fun getDailyStatsForMonth(userId: Long, year: Int, month: Int): List<DailyStatsResult>

    // ===== AVAILABLE MONTHS =====

    @Query("""
        SELECT DISTINCT year, month 
        FROM listening_sessions 
        WHERE userId = :userId
        ORDER BY year DESC, month DESC
    """)
    suspend fun getAvailableMonths(userId: Long): List<MonthYear>

    // ===== STREAKS =====

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStreak(streak: StreakEntity)

    @Query("UPDATE streaks SET isActive = 0 WHERE userId = :userId AND songId = :songId")
    suspend fun deactivateStreaksForSong(userId: Long, songId: Long)

    @Query("""
        SELECT * FROM streaks 
        WHERE userId = :userId AND streakLength >= :minLength 
        AND startDate LIKE :yearMonthPattern
        ORDER BY streakLength DESC 
        LIMIT 1
    """)
    suspend fun getLongestStreakForMonth(userId: Long, minLength: Int = 2, yearMonthPattern: String): StreakEntity?

    @Query("""
        SELECT * FROM streaks 
        WHERE userId = :userId AND isActive = 1 
        ORDER BY streakLength DESC
    """)
    suspend fun getActiveStreaks(userId: Long): List<StreakEntity>

    // ===== CLEANUP =====

    @Query("DELETE FROM listening_sessions WHERE userId = :userId AND year < :cutoffYear")
    suspend fun deleteOldListeningSessions(userId: Long, cutoffYear: Int)

    @Query("DELETE FROM streaks WHERE userId = :userId AND endDate < :cutoffDate")
    suspend fun deleteOldStreaks(userId: Long, cutoffDate: String)

    // ===== ADDITIONAL QUERIES WITH MINIMUM DURATION FILTER =====

    @Query("""
        SELECT songTitle, artistName, songId, 
               COUNT(*) as playCount, 
               SUM(duration) as totalTime
        FROM listening_sessions 
        WHERE userId = :userId AND year = :year AND month = :month 
        AND duration >= :minDuration
        GROUP BY songId 
        ORDER BY totalTime DESC, playCount DESC
        LIMIT :limit
    """)
    suspend fun getTopSongsForMonthWithMinDuration(
        userId: Long,
        year: Int,
        month: Int,
        minDuration: Long = 5000L, // 5 seconds minimum
        limit: Int = 10
    ): List<TopSongResult>

    @Query("""
        SELECT artistName, 
               COUNT(*) as playCount, 
               SUM(duration) as totalTime,
               COUNT(DISTINCT songId) as uniqueSongs
        FROM listening_sessions 
        WHERE userId = :userId AND year = :year AND month = :month 
        AND duration >= :minDuration
        GROUP BY artistName 
        ORDER BY totalTime DESC, uniqueSongs DESC
        LIMIT :limit
    """)
    suspend fun getTopArtistsForMonthWithMinDuration(
        userId: Long,
        year: Int,
        month: Int,
        minDuration: Long = 5000L, // 5 seconds minimum
        limit: Int = 10
    ): List<TopArtistResult>
}

// Helper data classes for query results
data class TopSongResult(
    val songTitle: String,
    val artistName: String,
    val songId: Long,
    val playCount: Int,
    val totalTime: Long
)

data class TopArtistResult(
    val artistName: String,
    val playCount: Int,
    val totalTime: Long,
    val uniqueSongs: Int
)

data class DailyStatsResult(
    val dayOfMonth: Int,
    val totalTime: Long,
    val sessionCount: Int,
    val uniqueSongs: Int
)

data class MonthYear(
    val year: Int,
    val month: Int
)