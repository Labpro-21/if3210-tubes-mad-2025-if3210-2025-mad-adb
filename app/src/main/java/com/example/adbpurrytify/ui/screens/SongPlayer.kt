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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.adbpurrytify.R
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit


fun formatTime(millis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format("%02d:%02d", minutes, seconds)
}


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
fun SongPlayer(songId: Int = -1) {

    var songPath: String
    var songImagePath = ""
    var songAudioPath = ""
    var songTitle: String
    var songAuthor: String

    if (songId == -1) { // Called without id param == placeholder song!
        songPath = "file:///sdcard/Download/All%20Time%20Low%20-%20Remembering%20Sunday%20feat.%20Lindsey%20Stirling%20&%20Lisa%20Gaskarth%20(ATL's%20Version).mp3"
        songTitle = "Remembering Sunday feat. Lindsey Stirling & Lisa Gaskarth (ATL's version)"
        songAuthor = "All Time Low"

    } else {
        // TODO: query song metadata FROM RoomDatabse using songId
        throw NotImplementedError("Belom dibikin woi")
        songImagePath = ""
        songAudioPath = ""
        songTitle = ""
        songAuthor = ""
    }


    // Initialize the player on first launch
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
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
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // Song Art
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("https://example.com/image.jpg")
                .crossfade(true)
                .build(),
            placeholder = painterResource(R.drawable.song_art_placeholder),
            contentDescription = "Song art for " + songTitle,
            contentScale = ContentScale.Crop,
        )

        Image(
            painter = painterResource(id = R.drawable.remembering_sunday),
            contentDescription = "Sample image art",
            modifier = Modifier
                .fillMaxWidth()
                .width(300.dp)
                .padding(horizontal = 20.dp)
                .clip(shape = RoundedCornerShape(4.dp))
        )

        // Song title
        Text(
            text = songTitle,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(all = 20.dp),
            textAlign = TextAlign.Center
        )

        // Song Author
        Text(
            text = songAuthor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(all = 12.dp),
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
            // TODO: add next and prev icon buttons
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
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        )

        // Timestamps
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            var duration = SongPlayer.getDuration()
            var elapsed = SongPlayer.getProgress()
            Text(text = formatTime(elapsed))
            Text(text = formatTime(duration))
        }
    }

    // Handle cleanup when composable is disposed
    DisposableEffect(songPath) {
        onDispose {
            SongPlayer.release()
        }
    }
}
