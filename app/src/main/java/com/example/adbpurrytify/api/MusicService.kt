package com.example.adbpurrytify.api

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.adbpurrytify.R

class MusicService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null

    override fun onCreate() {
        super.onCreate()

        player = ExoPlayer.Builder(this).build()

        // Load media from raw resource
        val mediaItem = MediaItem.fromUri("android.resource://$packageName/${R.raw.jojo}")
        player!!.setMediaItem(mediaItem)
        player!!.prepare()
//        player!!.play()

        mediaSession = MediaSession.Builder(this, player!!)
            .setId("MusicSession")
            .build()
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

    private fun buildNotification(): Notification {
        val channelId = "music_channel"

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                channelId,
//                "Music Playback",
//                NotificationManager.IMPORTANCE_LOW
//            )
//            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
//        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Music Service")
            .setContentText("Playing audio")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 1
    }
}
