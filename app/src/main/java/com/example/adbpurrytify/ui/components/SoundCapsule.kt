package com.example.adbpurrytify.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.adbpurrytify.R
import com.example.adbpurrytify.data.model.SoundCapsule
import com.example.adbpurrytify.data.model.DayStreak
import com.example.adbpurrytify.ui.navigation.Screen
import com.example.adbpurrytify.ui.theme.SpotifyGreen
import com.example.adbpurrytify.ui.theme.SpotifyLightBlack
import com.example.adbpurrytify.ui.viewmodels.AnalyticsViewModel
import com.example.adbpurrytify.ui.viewmodels.ProfileViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter

@Composable
fun MonthSelector(
    months: List<String>, // List of MM-YYYY strings
    selectedMonth: String, // MM-YYYY format
    onMonthSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Reverse the order to show newest first
    val reversedMonths = months.reversed()

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(reversedMonths) { month ->
            MonthBadge(
                month = formatMonthForDisplay(month), // Convert MM-YYYY to display format
                isSelected = month == selectedMonth,
                onClick = { onMonthSelected(month) }
            )
        }
    }
}

// Helper function to convert MM-YYYY to display format
private fun formatMonthForDisplay(monthYear: String): String {
    return try {
        val parts = monthYear.split("-")
        if (parts.size == 2) {
            val month = parts[0].toInt()
            val year = parts[1]
            val monthNames = listOf(
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
            )
            "${monthNames[month - 1]} $year"
        } else {
            monthYear
        }
    } catch (e: Exception) {
        monthYear
    }
}

@Composable
fun MonthBadge(
    month: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clickable { onClick() }
            .padding(4.dp),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) SpotifyGreen else Color.Transparent,
        border = BorderStroke(1.dp, if (isSelected) SpotifyGreen else Color.Gray)
    ) {
        Text(
            text = month,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (isSelected) Color.Black else Color.White,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun TimeListenedSection(
    timeListened: Int,
    month: String, // MM-YYYY format
    navController: NavHostController,
    isRealTime: Boolean = false,
    modifier: Modifier = Modifier,
    analyticsViewModel: AnalyticsViewModel = hiltViewModel()
) {
    // Real-time listening stats - this will update automatically
    val realTimeStats by analyticsViewModel.getRealTimeStats(month).collectAsState(initial = null)

    val displayTime = if (isRealTime) {
        // Always use real-time data for current month, even if starting from 0
        realTimeStats?.let { (it.totalListeningTime / 60000).toInt() } ?: timeListened
    } else {
        timeListened
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("${Screen.TimeListened.route}/$month")
            },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Time listened",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    if (isRealTime && realTimeStats?.isCurrentlyListening == true) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(SpotifyGreen, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "LIVE",
                            color = SpotifyGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$displayTime minutes",
                    color = if (isRealTime && realTimeStats?.isCurrentlyListening == true) SpotifyGreen else SpotifyGreen,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Icon(
                painter = painterResource(R.drawable.ic_arrow_forward),
                contentDescription = "View details",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun TopItemCard(
    title: String,
    itemName: String,
    imageUrl: String,
    month: String, // MM-YYYY format
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable {
            val route = if (title.contains("Artist", ignoreCase = true)) {
                "${Screen.TopArtists.route}/$month"
            } else {
                "${Screen.TopSongs.route}/$month"
            }
            navController.navigate(route)
        },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                Icon(
                    painter = painterResource(R.drawable.ic_arrow_forward),
                    contentDescription = "View details",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = itemName,
                color = if (title.contains("Artist", ignoreCase = true)) Color(0xFF4A9EFF) else Color(0xFFFFD700),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(if (title.contains("Artist", ignoreCase = true)) CircleShape else RoundedCornerShape(8.dp))
                    .background(Color.Gray)
            ) {
                // Better artwork handling with proper fallbacks
                if (imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = itemName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.song_art_placeholder),
                        error = painterResource(R.drawable.song_art_placeholder)
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.song_art_placeholder),
                        contentDescription = itemName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundCapsuleCard(
    soundCapsule: SoundCapsule,
    navController: NavHostController,
    onDownloadClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    analyticsViewModel: AnalyticsViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel() // Add ProfileViewModel for export functionality
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // State for download modal
    var showFormatDialog by remember { mutableStateOf(false) }
    var isDownloadAction by remember { mutableStateOf(true) }
    val exportState by profileViewModel.exportState.collectAsState()

    // Check if this is the current month for real-time updates
    val isCurrentMonth = remember(soundCapsule.month) {
        val currentMonth = java.text.SimpleDateFormat("MM-yyyy", java.util.Locale.getDefault())
            .format(java.util.Date())
        soundCapsule.month == currentMonth
    }

    // Real-time stats for current month
    val realTimeStats by analyticsViewModel.getRealTimeStats(soundCapsule.month).collectAsState(initial = null)

    // Check if we have any real-time data that should override "no data" state
    val hasRealTimeData = isCurrentMonth && realTimeStats?.totalListeningTime?.let { it > 0 } == true
    val shouldShowData = soundCapsule.hasData || hasRealTimeData

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header with title and icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Sound Capsule",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = {
                            isDownloadAction = true
                            showFormatDialog = true
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download CSV/PDF",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            isDownloadAction = false
                            showFormatDialog = true
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Text(
                text = soundCapsule.displayMonth,
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (!shouldShowData) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎼",
                            fontSize = 72.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Text(
                            text = "No listening data yet",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )

                        if (isCurrentMonth) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Start playing music to see your personalized analytics!",
                                color = Color(0xFFB3B3B3),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    }
                }
            } else {
                // Time listened section with real-time updates for current month
                TimeListenedSection(
                    timeListened = soundCapsule.timeListened,
                    month = soundCapsule.month,
                    navController = navController,
                    isRealTime = isCurrentMonth,
                    analyticsViewModel = analyticsViewModel
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Top artist and top song - show real-time data if available
                val displayTopArtist = soundCapsule.topArtist
                val displayTopSong = soundCapsule.topSong

                if (displayTopArtist != null || displayTopSong != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        displayTopArtist?.let { artist ->
                            TopItemCard(
                                title = "Top Artist",
                                itemName = artist.name,
                                imageUrl = artist.imageUrl,
                                month = soundCapsule.month,
                                navController = navController,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        displayTopSong?.let { song ->
                            TopItemCard(
                                title = "Top Song",
                                itemName = song.title,
                                imageUrl = song.imageUrl,
                                month = soundCapsule.month,
                                navController = navController,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Day streak section
                soundCapsule.dayStreak?.let { streak ->
                    DayStreakCard(
                        dayStreak = streak
                    )
                }
            }

            // Export state feedback
            when (exportState) {
                is ProfileViewModel.ExportState.Loading -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color(0xFF1DB954)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Processing...",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }

                is ProfileViewModel.ExportState.Success -> {
                    LaunchedEffect(exportState) {
                        kotlinx.coroutines.delay(2000)
                        profileViewModel.clearExportState()
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "✓ ${(exportState as ProfileViewModel.ExportState.Success).fileName} saved successfully!",
                        color = Color(0xFF1DB954),
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                is ProfileViewModel.ExportState.Error -> {
                    LaunchedEffect(exportState) {
                        kotlinx.coroutines.delay(3000)
                        profileViewModel.clearExportState()
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "✗ Error: ${(exportState as ProfileViewModel.ExportState.Error).message}",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                ProfileViewModel.ExportState.Idle -> {
                    // Do nothing
                }
            }
        }
    }

    // Format selection dialog
    if (showFormatDialog) {
        AlertDialog(
            onDismissRequest = { showFormatDialog = false },
            title = {
                Text(
                    text = if (isDownloadAction) "Choose Download Format" else "Choose Share Format",
                    color = Color.White
                )
            },
            text = {
                Column {
                    Text(
                        text = "Select the format for your Sound Capsule:",
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            showFormatDialog = false
                            if (isDownloadAction) {
                                profileViewModel.exportSoundCapsuleWithPermission(
                                    context,
                                    soundCapsule.month,
                                    ProfileViewModel.ExportFormat.CSV
                                )
                            } else {
                                profileViewModel.shareSoundCapsuleWithPermission(
                                    context,
                                    soundCapsule.month,
                                    ProfileViewModel.ExportFormat.CSV
                                )
                            }
                        }
                    ) {
                        Text("CSV", color = Color(0xFF1DB954))
                    }

                    TextButton(
                        onClick = {
                            showFormatDialog = false
                            if (isDownloadAction) {
                                profileViewModel.exportSoundCapsuleWithPermission(
                                    context,
                                    soundCapsule.month,
                                    ProfileViewModel.ExportFormat.PDF
                                )
                            } else {
                                profileViewModel.shareSoundCapsuleWithPermission(
                                    context,
                                    soundCapsule.month,
                                    ProfileViewModel.ExportFormat.PDF
                                )
                            }
                        }
                    ) {
                        Text("PDF", color = Color(0xFF1DB954))
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showFormatDialog = false }
                ) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }
}

@Composable
fun DayStreakCard(
    dayStreak: DayStreak,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Large album art image with better artwork handling
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray)
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
                    Image(
                        painter = painterResource(R.drawable.song_art_placeholder),
                        contentDescription = dayStreak.songTitle,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Streak text
            Text(
                text = "You had a ${dayStreak.streakDays}-day streak",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "You played ${dayStreak.songTitle} by ${dayStreak.artist} day after day. You were on fire.",
                color = Color.Gray,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Date range and share icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dayStreak.dateRange,
                    color = Color.Gray,
                    fontSize = 12.sp
                )

                IconButton(
                    onClick = { /* Share action */ },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}