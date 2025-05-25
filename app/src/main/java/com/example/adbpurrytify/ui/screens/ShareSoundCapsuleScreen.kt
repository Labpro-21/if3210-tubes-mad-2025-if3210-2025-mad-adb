// ShareSoundCapsuleScreen.kt
package com.example.adbpurrytify.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
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
import com.example.adbpurrytify.data.model.SoundCapsule
import com.example.adbpurrytify.ui.theme.SpotifyGreen
import com.example.adbpurrytify.ui.viewmodels.ShareSoundCapsuleViewModel
import com.example.adbpurrytify.ui.viewmodels.SharePlatform

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareSoundCapsuleScreen(
    navController: NavController,
    month: String,
    viewModel: ShareSoundCapsuleViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val soundCapsule by viewModel.soundCapsule.collectAsState()

    // Load real data when screen opens
    LaunchedEffect(month) {
        viewModel.loadAndGenerateImage(context, month)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Share Sound Capsule",
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
            // Sound Capsule Preview Card
            soundCapsule?.let { capsule ->
                SoundCapsulePreviewCard(soundCapsule = capsule)
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Share section
            when (val state = uiState) {
                is ShareSoundCapsuleViewModel.ShareUiState.Loading -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = SpotifyGreen,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Creating your beautiful Sound Capsule...",
                            color = Color.Gray,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                is ShareSoundCapsuleViewModel.ShareUiState.Success -> {
                    EnhancedShareSection(
                        onPlatformShare = { platform ->
                            viewModel.shareUniversal(context, state.imageUri, platform)
                        },
                        onGeneralShare = {
                            viewModel.shareUniversal(context, state.imageUri, null)
                        }
                    )
                }

                is ShareSoundCapsuleViewModel.ShareUiState.Error -> {
                    ErrorShareSection(
                        message = state.message,
                        onRetry = { viewModel.loadAndGenerateImage(context, month) }
                    )
                }

                ShareSoundCapsuleViewModel.ShareUiState.Idle -> {
                    // Loading will be triggered by LaunchedEffect
                }
            }
        }
    }
}

@Composable
fun SoundCapsulePreviewCard(
    soundCapsule: SoundCapsule,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.8f),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background with gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                SpotifyGreen.copy(alpha = 0.3f),
                                Color(0xFF1A1A1A),
                                Color(0xFF121212)
                            )
                        )
                    )
            )

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
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = soundCapsule.displayMonth,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }

                // Main content
                Column {
                    Text(
                        text = "My ${soundCapsule.displayMonth.split(" ")[0]}",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sound Capsule",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFFFD700), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "★",
                                color = Color.Black,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Stats preview
                    if (soundCapsule.hasData) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Top Artist",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = soundCapsule.topArtist?.name ?: "No data",
                                    color = Color(0xFF4A9EFF),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Column {
                                Text(
                                    text = "Top Song",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = soundCapsule.topSong?.title ?: "No data",
                                    color = Color(0xFFFFD700),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "${soundCapsule.timeListened} minutes",
                            color = SpotifyGreen,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Time listened",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    } else {
                        Text(
                            text = "No listening data yet",
                            color = Color.Gray,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedShareSection(
    onPlatformShare: (SharePlatform) -> Unit,
    onGeneralShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Share your Sound Capsule",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Choose where to share your musical journey",
            color = Color.Gray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Platform icons row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(SharePlatform.values()) { platform ->
                SharePlatformButton(
                    platform = platform,
                    onClick = { onPlatformShare(platform) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // General share button
        Button(
            onClick = onGeneralShare,
            colors = ButtonDefaults.buttonColors(
                containerColor = SpotifyGreen
            ),
            shape = RoundedCornerShape(50.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "More sharing options",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SharePlatformButton(
    platform: SharePlatform,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    color = getPlatformColor(platform),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(getPlatformIcon(platform)),
                contentDescription = platform.displayName,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = platform.displayName,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ErrorShareSection(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "😔",
            fontSize = 48.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Oops! Something went wrong",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            color = Color.Gray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen),
            shape = RoundedCornerShape(50.dp)
        ) {
            Text(
                text = "Try Again",
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun getPlatformColor(platform: SharePlatform): Color {
    return when (platform) {
        SharePlatform.LINE -> Color(0xFF00C300)
        SharePlatform.INSTAGRAM -> Color(0xFFE4405F)
        SharePlatform.TWITTER -> Color(0xFF1DA1F2)
        SharePlatform.WHATSAPP -> Color(0xFF25D366)
    }
}

private fun getPlatformIcon(platform: SharePlatform): Int {
    return when (platform) {
        SharePlatform.LINE -> R.drawable.ic_line
        SharePlatform.INSTAGRAM -> R.drawable.ic_instagram
        SharePlatform.TWITTER -> R.drawable.ic_twitter
        SharePlatform.WHATSAPP -> R.drawable.ic_whatsapp
    }
}