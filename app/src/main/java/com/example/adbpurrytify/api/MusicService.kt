package com.example.adbpurrytify.api

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.ConnectionResult
import androidx.media3.session.MediaSession.ConnectionResult.AcceptedResultBuilder
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.example.adbpurrytify.data.AnalyticsRepository
import com.example.adbpurrytify.data.AuthRepository
import com.example.adbpurrytify.data.SongRepository
import com.example.adbpurrytify.data.model.SongEntity
import com.example.adbpurrytify.ui.screens.SongPlayer
import com.example.adbpurrytify.ui.screens.globalPlayer
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

const val NEXT_SONG = "next song"
const val PREV_SONG = "prev song"
const val STOP = "stop"

@AndroidEntryPoint
class MusicService : MediaSessionService() {
    private val nextCommand = SessionCommand(NEXT_SONG, Bundle.EMPTY)
//    private val prevCommand = SessionCommand(PREV_SONG, Bundle.EMPTY)
    private val stopCommand = SessionCommand(STOP, Bundle.EMPTY)
    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null

    // Analytics integration
    @Inject lateinit var analyticsRepository: AnalyticsRepository
    @Inject lateinit var songRepository: SongRepository
    @Inject lateinit var authRepository: AuthRepository

    private var currentUserId: Long? = null
    private var currentSong: SongEntity? = null
    private var lastPosition: Long = 0
    private var progressUpdateJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        Log.d("MusicService", "Service created with analytics integration")

        // Initialize user ID
        initializeUser()

        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val nextButton = CommandButton.Builder(CommandButton.ICON_NEXT)
            .setDisplayName("Next Song")
            .setSessionCommand(nextCommand)
            .build()

//        val prevButton = CommandButton.Builder(CommandButton.ICON_PREVIOUS)
//            .setDisplayName("Previous Song")
//            .setSessionCommand(prevCommand)
//            .build()

        val stopButton = CommandButton.Builder(CommandButton.ICON_STOP)
            .setDisplayName("Stop")
            .setSessionCommand(stopCommand)
            .build()

        player = ExoPlayer.Builder(this).build()
        val playerListener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                super.onPlaybackStateChanged(state)
                handlePlaybackStateChange(state)
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                handlePlayingStateChange(isPlaying)
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                super.onPositionDiscontinuity(oldPosition, newPosition, reason)
                if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                    handleSeek(newPosition.positionMs)
                }
            }
        }
        player?.addListener(playerListener)
        globalPlayer.player = player // aku butuh istirahat

        val forwardingPlayer = object : ForwardingPlayer(player!!) {
            override fun seekToPrevious() {
                handlePreviousTrack()
            }

            override fun seekToNext() {
                handleNextTrack()
            }
        }

        mediaSession = MediaSession.Builder(this, forwardingPlayer)
            .setId("MusicSession")
            .setCustomLayout(mutableListOf(nextButton, stopButton))
            .setCallback(MyCallback(this))
            .setSessionActivity(pendingIntent)
            .build()

//        startForeground()

        startProgressTracking()
    }

    private fun initializeUser() {
        serviceScope.launch {
            try {
                val userResult = authRepository.getCurrentUser()
                if (userResult.isSuccess) {
                    currentUserId = userResult.getOrThrow().id
                    Log.d("MusicService", "Initialized with user ID: $currentUserId")
                }
            } catch (e: Exception) {
                Log.e("MusicService", "Failed to initialize user", e)
            }
        }
    }

    private fun startProgressTracking() {
        progressUpdateJob?.cancel()
        progressUpdateJob = serviceScope.launch {
            while (isActive) {
                delay(1000) // Update every second

                player?.let { player ->
                    if (player.isPlaying) {
                        val currentPosition = player.currentPosition

                        // Update analytics with current progress
                        try {
                            analyticsRepository.updateProgress(currentPosition, true)
                        } catch (e: Exception) {
                            Log.e("MusicService", "Error updating analytics progress", e)
                        }

                        lastPosition = currentPosition
                    }
                }
            }
        }
    }

    fun getCurrentAnalyticsSession(): Triple<Long?, SongEntity?, Long>? {
        return currentUserId?.let { userId ->
            currentSong?.let { song ->
                Triple(userId, song, lastPosition)
            }
        }
    }

    // Update the loadSongWithAnalytics method:
    fun loadSongWithAnalytics(song: SongEntity, userId: Long) {
        serviceScope.launch {
            try {
                // End previous session and start new one
                analyticsRepository.stopListening(userId, completed = false)
                analyticsRepository.startListening(userId, song)

                currentSong = song
                currentUserId = userId

                // Load song in player
                val mediaItem = MediaItem.Builder()
                    .setMediaId(song.id.toString())
                    .setUri(Uri.parse(song.audioUri))
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setArtist(song.author)
                            .setTitle(song.title)
                            .setArtworkUri(if (song.artUri.isNotEmpty()) Uri.parse(song.artUri) else null)
                            .build()
                    ).build()

                player?.apply {
                    stop()
                    clearMediaItems()
                    setMediaItem(mediaItem)
                    prepare()
                    playWhenReady = true
                }
                SongPlayer.curLoadedSongId = song.id

                Log.d("MusicService", "Loaded song with analytics: ${song.title}")
            } catch (e: Exception) {
                Log.e("MusicService", "Error loading song with analytics", e)
            }
        }
    }

    private fun handlePlaybackStateChange(state: Int) {
        when (state) {
            Player.STATE_ENDED -> {
                serviceScope.launch {
                    try {
                        currentUserId?.let { userId ->
                            analyticsRepository.stopListening(userId, completed = true)
                        }
                        tryToStartNextSong()
                    } catch (e: Exception) {
                        Log.e("MusicService", "Error handling song end", e)
                    }
                }
            }
            Player.STATE_READY -> {
                Log.d("MusicService", "Player ready, current song: ${currentSong?.title}")
            }
            Player.STATE_BUFFERING -> {
                Log.d("MusicService", "Player buffering")
            }
        }
    }

    private fun handlePlayingStateChange(isPlaying: Boolean) {
        serviceScope.launch {
            try {
                if (isPlaying) {
                    analyticsRepository.resumeListening()
                    Log.d("MusicService", "Resumed listening analytics")
                } else {
                    analyticsRepository.pauseListening()
                    Log.d("MusicService", "Paused listening analytics")
                }
            } catch (e: Exception) {
                Log.e("MusicService", "Error handling playing state change", e)
            }
        }
    }

    private fun handleSeek(position: Long) {
        serviceScope.launch {
            try {
                analyticsRepository.updateProgress(position, player?.isPlaying ?: false)
            } catch (e: Exception) {
                Log.e("MusicService", "Error handling seek", e)
            }
        }
    }

    private fun handleNextTrack() {
        serviceScope.launch {
            try {
                analyticsRepository.skipTrack()
                tryToStartNextSong()
            } catch (e: Exception) {
                Log.e("MusicService", "Error handling next track", e)
            }
        }
    }

    private fun handlePreviousTrack() {
        serviceScope.launch {
            try {
                analyticsRepository.skipTrack()
                tryToStartPreviousSong()
            } catch (e: Exception) {
                Log.e("MusicService", "Error handling previous track", e)
            }
        }
    }

    private inner class MyCallback(private val context: Context) : MediaSession.Callback {
        @OptIn(UnstableApi::class)
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): ConnectionResult {
            return AcceptedResultBuilder(session)
                .setAvailableSessionCommands(
                    ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                        .add(nextCommand)
//                        .add(prevCommand)
                        .add(stopCommand)
                        .build()
                ).build()
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            when (customCommand.customAction) {
                NEXT_SONG -> handleNextTrack()
//                PREV_SONG -> handlePreviousTrack()
                STOP -> {
                    player?.stop()
                    player?.clearMediaItems()
                    SongPlayer.curLoadedSongId = -2 // set for rendering songplayerscreen
                }
            }
            return super.onCustomCommand(session, controller, customCommand, args)
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        Log.d("MusicService", "Service destroyed")

        // End current analytics session
        serviceScope.launch {
            try {
                currentUserId?.let { userId ->
                    analyticsRepository.stopListening(userId, completed = false)
                }
            } catch (e: Exception) {
                Log.e("MusicService", "Error stopping analytics on destroy", e)
            }
        }

        progressUpdateJob?.cancel()
        serviceScope.cancel()
        mediaSession?.release()
        player?.release()
        super.onDestroy()
    }

    // ===== SONG LOADING WITH ANALYTICS =====

    private suspend fun tryToStartNextSong() {
        try {
            val curSongId = currentSong?.id ?: return
            val curUserId = currentUserId ?: return

            val nextSong = songRepository.getNextSong(curUserId, curSongId)
            if (nextSong != null) {
                loadSongWithAnalytics(nextSong, curUserId)
                Log.d("MusicService", "Started next song: ${nextSong.title}")
            } else {
                Log.d("MusicService", "No next song available")
            }
        } catch (e: Exception) {
            Log.e("MusicService", "Error starting next song", e)
        }
    }

    private suspend fun tryToStartPreviousSong() {
        try {
            val curSongId = currentSong?.id ?: return
            val curUserId = currentUserId ?: return

            val prevSong = songRepository.getPreviousSong(curUserId, curSongId)
            if (prevSong != null) {
                loadSongWithAnalytics(prevSong, curUserId)
                Log.d("MusicService", "Started previous song: ${prevSong.title}")
            } else {
                Log.d("MusicService", "No previous song available")
            }
        } catch (e: Exception) {
            Log.e("MusicService", "Error starting previous song", e)
        }
    }

    // ===== PUBLIC INTERFACE =====

    companion object {
        private const val NOTIFICATION_ID = 1

        // Static methods to control the service from other components
        fun startPlayback(context: Context, song: SongEntity, userId: Long) {
            val intent = Intent(context, MusicService::class.java).apply {
                action = "START_PLAYBACK"
                putExtra("song_id", song.id)
                putExtra("user_id", userId)
            }
            context.startService(intent)
        }

        fun stopPlayback(context: Context) {
            val intent = Intent(context, MusicService::class.java).apply {
                action = "STOP_PLAYBACK"
            }
            context.startService(intent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START_PLAYBACK" -> {
                val songId = intent.getLongExtra("song_id", -1)
                val userId = intent.getLongExtra("user_id", -1)
                if (songId != -1L && userId != -1L) {
                    serviceScope.launch {
                        try {
                            val song = songRepository.getSongById(songId)
                            if (song != null) {
                                loadSongWithAnalytics(song, userId)
                            }
                        } catch (e: Exception) {
                            Log.e("MusicService", "Error starting playback from intent", e)
                        }
                    }
                }
            }
            "STOP_PLAYBACK" -> {
                serviceScope.launch {
                    try {
                        currentUserId?.let { userId ->
                            analyticsRepository.stopListening(userId, completed = false)
                        }
                        player?.stop()
                    } catch (e: Exception) {
                        Log.e("MusicService", "Error stopping playback", e)
                    }
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
}
