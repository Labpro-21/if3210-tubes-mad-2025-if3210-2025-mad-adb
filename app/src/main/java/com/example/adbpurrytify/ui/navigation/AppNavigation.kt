package com.example.adbpurrytify.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.adbpurrytify.ui.screens.HomeScreen
import com.example.adbpurrytify.ui.screens.LibraryScreen
import com.example.adbpurrytify.ui.screens.LoginScreen
import com.example.adbpurrytify.ui.screens.NetworkSensingSnackbar
import com.example.adbpurrytify.ui.screens.ProfileScreen
import com.example.adbpurrytify.ui.screens.SongPlayerScreen
import com.example.adbpurrytify.ui.screens.SplashScreen
import com.example.adbpurrytify.ui.viewmodels.HomeViewModel
import com.example.adbpurrytify.ui.viewmodels.ProfileViewModel
import com.example.adbpurrytify.ui.viewmodels.SongViewModel

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {

    val navigationItems = listOf(
        NavigationItem.Home,
        NavigationItem.Library,
        NavigationItem.Profile
    )
    val mainRoutes = navigationItems.map { item -> item.route }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in mainRoutes

    // Local context for snackbar
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    navController = navController,
                    items = navigationItems
                )
            }
        }
    ) { paddingValues ->
        NetworkSensingSnackbar(context = context, snackbarHostState = snackbarHostState)
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
                // Using Hilt to get ViewModel
                val homeViewModel = hiltViewModel<HomeViewModel>()
                HomeScreen(navController, homeViewModel)
            }

            composable(Screen.Library.route) {
                // Using Hilt to get ViewModel
                val songViewModel = hiltViewModel<SongViewModel>()
                LibraryScreen(
                    navController = navController,
                    viewModel = songViewModel
                )
            }

            composable(Screen.Profile.route) {
                // Using Hilt to get ViewModel
                val viewModel = hiltViewModel<ProfileViewModel>()
                ProfileScreen(viewModel = viewModel, navController = navController)
            }

            // Add player route
            composable(
                route = "${Screen.Player.route}/{songId}",
                arguments = listOf(
                    navArgument("songId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val songId = backStackEntry.arguments?.getLong("songId") ?: -1L

                // Using Hilt to get ViewModel
                val songViewModel = hiltViewModel<SongViewModel>()
                SongPlayerScreen(
                    navController = navController,
                    songId = songId,
                    viewModel = songViewModel,
                    snackBarHostState = snackbarHostState
                )
            }

            // Handle deep links
            composable(
                route = "song/{songId}",
                deepLinks = listOf(navDeepLink { uriPattern = "myapp://song/{songId}" })
            ) {
                // Placeholder for deep link handling
            }
        }
    }
}