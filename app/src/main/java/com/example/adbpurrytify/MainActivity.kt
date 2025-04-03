package com.example.adbpurrytify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.adbpurrytify.ui.navigation.AppNavigation
import com.example.adbpurrytify.ui.theme.ADBPurrytifyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ADBPurrytifyTheme {
                AppNavigation()
            }
        }
    }
}





