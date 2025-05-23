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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.adbpurrytify.R
import com.example.adbpurrytify.ui.components.HorizontalSongsList
import com.example.adbpurrytify.ui.components.MiniPlayer
import com.example.adbpurrytify.ui.components.RecyclerSongsList
import com.example.adbpurrytify.ui.navigation.Screen
import com.example.adbpurrytify.ui.viewmodels.HomeViewModel

@Composable
fun HomeScreen(
    navController: NavController? = null,
    viewModel: HomeViewModel
) {
    val backgroundColor = Color(0xFF121212)
    val primaryColor = Color(0xFF1ED760)

    // Observe data from ViewModel
    val newSongs by viewModel.newSongs.observeAsState(emptyList())
    val recentlyPlayed by viewModel.recentlyPlayed.observeAsState(emptyList())
    val isNewSongsLoading by viewModel.isNewSongsLoading.observeAsState(true)
    val isRecentlyPlayedLoading by viewModel.isRecentlyPlayedLoading.observeAsState(true)

    // Get current user ID and load data
    LaunchedEffect(key1 = Unit) {
        viewModel.loadUserData()
    }

    val isLoading = isNewSongsLoading || isRecentlyPlayedLoading

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
                // Charts Section
                ChartsSection(navController)

                Spacer(modifier = Modifier.height(20.dp)) // Reduced from 32.dp

                // New Songs Section
                Text(
                    text = "New songs",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = Color.White,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp) // Reduced bottom padding
                )

                if (isNewSongsLoading) {
                    Box(
                        modifier = Modifier
                            .height(180.dp) // Reduced height
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
                    modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp) // Reduced spacing
                )

                if (isRecentlyPlayedLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp), // Reduced padding
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = primaryColor)
                    }
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
fun ChartsSection(navController: NavController?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Charts",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = Color.White
            )

            // Yellow circle with "B" (like in Figma)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFFFFD700), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "B",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top 50 Global Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp)
                    .clickable { /* Navigate to global charts */ },
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.top_50_global),
                        contentDescription = "Top 50 Global",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Optional: Add a gradient overlay for better text readability
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.3f)
                                    )
                                )
                            )
                    )

                }
            }

            // Top 10 Your Country Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp)
                    .clickable { /* Navigate to country charts */ },
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.top_10_yourcountry),
                        contentDescription = "Top 10 Your Country",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Optional: Add a gradient overlay for better text readability
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.3f)
                                    )
                                )
                            )
                    )


                }
            }
        }
    }
}
