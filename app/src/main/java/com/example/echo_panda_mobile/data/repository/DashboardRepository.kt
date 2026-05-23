package com.example.echo_panda_mobile.data.repository

import com.example.echo_panda_mobile.data.model.ArtistDashboardData
import com.example.echo_panda_mobile.data.model.DashboardMockData
import com.example.echo_panda_mobile.data.model.User
import kotlinx.coroutines.delay

// ─── Dashboard Result wrapper ──────────────────────────────────────────────────

sealed class DashboardResult<out T> {
    data class Success<T>(val data: T) : DashboardResult<T>()
    data class Error(val message: String) : DashboardResult<Nothing>()
    object Loading : DashboardResult<Nothing>()
}

class DashboardRepository {

    /**
     * Fetches artist dashboard data
     * Currently uses mock data with simulated delay
     * TODO: Replace with actual API call when backend is ready
     *
     * Example API call (when ready):
     * private val apiService: ApiService = ...
     * suspend fun getArtistDashboard(userId: String): DashboardResult<ArtistDashboardData> {
     *     return try {
     *         val response = apiService.getArtistDashboard(userId)
     *         DashboardResult.Success(response)
     *     } catch (e: Exception) {
     *         DashboardResult.Error(e.message ?: "Unknown error")
     *     }
     * }
     */

    suspend fun getArtistDashboard(
        user: User,
        useMockData: Boolean = true  // Toggle for testing
    ): DashboardResult<ArtistDashboardData> {
        return try {
            // Simulate network delay
            delay(1000)

            if (useMockData) {
                // Return mock data for UI testing
                val mockData = DashboardMockData.getMockDashboardData(user)
                DashboardResult.Success(mockData)
            } else {
                // TODO: Call actual API when ready
                // Example: val response = apiService.getArtistDashboard(user.id)
                // For now, fallback to mock
                val mockData = DashboardMockData.getMockDashboardData(user)
                DashboardResult.Success(mockData)
            }
        } catch (e: Exception) {
            DashboardResult.Error(e.message ?: "Failed to load dashboard")
        }
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
