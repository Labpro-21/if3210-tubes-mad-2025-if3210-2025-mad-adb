package com.example.adbpurrytify.ui.screens

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.example.adbpurrytify.R
import com.example.adbpurrytify.ui.navigation.Screen
import com.example.adbpurrytify.ui.theme.BLACK_BACKGROUND
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val scale = remember {
        Animatable(1f)
    }

    // To do: Implement the isLoggedIn functionality
    val isLoggedIn = true

    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 1.3f,
            animationSpec = tween(
                durationMillis = 500,
                easing = {
                    OvershootInterpolator(2f).getInterpolation(it)
                }
            )
        )
        delay(3000L)
        if (isLoggedIn) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        } else {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().background(
        BLACK_BACKGROUND
    )) {
        Image(
            painter = painterResource(id = R.drawable.logo_purrytify),
            contentDescription = "Logo",
            modifier = Modifier.scale(scale.value)
        )
    }
}
