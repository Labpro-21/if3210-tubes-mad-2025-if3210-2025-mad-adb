package com.example.adbpurrytify.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.adbpurrytify.R
import com.example.adbpurrytify.data.model.SongEntity

@Composable
fun HorizontalSongsList(
    songs: List<SongEntity>,
    showBorder: Boolean = true,
    onSongClick: (SongEntity) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp) // Spotify-like spacing
    ) {
        items(songs) { song ->
            HorizontalSongItem(
                song = song,
                showBorder = showBorder,
                onSongClick = onSongClick
            )
        }
    }
}

@Composable
fun HorizontalSongItem(
    song: SongEntity,
    showBorder: Boolean,
    onSongClick: (SongEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .width(140.dp) // Bigger width for better appearance
            .clickable { onSongClick(song) }
    ) {
        // Album Art - Made bigger and square
        Box(
            modifier = Modifier
                .size(140.dp) // Square and bigger
                .clip(RoundedCornerShape(8.dp))
        ) {
            val imageModel = if (song.artUri.isNotEmpty()) {
                song.artUri
            } else {
                R.drawable.song_art_placeholder
            }

            AsyncImage(
                model = imageModel,
                contentDescription = "Album art for ${song.title}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.song_art_placeholder),
                error = painterResource(R.drawable.song_art_placeholder)
            )
        }

        Spacer(modifier = Modifier.height(8.dp)) // Reduced spacing

        // Song Title - Improved typography to match vertical list
        Text(
            text = song.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            ),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(2.dp)) // Minimal spacing like vertical list

        // Artist Name - Matching vertical list typography
        Text(
            text = song.author,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            ),
            color = Color(0xFFB3B3B3), // Spotify's gray color
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
