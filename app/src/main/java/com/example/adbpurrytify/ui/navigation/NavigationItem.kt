package com.example.adbpurrytify.ui.navigation

import androidx.annotation.DrawableRes
import com.example.adbpurrytify.R

sealed class NavigationItem(
    val route: String,
    val title: String,
    @DrawableRes val iconRes: Int
) {
    object Home : NavigationItem(
        route = "home",
        title = "Home",
        iconRes = R.drawable.navbar_home
    )

    object Library : NavigationItem(
        route = "library",
        title = "Your Library",
        iconRes = R.drawable.navbar_your_library
    )

    object Profile : NavigationItem(
        route = "profile",
        title = "Profile",
        iconRes = R.drawable.navbar_profile
    )
}



