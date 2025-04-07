package com.example.adbpurrytify.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.adbpurrytify.data.model.Song
import com.example.adbpurrytify.ui.components.RecyclerSongsList
import com.example.adbpurrytify.ui.theme.ADBPurrytifyTheme
import com.example.adbpurrytify.ui.theme.DarkGrey
import com.example.adbpurrytify.ui.theme.Green


@Composable
fun ToggleAllLiked(
    isLikesOnly: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(modifier = Modifier.padding(8.dp)) {
        Button(
            onClick = { onToggle(false) }, // TODO: change song list contents based on "all" or "liked"
            colors = ButtonDefaults.buttonColors(
                containerColor = if (!isLikesOnly) Green else DarkGrey
            ),
            modifier = Modifier.weight(1f)
        ) {
            Text("All")
        }

        Spacer(modifier = Modifier.width(8.dp))

        Button(
            onClick = { onToggle(true) }, // TODO: change song list contents based on "all" or "liked"
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isLikesOnly) Green else DarkGrey
            ),
            modifier = Modifier.weight(1f)
        ) {
            Text("Liked")
        }
    }
}


@Composable
fun LibraryScreen() {
    Column {

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp).padding(top = 36.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Your Library", modifier = Modifier.padding(start = 12.dp))

            // Add song button
            IconButton(onClick = {
                // TODO: tambah add song behavior
            }) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = "Add sign",
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        // ini masih placeholder
        var song1 = Song(1, "Remembering Sunday", "All Time Low", "file:///sdcard/Download/remembering_sunday.jpeg", "")
        var song2 = Song(2, "Gold Steps", "Neck Deep", "", "")
        var song3 = Song(3, "Re:make", "ONE OK ROCK", "", "")
        var songs = mutableListOf<Song>()
        for (i in 1..5) {
            songs.add(song1)
            songs.add(song2)
            songs.add(song3)
        }

        var onlyLikedSongs by remember { mutableStateOf(false) }
        ToggleAllLiked(isLikesOnly = onlyLikedSongs) {
            onlyLikedSongs = it
        }

        RecyclerSongsList(songs, height = 800, showBorder = false)
    }
}

@Preview
@Composable
fun PreviewLibraryScreen() {
    ADBPurrytifyTheme {
        LibraryScreen()
    }
}