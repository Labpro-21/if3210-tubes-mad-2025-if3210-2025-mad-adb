package com.example.adbpurrytify.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Recommend
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.adbpurrytify.R
import com.example.adbpurrytify.data.model.SongEntity
import com.example.adbpurrytify.ui.components.HorizontalSongsList
import com.example.adbpurrytify.ui.components.MiniPlayer
import com.example.adbpurrytify.ui.components.RecyclerSongsList
import com.example.adbpurrytify.ui.navigation.Screen
import com.example.adbpurrytify.ui.viewmodels.HomeViewModel
import com.example.adbpurrytify.ui.viewmodels.SongViewModel
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    navController: NavController? = null,
    viewModel: HomeViewModel,
    songViewModel: SongViewModel = hiltViewModel()
) {
    val backgroundColor = Color(0xFF121212)
    val primaryColor = Color(0xFF1ED760)

    val scope = rememberCoroutineScope()

    // Observe data from ViewModel
    val newSongs by viewModel.newSongs.observeAsState(emptyList())
    val recentlyPlayed by viewModel.recentlyPlayed.observeAsState(emptyList())
    val recommendedSongs by viewModel.recommendedSongs.observeAsState(emptyList())
    val isNewSongsLoading by viewModel.isNewSongsLoading.observeAsState(true)
    val isRecentlyPlayedLoading by viewModel.isRecentlyPlayedLoading.observeAsState(true)
    val isRecommendationsLoading by viewModel.isRecommendationsLoading.observeAsState(true)

    // Get current user ID and load data
    LaunchedEffect(key1 = Unit) {
        viewModel.loadUserData()
    }

    val isLoading = isNewSongsLoading || isRecentlyPlayedLoading || isRecommendationsLoading

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
                    .verticalScroll(rememberScrollState())
            ) {

                Spacer(modifier = Modifier.height(20.dp))

                // Recommendations Section
                Text(
                    text = "Recommended for you",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = Color.White,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )

                if (isRecommendationsLoading) {
                    Box(
                        modifier = Modifier
                            .height(180.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = primaryColor)
                    }
                } else if (recommendedSongs.isEmpty()) {
                    EmptyStateCard(
                        icon = Icons.Default.Recommend,
                        title = "No recommendations yet",
                        subtitle = "Listen to more music to get personalized recommendations"
                    )
                } else {
                    HorizontalSongsList(
                        songs = recommendedSongs,
                        showBorder = false,
                        onSongClick = { song ->
                            scope.launch {
                                /** Instead of inserting it and then updating it pointlessly (my own stupidity)
                                 * why not change it's metadata first, and THEN inserting it?
                                 */
                                val updatedSong: SongEntity = song.copy(
                                    // Way cooler than making 2 separate updatedSong
                                    userId = if (songViewModel.getSongById(song.id, viewModel.getUserId()  ?: -1) == null) viewModel.getUserId() ?: song.userId else song.userId,
                                    lastPlayedTimestamp = System.currentTimeMillis(),
                                    lastPlayedPositionMs = 0
                                )
                                songViewModel.insert(updatedSong)
                            }
                            navController?.navigate("${Screen.Player.route}/${song.id}")
                        })
                }

                Spacer(modifier = Modifier.height(20.dp))

                // New Songs Section
                Text(
                    text = "New songs",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = Color.White,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )

                if (isNewSongsLoading) {
                    Box(
                        modifier = Modifier
                            .height(180.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = primaryColor)
                    }
                } else if (newSongs.isEmpty()) {
                    EmptyStateCard(
                        icon = Icons.Default.MusicNote,
                        title = "No new songs available",
                        subtitle = "Check back later for fresh music"
                    )
                } else {
                    HorizontalSongsList(
                        songs = newSongs,
                        showBorder = false,
                        onSongClick = { song ->
                            navController?.navigate("${Screen.Player.route}/${song.id}")
                        }
                    )
                }

                // Trending songs section
                TrendingSongsSection(navController, viewModel)

                // Recently Played Section
                Text(
                    text = "Recently played",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = Color.White,
                    modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp)
                )

                if (isRecentlyPlayedLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = primaryColor)
                    }
                } else if (recentlyPlayed.isEmpty()) {
                    EmptyStateCard(
                        icon = Icons.Default.History,
                        title = "No recently played songs",
                        subtitle = "Start listening to see your music history here"
                    )
                } else {
                    RecyclerSongsList(
                        songs = recentlyPlayed,
                        showBorder = false,
                        onSongClick = { song ->
                            navController?.navigate("${Screen.Player.route}/${song.id}")
                        }
                    )
                }

                // Add some bottom padding for the mini player
                Spacer(modifier = Modifier.height(80.dp))
            }

            MiniPlayer(navController = navController!!)
        }
    }
}

@Composable
fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1ED760).copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        radius = 300f
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF535353),
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp
                    ),
                    color = Color(0xFF9E9E9E),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}
