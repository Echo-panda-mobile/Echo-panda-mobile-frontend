package com.example.echo_panda_mobile.data.model

// ─── Dashboard Domain Models ───────────────────────────────────────────────────

data class DashboardStats(
    val monthlyRevenue: Double = 12450.0,
    val revenueGrowth: Double = 18.0,  // percentage
    val streams: String = "2.5M",
    val streamsGrowth: Double = 12.0,
    val listeners: String = "185K",
    val listenersGrowth: Double = 8.0,
    val publishedSongs: Int = 24
)

data class TopTrack(
    val id: String = "",
    val title: String = "Summer Nights",
    val streams: String = "456.2K",
    val ranking: String = "3rd most streamed",
    val imageUrl: String? = null
)

data class ActivityItem(
    val id: String = "",
    val text: String = "",
    val timestamp: Long = 0
)

data class ArtistDashboardData(
    val user: User,
    val stats: DashboardStats,
    val topTrack: TopTrack,
    val recentActivities: List<ActivityItem>
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
        publishedSongs = 24
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
        recentActivities = getMockActivities()
    )
}
