package com.example.adbpurrytify.ui.screens

import android.util.Log
import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.adbpurrytify.R
import com.example.adbpurrytify.ui.navigation.Screen
import com.example.adbpurrytify.ui.theme.BLACK_BACKGROUND
import com.example.adbpurrytify.ui.viewmodels.AuthViewModel
import com.example.adbpurrytify.worker.JwtExpiryWorker
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
fun SplashScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val scale = remember { Animatable(1f) }
    val context = LocalContext.current

    // State to hold the login status after checking
    var loginStatusDetermined by remember { mutableStateOf<Boolean?>(null) }

    // Effect to check login status ONCE
    LaunchedEffect(key1 = Unit) {
        Log.d("SplashScreen", "Checking authentication status...")

        try {
            // Check if the user is authenticated using AuthViewModel
            val isAuthenticated = authViewModel.checkAuthStatus()
            loginStatusDetermined = isAuthenticated

            Log.d("SplashScreen", "Authentication check complete. Logged in: $loginStatusDetermined")
        } catch (e: Exception) {
            Log.e("SplashScreen", "Error checking login status", e)
            loginStatusDetermined = false
        }
    }

    // Effect for animation and navigation, triggers when loginStatusDetermined changes from null
    LaunchedEffect(key1 = loginStatusDetermined) {
        // Only proceed if the login status has been determined (is not null)
        if (loginStatusDetermined != null) {
            Log.d("SplashScreen", "Login status determined ($loginStatusDetermined). Starting animation and navigation...")

            // Animate the logo
            scale.animateTo(
                targetValue = 1.3f,
                animationSpec = tween(
                    durationMillis = 500,
                    easing = { OvershootInterpolator(2f).getInterpolation(it) }
                )
            )

            // Delay after animation completes
            delay(1500L)

            // Navigate based on the determined status
            val destination = if (loginStatusDetermined == true) {
                // Schedule token refresh worker if logged in
                val workManager = WorkManager.getInstance(context.applicationContext)
                val firstWork = OneTimeWorkRequestBuilder<JwtExpiryWorker>()
                    .setInitialDelay(0, TimeUnit.SECONDS)
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    )
                    .build()

                workManager.enqueue(firstWork)
                Screen.Home.route
            } else {
                Screen.Login.route
            }

            Log.d("SplashScreen", "Navigating to $destination")
            navController.navigate(destination) {
                // Remove Splash screen from the back stack
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        } else {
            Log.d("SplashScreen", "Login status not determined yet. Waiting...")
        }
    }

    // UI for the Splash Screen
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(BLACK_BACKGROUND)
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_purrytify),
            contentDescription = "Logo",
            modifier = Modifier.scale(scale.value)
        )
    }
}