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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.adbpurrytify.data.model.Song
import com.example.adbpurrytify.ui.components.RecyclerSongsList
import com.example.adbpurrytify.ui.viewmodels.LibraryViewModel

@Composable
fun LibraryScreen(
    navController: NavController,
    viewModel: LibraryViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val backgroundColor = Color(0xFF121212)
    val tabBackgroundColor = Color(0xFF282828)
    val activeTabColor = Color(0xFF1ED760) // Spotify green

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("All", "Liked")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
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
                onClick = { /* Add functionality */ },
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

        // Song list
        Box(modifier = Modifier.weight(1f)) {
            RecyclerSongsList(
                songs = viewModel.songs,
                height = 800,  // This will be constrained by the parent Box with weight
                showBorder = false
            )
        }

        // Currently playing bar
        CurrentlyPlayingBar(song = viewModel.currentlyPlayingSong)
    }
}

@Composable
fun SongItem(song: Song) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Album artwork
        AsyncImage(
            model = song.coverUrl,
            contentDescription = "Album Art",
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Song details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            Text(
                text = song.author,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun CurrentlyPlayingBar(song: Song) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF460B41), // Dark red/purple color as in the image
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = song.coverUrl,
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
                onClick = { /* Pause functionality */ }
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
