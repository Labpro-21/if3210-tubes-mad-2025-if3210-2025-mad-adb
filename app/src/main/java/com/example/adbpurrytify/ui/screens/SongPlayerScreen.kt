package com.example.adbpurrytify.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.adbpurrytify.R
import com.example.adbpurrytify.data.local.AppDatabase
import com.example.adbpurrytify.data.model.SongEntity
import com.example.adbpurrytify.ui.viewmodels.HomeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Composable
fun SongPlayerScreen(
    navController: NavController,
    songId: Long,
    viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(
            AppDatabase.getDatabase(LocalContext.current).songDao()
        )
    )
) {
    val backgroundColor = Color(0xFF121212)
    val accentColor = Color(0xFF1ED760)

    var song by remember { mutableStateOf<SongEntity?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableStateOf(0L) }

    val context = LocalContext.current


    var playerReady by remember { mutableStateOf(false) }
    LaunchedEffect(songId) {

        song = runBlocking { viewModel.getSongById(songId) }
        song?.let {

            if (SongPlayer.songLoaded == false
                or ((SongPlayer.songLoaded) and (SongPlayer.curLoadedSongId != songId))) {

                SongPlayer.release()
                SongPlayer.loadSong(it.audioUri, context, song!!.id)
                // Wait for the player to be ready
                while (SongPlayer.getDuration() <= 0) {
                    delay(100)
                }
                playerReady = true
                isPlaying = true
            }

            else { //lagu yg sama
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
            Log.d("sliderPosition", sliderPosition.toString())
            delay(1000L)
        }
    }

    // UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.navigateUp() }) {
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

        // Song info dan art
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

            // Song Title + Author
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(currentSong.title, color = Color.White, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(currentSong.author, color = Color.LightGray, style = MaterialTheme.typography.bodyLarge)
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
                    CircularProgressIndicator(color = accentColor, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Play/Pause
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        .background(accentColor, shape = RoundedCornerShape(50))
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = accentColor)
            }
        }
    }

}
