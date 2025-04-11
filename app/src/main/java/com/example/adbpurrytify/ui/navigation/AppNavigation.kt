package com.example.adbpurrytify.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel // Correct import
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.adbpurrytify.api.RetrofitClient
import com.example.adbpurrytify.data.AuthRepository
import com.example.adbpurrytify.data.local.AppDatabase
import com.example.adbpurrytify.ui.screens.* // Import all screens
import com.example.adbpurrytify.ui.viewmodels.HomeViewModel // Import HomeViewModel
import com.example.adbpurrytify.ui.viewmodels.ProfileViewModel
import com.example.adbpurrytify.ui.viewmodels.ProfileViewModelFactory
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

    // Instantiate dependencies once
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val songDao = database.songDao()
    val authRepository = AuthRepository(RetrofitClient.instance)

    Scaffold(
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
                // Using your existing HomeScreen function signature
                HomeScreen(navController, authRepository)
            }

            composable(Screen.Library.route) {
                val songViewModel: SongViewModel = viewModel(
                    factory = SongViewModel.Factory(songDao)
                )

                LibraryScreen(
                    navController = navController,
                    viewModel = songViewModel,
                    authRepository = authRepository
                )
            }

            composable(Screen.Profile.route) {
                val viewModelFactory = ProfileViewModelFactory(RetrofitClient.instance)
                val viewModel: ProfileViewModel = viewModel(factory = viewModelFactory)

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

                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModel.Factory(songDao)
                )

                SongPlayerScreen(
                    navController = navController,
                    songId = songId,
                    viewModel = homeViewModel
                )
            }
        }
    }
}

