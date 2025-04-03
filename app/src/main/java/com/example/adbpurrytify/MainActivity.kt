package com.example.adbpurrytify

import android.os.Bundle
import android.view.animation.OvershootInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.adbpurrytify.ui.theme.ADBPurrytifyTheme
import com.example.adbpurrytify.ui.theme.BLACK_BACKGROUND
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ADBPurrytifyTheme {
                Navigation()
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ADBPurrytifyTheme {
        Greeting("Android")
    }
}

@Composable
fun Navigation() {
    val navController = rememberNavController()
    NavHost(navController= navController, startDestination = "splash_screen") {
        composable(route = "splash_screen") {
            SplashScreen(navController = navController)
        }

        composable(route = "main") {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Main screen", color = Color.White)
            }
        }

    }
}

@Composable
fun SplashScreen(navController: NavController) {
    val scale = remember {
        Animatable(1f)
    }

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
        navController.navigate("main")
    }

    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().background(BLACK_BACKGROUND)) {
        Image(
            painter = painterResource(id = R.drawable.logo_purrytify),
            contentDescription = "Logo",
            modifier = Modifier.scale(scale.value)
        )
    }
}

