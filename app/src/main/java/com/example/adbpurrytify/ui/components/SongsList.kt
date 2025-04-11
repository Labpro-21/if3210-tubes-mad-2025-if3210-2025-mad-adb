package com.example.adbpurrytify.ui.components

import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.adbpurrytify.R
import com.example.adbpurrytify.data.model.Song
import com.example.adbpurrytify.data.model.SongEntity
import com.example.adbpurrytify.ui.theme.ADBPurrytifyTheme
import com.example.adbpurrytify.ui.theme.BLACK_BACKGROUND

@Composable
fun SongsListRow(song: Song) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
fun SongsList(songs: List<Song>, height: Int, showBorder: Boolean) {
    LazyColumn(modifier = Modifier
        .height(height.dp)
        .border(
            width = 4.dp,
            color =
                if (showBorder) Color(255, 0, 0, 255)
                else Color(255, 0, 0, 0)
        )
    ) {
        songs.forEach { song ->
            item {
                SongsListRow(song)
            }
        }
    }
}

@Composable
fun RecyclerSongsList(
    songs: List<SongEntity>,
    height: Int,
    showBorder: Boolean,
    onSongClick: (SongEntity) -> Unit = {} // Add click handler
) {
    Box(
        modifier = Modifier
            .height(height.dp)
            .clip(RectangleShape)
            .background(BLACK_BACKGROUND)
    ) {
        AndroidView(
            factory = { context ->
                RecyclerView(context).apply {
                    layoutManager = LinearLayoutManager(context)
                    adapter = SongAdapter(songs, context, onSongClick) // Pass click handler to adapter
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        height
                    )
                    if (showBorder) {
                        setBackgroundColor(android.graphics.Color.RED)
                    }
                    setPadding(0, 0, 0, 0)
                    clipToPadding = true
                    clipChildren = true
                }
            },
            modifier = Modifier
                .height(height.dp)
                .fillMaxWidth()
                .background(BLACK_BACKGROUND),
            update = { recyclerView -> recyclerView.adapter = SongAdapter(songs, recyclerView.context) },
        )
    }
}




//@Preview
//@Composable
//fun TestSongsList() {
//
//    var song1 = Song(1, "Remembering Sunday", "All Time Low", "file:///sdcard/Download/remembering_sunday.jpeg", "")
//    var song2 = Song(2, "Gold Steps", "Neck Deep", "", "")
//    var song3 = Song(3, "Re:make", "ONE OK ROCK", "", "")
//    var songs = mutableListOf<Song>()
//    for (i in 1..5) {
//        songs.add(song1)
//        songs.add(song2)
//        songs.add(song3)
//    }
//
//    ADBPurrytifyTheme {
//        RecyclerSongsList(songs, 400, false)
//    }
//}