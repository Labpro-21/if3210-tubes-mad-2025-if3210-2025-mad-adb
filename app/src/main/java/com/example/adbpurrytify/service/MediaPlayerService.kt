package com.example.adbpurrytify.service

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for managing media playback.
 * Handles ExoPlayer lifecycle and provides media control functions.
 */
@Singleton
class MediaPlayerService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "MediaPlayerService"

    private var player: ExoPlayer? = null

    // Player state
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration

    private val _currentSongId = MutableStateFlow<Long?>(null)
    val currentSongId: StateFlow<Long?> = _currentSongId

    private val _isPlayerReady = MutableStateFlow(false)
    val isPlayerReady: StateFlow<Boolean> = _isPlayerReady

    init {
        initializePlayer()
    }

    private fun initializePlayer() {
        if (player == null) {
            player = ExoPlayer.Builder(context).build().apply {
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        when (state) {
                            Player.STATE_READY -> {
                                _isPlayerReady.value = true
                                _duration.value = player?.duration ?: 0
                            }
                            Player.STATE_ENDED -> {
                                _isPlaying.value = false
                            }
                            Player.STATE_BUFFERING -> {
                                _isPlayerReady.value = false
                            }
                            Player.STATE_IDLE -> {
                                _isPlayerReady.value = false
                            }
                        }
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _isPlaying.value = isPlaying
                    }
                })
            }
        }
    }

    fun loadSong(songPath: String, songId: Long) {
        Log.d(TAG, "Loading song: $songPath")

        try {
            val uri = Uri.parse(songPath)
            val mediaItem = MediaItem.fromUri(uri)

            player?.let { exoPlayer ->
                // Release previous resources
                exoPlayer.stop()

                // Load new media
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true

                _currentSongId.value = songId
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading song", e)
        }
    }

    fun play() {
        player?.play()
    }

    fun pause() {
        player?.pause()
    }

    fun stop() {
        player?.stop()
    }

    fun seekTo(position: Long) {
        player?.seekTo(position)
    }

    fun release() {
        player?.release()
        player = null
        _isPlayerReady.value = false
        _currentSongId.value = null
    }

    fun getCurrentPosition(): Long {
        return player?.currentPosition ?: 0
    }

    fun getDuration(): Long {
        return player?.duration ?: 0
    }

    fun formatTime(millis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun isPlayingNow(): Boolean {
        return player?.isPlaying == true
    }
}