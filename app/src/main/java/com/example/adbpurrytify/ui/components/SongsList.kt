package com.example.adbpurrytify.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.adbpurrytify.R
import com.example.adbpurrytify.data.model.Song
import com.example.adbpurrytify.ui.theme.ADBPurrytifyTheme

@Composable
fun SongsListRow(song: Song) {
    Row(
        modifier = Modifier
            .padding(all = 8.dp)
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        Image(
            painter = painterResource(id = R.drawable.remembering_sunday),
            contentDescription = "Sample image art",
            modifier = Modifier
                .width(64.dp)
                .clip(shape = RoundedCornerShape(4.dp))
        )
        Column(modifier = Modifier.padding(all = 8.dp)) {
            Text(song.title, fontSize = 16.sp)
            Text(song.author, fontSize = 12.sp)
        }
    }
}

@Composable
fun SongsList(songs: List<Song>) {
    LazyColumn {
        songs.forEach { song ->
            item {
                SongsListRow(song)
            }
        }
    }
}

@Preview
@Composable
fun TestSongsList() {
    var song1 = Song("Remembering Sunday", "All Time Low", "drawable/remembering_sunday.jpeg")
    var song2 = Song("Gold Steps", "Neck Deep", "drawable/remembering_sunday.jpeg")
    var song3 = Song("Re:make", "ONE OK ROCK", "drawable/remembering_sunday.jpeg")

    var songs = listOf(song1, song2, song3)
    ADBPurrytifyTheme {
        SongsList(songs)
    }
}