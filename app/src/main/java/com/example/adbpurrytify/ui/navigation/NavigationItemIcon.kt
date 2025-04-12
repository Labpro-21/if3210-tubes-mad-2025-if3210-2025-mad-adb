package com.example.adbpurrytify.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource

@Composable
fun NavigationItemIcon(item: NavigationItem, isSelected: Boolean) {
    val iconColor = if (isSelected) Color.White else Color.Gray

    Image(
        painter = painterResource(id = item.iconRes),
        contentDescription = item.title,
        colorFilter = ColorFilter.tint(iconColor)
    )
}
