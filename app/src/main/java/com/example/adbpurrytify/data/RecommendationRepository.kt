package com.example.adbpurrytify.data

import android.util.Log
import com.example.adbpurrytify.data.local.AnalyticsDao
import com.example.adbpurrytify.data.model.SongEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecommendationRepository @Inject constructor(
    private val analyticsDao: AnalyticsDao,
    private val songRepository: SongRepository
) {
    private val TAG = "RecommendationRepository"

    /**
     * Get personalized recommendations based on listening history
     * Returns top 10 songs ordered by listening duration, padded with country top songs if needed
     */
    suspend fun getPersonalizedRecommendations(
        userId: Long,
        userCountry: String?
    ): List<SongEntity> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Getting recommendations for user $userId, country: $userCountry")

            // Step 1: Get top songs based on listening duration from analytics
            val topListenedSongs = getTopListenedSongs(userId, limit = 10)
            Log.d(TAG, "Found ${topListenedSongs.size} songs from listening history")

            val recommendations = mutableListOf<SongEntity>()
            recommendations.addAll(topListenedSongs)

            // Step 2: If we don't have 10 songs, pad with country top songs
            val remainingSlots = 10 - recommendations.size
            if (remainingSlots > 0 && !userCountry.isNullOrBlank()) {
                Log.d(TAG, "Need $remainingSlots more songs, fetching from country: $userCountry")

                val countrySongs = songRepository.getTopCountrySongs(userCountry)
                val listenedSongIds = recommendations.map { it.id }.toSet()

                // Add country songs that user hasn't listened to yet
                val paddingSongs = countrySongs
                    .filter { it.id !in listenedSongIds }
                    .take(remainingSlots)

                recommendations.addAll(paddingSongs)
                Log.d(TAG, "Added ${paddingSongs.size} country songs as padding")
            }

            Log.d(TAG, "Final recommendations: ${recommendations.size} songs")
            return@withContext recommendations.take(10)

        } catch (e: Exception) {
            Log.e(TAG, "Error getting recommendations", e)
            return@withContext emptyList()
        }
    }

    /**
     * Get top listened songs based on total listening duration from analytics
     */
    private suspend fun getTopListenedSongs(userId: Long, limit: Int): List<SongEntity> {
        return try {
            // Get top songs from all time analytics data (not limited to specific month)
            val topSongResults = analyticsDao.getTopSongsAllTime(userId, limit)

            val songs = mutableListOf<SongEntity>()
            for (result in topSongResults) {
                val song = songRepository.getSongById(result.songId)
                if (song != null) {
                    songs.add(song)
                }
            }

            Log.d(TAG, "Retrieved ${songs.size} top listened songs")
            return songs

        } catch (e: Exception) {
            Log.e(TAG, "Error getting top listened songs", e)
            return emptyList()
        }
    }
}