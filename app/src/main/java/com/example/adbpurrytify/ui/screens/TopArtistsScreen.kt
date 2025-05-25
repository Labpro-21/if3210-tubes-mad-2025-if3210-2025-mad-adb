package com.example.adbpurrytify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.adbpurrytify.R
import com.example.adbpurrytify.ui.components.MiniPlayer
import com.example.adbpurrytify.ui.theme.SpotifyGreen
import com.example.adbpurrytify.ui.viewmodels.ArtistListeningData
import com.example.adbpurrytify.ui.viewmodels.TopArtistsData
import com.example.adbpurrytify.ui.viewmodels.TopArtistsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopArtistsScreen(
    navController: NavHostController,
    month: String,
    viewModel: TopArtistsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(month) {
        viewModel.loadTopArtistsData(month)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Top artists",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF121212)
            )
        )

        when (val state = uiState) {
            is TopArtistsViewModel.TopArtistsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SpotifyGreen)
                }
            }
            is TopArtistsViewModel.TopArtistsUiState.Success -> {
                TopArtistsContent(
                    data = state.data,
                    modifier = Modifier.weight(1f)
                )
            }
            is TopArtistsViewModel.TopArtistsUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: ${state.message}",
                        color = Color.White
                    )
                }
            }
        }

        MiniPlayer(navController = navController)
    }
}

@Composable
private fun TopArtistsContent(
    data: TopArtistsData,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            // Header
            Text(
                text = data.month,
                color = Color.Gray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "You listened to ${data.totalArtists} artists this month.",
                color = Color.White,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        itemsIndexed(data.artists) { index, artist ->
            EnhancedArtistListItem(artist = artist)
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun EnhancedArtistListItem(
    artist: ArtistListeningData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank
            Text(
                text = String.format("%02d", artist.rank),
                color = Color(0xFF4A9EFF),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(40.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Artist image with better handling
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            ) {
                AsyncImage(
                    model = if (artist.imageUrl.isNotEmpty()) {
                        artist.imageUrl
                    } else {
                        R.drawable.song_art_placeholder
                    },
                    contentDescription = artist.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onError = {
                        // Log error for debugging
                        println("Failed to load artist image: ${artist.imageUrl}")
                    }
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Artist info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = artist.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${artist.songsCount} songs",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            // Minutes listened
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${artist.minutesListened}",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "min",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }
}