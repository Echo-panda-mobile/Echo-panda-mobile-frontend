package com.example.echo_panda_mobile.presentation.navigation

object Routes {
    // Auth
    const val LOGIN             = "login"
    const val SIGNUP            = "signup"
    const val FORGOT_PASSWORD   = "forgot_password"
    const val VERIFY_EMAIL      = "verify_email"

    // Intro
    const val INTRO = "intro"

    // ── Graph routes (use these for startDestination and navigate()) ──────────
    const val USER_GRAPH   = "user_graph"
    const val ARTIST_GRAPH = "artist_graph"
    const val ADMIN_GRAPH  = "admin_graph"

    // ── User destinations (inside user_graph) ─────────────────────────────────
    const val USER_HOME         = "user/home"
    const val USER_DISCOVER     = "user/discover"
    const val USER_ALBUMS       = "user/albums"
    const val USER_ALBUM_DETAIL = "user/album_detail/{albumId}"
    const val USER_PLAYER       = "user/player/{trackId}"
    const val USER_LIBRARY      = "user/library"
    const val USER_FAVORITES    = "user/favorites"
    const val USER_PROFILE      = "user/profile"
    const val USER_SETTINGS     = "user/settings"

    // ── Artist destinations (inside artist_graph) ─────────────────────────────
    const val ARTIST_DASHBOARD = "artist/dashboard"
    const val ARTIST_MY_MUSIC  = "artist/my_music"
    const val ARTIST_UPLOAD    = "artist/upload"
    const val ARTIST_ANALYTICS = "artist/analytics"
    const val ARTIST_PROFILE   = "artist/profile"
    const val ARTIST_VIEW      = "artist/view/{artistId}"

    // ── Admin destinations (inside admin_graph) ───────────────────────────────
    const val ADMIN_DASHBOARD          = "admin/dashboard"
    const val ADMIN_USER_MANAGEMENT    = "admin/users"
    const val ADMIN_CONTENT_MODERATION = "admin/content"

    /**
     * Returns the GRAPH route for the role — safe to use as NavHost startDestination
     * and as the target of navController.navigate().
     */
    fun getHomeRoute(role: String?) = when (role?.uppercase()) {
        "ARTIST" -> ARTIST_GRAPH
        "ADMIN"  -> ADMIN_GRAPH
        else     -> USER_GRAPH
    }

    // Bottom nav index → route mapping (user destinations, not graph routes)
    fun bottomNavRoute(index: Int) = when (index) {
        0    -> USER_HOME
        1    -> USER_DISCOVER
        2    -> USER_ALBUMS
        3    -> USER_LIBRARY
        else -> USER_HOME
    }

    fun bottomNavIndex(route: String?) = when {
        route == null                          -> 0
        route.startsWith(USER_HOME)            -> 0
        route.startsWith(USER_DISCOVER)        -> 1
        route.startsWith(USER_ALBUMS)          -> 2
        route.startsWith("user/album_detail")  -> 2
        route.startsWith(USER_LIBRARY)         -> 3
        route.startsWith(USER_FAVORITES)       -> 3
        route.startsWith("artist/view")        -> 1
        else                                   -> 0
    }
}