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
import com.example.adbpurrytify.api.RetrofitClient // Corrected import
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
        AuthRepository(RetrofitClient.instance) // Use corrected import
    }
) {
    // Get database and create ViewModel
    val context = LocalContext.current
    val songDao = AppDatabase.getDatabase(context).songDao()
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory(songDao))

    val backgroundColor = Color(0xFF121212)
    val primaryColor = Color(0xFF1ED760) // Define primary color

    // Observe data from ViewModel
    // Default the loading states to true initially to show the loader first
    val newSongs by viewModel.newSongs.observeAsState(emptyList())
    val recentlyPlayed by viewModel.recentlyPlayed.observeAsState(emptyList())
    val isNewSongsLoading by viewModel.isNewSongsLoading.observeAsState(true)
    val isRecentlyPlayedLoading by viewModel.isRecentlyPlayedLoading.observeAsState(true)
    val currentlyPlayingSong by viewModel.currentlyPlayingSong.observeAsState()

    // Get current user ID and load data
    LaunchedEffect(key1 = Unit) {
        val userProfile = authRepository.currentUser()
        val userId = userProfile?.id ?: -1L
        if (userId != -1L) {
            viewModel.setCurrentUser(userId)
            // Data loading is triggered within setCurrentUser or subsequent calls in ViewModel
        }
        // Consider adding error handling or a state for when userId is -1L
    }

    // Determine the overall loading state
    val isLoading = isNewSongsLoading || isRecentlyPlayedLoading

    // Show a full-screen loader if isLoading is true
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = primaryColor)
        }
    } else {
        // Show the main content once loading is false
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f) // Takes up available space, pushing the player bar down
                    .fillMaxWidth()
                    .padding(top = 16.dp) // Add padding at the top of the scrollable content
            ) {
                // New Songs Section
                Text(
                    text = "New songs",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp, top = 16.dp)
                )


                if (isNewSongsLoading) { // This will likely be false here due to the outer check
                    Box(
                        modifier = Modifier
                            .height(170.dp) // Adjust height as needed
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = primaryColor)
                    }
                } else {
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

                if (isRecentlyPlayedLoading) { // This will likely be false here
                    Box(
                        modifier = Modifier
                            // Use appropriate sizing for this loader if needed
                            .fillMaxWidth()
                            .padding(vertical = 50.dp), // Example padding
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = primaryColor)
                    }
                } else {
                    // Ensure RecyclerSongsList doesn't take infinite height if inside a Column
                    // The height parameter you passed (400.dp) is good.
                    RecyclerSongsList(
                        songs = recentlyPlayed,
                        height = 400, // Keep explicit height or use weight modifier if needed
                        showBorder = false,
                        onSongClick = { song ->
                            navController?.navigate("${Screen.Player.route}/${song.id}")
                        }
                    )
                }
            } // End of scrollable content Column

            // Currently playing bar (stays at the bottom)
            currentlyPlayingSong?.let { song ->
                CurrentlyPlayingBar(
                    song = song,
                    onClick = {
                        navController?.navigate("${Screen.Player.route}/${song.id}")
                    }
                )
            }
        } // End of main content Column
    } // End of if/else
}