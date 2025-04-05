package com.example.adbpurrytify.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.adbpurrytify.data.model.Song
import com.example.adbpurrytify.ui.components.HorizontalSongsList
import com.example.adbpurrytify.ui.components.SongsList
import com.example.adbpurrytify.ui.theme.ADBPurrytifyTheme

@Preview
@Composable
fun HomePage() {

    ADBPurrytifyTheme {
        Column {

            Row {
                Text("New Songs", modifier = Modifier.padding(start = 12.dp, top = 36.dp))
            }

            // ini masih placeholder
            var song1 = Song(id=1,"Remembering Sunday", "All Time Low", "drawable/remembering_sunday.jpeg")
            var song2 = Song(id=2,"Gold Steps", "Neck Deep", "drawable/remembering_sunday.jpeg")
            var song3 = Song(id=3,"Re:make", "ONE OK ROCK", "drawable/remembering_sunday.jpeg")
            var songs = mutableListOf<Song>()
            for (i in 1..5) {
                songs.add(song1)
                songs.add(song2)
                songs.add(song3)
            }

            HorizontalSongsList(songs, showBorder = false)

            Row {
                Text("Recently Played", modifier = Modifier.padding(start = 12.dp, top = 36.dp))
            }

            SongsList(songs, height = 800, showBorder = false)
        }
    }
}