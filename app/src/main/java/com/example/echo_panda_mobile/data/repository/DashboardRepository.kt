package com.example.echo_panda_mobile.data.repository

import com.example.echo_panda_mobile.data.model.*
import com.example.echo_panda_mobile.data.model.DashboardStats
import com.example.echo_panda_mobile.data.remote.*
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

// ─── Dashboard Result wrapper ──────────────────────────────────────────────────

sealed class DashboardResult<out T> {
    data class Success<T>(val data: T) : DashboardResult<T>()
    data class Error(val message: String) : DashboardResult<Nothing>()
    object Loading : DashboardResult<Nothing>()
}

class DashboardRepository(
    private val apiService: MusicApiService? = null
) {

    suspend fun getNotifications(useMockData: Boolean = false): DashboardResult<List<NotificationDto>> = try {
        if (useMockData || apiService == null) {
            delay(500)
            DashboardResult.Success(getMockNotifications())
        } else {
            val response = apiService.getNotifications()
            if (response.isSuccessful) {
                DashboardResult.Success(response.body()?.data ?: emptyList())
            } else {
                // Fallback to mock data if API fails during development
                DashboardResult.Success(getMockNotifications())
            }
        }
    } catch (_: Exception) {
        // Fallback to mock data if network error occurs during development
        DashboardResult.Success(getMockNotifications())
    }

    private fun getMockNotifications(): List<NotificationDto> {
        return listOf(
            NotificationDto("1", "New Follower", "Alex Rivers started following you.", "follower", System.currentTimeMillis() - 3600000, false),
            NotificationDto("2", "Song Milestone", "Your track 'Midnight City' reached 10K streams!", "milestone", System.currentTimeMillis() - 86400000, false),
            NotificationDto("3", "New Comment", "Someone commented on your album 'Neon Dreams'.", "comment", System.currentTimeMillis() - 172800000, true),
            NotificationDto("4", "Trending", "You are trending in the 'Synthwave' genre!", "trending", System.currentTimeMillis() - 259200000, true)
        )
    }

    suspend fun createAlbum(request: CreateAlbumRequest): DashboardResult<AlbumDto> = try {
        val response = apiService?.createAlbum(request)
        if (response?.isSuccessful == true && response.body() != null) {
            DashboardResult.Success(response.body()!!.data)
        } else {
            DashboardResult.Error("Failed to create album")
        }
    } catch (e: Exception) {
        DashboardResult.Error(e.message ?: "Network error")
    }

    suspend fun createSong(request: CreateSongRequest): DashboardResult<SongDto> = try {
        val response = apiService?.createSong(request)
        if (response?.isSuccessful == true && response.body() != null) {
            DashboardResult.Success(response.body()!!.data)
        } else {
            DashboardResult.Error("Failed to create song")
        }
    } catch (e: Exception) {
        DashboardResult.Error(e.message ?: "Network error")
    }

    suspend fun uploadMedia(file: File, purpose: String): DashboardResult<UploadResponse> = try {
        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
        val purposePart = purpose.toRequestBody("text/plain".toMediaTypeOrNull())
        
        val response = apiService?.uploadMedia(part, purposePart)
        if (response?.isSuccessful == true && response.body() != null) {
            DashboardResult.Success(response.body()!!)
        } else {
            DashboardResult.Error("Upload failed")
        }
    } catch (e: Exception) {
        DashboardResult.Error(e.message ?: "Network error")
    }

    suspend fun getArtistDashboard(
        user: User,
        useMockData: Boolean = true
    ): DashboardResult<ArtistDashboardData> {
        return try {
            android.util.Log.d("DashboardRepository", "━━━ FETCH DASHBOARD START ━━━")
            android.util.Log.d("DashboardRepository", "User: ${user.name} (ID: ${user.id}, Role: ${user.role})")
            
            if (useMockData || apiService == null) {
                android.util.Log.d("DashboardRepository", "Using mock data or API service is null")
                delay(1000)
                val mockData = DashboardMockData.getMockDashboardData(user)
                DashboardResult.Success(mockData)
            } else {
                android.util.Log.d("DashboardRepository", "Making API call to: artist/analytics")
                val response = apiService.getArtistAnalytics()
                
                android.util.Log.d("DashboardRepository", "Response Code: ${response.code()}")
                
                if (response.isSuccessful && response.body() != null) {
                    val fullBody = response.body()
                    android.util.Log.d("DashboardRepository", "✓ API Response Success: $fullBody")
                    val analytics = fullBody?.data

                    if (analytics == null) {
                        android.util.Log.w("DashboardRepository", "⚠️ Analytics data field is null in response")
                        return DashboardResult.Success(createEmptyDashboard(user))
                    }

                    val dashboardData = ArtistDashboardData(
                        user = user,
                        stats = DashboardStats(
                            monthlyRevenue = analytics.stats.monthlyRevenue,
                            revenueGrowth = analytics.stats.revenueGrowth,
                            streams = analytics.stats.streams,
                            listeners = analytics.stats.listeners,
                            followers = analytics.stats.followers,
                            publishedSongs = analytics.stats.publishedSongs,
                            totalAlbums = analytics.stats.totalAlbums,
                            totalLikes = analytics.stats.totalLikes
                        ),
                        topTrack = TopTrack(
                            id = analytics.topTrack?.id?.toString() ?: "",
                            title = analytics.topTrack?.title ?: "No tracks yet",
                            streams = "0",
                            ranking = if (analytics.topTrack != null) "Top Track" else "Upload your first track",
                            imageUrl = analytics.topTrack?.getDisplayCoverUrl()
                        ),
                        recentActivities = analytics.recentActivities.map {
                            ActivityItem(it.id, it.text, it.timestamp)
                        }.ifEmpty { 
                            listOf(ActivityItem("0", "Welcome! Your activities will appear here.", System.currentTimeMillis()))
                        },
                        globalReach = listOf(
                            "United States" to "0%",
                            "United Kingdom" to "0%",
                            "Germany" to "0%",
                            "Cambodia" to "0%",
                            "Others" to "0%"
                        )
                    )
                    DashboardResult.Success(dashboardData)
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("DashboardRepository", "API Error Body: $errorBody")
                    DashboardResult.Error("Failed to fetch dashboard: ${response.message()}")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("DashboardRepository", "Exception: ${e.message}", e)
            DashboardResult.Error(e.message ?: "Failed to load dashboard")
        }
    }

    private fun createEmptyDashboard(user: User): ArtistDashboardData {
        return ArtistDashboardData(
            user = user,
            stats = DashboardStats(
                monthlyRevenue = 0.0,
                revenueGrowth = 0.0,
                streams = "0",
                listeners = "0",
                followers = "0",
                publishedSongs = 0,
                totalAlbums = 0,
                totalLikes = "0"
            ),
            topTrack = TopTrack(
                title = "No tracks yet",
                streams = "0",
                ranking = "Upload your first track"
            ),
            recentActivities = listOf(
                ActivityItem("0", "Welcome to Echo Panda! Upload your first song to see analytics.", System.currentTimeMillis())
            ),
            globalReach = listOf("N/A" to "0%")
        )
    }

    /**
     * Refresh dashboard data
     * Can be used for pull-to-refresh functionality
     */
    suspend fun refreshDashboard(
        user: User,
        useMockData: Boolean = true
    ): DashboardResult<ArtistDashboardData> {
        // Simulate shorter delay for refresh
        delay(500)
        return getArtistDashboard(user, useMockData)
    }
}
