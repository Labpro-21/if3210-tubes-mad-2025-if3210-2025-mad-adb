package com.example.adbpurrytify.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.adbpurrytify.data.model.SongEntity
import com.example.adbpurrytify.ui.components.HorizontalSongsList
import com.example.adbpurrytify.ui.navigation.Screen
import com.example.adbpurrytify.ui.theme.Green
import com.example.adbpurrytify.ui.viewmodels.HomeViewModel
import com.example.adbpurrytify.ui.viewmodels.SongViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun TrendingSongsSection(
    navController: NavController?,
    viewModel: HomeViewModel,
    songViewModel: SongViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()

    val trendingGlobalSongs by viewModel.trendingGlobalSongs.observeAsState()
    val trendingCountrySongs by viewModel.trendingCountrySongs.observeAsState()
    val isTrendingGlobalLoading by viewModel.isTrendingGlobalLoading.observeAsState(initial = false)
    val isTrendingCountryLoading by viewModel.isTrendingCountryLoading.observeAsState(initial = false)

    val userId = viewModel.getUserId()
    val userCountry = viewModel.getUserLocation()

    val supportedCountries = listOf("ID", "MY", "US", "GB", "CH", "DE", "BR")

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        SongsSection(
            "Trending Global",
            null,
            null,
            isTrendingGlobalLoading,
            trendingGlobalSongs,
            scope,
            songViewModel,
            userId,
            navController
        )

        SongsSection(
            "Trending in Your Country",
            userCountry,
            supportedCountries,
            isTrendingCountryLoading,
            trendingCountrySongs,
            scope,
            songViewModel,
            userId,
            navController
        )
    }
}

@Composable
fun SongsSection(
    text: String,
    userCountry: String?,
    supportedCountries: List<String>?,
    isLoading: Boolean,
    songsList: List<SongEntity>?,
    scope: CoroutineScope,
    songViewModel: SongViewModel,
    userId: Long?,
    navController: NavController?
) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
    if ((supportedCountries != null) && !supportedCountries.contains(userCountry)) {
        Box(
            modifier = Modifier
                .height(150.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "Trending songs for your country are not available.",
                color = Color.Gray,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    } else if (isLoading || songsList == null) {
        Box(
            modifier = Modifier
                .height(170.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Green)
        }
    } else {
        HorizontalSongsList(
            songs = songsList, showBorder = false, onSongClick = { song ->
                scope.launch {
                    /** Instead of inserting it and then updating it pointlessly (my own stupidity)
                     * why not change it's metadata first, and THEN inserting it?
                     */
                    val updatedSong: SongEntity = song.copy(
                        // Way cooler than making 2 separate updatedSong
                        userId = if (songViewModel.getSongById(song.id) == null) userId ?: song.userId else song.userId,
                        lastPlayedTimestamp = System.currentTimeMillis(),
                        lastPlayedPositionMs = 0
                    )
                    songViewModel.insert(updatedSong)
                }
                navController?.navigate("${Screen.Player.route}/${song.id}")
            })
    }
}