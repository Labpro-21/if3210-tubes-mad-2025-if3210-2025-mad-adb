package com.example.adbpurrytify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import com.example.adbpurrytify.ui.viewmodels.SongViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    navController: NavController,
    viewModel: SongViewModel
) {
    var showAddSongSheet by remember { mutableStateOf(false) }

    val backgroundColor = Color(0xFF121212)
    val tabBackgroundColor = Color(0xFF282828)
    val activeTabColor = Color(0xFF1ED760) // Spotify green

    // Observe songs and loading state from LiveData
    val allSongs by viewModel.allSongs.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false) // Observe loading state

    // Load user data
    LaunchedEffect(key1 = Unit) {
        viewModel.loadUserData()
    }

    // Filter songs based on selected tab
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("All", "Liked")

    // When tab changes, update the songs list
    LaunchedEffect(selectedTabIndex) {
        viewModel.loadSongsForTab(selectedTabIndex)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
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
                onClick = {
                    showAddSongSheet = true
                },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Transparent)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Song",
                    tint = Color.White
                )
            }
        }

        // Tabs
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
        ) {
            tabs.forEachIndexed { index, tab ->
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (selectedTabIndex == index) activeTabColor
                            else tabBackgroundColor
                        )
                        .clickable { selectedTabIndex = index }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = tab,
                        color = if (selectedTabIndex == index) Color.Black else Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Content Area: Loading / Empty / List ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(), // Ensure Box takes full width for centering
            contentAlignment = Alignment.Center // Center content by default
        ) {
            when {
                // 1. Show Loading Indicator
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = activeTabColor // Use an accent color
                    )
                }
                // 2. Show Empty State (only if not loading)
                allSongs.isEmpty() -> {
                    Text(
                        text = if (selectedTabIndex == 0) "Add songs to your library" else "Like songs to see them here",
                        color = Color.Gray, // Use a less prominent color
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
        MiniPlayer(navController = navController)
        // --- End Content Area ---
    }


    // Conditionally show the bottom sheet
    if (showAddSongSheet) {
        AddSong(
            show = showAddSongSheet,
            onDismiss = { showAddSongSheet = false }
        )
    }
}