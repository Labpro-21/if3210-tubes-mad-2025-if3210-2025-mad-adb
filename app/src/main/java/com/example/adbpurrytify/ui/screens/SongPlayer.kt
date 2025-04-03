package com.example.adbpurrytify.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import kotlinx.coroutines.delay


object SongPlayer {
    private var player: ExoPlayer? = null


    @OptIn(UnstableApi::class)
    fun loadSong(songPath: String, context: Context) {
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
        player!!.playWhenReady = false
        player!!.setSeekParameters(SeekParameters.CLOSEST_SYNC)
    }

    fun isPlaying(): Boolean {
        return player?.isPlaying ?: false
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
        player!!.playWhenReady = false
        player!!.seekTo(position)
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

@Preview
@Composable
fun SongPlayer() {

    var songPath = "file:///sdcard/Download/All%20Time%20Low%20-%20Remembering%20Sunday%20feat.%20Lindsey%20Stirling%20&%20Lisa%20Gaskarth%20(ATL's%20Version).mp3"
    var isPlaying by remember { mutableStateOf(false) }
    var songTitle by remember { mutableStateOf("Sample Song") }

    // Initialize the player on first launch
    val context = LocalContext.current
    var sliderPosition by remember { mutableStateOf(0L) }

    LaunchedEffect(songPath) {
        SongPlayer.loadSong(songPath, context)
    }

    // Update slider position every second
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            sliderPosition = SongPlayer.getProgress()
            Log.d("sliderPosition", sliderPosition.toString())
            delay(1000L)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Song title
        Text(
            text = songTitle,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Play/Pause button
        IconButton(onClick = {
            if (isPlaying) {
                SongPlayer.pause()
            } else {
                SongPlayer.play()
            }
            isPlaying = !isPlaying
        }) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Clear else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                modifier = Modifier.size(48.dp)
            )
        }

        // Seekbar Slider
        Slider(
            value = sliderPosition.toFloat(),
            onValueChange = {
                sliderPosition = it.toLong()
            },
            onValueChangeFinished = {
                SongPlayer.seekTo(sliderPosition.toLong())
            },
            valueRange = 0f..SongPlayer.getDuration().toFloat(),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )

    }

    // Handle cleanup when composable is disposed
    DisposableEffect(songPath) {
        onDispose {
            SongPlayer.release()
        }
    }
}
