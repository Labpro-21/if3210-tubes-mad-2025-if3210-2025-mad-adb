// ui/screens/ProfileScreen.kt
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil3.compose.SubcomposeAsyncImage
import com.example.adbpurrytify.R
import com.example.adbpurrytify.data.TokenManager
import com.example.adbpurrytify.data.model.User
import com.example.adbpurrytify.data.model.UserStats
import com.example.adbpurrytify.data.model.SoundCapsule
import com.example.adbpurrytify.ui.components.MiniPlayer
import com.example.adbpurrytify.ui.components.MonthSelector
import com.example.adbpurrytify.ui.components.SoundCapsuleCard
import com.example.adbpurrytify.ui.navigation.Screen
import com.example.adbpurrytify.ui.screens.SongPlayer
import com.example.adbpurrytify.ui.theme.ADBPurrytifyTheme
import com.example.adbpurrytify.ui.theme.SpotifyGreen
import com.example.adbpurrytify.ui.theme.SpotifyLightBlack
import com.example.adbpurrytify.ui.viewmodels.ProfileViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()

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
                        soundCapsules = state.soundCapsules,
                        selectedMonth = selectedMonth,
                        onMonthSelected = viewModel::selectMonth,
                        navController = navController,
                        viewModel = viewModel
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
private fun ProfileContent(
    user: User,
    stats: UserStats,
    soundCapsules: Map<String, SoundCapsule>,
    selectedMonth: String,
    onMonthSelected: (String) -> Unit,
    navController: NavHostController,
    viewModel: ProfileViewModel
) {
    val padding = 8.dp
    val backgroundColor = Color(0xFF121212)

    // For debugging - print the image URL to Logcat
    val imageUrl = if (!user.image.isBlank()) {
        "http://34.101.226.132:3000/uploads/profile-picture/${user.image}"
    } else null

    // Log the URL for debugging
    Log.d("ProfileScreen", "Image URL: $imageUrl")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Profile header section
            Column(
                modifier = Modifier.padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.padding(padding * 2))

                // Profile image
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                ) {
                    if (user.image.isBlank()) {
                        Image(
                            painter = painterResource(R.drawable.remembering_sunday),
                            contentDescription = "Default Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        SubcomposeAsyncImage(
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
                                Log.e("Error", "${it}")
                                Image(
                                    painter = painterResource(R.drawable.navbar_home),
                                    contentDescription = "Error Loading Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.padding(padding * 1/2f))
                Text(text = user.userName, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.padding(padding * 1/8f))
                Text(text = user.location, color = Color.Gray)
                Spacer(modifier = Modifier.padding(padding * 1/2f))

                // Action buttons row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                ) {
                    OutlinedButton(
                        onClick = { navController.navigate("edit_profile") },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = SpotifyGreen,
                            containerColor = Color.Transparent
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("EDIT PROFILE")
                    }

                    Button(
                        onClick = { logout(navController) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("LOGOUT")
                    }
                }

                Spacer(modifier = Modifier.padding(padding * 1 / 2))

                // Stats row
                Row {
                    Column(
                        modifier = Modifier
                            .clickable(true, onClick = {})
                            .fillMaxWidth(1 / 3f)
                            .height(70.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("${stats.songCount}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Songs", color = Color.Gray)
                    }
                    Column(
                        modifier = Modifier
                            .clickable(true, onClick = {})
                            .fillMaxWidth(1 / 2f)
                            .height(70.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("${stats.likedCount}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Liked", color = Color.Gray)
                    }
                    Column(
                        modifier = Modifier
                            .clickable(true, onClick = {})
                            .fillMaxWidth(1f)
                            .height(70.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("${stats.listenedCount}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Listened", color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Sound Capsule section
            Column {
                // Month selector
                MonthSelector(
                    months = soundCapsules.keys.toList(),
                    selectedMonth = selectedMonth,
                    onMonthSelected = onMonthSelected
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Sound capsule card with integrated download/share functionality
                soundCapsules[selectedMonth]?.let { capsule ->
                    SoundCapsuleCard(
                        soundCapsule = capsule,
                        navController = navController,
                        profileViewModel = viewModel  // Pass the ProfileViewModel
                    )
                }
            }

            // Add bottom padding for the mini player (same as HomeScreen)
            Spacer(modifier = Modifier.height(80.dp))
        }

        // MiniPlayer stuck at bottom (same pattern as HomeScreen)
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
    val navController = rememberNavController()

    ADBPurrytifyTheme {
        Surface {
            // Preview with mock data would go here
        }
    }
}