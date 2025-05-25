package com.example.adbpurrytify.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.adbpurrytify.R
import com.example.adbpurrytify.data.model.SongEntity
import com.example.adbpurrytify.ui.navigation.Screen
import com.example.adbpurrytify.ui.screens.SongPlayer
import com.example.adbpurrytify.ui.utils.DynamicColorExtractor
import com.example.adbpurrytify.ui.viewmodels.SongViewModel
import kotlinx.coroutines.delay

@Composable
fun MiniPlayer(
    navController: NavController,
    viewModel: SongViewModel = hiltViewModel()
) {
    var currentSong by remember { mutableStateOf<SongEntity?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var dominantColor by remember { mutableStateOf(Color(0xFF1ED760)) }

    val context = LocalContext.current

    // Update current song and playing state
    LaunchedEffect(currentSong) {
        if (SongPlayer.curLoadedSongId != -1L) {
            currentSong = viewModel.getSongById(SongPlayer.curLoadedSongId)
            currentSong?.let { song ->
                // Extract dominant color from album art
                val imageUrl = if (song.artUri.isNotEmpty()) song.artUri else R.drawable.song_art_placeholder
                dominantColor = DynamicColorExtractor.extractDominantColor(
                    imageUrl.toString(),
                    context,
                    Color(0xFF1ED760)
                )
            }
        }
    }

    // Update playing state and progress
    LaunchedEffect(Unit) {
        while (true) {
            delay(200L)
            isPlaying = SongPlayer.isPlaying()
            if (currentSong?.id != SongPlayer.curLoadedSongId) currentSong = null
            if (SongPlayer.getDuration() > 0) {
                progress = SongPlayer.getProgress().toFloat() / SongPlayer.getDuration().toFloat()
            }
        }
    }

    currentSong?.let { song ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .clickable {
                    navController.navigate("${Screen.Player.route}/${song.id}")
                },
            colors = CardDefaults.cardColors(
                containerColor = DynamicColorExtractor.darkenColor(dominantColor, 0.7f)
            ),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Album Art
                    AsyncImage(
                        model = if (song.artUri.isNotEmpty()) song.artUri else R.drawable.song_art_placeholder,
                        contentDescription = "Album art",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Song Info
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = song.title,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = song.author,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Play/Pause Button
                    IconButton(
                        onClick = {
                            if (isPlaying) {
                                SongPlayer.pause()
                            } else {
                                SongPlayer.play()
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(dominantColor, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Progress Bar
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = dominantColor,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
            }
        }
    }
}
