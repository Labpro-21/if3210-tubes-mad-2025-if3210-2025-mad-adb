package com.example.adbpurrytify.api

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.adbpurrytify.R
import com.example.adbpurrytify.data.SongRepository_Factory
import com.example.adbpurrytify.data.local.AppDatabase
import com.example.adbpurrytify.data.local.SongDao
import com.example.adbpurrytify.data.model.SongEntity
import com.example.adbpurrytify.di.DatabaseModule
import com.example.adbpurrytify.ui.screens.SongPlayer
import com.example.adbpurrytify.ui.viewmodels.SongViewModel
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.runBlocking

const val NEXT_SONG = "next song"


class MusicService : MediaSessionService() {
    private val nextCommand = SessionCommand(NEXT_SONG, Bundle.EMPTY)
    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null

    @androidx.annotation.OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        val nextButton =
            CommandButton.Builder(CommandButton.ICON_NEXT)
                .setDisplayName("Next Song")
                .setSessionCommand(nextCommand)
                .build()

        player = ExoPlayer.Builder(this).build()
        val playerListener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                super.onPlaybackStateChanged(state)
                if (state == Player.STATE_ENDED)
                    tryToStartNextSong(applicationContext)
            }
        }
        player?.addListener(playerListener)

        mediaSession = MediaSession.Builder(this, player!!)
            .setId("MusicSession")
            .setCustomLayout(mutableListOf<CommandButton>(nextButton))
            .setCallback(MyCallback(this))
            .build()

    }

    private inner class MyCallback(private val context: Context) : MediaSession.Callback {
        @OptIn(UnstableApi::class)
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): ConnectionResult {
            // Set available player and session commands.
            return AcceptedResultBuilder(session)
                .setAvailableSessionCommands(
                    ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon().add(nextCommand).build()
                ).build()
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            if (customCommand.customAction == NEXT_SONG) tryToStartNextSong(context)
            return super.onCustomCommand(session, controller, customCommand, args)
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        Log.d("Service", "RELEASED")
        mediaSession?.release()
        player?.release()
        super.onDestroy()
    }


    private fun tryToStartNextSong(context: Context) {
        var curSongId = SongPlayer.curLoadedSongId
        var curUserId = SongPlayer.curUserId

        val songDao =
            DatabaseModule.provideSongDao(AppDatabase.getDatabase(context))

        var nextSong: SongEntity?
        runBlocking { nextSong = songDao.getNextSong(curUserId, curSongId) }
        if (nextSong != null) {
            Log.d("NOTIF", nextSong.title)
            val mediaItem =
                MediaItem.Builder()
                    .setMediaId("998244353")
                    .setUri(nextSong.audioUri)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setArtist(nextSong.author)
                            .setTitle(nextSong.title)
                            .setArtworkUri(Uri.parse(nextSong.artUri))
                            .build()
                    ).build()
            player?.stop()
            player?.clearMediaItems()
            player?.setMediaItem(mediaItem)
            player?.prepare()
            player?.playWhenReady = true
            SongPlayer.curLoadedSongId = nextSong.id
        }

        else {
            // ini sbnernya muncul tp gk keliatan karena ketutupan notif, yaudala :v
            Toast.makeText(applicationContext, "There is no next song.", Toast.LENGTH_SHORT).show()
        }
    }




    companion object {
        private const val NOTIFICATION_ID = 1
    }
}
