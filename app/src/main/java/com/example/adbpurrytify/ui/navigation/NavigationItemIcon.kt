package com.example.adbpurrytify.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.Composable

@Composable
fun NavigationItemIcon(item: NavigationItem) {
    Image(
        painter = painterResource(id = item.iconRes),
        contentDescription = item.title
    )
}
