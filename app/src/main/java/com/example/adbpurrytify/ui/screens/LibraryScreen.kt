package com.example.adbpurrytify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.adbpurrytify.data.AuthRepository
import com.example.adbpurrytify.data.model.SongEntity
import com.example.adbpurrytify.ui.components.RecyclerSongsList
import com.example.adbpurrytify.ui.viewmodels.SongViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    navController: NavController,
    viewModel: SongViewModel,
    authRepository: AuthRepository
) {
    var showAddSongSheet by remember { mutableStateOf(false) }

    val backgroundColor = Color(0xFF121212)
    val tabBackgroundColor = Color(0xFF282828)
    val activeTabColor = Color(0xFF1ED760) // Spotify green

    // Observe songs and loading state from LiveData
    val allSongs by viewModel.allSongs.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false) // Observe loading state

    // Track user ID for adding songs
    var currentUserId by remember { mutableStateOf(-1L) }

    // Get current user ID
    LaunchedEffect(key1 = Unit) { // Use Unit for one-time effect
        val userProfile = authRepository.currentUser()
        val fetchedUserId = userProfile?.id ?: -1L
        if (fetchedUserId != -1L) {
            // Set the user ID in the ViewModel *once* it's fetched
            viewModel.setCurrentUser(fetchedUserId)
            currentUserId = fetchedUserId // Update local state for LaunchedEffect below
        }
        // If userProfile is null, currentUserId remains -1L, preventing loads
    }

    // Filter songs based on selected tab
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("All", "Liked")

    // When tab changes OR userId becomes valid, update the songs list
    LaunchedEffect(selectedTabIndex, currentUserId) {
        // Only load if we have a valid user ID
        if (currentUserId != -1L) {
            when (selectedTabIndex) {
                0 -> viewModel.loadAllSongs(currentUserId)
                1 -> viewModel.loadLikedSongs(currentUserId)
            }
        } else {
            // If user ID is not valid yet, maybe clear the list or show specific state
            // For now, the loading functions handle the -1L case internally
        }
    }

    // Keep track of currently playing song
    var currentlyPlayingSong by remember { mutableStateOf<SongEntity?>(null) }

    // When songs list changes, update currently playing song if needed
    // This logic might need refinement depending on playback requirements
    LaunchedEffect(allSongs) {
        if (currentlyPlayingSong == null && allSongs.isNotEmpty()) {
            // Maybe only set if nothing was playing before?
            // Or maybe don't auto-select the first song? Depends on desired UX.
            // currentlyPlayingSong = allSongs[0]
        }
        // Handle case where the currently playing song is removed from the list
        if (currentlyPlayingSong != null && !allSongs.contains(currentlyPlayingSong)) {
            currentlyPlayingSong = null // Stop playing if song disappears
        }
    }

    // For the Add Song bottom sheet
    // val sheetState = rememberModalBottomSheetState() // Not used currently
    // val scope = rememberCoroutineScope() // Not used currently

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
                    // Only allow adding if user is valid
                    if (currentUserId != -1L) {
                        showAddSongSheet = true
                    } else {
                        // Optional: Show a message that user needs to be loaded/logged in
                    }
                },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Transparent)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Song", // Improved description
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
                        height = 800, // Consider making this dynamic or using weight
                        showBorder = false,
                        // --- Re-enable these when implementing the actions ---
                        // onSongClick = { song ->
                        //     currentlyPlayingSong = song
                        //     // TODO: Add playback logic
                        // },
                        // onLikeClick = { song ->
                        //     viewModel.toggleLikeSong(song)
                        // }
                        // --- ---
                    )
                }
            }
        }
        // --- End Content Area ---

        // Currently playing bar (only show if we have a song playing)
        currentlyPlayingSong?.let { song ->
            CurrentlyPlayingBar(song = song, onClick = {})
        }
    }

    // Conditionally show the bottom sheet
    if (showAddSongSheet) {
        AddSong(
            show = showAddSongSheet, // Pass the state
            onDismiss = { showAddSongSheet = false }
            // TODO: Pass necessary ViewModel functions or user ID to AddSong
        )
    }
}


@Composable
fun CurrentlyPlayingBar(song: SongEntity,
                        onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(true, onClick = onClick),
        color = Color(0xFF460B41), // Consider MaterialTheme colors
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = song.artUri,
                contentDescription = "Album art for ${song.title}", // Better description
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    maxLines = 1 // Prevent long titles from wrapping too much
                )
                Text(
                    text = song.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray,
                    maxLines = 1
                )
            }

            IconButton(
                onClick = {
                    // TODO: Implement Pause/Play toggle logic
                }
            ) {
                Icon(
                    // TODO: Change icon based on actual playback state
                    imageVector = Icons.Default.Pause,
                    contentDescription = "Pause", // Change based on state ("Play")
                    tint = Color.White
                )
            }
        }
    }
}
