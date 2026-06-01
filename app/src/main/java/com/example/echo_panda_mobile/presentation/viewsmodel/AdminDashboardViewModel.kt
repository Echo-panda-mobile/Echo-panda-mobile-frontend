package com.example.echo_panda_mobile.presentation.viewsmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.remote.DashboardStats
import com.example.echo_panda_mobile.data.remote.GenreData
import com.example.echo_panda_mobile.data.remote.TagData
import com.example.echo_panda_mobile.data.repository.AdminRepository
import com.example.echo_panda_mobile.data.repository.MusicRepository
import com.example.echo_panda_mobile.data.repository.MusicResult
import com.example.echo_panda_mobile.data.repository.TokenStorage
import com.example.echo_panda_mobile.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminDashboardUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val stats: DashboardStats? = null,
    val tags: List<TagData> = emptyList(),
    val genres: List<GenreData> = emptyList()
)

class AdminDashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenStorage = TokenStorage(application)
    private val adminRepository = AdminRepository(tokenStorage)
    private val musicRepository = MusicRepository(RetrofitClient.getMusicService(tokenStorage))

    private val _uiState = MutableStateFlow(AdminDashboardUiState(isLoading = true))
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = AdminDashboardUiState(isLoading = true, errorMessage = null)

            try {
                // Fetch data from multiple sources
                val directoryResult = adminRepository.getAdminDirectory()
                val tags = adminRepository.getTags()
                val genres = adminRepository.getGenres()
                
                // Fetch songs and albums counts
                val songsResult = musicRepository.getAllSongs()
                val albumsResult = musicRepository.getAllAlbums()
                
                val totalSongs = when (songsResult) {
                    is MusicResult.Success -> songsResult.data.size
                    else -> 0
                }
                
                val totalAlbums = when (albumsResult) {
                    is MusicResult.Success -> albumsResult.data.size
                    else -> 0
                }
                
                // Compose dashboard stats
                val stats = when (directoryResult) {
                    is com.example.echo_panda_mobile.data.repository.AdminResult.Success -> {
                        val allData = directoryResult.data
                        DashboardStats(
                            totalUsers = allData.normalUsers.size,
                            activeArtists = allData.artistUsers.size,
                            totalAdmins = allData.adminUsers.size,
                            totalSongs = totalSongs,
                            totalAlbums = totalAlbums,
                            totalGenres = genres.size,
                            totalTags = tags.size,
                            flaggedContent = 0,
                            pendingReports = 0
                        )
                    }
                    else -> null
                }

                _uiState.value = AdminDashboardUiState(
                    isLoading = false,
                    stats = stats,
                    tags = tags,
                    genres = genres
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Failed to load dashboard"
                )
            }
        }
    }

    fun refresh() {
        loadDashboard()
    }
}
