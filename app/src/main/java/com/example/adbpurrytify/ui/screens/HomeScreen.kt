package com.example.adbpurrytify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.adbpurrytify.api.RetrofitClient
import com.example.adbpurrytify.data.AuthRepository
import com.example.adbpurrytify.data.local.AppDatabase
import com.example.adbpurrytify.ui.components.HorizontalSongsList
import com.example.adbpurrytify.ui.components.MiniPlayer
import com.example.adbpurrytify.ui.components.RecyclerSongsList
import com.example.adbpurrytify.ui.navigation.Screen
import com.example.adbpurrytify.ui.viewmodels.HomeViewModel

@Composable
fun HomeScreen(
    navController: NavController? = null,
    authRepository: AuthRepository = remember {
        AuthRepository(RetrofitClient.instance) // Use corrected import
    }
) {
    // Get database and create ViewModel
    val context = LocalContext.current
    val songDao = AppDatabase.getDatabase(context).songDao()
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory(songDao))

    val backgroundColor = Color(0xFF121212)
    val primaryColor = Color(0xFF1ED760) // Define primary color

    // Observe data from ViewModel
    // Default the loading states to true initially to show the loader first
    val newSongs by viewModel.newSongs.observeAsState(emptyList())
    val recentlyPlayed by viewModel.recentlyPlayed.observeAsState(emptyList())
    val isNewSongsLoading by viewModel.isNewSongsLoading.observeAsState(true)
    val isRecentlyPlayedLoading by viewModel.isRecentlyPlayedLoading.observeAsState(true)

    // Get current user ID and load data
    LaunchedEffect(key1 = Unit) {
        val userProfile = authRepository.currentUser()
        val userId = userProfile?.id ?: -1L
        val userLocation = userProfile?.location ?: ""
        if (userId != -1L) {
            viewModel.setCurrentUser(userId = userId)
            viewModel.setCurrentUser(location = userLocation)
        }
    }


    val isLoading = isNewSongsLoading || isRecentlyPlayedLoading

    // Show a full-screen loader if isLoading is true
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = primaryColor)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(
                    text = "New songs",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp, top = 16.dp)
                )


                if (isNewSongsLoading) {
                    Box(
                        modifier = Modifier
                            .height(170.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = primaryColor)
                    }
                } else {
                    HorizontalSongsList(
                        songs = newSongs,
                        showBorder = false,
                        onSongClick = { song ->
                            navController?.navigate("${Screen.Player.route}/${song.id}")
                        }
                    )
                }

                // Recently Played Section
                Text(
                    text = "Recently played",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
                )

                if (isRecentlyPlayedLoading) { // This will likely be false here
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = primaryColor)
                    }
                } else {
                    RecyclerSongsList(
                        songs = recentlyPlayed,
                        height = 600,
                        showBorder = false,
                        onSongClick = { song ->
                            navController?.navigate("${Screen.Player.route}/${song.id}")
                        }
                    )
                }
            } // End of scrollable content Column

            MiniPlayer(navController = navController!!)

        } // End of main content Column
    } // End of if/else
}