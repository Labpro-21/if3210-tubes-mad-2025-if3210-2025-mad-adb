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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.adbpurrytify.data.AuthRepository
import com.example.adbpurrytify.data.model.SongEntity
import com.example.adbpurrytify.ui.components.RecyclerSongsList
import com.example.adbpurrytify.ui.navigation.Screen
import com.example.adbpurrytify.ui.viewmodels.SongViewModel
import java.time.Instant

// Assuming this is your color definition
val BLACK_BACKGROUND = Color(0xFF121212)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreenn(
    navController: NavController,
    viewModel: SongViewModel,
    authRepository: AuthRepository
) {
    var showAddSongSheet by remember { mutableStateOf(false) }

    val backgroundColor = Color(0xFF121212)
    val tabBackgroundColor = Color(0xFF282828)
    val activeTabColor = Color(0xFF1ED760) // Spotify green

    // Observe songs from LiveData
    val allSongs by viewModel.allSongs.observeAsState(emptyList())

    // Track user ID for adding songs
    var currentUserId by remember { mutableStateOf(-1L) }

    // Get current user ID
    LaunchedEffect(key1 = true) {
        val userProfile = authRepository.currentUser()
        // Convert Long ID to String for the DAO
        currentUserId = userProfile?.id ?: -1L
        if (currentUserId != -1L) {
            viewModel.setCurrentUser(currentUserId)
        }
    }

    // Filter songs based on selected tab
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("All", "Liked")

    // When tab changes, update the songs list
    LaunchedEffect(selectedTabIndex, currentUserId) {
        if (currentUserId != -1L) {
            when (selectedTabIndex) {
                0 -> viewModel.loadAllSongs()
                1 -> viewModel.loadLikedSongs(currentUserId)
            }
        }
    }

    // Keep track of currently playing song
    var currentlyPlayingSong by remember { mutableStateOf<SongEntity?>(null) }

    // When songs list changes, update currently playing song if needed
    LaunchedEffect(allSongs) {
        if (allSongs.isNotEmpty() && currentlyPlayingSong == null) {
            currentlyPlayingSong = allSongs[0]
        }
    }

    // For the Add Song bottom sheet
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

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
                    contentDescription = "Add",
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

        // Songs list
        Box(modifier = Modifier.weight(1f)) {
            if (allSongs.isEmpty()) {
                // Show empty state with message based on selected tab
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedTabIndex == 0) "No songs in your library yet" else "No liked songs yet",
                        color = Color.White
                    )
                }
            } else {
                RecyclerSongsList(
                    songs = allSongs,
                    height = 800,
                    showBorder = false,
//                    onSongClick = { song ->
//                        currentlyPlayingSong = song
//                    },
//                    onLikeClick = { song ->
//                        viewModel.toggleLikeSong(song)
//                    }
                )
            }
        }

        // Currently playing bar (only show if we have a song playing)
        currentlyPlayingSong?.let { song ->
            CurrentlyPlayingBar(song = song)
        }
    }

    AddSong(
        show = showAddSongSheet,
        onDismiss = { showAddSongSheet = false }
    )
}


@Composable
fun CurrentlyPlayingBar(song: SongEntity) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF460B41),
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = song.artUri,
                contentDescription = "Now Playing",
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
                    color = Color.White
                )
                Text(
                    text = song.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray
                )
            }

            IconButton(
                onClick = {

                }
            ) {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = "Pause",
                    tint = Color.White
                )
            }
        }
    }
}
