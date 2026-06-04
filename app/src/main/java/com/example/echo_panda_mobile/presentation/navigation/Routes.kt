package com.example.echo_panda_mobile.presentation.navigation

object Routes {
    // Auth
    const val LOGIN             = "login"
    const val SIGNUP            = "signup"
    const val VERIFY_EMAIL      = "verify_email"
    const val FORGOT_PASSWORD   = "forgot_password"

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
    const val USER_PLAYLIST_DETAIL = "user/playlist_detail/{playlistId}"
    const val USER_PLAYER       = "user/player/{trackId}"
    const val USER_LIBRARY      = "user/library"
    const val USER_FAVORITES    = "user/favorites"
    const val USER_PROFILE      = "user/profile"
    const val USER_SETTINGS     = "user/settings"
    const val USER_ALL_ARTISTS  = "user/all_artists"
    const val USER_ALL_SONGS    = "user/all_songs/{type}?title={title}"

    // ── Artist destinations (inside artist_graph) ─────────────────────────────
    const val ARTIST_DASHBOARD = "artist/home"
    const val ARTIST_MY_MUSIC  = "artist/my_music"
    const val ARTIST_UPLOAD    = "artist/upload"
    const val ARTIST_ANALYTICS = "artist/analytics"
    const val ARTIST_PROFILE   = "artist/profile"
    const val ARTIST_VIEW      = "artist/view/{artistId}"
    const val ARTIST_NOTIFICATIONS = "artist/notifications"
    const val ARTIST_ALBUMS        = "artist/albums"
    const val ARTIST_COMMENTS      = "artist/comments"
    const val ARTIST_CREATE_ALBUM  = "artist/create_album"
    const val ARTIST_PREFERENCES   = "artist/preferences"
    const val ARTIST_HELP_SUPPORT  = "artist/help_support"
    const val ARTIST_SECURITY      = "artist/security"
    const val ARTIST_EDIT_PROFILE  = "artist/edit_profile"

    // ── Admin destinations (inside admin_graph) ───────────────────────────────
    const val ADMIN_DASHBOARD          = "admin/home"
    const val ADMIN_USER_MANAGEMENT    = "admin/users"
    const val ADMIN_ADD_ARTIST         = "admin/add_artist"
    const val ADMIN_USER_DETAIL        = "admin/user_detail/{userId}/{role}"
    const val ADMIN_SONG_DETAIL        = "admin/song_detail/{songId}"
    const val ADMIN_ALBUM_DETAIL       = "admin/album_detail/{albumId}"
    const val ADMIN_CONTENT_MODERATION = "admin/content"
    const val ADMIN_MUSIC             = "admin/music"
    const val ADMIN_LIBRARY           = "admin/library"
    const val ADMIN_TAG_DETAIL         = "admin/tag_detail/{tagId}"
    const val ADMIN_TAG_ALBUMS         = "admin/tag_albums/{tagId}"
    const val ADMIN_CATEGORY_DETAIL    = "admin/category_detail/{categoryId}"
    const val ADMIN_CATEGORY_ALBUMS    = "admin/category_albums/{categoryId}"
    const val ADMIN_PROFILE           = "admin/profile"

    /**
     * Returns the GRAPH route for the role — safe to use as NavHost startDestination
     * and as the target of navController.navigate().
     */
    fun getHomeRoute(role: String?): String {
        val cleanRole = role?.trim()?.uppercase() ?: "USER"
        android.util.Log.d("ROLE_CHECK", "Determining Home for role: '$cleanRole'")
        
        return when (cleanRole) {
            "ADMIN"            -> ADMIN_GRAPH
            "ARTIST", "PUBLISHER" -> ARTIST_GRAPH
            else               -> USER_GRAPH
        }
    }

    /**
     * Resolves a backend-provided route (e.g. "/", "/admin/dashboard") 
     * into a valid Compose Navigation route.
     */
    fun resolveRoute(destination: String?, role: String?): String {
        val cleanRole = role?.trim()?.uppercase() ?: "USER"
        val fallback = getHomeRoute(cleanRole)

        if (destination.isNullOrBlank()) return fallback
        
        val trimmed = destination.trim()
        val sanitized = trimmed.removePrefix("/")
        
        android.util.Log.d("NAVIGATION", "Resolving route: '$destination' for role: $cleanRole")

        // SECURE ROLE CHECK: Prevent non-admins from going to admin routes
        if (sanitized.startsWith("admin") && cleanRole != "ADMIN") {
            android.util.Log.w("NAVIGATION", "Security Breach Attempt: $cleanRole tried to access admin route. Redirecting to $fallback")
            return fallback
        }

        return when {
            // Admin mappings
            sanitized == "admin/dashboard" || sanitized == "admin/home" || sanitized == "admin" -> ADMIN_GRAPH
            
            // Artist mappings
            sanitized == "artist/dashboard" || sanitized == "artist/home" || sanitized == "artist" -> ARTIST_GRAPH
            
            // User mappings
            sanitized == "user/home" || sanitized == "home" || sanitized == "" || sanitized == "/" -> USER_GRAPH
            
            // Direct matches for graphs
            sanitized == USER_GRAPH || sanitized == ARTIST_GRAPH || sanitized == ADMIN_GRAPH -> sanitized
            
            // Allow sub-routes
            sanitized.startsWith("user/") || sanitized.startsWith("admin/") || sanitized.startsWith("artist/") -> sanitized

            else -> fallback
        }
    }

    fun isAdminRole(role: String?) = role?.uppercase() == "ADMIN"
    fun isArtistRole(role: String?) = role?.uppercase() == "ARTIST"

    // Bottom nav index → route mapping (user destinations, not graph routes)
    fun bottomNavRoute(index: Int) = when (index) {
        0    -> USER_HOME
        1    -> USER_DISCOVER
        2    -> USER_ALBUMS
        3    -> USER_LIBRARY
        else -> USER_HOME
    }

    fun adminBottomNavRoute(index: Int) = when (index) {
        0    -> ADMIN_DASHBOARD
        1    -> ADMIN_USER_MANAGEMENT
        2    -> ADMIN_MUSIC
        3    -> ADMIN_LIBRARY
        else -> ADMIN_DASHBOARD
    }

    fun bottomNavIndex(route: String?) = when {
        route == null                          -> 0
        route.startsWith(USER_HOME)            -> 0
        route.startsWith(USER_DISCOVER)        -> 1
        route.startsWith("user/all_songs")   -> 1
        route.startsWith(USER_ALBUMS)          -> 2
        route.startsWith("user/album_detail")  -> 2
        route.startsWith(USER_LIBRARY)         -> 3
        route.startsWith(USER_FAVORITES)       -> 3
        route.startsWith("artist/view")        -> 1
        // Admin routes
        route.startsWith(ADMIN_DASHBOARD)       -> 0
        route.startsWith(ADMIN_USER_MANAGEMENT) -> 1
        route.startsWith(ADMIN_MUSIC)           -> 2
        route.startsWith(ADMIN_LIBRARY)         -> 3
        else                                   -> 0
    }
}