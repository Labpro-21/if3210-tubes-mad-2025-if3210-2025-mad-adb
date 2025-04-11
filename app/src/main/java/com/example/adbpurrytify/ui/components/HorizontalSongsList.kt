package com.example.adbpurrytify.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.adbpurrytify.R
import com.example.adbpurrytify.data.model.SongEntity

@Composable
fun HorizontalSongsListColumn(
    song: SongEntity,
    onSongClick: (SongEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .width(144.dp)
            .padding(all = 8.dp)
            .background(color = MaterialTheme.colorScheme.background)
            .clickable { onSongClick(song) }, // Add clickable modifier with callback
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.remembering_sunday),
            contentDescription = "Album art for ${song.title}",
            modifier = Modifier
                .width(92.dp)
                .padding(bottom = 6.dp)
                .clip(shape = RoundedCornerShape(4.dp))
        )

        var title = song.title.take(10)
        var author = song.author.take(12)
        if (song.title.length > 10) {
            title = title.plus("...")
        }
        if (song.author.length > 12) {
            author = author.plus("...")
        }
        Text(title, fontSize = 12.sp, color = Color.White)
        Text(author, fontSize = 10.sp, color = Color.LightGray)
    }
}

@Composable
fun HorizontalSongsList(
    songs: List<SongEntity>,
    showBorder: Boolean,
    onSongClick: (SongEntity) -> Unit = {} // Add click handler
) {
    LazyRow (modifier = Modifier
        .fillMaxWidth()
        .border(
            width = 4.dp,
            color =
                if (showBorder) Color(255, 0, 0, 255)
                else Color(255, 0, 0, 0)
        )
    ) {
        songs.forEach { song ->
            item {
                HorizontalSongsListColumn(
                    song = song,
                    onSongClick = onSongClick
                )
            }
        }
    }
}
