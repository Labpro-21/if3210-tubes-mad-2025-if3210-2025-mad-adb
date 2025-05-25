package com.example.adbpurrytify.data

import android.util.Log
import com.example.adbpurrytify.data.analytics.AnalyticsService
import com.example.adbpurrytify.data.analytics.RealTimeStats
import com.example.adbpurrytify.data.local.AnalyticsDao
import com.example.adbpurrytify.data.local.DailyStatsResult
import com.example.adbpurrytify.data.local.TopArtistResult
import com.example.adbpurrytify.data.local.TopSongResult
import com.example.adbpurrytify.data.model.Artist
import com.example.adbpurrytify.data.model.DayStreak
import com.example.adbpurrytify.data.model.Song
import com.example.adbpurrytify.data.model.SongEntity
import com.example.adbpurrytify.data.model.SoundCapsule
import com.example.adbpurrytify.data.model.analytics.ListeningSessionEntity
import com.example.adbpurrytify.ui.viewmodels.ArtistListeningData
import com.example.adbpurrytify.ui.viewmodels.DailyListeningData
import com.example.adbpurrytify.ui.viewmodels.SongListeningData
import com.example.adbpurrytify.ui.viewmodels.TimeListenedData
import com.example.adbpurrytify.ui.viewmodels.TopArtistsData
import com.example.adbpurrytify.ui.viewmodels.TopSongsData
import com.example.adbpurrytify.ui.viewmodels.WeeklyListeningData
import com.example.adbpurrytify.utils.DateUtils
import com.example.adbpurrytify.utils.DateUtils.formatMonthForDisplay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsRepository @Inject constructor(
    private val analyticsDao: AnalyticsDao,
    private val analyticsService: AnalyticsService,
    private val songRepository: SongRepository
) {

    companion object {
        private const val TAG = "AnalyticsRepository"
    }

    // ===== SESSION MANAGEMENT =====

    suspend fun startListening(userId: Long, song: SongEntity) {
        analyticsService.startListeningSession(userId, song)
    }

    suspend fun updateProgress(currentPosition: Long, isPlaying: Boolean) {
        analyticsService.updateListeningProgress(currentPosition, isPlaying)
    }

    suspend fun pauseListening() {
        analyticsService.pauseListeningSession()
    }

    suspend fun resumeListening() {
        analyticsService.resumeListeningSession()
    }

    suspend fun skipTrack() {
        analyticsService.skipTrack()
    }

    suspend fun stopListening(userId: Long, completed: Boolean = false) {
        analyticsService.endListeningSession(userId, completed)
    }

    // ===== IMPROVED ARTWORK HANDLING =====

    /**
     * Get artwork for an artist by finding any song by that artist
     * Returns the first available artwork, trying multiple strategies
     */
    private suspend fun getArtistArtwork(userId: Long, artistName: String): String {
        return try {
            // Strategy 1: Get all user songs and find first match
            val userSongs = songRepository.getAllSongs(userId).first()
            val artistSong = userSongs.firstOrNull {
                it.author.equals(artistName, ignoreCase = true) && it.artUri.isNotEmpty()
            }

            if (artistSong != null && artistSong.artUri.isNotEmpty()) {
                Log.d(TAG, "Found artist artwork for $artistName: ${artistSong.artUri}")
                return artistSong.artUri
            }

            // Strategy 2: Try recently played songs
            val recentSongs = songRepository.getRecentlyPlayedSongs(userId).first()
            val recentArtistSong = recentSongs.firstOrNull {
                it.author.equals(artistName, ignoreCase = true) && it.artUri.isNotEmpty()
            }

            if (recentArtistSong != null && recentArtistSong.artUri.isNotEmpty()) {
                Log.d(TAG, "Found artist artwork in recent for $artistName: ${recentArtistSong.artUri}")
                return recentArtistSong.artUri
            }

            Log.w(TAG, "No artwork found for artist: $artistName")
            ""
        } catch (e: Exception) {
            Log.e(TAG, "Error getting artist artwork for $artistName", e)
            ""
        }
    }

    /**
     * Get artwork for a song by song ID with fallback strategies
     */
    private suspend fun getSongArtwork(songId: Long): String {
        return try {
            val songEntity = songRepository.getSongById(songId)
            if (songEntity?.artUri?.isNotEmpty() == true) {
                Log.d(TAG, "Found song artwork for ID $songId: ${songEntity.artUri}")
                return songEntity.artUri
            }

            Log.w(TAG, "No artwork found for song ID: $songId")
            ""
        } catch (e: Exception) {
            Log.e(TAG, "Error getting song artwork for ID $songId", e)
            ""
        }
    }

    // ===== SOUND CAPSULE DATA =====

    suspend fun getSoundCapsule(userId: Long, year: Int, month: Int): SoundCapsule {
        val totalTime = analyticsDao.getTotalListeningTimeForMonth(userId, year, month) ?: 0
        val topArtists = analyticsDao.getTopArtistsForMonth(userId, year, month, 1)
        val topSongs = analyticsDao.getTopSongsForMonth(userId, year, month, 1)
        val yearMonthPattern = "${year}-%02d".format(month) + "%"
        val longestStreak = analyticsDao.getLongestStreakForMonth(userId, 2, yearMonthPattern)

        val monthStr = DateUtils.formatMonthKey(year, month)
        val displayMonth = DateUtils.formatMonthForDisplay(monthStr)

        return if (totalTime > 0) {
            SoundCapsule(
                month = monthStr,
                displayMonth = displayMonth,
                timeListened = (totalTime / 60000).toInt(),
                topArtist = topArtists.firstOrNull()?.let { result ->
                    Artist(
                        id = 0,
                        name = result.artistName,
                        imageUrl = getArtistArtwork(userId, result.artistName)
                    )
                },
                topSong = topSongs.firstOrNull()?.let { result ->
                    Song(
                        id = result.songId,
                        title = result.songTitle,
                        artist = result.artistName,
                        imageUrl = getSongArtwork(result.songId)
                    )
                },
                dayStreak = longestStreak?.takeIf { it.streakLength >= 2 }?.let { streak ->
                    DayStreak(
                        songTitle = streak.songTitle,
                        artist = streak.artistName,
                        imageUrl = getSongArtwork(streak.songId),
                        streakDays = streak.streakLength,
                        dateRange = formatDateRange(streak.startDate, streak.endDate)
                    )
                },
                hasData = true
            )
        } else {
            SoundCapsule(
                month = monthStr,
                displayMonth = displayMonth,
                timeListened = 0,
                topArtist = null,
                topSong = null,
                dayStreak = null,
                hasData = false
            )
        }
    }

    suspend fun getAvailableMonths(userId: Long): List<String> {
        val months = analyticsDao.getAvailableMonths(userId)
        return months.map { DateUtils.formatMonthKey(it.year, it.month) }
            .distinct()
            .sortedDescending()
    }

    // ===== REAL-TIME STATS =====

    fun getRealTimeStats(userId: Long, year: Int, month: Int): Flow<RealTimeStats> {
        return analyticsService.getRealTimeListeningStats(userId, year, month)
    }

    // ===== TIME LISTENED DETAILS =====

    suspend fun getTimeListenedData(userId: Long, year: Int, month: Int): TimeListenedData {
        val totalTime = analyticsDao.getTotalListeningTimeForMonth(userId, year, month) ?: 0
        val dailyStats = analyticsDao.getDailyStatsForMonth(userId, year, month)

        val monthStr = DateUtils.formatMonthKey(year, month)
        val displayMonth = DateUtils.formatMonthForDisplay(monthStr)

        // Convert daily stats to weekly data
        val weeklyData = aggregateToWeeklyData(dailyStats, year, month)

        val totalMinutes = (totalTime / 60000).toInt()
        val daysInMonth = getDaysInMonth(year, month)

        return TimeListenedData(
            month = monthStr,
            displayMonth = displayMonth,
            totalMinutes = totalMinutes,
            dailyAverage = if (daysInMonth > 0) totalMinutes / daysInMonth else 0,
            weeklyAverage = if (weeklyData.isNotEmpty()) weeklyData.map { it.minutes }.average().toInt() else 0,
            dailyData = dailyStats.map {
                DailyListeningData(
                    day = it.dayOfMonth,
                    minutes = (it.totalTime / 60000).toInt(),
                    date = formatDayDate(year, month, it.dayOfMonth)
                )
            },
            weeklyData = weeklyData
        )
    }


    // ===== EXPORT FUNCTIONALITY =====

    suspend fun exportToCsv(userId: Long, year: Int, month: Int): String {
        val totalTime = analyticsDao.getTotalListeningTimeForMonth(userId, year, month) ?: 0
        val totalSongs = analyticsDao.getUniqueSongsForMonth(userId, year, month)
        val totalArtists = analyticsDao.getUniqueArtistsForMonth(userId, year, month)
        val topArtists = analyticsDao.getTopArtistsForMonth(userId, year, month, 10)
        val topSongs = analyticsDao.getTopSongsForMonth(userId, year, month, 10)
        val yearMonthPattern = "${year}-%02d".format(month) + "%"
        val streaks = analyticsDao.getLongestStreakForMonth(userId, 2, yearMonthPattern)

        val csv = StringBuilder()

        // Header
        csv.appendLine("Sound Capsule Export - $year-${String.format("%02d", month)}")
        csv.appendLine("")

        // Monthly Summary
        csv.appendLine("Monthly Summary")
        csv.appendLine("Total Listening Time (minutes),${totalTime / 60000}")
        csv.appendLine("Total Songs,$totalSongs")
        csv.appendLine("Unique Artists,$totalArtists")
        csv.appendLine("")

        // Top Artists
        csv.appendLine("Top Artists")
        csv.appendLine("Rank,Artist Name,Play Count,Listening Time (minutes)")
        topArtists.forEachIndexed { index, artist ->
            csv.appendLine("${index + 1},${artist.artistName},${artist.playCount},${artist.totalTime / 60000}")
        }
        csv.appendLine("")

        // Top Songs
        csv.appendLine("Top Songs")
        csv.appendLine("Rank,Song Title,Artist,Play Count,Listening Time (minutes)")
        topSongs.forEachIndexed { index, song ->
            csv.appendLine("${index + 1},${song.songTitle},${song.artistName},${song.playCount},${song.totalTime / 60000}")
        }
        csv.appendLine("")

        // Streaks
        streaks?.let { streak ->
            csv.appendLine("Day Streaks")
            csv.appendLine("Song,Artist,Streak Length,Start Date,End Date")
            csv.appendLine("${streak.songTitle},${streak.artistName},${streak.streakLength},${streak.startDate},${streak.endDate}")
        }

        return csv.toString()
    }

    // ===== HELPER FUNCTIONS =====

    private fun formatDateRange(startDate: String, endDate: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val start = sdf.parse(startDate)
            val end = sdf.parse(endDate)

            val displayFormat = SimpleDateFormat("MMM d", Locale.getDefault())
            val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())

            if (start != null && end != null) {
                if (yearFormat.format(start) == yearFormat.format(end)) {
                    "${displayFormat.format(start)}-${displayFormat.format(end)}, ${yearFormat.format(end)}"
                } else {
                    "${displayFormat.format(start)}, ${yearFormat.format(start)} - ${displayFormat.format(end)}, ${yearFormat.format(end)}"
                }
            } else {
                "$startDate - $endDate"
            }
        } catch (e: Exception) {
            "$startDate - $endDate"
        }
    }

    private fun formatDayDate(year: Int, month: Int, day: Int): String {
        val calendar = Calendar.getInstance().apply {
            set(year, month - 1, day)
        }
        return SimpleDateFormat("MMM d", Locale.getDefault()).format(calendar.time)
    }

    private fun aggregateToWeeklyData(dailyStats: List<DailyStatsResult>, year: Int, month: Int): List<WeeklyListeningData> {
        val weeks = mutableListOf<WeeklyListeningData>()
        val daysInMonth = getDaysInMonth(year, month)
        val monthAbbr = SimpleDateFormat("MMM", Locale.getDefault()).format(
            Calendar.getInstance().apply { set(year, month - 1, 1) }.time
        )

        for (weekIndex in 0 until (daysInMonth + 6) / 7) {
            val startDay = weekIndex * 7 + 1
            val endDay = minOf(startDay + 6, daysInMonth)

            val weekStats = dailyStats.filter { it.dayOfMonth in startDay..endDay }
            val weekMinutes = weekStats.sumOf { (it.totalTime / 60000).toInt() }

            weeks.add(
                WeeklyListeningData(
                    weekNumber = weekIndex + 1,
                    minutes = weekMinutes,
                    weekLabel = "Week ${weekIndex + 1}",
                    dateRange = "$monthAbbr $startDay-$endDay"
                )
            )
        }

        return weeks
    }

    private fun getDaysInMonth(year: Int, month: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1)
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }


    suspend fun getTopArtistsData(userId: Long, year: Int, month: Int): TopArtistsData {
        val topArtistResults = analyticsDao.getTopArtistsForMonth(userId, year, month, 20)

        // ✅ PRESERVE ORDER - Use mapIndexed
        val artists = topArtistResults.mapIndexed { index, result ->
            ArtistListeningData(
                id = result.artistName.hashCode().toLong(), // Generate ID from name
                name = result.artistName,
                imageUrl = getArtistImageUrl(userId, result.artistName), // Helper method
                minutesListened = (result.totalTime / 60000).toInt(),
                songsCount = result.uniqueSongs,
                rank = index + 1 // Preserve SQL order
            )
        }

        val displayMonth = formatMonthForDisplay(String.format("%02d-%04d", month, year))

        return TopArtistsData(
            month = String.format("%02d-%04d", month, year),
            displayMonth = displayMonth,
            totalArtists = artists.size,
            artists = artists // Order preserved!
        )
    }

    // Helper method to get artist image (you might want to implement this)
    private suspend fun getArtistImageUrl(userId: Long, artistName: String): String {
        // Try to get an image from any song by this artist
        // This is just an example - implement based on your needs
        return try {
            val songByArtist = songRepository.getAllSongs(userId)
                .first()
                .find { it.author.equals(artistName, ignoreCase = true) }
            songByArtist?.artUri ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun getTopSongsData(userId: Long, year: Int, month: Int): TopSongsData {
        // Get ordered results from DAO
        val topSongResults = analyticsDao.getTopSongsForMonth(userId, year, month, 20)

        // ✅ PRESERVE ORDER - Use mapIndexed to maintain SQL ordering
        val songs = topSongResults.mapIndexed { index, result ->
            // Get song metadata while preserving order
            val song = songRepository.getSongById(result.songId)

            SongListeningData(
                id = result.songId,
                title = result.songTitle,
                artist = result.artistName,
                imageUrl = song?.artUri ?: "", // Get image from existing song data
                playsCount = result.playCount,
                minutesListened = (result.totalTime / 60000).toInt(), // Convert to minutes
                rank = index + 1 // Use index to preserve original SQL order
            )
        }

        val displayMonth = formatMonthForDisplay(String.format("%02d-%04d", month, year))

        return TopSongsData(
            month = String.format("%02d-%04d", month, year),
            displayMonth = displayMonth,
            totalSongs = songs.size,
            songs = songs // Order is preserved!
        )
    }

    private fun isCurrentMonth(year: Int, month: Int): Boolean {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        return year == currentYear && month == currentMonth
    }

    private suspend fun getBestSongByArtist(userId: Long, artistName: String): SongEntity? {
        return try {
            val allSongs = songRepository.getAllSongs(userId).first()
            val artistSongs = allSongs.filter { it.author.equals(artistName, ignoreCase = true) }

            // Prefer songs with artwork
            artistSongs.firstOrNull { it.artUri.isNotEmpty() } ?: artistSongs.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    private fun mergeCurrentSessionWithArtists(
        dbArtists: List<TopArtistResult>,
        currentSession: ListeningSessionEntity,
        sessionDuration: Long
    ): List<TopArtistResult> {
        val mutableArtists = dbArtists.toMutableList()

        // Find if current artist already exists in the list
        val existingIndex = mutableArtists.indexOfFirst {
            it.artistName.equals(currentSession.artistName, ignoreCase = true)
        }

        if (existingIndex >= 0) {
            // Update existing artist with current session data
            val existing = mutableArtists[existingIndex]
            mutableArtists[existingIndex] = existing.copy(
                totalTime = existing.totalTime + sessionDuration,
                playCount = existing.playCount + 1
            )
        } else if (sessionDuration > 5000) { // Only add if session is meaningful (5+ seconds)
            // Add current artist as new entry
            mutableArtists.add(
                TopArtistResult(
                    artistName = currentSession.artistName,
                    playCount = 1,
                    totalTime = sessionDuration,
                    uniqueSongs = 1
                )
            )
        }

        // Re-sort by play count and total time
        return mutableArtists.sortedWith(
            compareByDescending<TopArtistResult> { it.playCount }
                .thenByDescending { it.totalTime }
        )
    }

    private fun mergeCurrentSessionWithSongs(
        dbSongs: List<TopSongResult>,
        currentSession: ListeningSessionEntity,
        sessionDuration: Long
    ): List<TopSongResult> {
        val mutableSongs = dbSongs.toMutableList()

        // Find if current song already exists in the list
        val existingIndex = mutableSongs.indexOfFirst { it.songId == currentSession.songId }

        if (existingIndex >= 0) {
            // Update existing song with current session data
            val existing = mutableSongs[existingIndex]
            mutableSongs[existingIndex] = existing.copy(
                totalTime = existing.totalTime + sessionDuration,
                playCount = existing.playCount + 1
            )
        } else if (sessionDuration > 5000) { // Only add if session is meaningful (5+ seconds)
            // Add current song as new entry
            mutableSongs.add(
                TopSongResult(
                    songTitle = currentSession.songTitle,
                    artistName = currentSession.artistName,
                    songId = currentSession.songId,
                    playCount = 1,
                    totalTime = sessionDuration
                )
            )
        }

        // Re-sort by play count and total time
        return mutableSongs.sortedWith(
            compareByDescending<TopSongResult> { it.playCount }
                .thenByDescending { it.totalTime }
        )
    }
}