package com.example.adbpurrytify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.adbpurrytify.data.AuthRepository // Keep if needed directly
import com.example.adbpurrytify.data.model.SongEntity // Use SongEntity
import com.example.adbpurrytify.ui.components.HorizontalSongsList // Assuming this takes List<SongEntity>
import com.example.adbpurrytify.ui.components.RecyclerSongsList // Assuming this takes List<SongEntity>
import com.example.adbpurrytify.ui.viewmodels.HomeViewModel

@Composable
fun HomeScreen(
    navController: NavController, // Keep navController if needed for navigation from home
    viewModel: HomeViewModel
    // authRepository is now injected into the ViewModel, so likely not needed here
) {
    val backgroundColor = Color(0xFF121212)
    val textColor = Color.White
    val accentColor = Color(0xFF1ED760) // Spotify green for loading indicators

    // Observe state from ViewModel
    val newSongs by viewModel.newSongs.observeAsState(emptyList())
    val recentlyPlayedSongs by viewModel.recentlyPlayedSongs.observeAsState(
        emptyList()
    )
    val currentlyPlayingSong by viewModel.currentlyPlayingSong.observeAsState(null)
    val isLoadingNew by viewModel.isLoadingNew.observeAsState(false)
    val isLoadingRecent by viewModel.isLoadingRecent.observeAsState(false)
    val error by viewModel.error.observeAsState(null) // Observe error state

    // TODO: Display error messages appropriately (e.g., Snackbar)
    // error?.let { /* Show Snackbar or error message */ }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // --- New Songs Section ---
        Text(
            text = "New Songs",
            color = textColor,
            fontSize = 20.sp, // Adjust size as needed
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp), // Adjust height for HorizontalSongsList items
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoadingNew -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = accentColor
                    )
                }
                newSongs.isEmpty() && !isLoadingNew -> {
                    Text("No new songs found.", color = Color.Gray)
                }
                else -> {
                    // Assuming HorizontalSongsList takes List<SongEntity>
                    HorizontalSongsList(
                        songs = newSongs,
                        showBorder = false,
//                        onSongClick = { song -> viewModel.playSong(song) }
                        // Add onLikeClick if HorizontalSongsList supports it
                    )
                }
            }
        }

        // --- Recently Played Section ---
        Text(
            text = "Recently Played",
            color = textColor,
            fontSize = 20.sp, // Adjust size as needed
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f) // Takes remaining vertical space
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoadingRecent -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = accentColor
                    )
                }
                recentlyPlayedSongs.isEmpty() && !isLoadingRecent -> {
                    Text("No recently played songs.", color = Color.Gray)
                }
                else -> {
                    // Assuming RecyclerSongsList takes List<SongEntity>
                    // Remove fixed height if RecyclerSongsList can adapt
                    RecyclerSongsList(
                        songs = recentlyPlayedSongs,
                         height = 800, // Remove fixed height if possible
                        showBorder = false,
//                        onSongClick = { song -> viewModel.playSong(song) },
//                        onLikeClick = { song -> viewModel.toggleLikeSong(song) }
                    )
                }
            }
        }

        // --- Currently Playing Bar ---
        // Use the same CurrentlyPlayingBar composable from LibraryScreen
        currentlyPlayingSong?.let { song ->
            CurrentlyPlayingBar(song = song)
            // TODO: Add onClick for play/pause to CurrentlyPlayingBar
            // and connect it to viewModel functions if needed
        }
    }
}



// Remove the @Preview HomePage composable if it's no longer needed
// or update it to use the new HomeScreen structure if desired for previews.
