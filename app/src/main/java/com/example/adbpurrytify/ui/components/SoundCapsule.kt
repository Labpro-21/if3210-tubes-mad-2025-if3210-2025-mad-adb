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
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.adbpurrytify.R
import com.example.adbpurrytify.data.model.SoundCapsule
import com.example.adbpurrytify.data.model.DayStreak
import com.example.adbpurrytify.ui.navigation.Screen
import com.example.adbpurrytify.ui.theme.SpotifyGreen
import com.example.adbpurrytify.ui.theme.SpotifyLightBlack

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
    modifier: Modifier = Modifier
) {
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
                Text(
                    text = "Time listened",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$timeListened minutes",
                    color = SpotifyGreen,
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
            val route = if (title.contains("artist")) {
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
                color = if (title.contains("artist")) Color(0xFF4A9EFF) else Color(0xFFFFD700),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            ) {
                Image(
                    painter = painterResource(R.drawable.remembering_sunday),
                    contentDescription = itemName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun SoundCapsuleCard(
    soundCapsule: SoundCapsule,
    navController: NavHostController,
    onDownloadClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
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
                        onClick = onDownloadClick,
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
                        onClick = onShareClick,
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
                text = soundCapsule.displayMonth, // Use displayMonth for UI
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (!soundCapsule.hasData) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📊",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No data available",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                // Time listened section with navigation (use month in MM-YYYY format)
                TimeListenedSection(
                    timeListened = soundCapsule.timeListened,
                    month = soundCapsule.month, // MM-YYYY format
                    navController = navController
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Top artist and top song with navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    soundCapsule.topArtist?.let { artist ->
                        TopItemCard(
                            title = "Top artist",
                            itemName = artist.name,
                            imageUrl = artist.imageUrl,
                            month = soundCapsule.month, // MM-YYYY format
                            navController = navController,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    soundCapsule.topSong?.let { song ->
                        TopItemCard(
                            title = "Top song",
                            itemName = song.title,
                            imageUrl = song.imageUrl,
                            month = soundCapsule.month, // MM-YYYY format
                            navController = navController,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                soundCapsule.dayStreak?.let { streak ->
                    DayStreakCard(dayStreak = streak)
                }
            }
        }
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
            // Large album art image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray)
            ) {
                Image(
                    painter = painterResource(R.drawable.remembering_sunday), // Placeholder
                    contentDescription = dayStreak.songTitle,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
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
