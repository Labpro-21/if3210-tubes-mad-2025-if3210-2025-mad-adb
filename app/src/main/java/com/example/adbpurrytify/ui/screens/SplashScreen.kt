package com.example.adbpurrytify.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToLogin: () -> Unit) {
    // Auto-navigate to login after delay
    LaunchedEffect(key1 = true) {
        delay(2000) // 2 seconds delay
        onNavigateToLogin()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // Your logo or splash content here
        Text(text = "ADBPurrytify")
    }
}