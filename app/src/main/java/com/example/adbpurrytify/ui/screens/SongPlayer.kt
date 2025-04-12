package com.example.adbpurrytify.ui.screens

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import java.util.concurrent.TimeUnit


fun formatTime(millis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format("%02d:%02d", minutes, seconds)
}


object SongPlayer {
    private var player: ExoPlayer? = null

    var songLoaded: Boolean = false
    var curLoadedSongId: Long = -1

    @OptIn(UnstableApi::class)
    fun loadSong(songPath: String, context: Context, songId: Long) {
        Log.d("path str", songPath)
        if (player == null) {
            player = ExoPlayer.Builder(context).build()
        }
        var uriparseres = Uri.parse(songPath)
        Log.d("URI Parse Res", uriparseres.toString())

        val mediaItem = MediaItem.fromUri(Uri.parse(songPath))
        Log.d("Media Id", mediaItem.mediaId)

        player!!.setMediaItem(mediaItem)
        player!!.prepare()
        player!!.playWhenReady = true
        player!!.setSeekParameters(SeekParameters.CLOSEST_SYNC)

        songLoaded = true
        curLoadedSongId = songId
    }

    fun isPlaying(): Boolean {
        return player?.isPlaying == true
    }

    fun play() {
        player!!.play()
    }

    fun pause() {
        player!!.playWhenReady = false
    }

    fun stop() {
        player!!.stop()
    }

    fun seekTo(position: Long) {
        var wasPlaying = player!!.playWhenReady
        if (player!!.playWhenReady)
            player!!.playWhenReady = false

        player!!.seekTo(position)

        if (wasPlaying)
            player!!.playWhenReady = true
    }

    fun release() {
        player?.release()
        player = null
    }

    fun getDuration(): Long {
        return player?.duration ?: 0L
    }

    fun getProgress(): Long {
        return player?.currentPosition ?: 0L
    }

    fun isPrepared(): Boolean {
        val playerState = player?.playbackState
        return playerState != null && playerState != ExoPlayer.STATE_IDLE && playerState != ExoPlayer.STATE_ENDED
    }
}
