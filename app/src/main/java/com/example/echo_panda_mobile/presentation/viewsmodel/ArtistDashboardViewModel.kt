package com.example.echo_panda_mobile.presentation.viewsmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.model.ArtistDashboardData
import com.example.echo_panda_mobile.data.model.DashboardMockData
import com.example.echo_panda_mobile.data.model.User
import com.example.echo_panda_mobile.data.remote.RetrofitClient
import com.example.echo_panda_mobile.data.repository.AuthRepository
import com.example.echo_panda_mobile.data.repository.DashboardRepository
import com.example.echo_panda_mobile.data.repository.DashboardResult
import com.example.echo_panda_mobile.data.repository.TokenStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Artist Dashboard Screen
 * Manages loading state, dashboard data, and error handling
 * Supports both mock data (for UI testing) and real API calls
 */
class ArtistDashboardViewModel(
    private val dashboardRepository: DashboardRepository,
    private val authRepository: AuthRepository,
    private val tokenStorage: TokenStorage
) : ViewModel() {

    // ─── State Definitions ─────────────────────────────────────────────────────

    sealed class DashboardUiState {
        object Loading : DashboardUiState()
        data class Success(val data: ArtistDashboardData) : DashboardUiState()
        data class Error(val message: String) : DashboardUiState()
    }

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Use mock data flag (can be changed for testing)
    private val _useMockData = MutableStateFlow(false)
    val useMockData: StateFlow<Boolean> = _useMockData.asStateFlow()

    private val _hasUnreadNotifications = MutableStateFlow(false)
    val hasUnreadNotifications: StateFlow<Boolean> = _hasUnreadNotifications.asStateFlow()

    init {
        loadDashboard()
    }

    // ─── Public Functions ─────────────────────────────────────────────────────

    /**
     * Load dashboard data. Automatically fetches current user if not provided.
     */
    fun loadDashboard(user: User? = null) {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading

            val currentUser = user ?: authRepository.getCurrentUserProfile()
            
            if (currentUser == null && !_useMockData.value) {
                _uiState.value = DashboardUiState.Error("Artist profile not found. Please log in again.")
                return@launch
            }
            
            // Verify role if we're not using mock data
            if (!_useMockData.value && currentUser?.role?.lowercase() != "artist") {
                android.util.Log.e("ArtistDashboardVM", "Access denied: User is not an artist. Role: ${currentUser?.role}")
                _uiState.value = DashboardUiState.Error("Access Denied: Only artists can view this dashboard.")
                return@launch
            }

            // If we have no user but are using mock data, create a dummy artist
            val effectiveUser = currentUser ?: User(
                id = 0,
                name = "Artist",
                email = "artist@echopanda.com",
                role = "artist",
                token = ""
            )

            val result = dashboardRepository.getArtistDashboard(
                user = effectiveUser,
                useMockData = _useMockData.value
            )

            android.util.Log.d("ArtistDashboardVM", "Result received: $result")

            _uiState.value = when (result) {
                is DashboardResult.Success -> {
                    android.util.Log.d("ArtistDashboardVM", "✓ Successfully loaded dashboard for ${effectiveUser.name}")
                    checkNotifications()
                    DashboardUiState.Success(result.data)
                }
                is DashboardResult.Error -> {
                    android.util.Log.e("ArtistDashboardVM", "❌ Failed to load dashboard: ${result.message}")
                    DashboardUiState.Error(result.message)
                }
                DashboardResult.Loading -> DashboardUiState.Loading
            }
        }
    }

    /**
     * Refresh dashboard data (shorter timeout)
     */
    fun refreshDashboard(user: User) {
        viewModelScope.launch {
            _isRefreshing.value = true

            val result = dashboardRepository.refreshDashboard(
                user = user,
                useMockData = _useMockData.value
            )

            when (result) {
                is DashboardResult.Success -> {
                    _uiState.value = DashboardUiState.Success(result.data)
                checkNotifications()
                }
                is DashboardResult.Error -> {
                    // Keep previous state on refresh error
                    val currentState = _uiState.value
                    if (currentState !is DashboardUiState.Success) {
                        _uiState.value = DashboardUiState.Error(result.message)
                    }
                }
                DashboardResult.Loading -> {}
            }

            _isRefreshing.value = false
        }
    }

    /**
     * Toggle between mock data and real API calls
     * Useful for testing and development
     */
    fun setUseMockData(useMock: Boolean) {
        _useMockData.value = useMock
        // Reload data with new setting
        val currentState = _uiState.value
        if (currentState is DashboardUiState.Success) {
            loadDashboard(currentState.data.user)
        }
    }

    /**
     * Retry loading dashboard after error
     */
    fun retry(user: User) {
        loadDashboard(user)
    }

    private fun checkNotifications() {
        viewModelScope.launch {
            val result = dashboardRepository.getNotifications()
            if (result is DashboardResult.Success) {
                _hasUnreadNotifications.value = result.data.any { !it.isRead }
            }
        }
    }

    /**
     * Get mock data directly (useful for immediate UI preview)
     */
    fun getMockData(user: User): ArtistDashboardData {
        return DashboardMockData.getMockDashboardData(user)
    }
}

class ArtistDashboardViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArtistDashboardViewModel::class.java)) {
            val tokenStorage = TokenStorage.getInstance(context.applicationContext)
            val musicApiService = RetrofitClient.getMusicService(tokenStorage)
            val repository = DashboardRepository(musicApiService)
            val authRepo = AuthRepository(tokenStorage)
            @Suppress("UNCHECKED_CAST")
            return ArtistDashboardViewModel(repository, authRepo, tokenStorage) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
