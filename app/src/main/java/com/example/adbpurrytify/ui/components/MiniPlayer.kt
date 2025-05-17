package com.example.adbpurrytify.ui.components


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.adbpurrytify.R
import com.example.adbpurrytify.api.RetrofitClient
import com.example.adbpurrytify.data.AuthRepository
import com.example.adbpurrytify.data.local.AppDatabase
import com.example.adbpurrytify.data.model.SongEntity
import com.example.adbpurrytify.ui.navigation.Screen
import com.example.adbpurrytify.ui.screens.SongPlayer
import com.example.adbpurrytify.ui.viewmodels.SongViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

@Composable
fun MiniPlayer(
    viewModel: SongViewModel = SongViewModel(
        AppDatabase.getDatabase(LocalContext.current).songDao()
    ),
    authRepository: AuthRepository = remember {
        AuthRepository(RetrofitClient.instance) // Use corrected import
    },
    navController: NavController
) {

    if (!SongPlayer.songLoaded) return
    var songId by remember { mutableStateOf(SongPlayer.curLoadedSongId) }
    var sliderPosition by remember { mutableStateOf(SongPlayer.getProgress()) }
    var isPlaying by remember { mutableStateOf(SongPlayer.isPlaying()) }
    var song by remember { mutableStateOf<SongEntity?>(null) }
    var playerReady by remember { mutableStateOf(false) }

    val accentColor = Color(0xFF1ED760)

    val context = LocalContext.current

    LaunchedEffect(songId) {

        val userProfile = authRepository.currentUser()
        val userId = userProfile?.id ?: -1L
        if (userId != -1L) {
            viewModel.setCurrentUser(userId)
        }

        song = runBlocking { viewModel.getSongById(songId) }
        song?.let {

            if (SongPlayer.songLoaded == false
                or ((SongPlayer.songLoaded) and (SongPlayer.curLoadedSongId != songId))) {

                SongPlayer.release()
                SongPlayer.loadSong(it.audioUri, context, it.id)
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
            // Commented out because it's adding noise to logcat
//            Log.d("sliderPosition", sliderPosition.toString())
            delay(1000L)
        }
    }

    // UI
    // Song info dan art
    song?.let { currentSong ->

        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 24.dp)
                .clickable(true, onClick = {
                    navController.navigate("${Screen.Player.route}/${songId}")
                })
        ) {

            Row {

                Box( // img
                    modifier = Modifier
                        .width(64.dp)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp)),
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

                // Song Title + Author
                Column(modifier = Modifier.padding(all = 4.dp)) {
                    Text(currentSong.title, color = Color.White, style = MaterialTheme.typography.titleSmall)
                    Text(currentSong.author, color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                }
            }

            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp).clickable(true, onClick = {
                    if (isPlaying) {
                        SongPlayer.pause()
                    } else {
                        SongPlayer.play()
                    }
                    isPlaying = !isPlaying
                })
            )


        }

        // Slider section
        if (playerReady) {
            LinearProgressIndicator(
                progress = (sliderPosition.toFloat() / SongPlayer.getDuration().toFloat()).coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = Color.LightGray,
                trackColor = Color.DarkGray
            )
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

    } ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = accentColor)
        }
    }


}
