package com.example.adbpurrytify.ui.screens

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.session.MediaController
import com.example.adbpurrytify.data.model.SongEntity
import com.example.adbpurrytify.ui.theme.Green
import java.util.concurrent.TimeUnit




object SongPlayer {

    // is this bad code? maybe
    // but at least it works :v
    var mediaController: MediaController? = null
    var songLoaded: Boolean = false
    var curLoadedSongId: Long = -1
    var curUserId: Long = -1

    @OptIn(UnstableApi::class)
    fun loadSong(song: SongEntity, context: Context, songId: Long) {
        Log.d("path str", song.audioUri)
        if (mediaController == null) {
            Log.d("LOAD CALLED", "NULL")
            return
        }
        var uriparseres = Uri.parse(song.audioUri)
        Log.d("URI Parse Res", uriparseres.toString())

//        val mediaItem = MediaItem.fromUri(uriparseres)
        val mediaItem =
            MediaItem.Builder()
                .setMediaId("998244353")
                .setUri(uriparseres)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setArtist(song.author)
                        .setTitle(song.title)
                        .setArtworkUri(Uri.parse(song.artUri))
                        .build()
                )
                .build()

        Log.d("Media Id", mediaItem.mediaId)
        mediaController!!.setMediaItem(mediaItem)
        mediaController!!.prepare()
        mediaController!!.playWhenReady = true

        songLoaded = true
        curLoadedSongId = songId
    }

    fun isPlaying(): Boolean {
        return mediaController?.isPlaying == true
    }

    fun play() {
        mediaController!!.play()
    }

    fun pause() {
        mediaController!!.playWhenReady = false
    }

    fun stop() {
        mediaController!!.stop()
    }

    fun seekTo(position: Long) {
        var wasPlaying = mediaController!!.playWhenReady
        if (mediaController!!.playWhenReady)
            mediaController!!.playWhenReady = false

        mediaController!!.seekTo(position)

        if (wasPlaying)
            mediaController!!.playWhenReady = true
    }

    fun release() {
        mediaController?.release()
        mediaController = null
    }

    fun getDuration(): Long {
        return mediaController?.duration ?: 0L
    }

    fun getProgress(): Long {
        return mediaController?.currentPosition ?: 0L
    }

    fun isPrepared(): Boolean {
        val playerState = mediaController?.playbackState
        return playerState != null && playerState != ExoPlayer.STATE_IDLE && playerState != ExoPlayer.STATE_ENDED
    }

}

// I'm going insane, this is for sanity check
@Composable
fun TestPlayer(songUrl: String) {
    val context = LocalContext.current
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(songUrl))
            prepare()
            playWhenReady = true
        }
    }

    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Testing ExoPlayer's capability to play from http/https", color = Green)
    }
}
