package com.example.adbpurrytify.ui.components
//
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Pause
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.LinearProgressIndicator
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import coil3.compose.AsyncImage
//import com.example.adbpurrytify.data.model.SongEntity
//
//
//@Composable
//fun CurrentlyPlayingBar(songId: Long,
//                        onClick: () -> Unit
//) {
//
//    val backgroundColor = Color(0xFF121212)
//    val accentColor = Color(0xFF1ED760)
//
//    var song by remember { mutableStateOf<SongEntity?>(null) }
//    var isPlaying by remember { mutableStateOf(false) }
//    var sliderPosition by remember { mutableStateOf(0L) }
//
//    val context = LocalContext.current
//
//    Surface(
//        modifier = Modifier.fillMaxWidth().clickable(true, onClick = onClick),
//        color = Color(0xFF460B41), // Consider MaterialTheme colors
//        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
//    ) {
//        Row(
//            modifier = Modifier
//                .padding(12.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            AsyncImage(
//                model = song.artUri,
//                contentDescription = "Album art for ${song.title}", // Better description
//                modifier = Modifier
//                    .size(40.dp)
//                    .clip(RoundedCornerShape(4.dp)),
//                contentScale = ContentScale.Crop
//            )
//
//            Spacer(modifier = Modifier.width(12.dp))
//
//            Column(
//                modifier = Modifier.weight(1f)
//            ) {
//                Text(
//                    text = song.title,
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = Color.White,
//                    maxLines = 1 // Prevent long titles from wrapping too much
//                )
//                Text(
//                    text = song.author,
//                    style = MaterialTheme.typography.bodySmall,
//                    color = Color.LightGray,
//                    maxLines = 1
//                )
//            }
//
//            IconButton(
//                onClick = {
//                    // TODO: Implement Pause/Play toggle logic
//                }
//            ) {
//                Icon(
//                    // TODO: Change icon based on actual playback state
//                    imageVector = Icons.Default.Pause,
//                    contentDescription = "Pause", // Change based on state ("Play")
//                    tint = Color.White
//                )
//            }
//
//            Spacer(modifier = Modifier.height(4.dp))
//
//            // Progress bar
//            LinearProgressIndicator(
//                progress = progress,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(4.dp)
//                    .clip(RoundedCornerShape(2.dp)),
//                color = Color.LightGray,
//                trackColor = Color.DarkGray
//            )
//        }
//    }
//}