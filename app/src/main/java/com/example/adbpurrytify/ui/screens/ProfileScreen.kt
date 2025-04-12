package com.example.adbpurrytify.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil3.compose.SubcomposeAsyncImage
import com.example.adbpurrytify.R
import com.example.adbpurrytify.data.TokenManager
import com.example.adbpurrytify.data.model.User
import com.example.adbpurrytify.data.model.UserStats
import com.example.adbpurrytify.ui.components.MiniPlayer
import com.example.adbpurrytify.ui.navigation.Screen
import com.example.adbpurrytify.ui.theme.ADBPurrytifyTheme
import com.example.adbpurrytify.ui.viewmodels.MockProfileViewModel
import com.example.adbpurrytify.ui.viewmodels.ProfileViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsState()

    ADBPurrytifyTheme {
        Surface {
            when (val state = uiState) {
                is ProfileViewModel.ProfileUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ProfileViewModel.ProfileUiState.Success -> {
                    ProfileContent(
                        user = state.user,
                        stats = state.stats,
                        navController = navController
                    )
                }
                is ProfileViewModel.ProfileUiState.Error -> {
                    ErrorContent(message = state.message, onRetry = { viewModel.loadProfile() })
                }
            }
        }
    }
}
@Composable
private fun ProfileContent(user: User,
                           stats: UserStats, navController: NavHostController) {
    val padding = 8.dp
    val columnFillHeight = 70.dp

    // For debugging - print the image URL to Logcat
    val imageUrl = if (!user.image.isBlank()) {
        "http://34.101.226.132:3000/uploads/profile-picture/${user.image}"
    } else null

    // Log the URL for debugging
    android.util.Log.d("ProfileScreen", "Image URL: $imageUrl")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.padding(padding * 2))
        Row(modifier = Modifier.padding(all = padding)) {
            // Improved image loading with placeholders and error handling
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
            ) {
                if (user.image.isBlank()) {
                    // If no image URL, just show a default image
                    Image(
                        painter = painterResource(R.drawable.remembering_sunday),
                        contentDescription = "Default Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Try directly with Coil's SubcomposeAsyncImage for better state handling
                    SubcomposeAsyncImage (
                        model = imageUrl,
                        contentDescription = "User Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),

                        loading = {
                            Box(modifier = Modifier.fillMaxSize()) {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        },
                        error = {

                            Log.e("Error", "${it}" )

                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.navbar_home),
                                    contentDescription = "Error Loading Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                // Display a tiny error indicator
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.End)
                                        .background(Color.Red, CircleShape)
                                ) {
                                    Text(
                                        "!",
                                        color = Color.White,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.padding(padding * 1/2f))
        Text(text = user.userName)
        Spacer(modifier = Modifier.padding(padding * 1/8f))
        Text(text = user.location)
        Spacer(modifier = Modifier.padding(padding * 1/2f))

        // Styled logout button
        Button(
            onClick = { logout(navController) },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red,
                contentColor = Color.White
            ),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text("LOGOUT")
        }


        Spacer(modifier = Modifier.padding(padding * 1 / 2))
        Row {
            Column(
                modifier = Modifier
                    .clickable(true, onClick = {})
                    .fillMaxHeight()
                    .fillMaxWidth(1 / 3f)
                    .requiredHeight(columnFillHeight),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("${stats.songCount}")
                Text("Songs")
            }
            Column(
                modifier = Modifier
                    .clickable(true, onClick = {})
                    .fillMaxHeight()
                    .fillMaxWidth(1 / 2f)
                    .requiredHeight(columnFillHeight),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("${stats.likedCount}")
                Text("Liked")
            }
            Column(
                modifier = Modifier
                    .clickable(true, onClick = {})
                    .fillMaxHeight()
                    .fillMaxWidth(1f)
                    .requiredHeight(columnFillHeight),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("${stats.listenedCount}")
                Text("Listened")
            }
        }
        Spacer(modifier = Modifier.padding(vertical = 200.dp))
        MiniPlayer(navController = navController)
    }


}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Error loading profile: $message")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

fun logout(navController: NavHostController) {
    TokenManager.clearTokens()
    navController.navigate(Screen.Login.route) {
        if (SongPlayer.isPlaying()) {
            SongPlayer.stop()
            SongPlayer.release()
        }
        // Clear the back stack so user can't go back to profile after logout
        popUpTo(Screen.Login.route) { inclusive = true }
    }
}

@Preview
@Composable
fun PreviewProfileScreen() {
    val mockViewModel = MockProfileViewModel()
    val navController = rememberNavController()

    ADBPurrytifyTheme {
        Surface {
            val state by mockViewModel.uiState.collectAsState()

            when (val uiState = state) {
                is ProfileViewModel.ProfileUiState.Success -> {
                    ProfileContent(
                        user = uiState.user,
                        stats = uiState.stats,
                        navController = navController
                    )
                }
                else -> {
                    // This won't be called in preview since we're using a mock viewmodel
                }
            }
        }
    }
}
