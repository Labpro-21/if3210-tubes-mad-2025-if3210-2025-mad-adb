package com.example.adbpurrytify.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Home : Screen("home")
    object Library : Screen("library")
    object Profile : Screen("profile")
    object Player : Screen("player")
    object EditProfile : Screen("edit_profile")

    // Sound Capsule detail screens
    object TimeListened : Screen("time_listened")
    object TopArtists : Screen("top_artists")
    object TopSongs : Screen("top_songs")

    // Share screens
    object ShareSoundCapsule : Screen("share_sound_capsule")
    object ShareDayStreak : Screen("share_day_streak")
}