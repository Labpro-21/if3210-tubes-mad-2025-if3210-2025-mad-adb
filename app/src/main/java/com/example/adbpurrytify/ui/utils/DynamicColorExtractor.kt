package com.example.adbpurrytify.ui.utils

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

object DynamicColorExtractor {
    // Simplified version that generates colors based on image path/name
    suspend fun extractDominantColor(
        imageUrl: String,
        context: android.content.Context,
        defaultColor: Color = Color(0xFF1ED760)
    ): Color {
        return try {
            // Generate a consistent color based on the image URL hash
            val hash = imageUrl.hashCode()
            val colors = listOf(
                Color(0xFF1ED760), // Spotify Green
                Color(0xFF1DB954), // Darker Green
                Color(0xFF191414), // Spotify Black
                Color(0xFF535353), // Gray
                Color(0xFFFFFFFF), // White
                Color(0xFF2E7D8A), // Teal
                Color(0xFFE74C3C), // Red
                Color(0xFF9B59B6), // Purple
                Color(0xFFF39C12), // Orange
                Color(0xFF3498DB), // Blue
            )
            colors[kotlin.math.abs(hash) % colors.size]
        } catch (e: Exception) {
            defaultColor
        }
    }

    fun darkenColor(color: Color, factor: Float = 0.3f): Color {
        return Color(
            red = (color.red * (1 - factor)).coerceIn(0f, 1f),
            green = (color.green * (1 - factor)).coerceIn(0f, 1f),
            blue = (color.blue * (1 - factor)).coerceIn(0f, 1f),
            alpha = color.alpha
        )
    }
}
