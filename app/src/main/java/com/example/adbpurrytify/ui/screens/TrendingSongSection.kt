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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.adbpurrytify.ui.components.HorizontalSongsList
import com.example.adbpurrytify.ui.navigation.Screen
import com.example.adbpurrytify.ui.theme.Green
import com.example.adbpurrytify.ui.viewmodels.HomeViewModel

@Composable
fun TrendingSongsSection(
    navController: NavController?,
    viewModel: HomeViewModel
) {
    val trendingGlobalSongs by viewModel.trendingGlobalSongs.observeAsState()
    val trendingCountrySongs by viewModel.trendingCountrySongs.observeAsState()
    val isTrendingGlobalLoading by viewModel.isTrendingGlobalLoading.observeAsState(initial = false)
    val isTrendingCountryLoading by viewModel.isTrendingCountryLoading.observeAsState(initial = false)
    val supportedCountries = listOf("ID", "MY", "US", "GB", "CH", "DE", "BR")
    val userCountry = viewModel.getUserLocation()

//    LaunchedEffect(trendingGlobalSongs) {
//        Log.d("TrendingSongs", "Trending Global Songs updated: $trendingGlobalSongs")
//    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = "Trending Global",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
        )
        if (isTrendingGlobalLoading || trendingGlobalSongs == null) {
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
                songs = trendingGlobalSongs!!,
                showBorder = false,
                onSongClick = { song ->
                    navController?.navigate("${Screen.Player.route}/${song.id}")
                }
            )
        }

        Text(
            text = "Trending in Your Country",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
        )
        if (userCountry !in supportedCountries) {
            Box(
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Trending songs for your country are not available.",
                    color = Color.Gray
                )
            }
        } else if (isTrendingCountryLoading || trendingCountrySongs == null) {
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
                songs = trendingCountrySongs!!,
                showBorder = false,
                onSongClick = { song ->
                    navController?.navigate("${Screen.Player.route}/${song.id}")
                }
            )
        }
    }
}
