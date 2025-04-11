package com.example.adbpurrytify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.adbpurrytify.data.AuthRepository
import com.example.adbpurrytify.data.local.AppDatabase
import com.example.adbpurrytify.ui.components.HorizontalSongsList
import com.example.adbpurrytify.ui.components.RecyclerSongsList
import com.example.adbpurrytify.ui.navigation.Screen
import com.example.adbpurrytify.ui.viewmodels.HomeViewModel

@Composable
fun HomeScreen(
    navController: NavController? = null,
    authRepository: AuthRepository = remember {
        AuthRepository(com.example.adbpurrytify.api.RetrofitClient.instance)
    }
) {
    // Get database and create ViewModel
    val context = LocalContext.current
    val songDao = AppDatabase.getDatabase(context).songDao()
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory(songDao))

    val backgroundColor = Color(0xFF121212)

    // Observe data from ViewModel
    val newSongs by viewModel.newSongs.observeAsState(emptyList())
    val recentlyPlayed by viewModel.recentlyPlayed.observeAsState(emptyList())
    val isNewSongsLoading by viewModel.isNewSongsLoading.observeAsState(false)
    val isRecentlyPlayedLoading by viewModel.isRecentlyPlayedLoading.observeAsState(false)
    val currentlyPlayingSong by viewModel.currentlyPlayingSong.observeAsState()

    // Get current user ID and load data
    LaunchedEffect(key1 = Unit) {
        val userProfile = authRepository.currentUser()
        val userId = userProfile?.id ?: -1L
        if (userId != -1L) {
            viewModel.setCurrentUser(userId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            // New Songs Section
            Text(
                text = "New songs",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp, top = 16.dp)
            )

            if (isNewSongsLoading) {
                Box(
                    modifier = Modifier
                        .height(170.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF1ED760))
                }
            } else {
                // Add click handler to navigate to player
                HorizontalSongsList(
                    songs = newSongs,
                    showBorder = false,
                    onSongClick = { song ->
                        navController?.navigate("${Screen.Player.route}/${song.id}")
                    }
                )
            }

            // Recently Played Section
            Text(
                text = "Recently played",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
            )

            if (isRecentlyPlayedLoading) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF1ED760))
                }
            } else {
                // Add click handler to na
                // vigate to player
                RecyclerSongsList(
                    songs = recentlyPlayed,
                    height = 400,
                    showBorder = false,
                    onSongClick = { song ->
                        navController?.navigate("${Screen.Player.route}/${song.id}")
                    }
                )
            }
        }

        // Currently playing bar with navigation to player
        currentlyPlayingSong?.let { song ->
            CurrentlyPlayingBar(
                song = song,
                onClick = {
                    navController?.navigate("${Screen.Player.route}/${song.id}")
                }
            )
        }
    }
}
