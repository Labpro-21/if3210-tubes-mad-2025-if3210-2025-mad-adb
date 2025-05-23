package com.example.adbpurrytify.ui.theme

import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DarkColorScheme = darkColorScheme(
    primary = Green,
    secondary = LightGrey,
    tertiary = DarkGrey
)

// Add these to your theme.kt file
val SpotifyBlack = Color(0xFF121212)
val SpotifyGreen = Color(0xFF1DB954)
val SpotifyLightBlack = Color(0xFF282828)
val SpotifyGray = Color(0xFF535353)
val SpotifyLightGray = Color(0xFFB3B3B3)

val SpotifyRoundedCornerShape = RoundedCornerShape(8.dp)
val SpotifyCardShape = RoundedCornerShape(4.dp)
val SpotifyButtonShape = RoundedCornerShape(50.dp)


private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun ADBPurrytifyTheme(
    darkTheme: Boolean = true, // dahlah cuk pake darktheme aja :v

//    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {

    val systemUiController = rememberSystemUiController()
    systemUiController.statusBarDarkContentEnabled = false

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SpotifyTypography,
        content = content
    )
}