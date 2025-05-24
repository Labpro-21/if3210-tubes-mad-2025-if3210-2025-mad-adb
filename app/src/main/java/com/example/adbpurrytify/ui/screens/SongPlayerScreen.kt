package com.example.adbpurrytify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardDoubleArrowLeft
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.adbpurrytify.R
import com.example.adbpurrytify.data.download.downloadSong
import com.example.adbpurrytify.data.model.SongEntity
import com.example.adbpurrytify.ui.navigation.Screen
import com.example.adbpurrytify.ui.theme.BLACK_BACKGROUND
import com.example.adbpurrytify.ui.theme.Green
import com.example.adbpurrytify.ui.theme.SpotifyGreen
import com.example.adbpurrytify.ui.theme.SpotifyLightGray
import com.example.adbpurrytify.ui.theme.TEXT_FIELD_TEXT
import com.example.adbpurrytify.ui.viewmodels.SongViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.ContentScale
import androidx.media3.common.Player
import coil3.compose.AsyncImage
import com.example.adbpurrytify.ui.utils.DynamicColorExtractor
import java.lang.Long.max

// Update the SongPlayerScreen composable
@Composable
fun SongPlayerScreen(
    navController: NavController,
    songId: Long,
    snackBarHostState: SnackbarHostState,
    viewModel: SongViewModel = hiltViewModel()
) {
    var firstsong = runBlocking { viewModel.getSongById(songId) }
    var song by remember { mutableStateOf<SongEntity?>(firstsong) }
    var prevSong by remember { mutableStateOf<SongEntity?>(null) }
    var nextSong by remember { mutableStateOf<SongEntity?>(null) }

    var isLiked by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableStateOf(0L) }
    var dominantColor by remember { mutableStateOf(Color(0xFF121212)) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var playerReady by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(song) {
        viewModel.loadUserData()
        while(viewModel.getCurrentUserId() == null) {
            delay(50)
        }
        SongPlayer.curUserId = viewModel.getCurrentUserId()!!

        song?.let {

            prevSong = viewModel.getPrevSong(it.id)
            nextSong = viewModel.getNextSong(it.id)

            // Extract dominant color
            val imageUrl = if (it.artUri.isNotEmpty()) it.artUri else R.drawable.song_art_placeholder
            dominantColor = DynamicColorExtractor.extractDominantColor(
                imageUrl.toString(),
                context,
                Color(0xFF121212)
            )

            viewModel.updateSongTimestamp(it)
            isLiked = it.isLiked

            if (SongPlayer.songLoaded == false
                or ((SongPlayer.songLoaded) and (SongPlayer.curLoadedSongId != it.id))) {

                playerReady = false
                SongPlayer.loadSong(it, context, it.id)

                while (SongPlayer.getDuration() <= 0) {
                    delay(100)
                }
                playerReady = true
                isPlaying = true
            }
            else { // same song
                isPlaying = SongPlayer.isPlaying()
                sliderPosition = SongPlayer.getProgress()
                playerReady = true
            }
        }
    }

    // Update slider position every second
    LaunchedEffect(Unit) {
        while (true) {
            sliderPosition = SongPlayer.getProgress()
            isPlaying = SongPlayer.isPlaying()
            if (!isPlaying) {delay(100L) ; continue}

            delay(500L)

            if (nextSong == null &&
                SongPlayer.mediaController?.playbackState == Player.STATE_ENDED)
            { // ini udh nyampe akhir artinya
                isPlaying = false
            }

            else if (nextSong != null && SongPlayer.curLoadedSongId == nextSong?.id)
            { // detect maju dari listener player sm dari control notif
                // (ini keknya horrible tp idk)

                // go next
                isPlaying = false
                playerReady = false
                song = nextSong
            }

            else if (prevSong != null && SongPlayer.curLoadedSongId == prevSong?.id)
            { // detect mundur dari notif
                isPlaying = false
                playerReady = false
                song = prevSong
            }
        }
    }

    // Background with blurred album art
    Box(modifier = Modifier.fillMaxSize()) {
        // Blurred background
        song?.let { currentSong ->
            AsyncImage(
                model = if (currentSong.artUri.isNotEmpty()) currentSong.artUri else R.drawable.song_art_placeholder,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(50.dp),
                contentScale = ContentScale.Crop,
                alpha = 0.3f
            )
        }

        // Dark overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    DynamicColorExtractor.darkenColor(dominantColor, 0.8f).copy(alpha = 0.9f)
                )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
// Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.navigate(Screen.Home.route) }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Go back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Now Playing",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

// Song info and art
            song?.let { currentSong ->
                // Album Art
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    val art = if (currentSong.artUri.isNotEmpty()) currentSong.artUri else R.drawable.song_art_placeholder
                    AsyncImage(
                        model = art,
                        contentDescription = "Album art",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Song Title + Author and Download + Like Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Download button
                    val isTrendingSong = currentSong.audioUri.startsWith("http")
                    // Change the filename pattern if it's changed on DownloadSong
                    val downloadFile =
                        File(context.filesDir, "${currentSong.author + "-" + currentSong.title}.mp3")
                    if (isTrendingSong && !downloadFile.exists() || isDownloading) {
                        if (isDownloading) {
                            CircularProgressIndicator(
                                progress = { downloadProgress },
                                modifier = Modifier.size(28.dp),
                                color = Green
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${(downloadProgress * 100f).toInt()}%",
                                color = TEXT_FIELD_TEXT,
                                fontSize = 16.sp
                            )
                        } else {
                            IconButton(
                                onClick = {
                                    isDownloading = true
                                    coroutineScope.launch {
                                        val path = downloadSong(
                                            currentSong, context, Dispatchers.IO
                                        ) { progress ->
                                            coroutineScope.launch(Dispatchers.Main) {
                                                downloadProgress = progress
                                            }
                                        }
                                        isDownloading = false
                                        val message = if (path != null) {
                                            val updatedSong = song!!.copy(audioUri = path)
                                            viewModel.update(updatedSong)
                                            "Song download completed!"
                                        } else "Download error. Please try again."
                                        snackBarHostState.showSnackbar(
                                            message, duration = SnackbarDuration.Short
                                        )
                                    }
                                }) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Download song",
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(currentSong.title, color = Color.White, style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(currentSong.author, color = Color.LightGray, style = MaterialTheme.typography.bodyLarge)
                    }

                    // Like Button - Using the separate isLiked state for UI
                    IconButton(
                        onClick = {
                            // Immediately update UI (optimistic)
                            isLiked = !isLiked

                            // Update in background
                            coroutineScope.launch {
                                // Create an updated version of the song with new like status
                                val updatedSong = currentSong.copy(isLiked = isLiked)
                                viewModel.update(updatedSong)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isLiked) "Unlike song" else "Like song",
                            tint = if (isLiked) Green else Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Slider section
                    // Slider
                    Slider(
                        value = sliderPosition.toFloat(),
                        onValueChange = { sliderPosition = it.toLong() },
                        onValueChangeFinished = { SongPlayer.seekTo(sliderPosition) },
                        valueRange = 0f..(max(0L, SongPlayer.getDuration()).toFloat()),
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = SpotifyGreen,
                            activeTrackColor = SpotifyGreen,
                            inactiveTrackColor = SpotifyLightGray.copy(alpha = 0.3f)
                        )
                    )

                if (playerReady) {
                    // Time display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(formatTime(SongPlayer.getProgress()), color = Color.White)
                        Text(formatTime(max(0L, SongPlayer.getDuration())), color = Color.White)
                    }
                } else {
                    // Show loading indicator for slider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Green, modifier = Modifier.size(24.dp))
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Play/Pause and navigation buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (prevSong != null)
                        IconButton(
                            onClick = {
                                // TODO: implement
                                song = prevSong
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .background(Green, shape = RoundedCornerShape(50))
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardDoubleArrowLeft,
                                contentDescription = "Previous song",
                                tint = Color.Black,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    Spacer(modifier = Modifier.width(32.dp))

                    IconButton(
                        onClick = {
                            if (isPlaying) {
                                SongPlayer.pause()
                            } else {
                                SongPlayer.play()
                            }
                            isPlaying = !isPlaying
                        },
                        modifier = Modifier
                            .size(64.dp)
                            .background(SpotifyGreen, CircleShape)
                    ) {

                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.Black, // Black icon on green background for contrast
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(32.dp))

                    if (nextSong != null)
                        IconButton(
                            onClick = {
                                song = nextSong // TODO
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .background(Green, shape = RoundedCornerShape(50))
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardDoubleArrowRight,
                                contentDescription = "Next song",
                                tint = Color.Black,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                }
            } ?: run {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Green)
                }
            }
        }
    }
}


