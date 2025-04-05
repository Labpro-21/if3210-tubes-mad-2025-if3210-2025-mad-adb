package com.example.adbpurrytify.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.adbpurrytify.R
import com.example.adbpurrytify.data.model.Song
import com.example.adbpurrytify.ui.theme.ADBPurrytifyTheme

@Composable
fun HorizontalSongsListColumn(song: Song) {
    Column(
        modifier = Modifier
            .width(144.dp)
            .padding(all = 8.dp)
            .background(color = MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.remembering_sunday),
            contentDescription = "Sample image art",
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
        Text(title, fontSize = 12.sp)
        Text(author, fontSize = 10.sp)
    }
}

@Composable
fun HorizontalSongsList(songs: List<Song>, showBorder: Boolean) {
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
                HorizontalSongsListColumn(song)
            }
        }
    }
}

@Composable
fun HorizontalTestSongsList() {
    var song1 = Song(1, "Remembering Sunday", "All Time Low", "drawable/remembering_sunday.jpeg")
    var song2 = Song(2, "Gold Steps", "Neck Deep", "drawable/remembering_sunday.jpeg")
    var song3 = Song(3, "Re:make", "ONE OK ROCK", "drawable/remembering_sunday.jpeg")
    var songs = mutableListOf<Song>()
    for (i in 1..5) {
        songs.add(song1)
        songs.add(song2)
        songs.add(song3)
    }

    ADBPurrytifyTheme {
        HorizontalSongsList(songs, true)
    }
}