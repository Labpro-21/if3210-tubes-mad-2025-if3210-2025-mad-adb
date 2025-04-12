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
import androidx.navigation.NavController
import com.example.adbpurrytify.R
import com.example.adbpurrytify.api.RetrofitClient
import com.example.adbpurrytify.data.AuthRepository
import com.example.adbpurrytify.data.TokenManager
import com.example.adbpurrytify.ui.navigation.Screen
import com.example.adbpurrytify.ui.theme.BLACK_BACKGROUND
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val scale = remember { Animatable(1f) }
    val context = LocalContext.current // Get context for TokenManager initialization

    // State to hold the login status after checking
    // Start as null to indicate the check hasn't completed yet.
    var loginStatusDetermined by remember { mutableStateOf<Boolean?>(null) }

    // Effect to initialize TokenManager and check login status ONCE
    LaunchedEffect(key1 = Unit) {
        TokenManager.initialize(context)
        Log.d("SplashScreen", "Initializing TokenManager and checking token...")
        try {
            val authRepository = AuthRepository(RetrofitClient.instance)

            // First check if we have tokens
            if (TokenManager.hasTokens()) {
                // Try to validate or refresh tokens
                loginStatusDetermined = authRepository.validateAndRefreshTokenIfNeeded()
            } else {
                // No tokens stored, user needs to log in
                loginStatusDetermined = false
            }

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
            scale.animateTo(
                targetValue = 1.3f,
                animationSpec = tween(
                    durationMillis = 500,
                    easing = { OvershootInterpolator(2f).getInterpolation(it) }
                )
            )
            // Delay after animation completes - adjust as needed for UX
            delay(1500L)

            // Navigate based on the determined status
            val destination = if (loginStatusDetermined == true) Screen.Home.route else Screen.Login.route
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
        modifier = Modifier.fillMaxSize().background(BLACK_BACKGROUND)
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_purrytify),
            contentDescription = "Logo",
            modifier = Modifier.scale(scale.value)
        )
    }
}
