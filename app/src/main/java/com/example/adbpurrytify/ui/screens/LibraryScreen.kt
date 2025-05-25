package com.example.adbpurrytify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.adbpurrytify.ui.components.MiniPlayer
import com.example.adbpurrytify.ui.components.RecyclerSongsList
import com.example.adbpurrytify.ui.navigation.Screen
import com.example.adbpurrytify.ui.theme.SpotifyGreen
import com.example.adbpurrytify.ui.theme.SpotifyLightBlack
import com.example.adbpurrytify.ui.viewmodels.SongViewModel

@Composable
fun LibraryScreen(
    navController: NavController,
    viewModel: SongViewModel
) {
    var showAddSongSheet by remember { mutableStateOf(false) }

    val backgroundColor = Color(0xFF121212)
    val activeTabColor = Color(0xFF1ED760) // Spotify green

    // Observe songs and loading state from LiveData
    val allSongs by viewModel.allSongs.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)

    // Load user data
    LaunchedEffect(Unit) {
        viewModel.loadUserData()
    }

    // Filter songs based on selected tab
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("All", "Liked", "Downloaded")

    // When tab changes, update the songs list
    LaunchedEffect(selectedTabIndex) {
        viewModel.loadSongsForTab(selectedTabIndex)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
    ) {
        // Fixed header content that won't scroll
        Column(
            modifier = Modifier.background(backgroundColor) // Ensure background consistency
        ) {
            // Top bar with title and add button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Library",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = { showAddSongSheet = true },
                    modifier = Modifier
                        .size(40.dp)
                        .background(SpotifyGreen, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Song",
                        tint = Color.Black
                    )
                }
            }

            // Tabs
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp) // Add bottom padding
            ) {
                tabs.forEachIndexed { index, tab ->
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (selectedTabIndex == index) SpotifyGreen
                                else SpotifyLightBlack
                            )
                            .clickable { selectedTabIndex = index }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = tab,
                            color = if (selectedTabIndex == index) Color.Black else Color.White,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Scrollable content area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(backgroundColor), // Ensure consistent background
            contentAlignment = if (isLoading || allSongs.isEmpty()) Alignment.Center else Alignment.TopStart
        ) {
            when {
                // 1. Show Loading Indicator
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = activeTabColor
                    )
                }
                // 2. Show Empty State (only if not loading)
                allSongs.isEmpty() -> {
                    Text(
                        text = if (selectedTabIndex == 0) "Add songs to your library"
                        else if (selectedTabIndex == 1) "Like songs to see them here"
                        else "Download online songs to see them here",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                // 3. Show Songs List (only if not loading and not empty)
                else -> {
                    RecyclerSongsList(
                        songs = allSongs,
                        showBorder = false,
                        onSongClick = { song ->
                            navController.navigate("${Screen.Player.route}/${song.id}")
                        }
                    )
                }
            }
        }

        // Fixed bottom mini player
        MiniPlayer(navController = navController)
    }

    // Conditionally show the bottom sheet
    if (showAddSongSheet) {
        AddSong(
            show = showAddSongSheet,
            onDismiss = { showAddSongSheet = false }
        )
    }
}
