package com.example.adbpurrytify.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.adbpurrytify.R
import com.example.adbpurrytify.data.model.SongEntity
import com.example.adbpurrytify.ui.navigation.Screen
import com.example.adbpurrytify.ui.theme.BLACK_BACKGROUND
import com.example.adbpurrytify.ui.theme.Green
import com.example.adbpurrytify.ui.viewmodels.SongViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Composable
fun SongPlayerScreen(
    navController: NavController,
    songId: Long,
    viewModel: SongViewModel = hiltViewModel()
) {
    var song by remember { mutableStateOf<SongEntity?>(null) }
    var isLiked by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableStateOf(0L) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var prevId by remember { mutableStateOf(-1L) }
    var nextId by remember { mutableStateOf(-1L) }

    var playerReady by remember { mutableStateOf(false) }

    LaunchedEffect(songId) {
        // Load user data first to ensure userId is available
        viewModel.loadUserData()
        // Wait until currentUserId is set
        while(viewModel.getCurrentUserId() == null) {
            delay(50)
        }
        // Now get navigation IDs
        prevId = viewModel.getPrevSongId(songId)
        nextId = viewModel.getNextSongId(songId)

        // Get song details
        song = runBlocking { viewModel.getSongById(songId) }
        song?.let {
            // Update last played timestamp
            viewModel.updateSongTimestamp(it)

            // Initialize liked state
            isLiked = it.isLiked

            // Handle player initialization
            if (!SongPlayer.songLoaded || (SongPlayer.songLoaded && SongPlayer.curLoadedSongId != songId)) {
                SongPlayer.release()
                SongPlayer.loadSong(it.audioUri, context, it.id)

                // Wait for player to be ready
                while (SongPlayer.getDuration() <= 0) {
                    delay(100)
                }
                playerReady = true
                isPlaying = true
            } else { // Same song
                isPlaying = SongPlayer.isPlaying()
                sliderPosition = SongPlayer.getProgress()
                playerReady = true
            }
        }
    }

    // Update slider position every second
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            sliderPosition = SongPlayer.getProgress()
            delay(1000L)

            // Auto-navigate to next song when current song ends
            if (sliderPosition >= SongPlayer.getDuration()) {
                if (nextId > -1) {
                    navController.navigate("${Screen.Player.route}/${nextId}")
                } else {
                    isPlaying = false
                }
            }
        }
    }

    // UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BLACK_BACKGROUND)
            .padding(16.dp)
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
                val art = if (currentSong.artUri.isNotEmpty()) currentSong.artUri else R.drawable.remembering_sunday
                AsyncImage(
                    model = art,
                    contentDescription = "Album art",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Song Title + Author and Like Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(currentSong.title, color = Color.White, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(currentSong.author, color = Color.LightGray, style = MaterialTheme.typography.bodyLarge)
                }

                // Like Button - Using the separate isLiked state for UI
                IconButton(
                    onClick = {
                        // Immediately update UI (optimistic)
                        isLiked = !isLiked

                        // Update in background
                        coroutineScope.launch {
                            // Create an updated version of the song with new like status
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
            if (playerReady) {
                // Slider
                Slider(
                    value = sliderPosition.toFloat(),
                    onValueChange = { sliderPosition = it.toLong() },
                    onValueChangeFinished = {
                        SongPlayer.seekTo(sliderPosition)
                    },
                    valueRange = 0f..(SongPlayer.getDuration().toFloat()),
                    modifier = Modifier.fillMaxWidth()
                )

                // Time display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(formatTime(SongPlayer.getProgress()), color = Color.White)
                    Text(formatTime(SongPlayer.getDuration()), color = Color.White)
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
                if (prevId > -1)
                    IconButton(
                        onClick = {
                            navController.navigate("${Screen.Player.route}/${prevId}")
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

                IconButton(
                    onClick = {
                        if (isPlaying) {
                            SongPlayer.pause()
                        } else {
                            SongPlayer.play()
                        }
                        isPlaying = !isPlaying
                    },
                    modifier = Modifier
                        .size(64.dp)
                        .background(Green, shape = RoundedCornerShape(50))
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.width(32.dp))

                if (nextId > -1)
                    IconButton(
                        onClick = {
                            navController.navigate("${Screen.Player.route}/${nextId}")
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