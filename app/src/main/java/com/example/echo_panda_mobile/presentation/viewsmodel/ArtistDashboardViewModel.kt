package com.example.echo_panda_mobile.presentation.viewsmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.model.ArtistDashboardData
import com.example.echo_panda_mobile.data.model.DashboardMockData
import com.example.echo_panda_mobile.data.model.User
import com.example.echo_panda_mobile.data.repository.DashboardRepository
import com.example.echo_panda_mobile.data.repository.DashboardResult
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
    private val dashboardRepository: DashboardRepository = DashboardRepository(),
    private val authRepository: com.example.echo_panda_mobile.data.repository.AuthRepository = com.example.echo_panda_mobile.data.repository.AuthRepository()
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
    private val _useMockData = MutableStateFlow(true)
    val useMockData: StateFlow<Boolean> = _useMockData.asStateFlow()

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
                _uiState.value = DashboardUiState.Error("Artist profile not found.")
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

            _uiState.value = when (result) {
                is DashboardResult.Success -> DashboardUiState.Success(result.data)
                is DashboardResult.Error -> DashboardUiState.Error(result.message)
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

    /**
     * Get mock data directly (useful for immediate UI preview)
     */
    fun getMockData(user: User): ArtistDashboardData {
        return DashboardMockData.getMockDashboardData(user)
    }
}
