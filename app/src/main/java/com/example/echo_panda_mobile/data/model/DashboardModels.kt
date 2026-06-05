package com.example.echo_panda_mobile.data.model

// ─── Dashboard Domain Models ───────────────────────────────────────────────────

data class DashboardStats(
    val monthlyRevenue: Double = 0.0,
    val revenueGrowth: Double = 0.0,
    val streams: String = "0",
    val streamsGrowth: Double = 0.0,
    val listeners: String = "0",
    val listenersGrowth: Double = 0.0,
    val followers: String = "0",
    val followersGrowth: Double = 0.0,
    val publishedSongs: Int = 0,
    val totalAlbums: Int = 0,
    val totalLikes: String = "0"
)

data class TopTrack(
    val id: String = "",
    val title: String = "No track found",
    val streams: String = "0",
    val ranking: String = "-",
    val imageUrl: String? = null
)

data class ActivityItem(
    val id: String = "",
    val text: String = "",
    val timestamp: Long = 0
)

data class TopListenedSong(
    val id: String = "",
    val title: String = "",
    val imageUrl: String? = null,
    val playCount: Int = 0
)

data class ArtistDashboardData(
    val user: User,
    val stats: DashboardStats,
    val topTrack: TopTrack,
    val topListenedSongs: List<TopListenedSong> = emptyList(),
    val recentActivities: List<ActivityItem>,
    val globalReach: List<Pair<String, String>> = listOf(
        "United States" to "45%",
        "United Kingdom" to "18%",
        "Germany" to "12%",
        "Cambodia" to "10%",
        "Others" to "15%"
    )
)

// Mock data for UI testing
object DashboardMockData {
    fun getMockStats() = DashboardStats(
        monthlyRevenue = 12450.0,
        revenueGrowth = 18.0,
        streams = "2.5M",
        streamsGrowth = 12.0,
        listeners = "185K",
        listenersGrowth = 8.0,
        followers = "12.5K",
        followersGrowth = 5.0,
        publishedSongs = 24,
        totalAlbums = 5,
        totalLikes = "85K"
    )

    fun getMockTopTrack() = TopTrack(
        id = "track_1",
        title = "Summer Nights",
        streams = "456.2K",
        ranking = "3rd most streamed"
    )

    fun getMockActivities() = listOf(
        ActivityItem(id = "1", text = "102K new listeners this week"),
        ActivityItem(id = "2", text = "8.5K saves on 'Summer Nights'"),
        ActivityItem(id = "3", text = "Your playlist trending in 5 countries"),
        ActivityItem(id = "4", text = "New fan reached 1K followers")
    )

    fun getMockTopListenedSongs() = listOf(
        TopListenedSong(id = "1", title = "Summer Nights", playCount = 456200),
        TopListenedSong(id = "2", title = "Midnight Drive", playCount = 312800),
        TopListenedSong(id = "3", title = "Neon Dreams", playCount = 245100),
        TopListenedSong(id = "4", title = "City Lights", playCount = 198400),
        TopListenedSong(id = "5", title = "Ocean Wave", playCount = 156700),
        TopListenedSong(id = "6", title = "Starfall", playCount = 124300)
    )

    fun getMockDashboardData(user: User = User(
        id = 1,
        name = "Artist",
        email = "artist@example.com",
        role = "artist",
        token = ""
    )) = ArtistDashboardData(
        user = user,
        stats = getMockStats(),
        topTrack = getMockTopTrack(),
        topListenedSongs = getMockTopListenedSongs(),
        recentActivities = getMockActivities()
    )
}
