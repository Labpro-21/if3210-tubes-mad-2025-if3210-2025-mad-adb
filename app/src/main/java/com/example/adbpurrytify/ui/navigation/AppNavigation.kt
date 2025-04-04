package com.example.adbpurrytify.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.adbpurrytify.ui.screens.HomePage
import com.example.adbpurrytify.ui.screens.LoginScreen
import com.example.adbpurrytify.ui.screens.PreviewProfileScreen
import com.example.adbpurrytify.ui.screens.SongPlayer
import com.example.adbpurrytify.ui.screens.SplashScreen

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {

    // Get the main screens that will be shown at the bottom navbar
    val navigationItems = listOf(
        NavigationItem.Home,
        NavigationItem.Library,
        NavigationItem.Profile
    )

    // For each of the navigation item, then get the route
    val mainRoutes = navigationItems.map { item -> item.route }


    // Get current route to determine if we should show bottom navigation
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    /**
     * Should we show the bottom bar?
     *
     * Note from @ganadipa: I think this will be useful for some pages that don't need
     * a navbar, perhaps they only need a back button, what do u think?
     */
    val showBottomBar = currentRoute in mainRoutes

    Scaffold (
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    navController = navController,
                    items = navigationItems
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(navController)
            }

            composable(Screen.Login.route) {
                LoginScreen(navController)
            }

            composable(Screen.Home.route) {
                HomePage()
            }

            composable(Screen.Library.route) {
                // TO DO
                SongPlayer()
            }

            composable(Screen.Profile.route) {
                // TO DO
                PreviewProfileScreen()
            }

        }
    }
}

