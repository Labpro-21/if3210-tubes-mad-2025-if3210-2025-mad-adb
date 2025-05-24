package com.example.adbpurrytify.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardDoubleArrowLeft
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.adbpurrytify.R
import com.example.adbpurrytify.api.MusicService
import com.example.adbpurrytify.data.download.downloadSong
import com.example.adbpurrytify.data.model.SongEntity
import com.example.adbpurrytify.ui.navigation.Screen
import com.example.adbpurrytify.ui.theme.BLACK_BACKGROUND
import com.example.adbpurrytify.ui.theme.Green
import com.example.adbpurrytify.ui.theme.SpotifyGreen
import com.example.adbpurrytify.ui.theme.SpotifyLightGray
import com.example.adbpurrytify.ui.theme.TEXT_FIELD_TEXT
import com.example.adbpurrytify.ui.utils.DynamicColorExtractor
import com.example.adbpurrytify.ui.viewmodels.SongViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.math.max

fun formatTime(millis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format("%02d:%02d", minutes, seconds)
}

@Composable
fun SongPlayerScreen(
    navController: NavController,
    songId: Long,
    snackBarHostState: SnackbarHostState,
    viewModel: SongViewModel = hiltViewModel()
) {

    var firstsong = runBlocking { viewModel.getSongById(songId) }
    var song by remember { mutableStateOf<SongEntity?>(firstsong) }
    var prevSong by remember { mutableStateOf<SongEntity?>(null) }
    var nextSong by remember { mutableStateOf<SongEntity?>(null) }

    var isLiked by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableStateOf(0L) }
    var dominantColor by remember { mutableStateOf(Color(0xFF121212)) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var playerReady by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(song) {
        viewModel.loadUserData()
        while(viewModel.getCurrentUserId() == null) {
            delay(50)
        }

        val userId = viewModel.getCurrentUserId()!!
        SongPlayer.curUserId = userId

        song?.let {
            prevSong = viewModel.getPrevSong(it.id)
            nextSong = viewModel.getNextSong(it.id)

            // Extract dominant color
            val imageUrl = if (it.artUri.isNotEmpty()) it.artUri else R.drawable.song_art_placeholder
            dominantColor = DynamicColorExtractor.extractDominantColor(
                imageUrl.toString(),
                context,
                Color(0xFF121212)
            )

            viewModel.updateSongTimestamp(it)
            isLiked = it.isLiked

            if (SongPlayer.songLoaded == false
                or ((SongPlayer.songLoaded) and (SongPlayer.curLoadedSongId != it.id))) {

                playerReady = false

                // Start analytics session through the enhanced music service
                MusicService.startPlayback(context, it, userId)

                SongPlayer.loadSong(it, context, it.id)

                while (SongPlayer.getDuration() <= 0) {
                    delay(100)
                }
                playerReady = true

                // Properly sync initial playing state
                isPlaying = SongPlayer.isPlaying()
                Log.d("SongPlayer", "New song loaded: ${it.title}, playing: $isPlaying")
            }
            else { // same song
                // Always sync with actual player state
                isPlaying = SongPlayer.isPlaying()
                sliderPosition = SongPlayer.getProgress()
                playerReady = true
                Log.d("SongPlayer", "Same song, syncing state: playing=$isPlaying")
            }
        }
    }

    // Improved state synchronization with stop detection
    LaunchedEffect(Unit) {
        while (true) {
            delay(200L) // Check more frequently for better UI responsiveness

            // Check for stop command from notification
            if (SongPlayer.curLoadedSongId == -2L) {
                SongPlayer.curLoadedSongId = -1
                Log.d("REDIRECT", "going home")
                navController.navigate(Screen.Home.route)
                break
            }

            // Always sync UI state with actual player state
            val actuallyPlaying = SongPlayer.isPlaying()
            if (isPlaying != actuallyPlaying) {
                Log.d("SongPlayer", "UI state mismatch! UI: $isPlaying, Player: $actuallyPlaying - syncing")
                isPlaying = actuallyPlaying
            }

            // Update position if player is ready
            if (playerReady) {
                sliderPosition = SongPlayer.getProgress()
            }

            // Handle song transitions
            if (nextSong != null && SongPlayer.mediaController != null
                && SongPlayer.curLoadedSongId == nextSong?.id) {
                Log.d("SongPlayer", "Detected song change to next song")
                playerReady = false
                song = nextSong
                break // Exit loop to restart with new song
            }
            else if (prevSong != null && SongPlayer.mediaController != null
                && SongPlayer.curLoadedSongId == prevSong?.id) {
                Log.d("SongPlayer", "Detected song change to previous song")
                playerReady = false
                song = prevSong
                break // Exit loop to restart with new song
            }
            else if (nextSong == null &&
                SongPlayer.mediaController?.playbackState == Player.STATE_ENDED) {
                Log.d("SongPlayer", "Song ended, no next song")
                isPlaying = false
            }
        }
    }

    // Background with blurred album art
    Box(modifier = Modifier.fillMaxSize()) {
        // Blurred background
        song?.let { currentSong ->
            AsyncImage(
                model = if (currentSong.artUri.isNotEmpty()) currentSong.artUri else R.drawable.song_art_placeholder,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(50.dp),
                contentScale = ContentScale.Crop,
                alpha = 0.3f
            )
        }

        // Dark overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    DynamicColorExtractor.darkenColor(dominantColor, 0.8f).copy(alpha = 0.9f)
                )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.navigate(Screen.Home.route) }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Go back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Now Playing",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Song info and art
            song?.let { currentSong ->
                // Album Art
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    val art = if (currentSong.artUri.isNotEmpty()) currentSong.artUri else R.drawable.song_art_placeholder
                    AsyncImage(
                        model = art,
                        contentDescription = "Album art",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Song Title + Author and Download + Like Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Download button
                    val isTrendingSong = currentSong.audioUri.startsWith("http")
                    val downloadFile =
                        File(context.filesDir, "${currentSong.author + "-" + currentSong.title}.mp3")
                    if (isTrendingSong && !downloadFile.exists() || isDownloading) {
                        if (isDownloading) {
                            CircularProgressIndicator(
                                progress = { downloadProgress },
                                modifier = Modifier.size(28.dp),
                                color = Green
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${(downloadProgress * 100f).toInt()}%",
                                color = TEXT_FIELD_TEXT,
                                fontSize = 16.sp
                            )
                        } else {
                            IconButton(
                                onClick = {
                                    isDownloading = true
                                    coroutineScope.launch {
                                        val path = downloadSong(
                                            currentSong, context, Dispatchers.IO
                                        ) { progress ->
                                            coroutineScope.launch(Dispatchers.Main) {
                                                downloadProgress = progress
                                            }
                                        }
                                        isDownloading = false
                                        val message = if (path != null) {
                                            val updatedSong = song!!.copy(audioUri = path)
                                            viewModel.update(updatedSong)
                                            "Song download completed!"
                                        } else "Download error. Please try again."
                                        snackBarHostState.showSnackbar(
                                            message, duration = SnackbarDuration.Short
                                        )
                                    }
                                }) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Download song",
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(currentSong.title, color = Color.White, style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(currentSong.author, color = Color.LightGray, style = MaterialTheme.typography.bodyLarge)
                    }

                    // Like Button
                    IconButton(
                        onClick = {
                            // Immediately update UI (optimistic)
                            isLiked = !isLiked

                            // Update in background
                            coroutineScope.launch {
                                val updatedSong = currentSong.copy(isLiked = isLiked)
                                viewModel.update(updatedSong)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isLiked) "Unlike song" else "Like song",
                            tint = if (isLiked) Green else Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Slider section
                Slider(
                    value = sliderPosition.toFloat(),
                    onValueChange = { sliderPosition = it.toLong() },
                    onValueChangeFinished = {
                        try {
                            SongPlayer.seekTo(sliderPosition)
                            Log.d("SongPlayer", "Seeked to position: $sliderPosition")

                            // Ensure UI state remains consistent after seek
                            coroutineScope.launch {
                                delay(100)
                                isPlaying = SongPlayer.isPlaying()
                                Log.d("SongPlayer", "After seek, playing state: $isPlaying")
                            }
                        } catch (e: Exception) {
                            Log.e("SongPlayer", "Error seeking", e)
                        }
                    },
                    valueRange = 0f..(max(0L, SongPlayer.getDuration()).toFloat()),
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = SpotifyGreen,
                        activeTrackColor = SpotifyGreen,
                        inactiveTrackColor = SpotifyLightGray.copy(alpha = 0.3f)
                    )
                )

                if (playerReady) {
                    // Time display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(formatTime(SongPlayer.getProgress()), color = Color.White)
                        Text(formatTime(max(0L, SongPlayer.getDuration())), color = Color.White)
                    }
                } else {
                    // Show loading indicator for slider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Green, modifier = Modifier.size(24.dp))
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Play/Pause and navigation buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (prevSong != null)
                        IconButton(
                            onClick = {
                                Log.d("SongPlayer", "Previous song clicked")
                                coroutineScope.launch {
                                    try {
                                        song = prevSong
                                        // Reset states for new song
                                        playerReady = false
                                        isPlaying = false
                                    } catch (e: Exception) {
                                        Log.e("SongPlayer", "Error switching to previous song", e)
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .background(Green, shape = RoundedCornerShape(50))
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardDoubleArrowLeft,
                                contentDescription = "Previous song",
                                tint = Color.Black,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    Spacer(modifier = Modifier.width(32.dp))

                    // Improved Play/Pause button with better state handling
                    IconButton(
                        onClick = {
                            Log.d("SongPlayer", "Play/Pause clicked. Current state - UI: $isPlaying, Player: ${SongPlayer.isPlaying()}")

                            try {
                                if (isPlaying) {
                                    SongPlayer.pause()
                                    Log.d("SongPlayer", "Called SongPlayer.pause()")
                                } else {
                                    SongPlayer.play()
                                    Log.d("SongPlayer", "Called SongPlayer.play()")
                                }

                                // Give the player a moment to update its state
                                coroutineScope.launch {
                                    delay(100) // Small delay to let player state update
                                    val newState = SongPlayer.isPlaying()
                                    Log.d("SongPlayer", "After action, player state: $newState")
                                    isPlaying = newState
                                }

                            } catch (e: Exception) {
                                Log.e("SongPlayer", "Error in play/pause", e)
                            }
                        },
                        modifier = Modifier
                            .size(64.dp)
                            .background(SpotifyGreen, CircleShape)
                    ) {

                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(32.dp))

                    if (nextSong != null)
                        IconButton(
                            onClick = {
                                Log.d("SongPlayer", "Next song clicked")
                                coroutineScope.launch {
                                    try {
                                        song = nextSong
                                        // Reset states for new song
                                        playerReady = false
                                        isPlaying = false
                                    } catch (e: Exception) {
                                        Log.e("SongPlayer", "Error switching to next song", e)
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .background(Green, shape = RoundedCornerShape(50))
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardDoubleArrowRight,
                                contentDescription = "Next song",
                                tint = Color.Black,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                }
            } ?: run {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Green)
                }
            }
        }
    }

    // Handle when user leaves the screen
    DisposableEffect(Unit) {
        onDispose {
            Log.d("SongPlayer", "SongPlayerScreen disposed")
        }
    }
}
