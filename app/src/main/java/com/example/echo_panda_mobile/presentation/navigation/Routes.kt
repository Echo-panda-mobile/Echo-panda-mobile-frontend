package com.example.echo_panda_mobile.presentation.navigation

object Routes {
    // Auth
    const val LOGIN  = "login"
    const val SIGNUP = "signup"
    const val FORGOT_PASSWORD = "forgot_password"
    const val VERIFY_EMAIL = "verify_email"

    // User
    const val USER_HOME     = "user/home"
    const val USER_DISCOVER = "user/discover"
    const val USER_ALBUMS   = "user/albums"
    const val USER_LIBRARY  = "user/library"
    const val USER_PLAYER   = "user/player"
    const val USER_PROFILE  = "user/profile"
    const val USER_SETTINGS = "user/settings"

    // Artist
    const val ARTIST_DASHBOARD = "artist/dashboard"
    const val ARTIST_MY_MUSIC  = "artist/my_music"
    const val ARTIST_UPLOAD    = "artist/upload"
    const val ARTIST_PROFILE   = "artist/profile"

    // Bottom nav index → route mapping
    fun bottomNavRoute(index: Int) = when (index) {
        0 -> USER_HOME
        1 -> USER_DISCOVER
        2 -> USER_ALBUMS
        3 -> USER_LIBRARY
        else -> USER_HOME
    }

    fun bottomNavIndex(route: String?) = when (route) {
        USER_HOME     -> 0
        USER_DISCOVER -> 1
        USER_ALBUMS   -> 2
        USER_LIBRARY  -> 3
        else          -> 0
    }
}