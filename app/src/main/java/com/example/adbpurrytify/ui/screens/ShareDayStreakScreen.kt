package com.example.adbpurrytify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.adbpurrytify.R
import com.example.adbpurrytify.data.model.DayStreak
import com.example.adbpurrytify.ui.theme.SpotifyGreen
import com.example.adbpurrytify.ui.viewmodels.ShareDayStreakViewModel
import com.example.adbpurrytify.ui.viewmodels.SharePlatform

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareDayStreakScreen(
    navController: NavController,
    month: String,
    viewModel: ShareDayStreakViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val dayStreak by viewModel.dayStreak.collectAsState()

    LaunchedEffect(month) {
        viewModel.loadAndGenerateImage(context, month)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Share Day Streak",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF121212)
                )
            )
        },
        containerColor = Color(0xFF121212)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            dayStreak?.let { streak ->
                DayStreakPreviewCard(dayStreak = streak)
                Spacer(modifier = Modifier.height(32.dp))
            }

            when (val state = uiState) {
                is ShareDayStreakViewModel.ShareUiState.Loading -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = SpotifyGreen,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Creating your streak showcase...",
                            color = Color.Gray,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                is ShareDayStreakViewModel.ShareUiState.Success -> {
                    EnhancedShareSection(
                        onPlatformShare = { platform ->
                            viewModel.shareUniversal(context, state.imageUri, platform)
                        },
                        onGeneralShare = {
                            viewModel.shareUniversal(context, state.imageUri, null)
                        }
                    )
                }

                is ShareDayStreakViewModel.ShareUiState.Error -> {
                    ErrorShareSection(
                        message = state.message,
                        onRetry = { viewModel.loadAndGenerateImage(context, month) }
                    )
                }

                ShareDayStreakViewModel.ShareUiState.Idle -> {
                    // Loading will be triggered by LaunchedEffect
                }
            }
        }
    }
}

@Composable
fun DayStreakPreviewCard(
    dayStreak: DayStreak,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.8f),
        colors = CardDefaults.cardColors(containerColor = SpotifyGreen),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🎵",
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Purrytify",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = dayStreak.dateRange,
                    color = Color.Black.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }

            // Main content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "My ${dayStreak.streakDays}-day",
                    color = Color.Black,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "streak 🔥",
                    color = Color.Black,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Album art preview
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.1f))
                ) {
                    if (dayStreak.imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = dayStreak.imageUrl,
                            contentDescription = dayStreak.songTitle,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.song_art_placeholder),
                            error = painterResource(R.drawable.song_art_placeholder)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "♪",
                                fontSize = 48.sp,
                                color = Color.Black.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = dayStreak.artist,
                    color = Color.Black.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = dayStreak.songTitle,
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}