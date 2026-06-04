package com.example.echo_panda_mobile.presentation.viewsmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.remote.BackendUser
import com.example.echo_panda_mobile.data.remote.RetrofitClient
import com.example.echo_panda_mobile.data.repository.AdminRepository
import com.example.echo_panda_mobile.data.repository.AdminResult
import com.example.echo_panda_mobile.data.repository.MusicRepository
import com.example.echo_panda_mobile.data.repository.TokenStorage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminUserManagementUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val users: List<BackendUser> = emptyList(),
    val artistUsers: List<BackendUser> = emptyList(),
    val adminUsers: List<BackendUser> = emptyList(),
    val artistImageUrlsByUserId: Map<Int, String> = emptyMap()
)

class AdminUserManagementViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenStorage = TokenStorage(application)
    private val repository = AdminRepository(tokenStorage)
    private val musicRepository = MusicRepository(RetrofitClient.getMusicService(tokenStorage))

    private val _uiState = MutableStateFlow(AdminUserManagementUiState(isLoading = true))
    val uiState: StateFlow<AdminUserManagementUiState> = _uiState.asStateFlow()

    init {
        loadDirectory()
    }

    fun loadDirectory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            when (val result = repository.getAdminDirectory()) {
                is AdminResult.Success -> {
                    val artistImageUrls = resolveArtistImageUrls(result.data.artistUsers)
                    _uiState.value = AdminUserManagementUiState(
                        isLoading = false,
                        users = result.data.normalUsers,
                        artistUsers = result.data.artistUsers,
                        adminUsers = result.data.adminUsers,
                        artistImageUrlsByUserId = artistImageUrls
                    )
                }
                is AdminResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    private suspend fun resolveArtistImageUrls(artistUsers: List<BackendUser>): Map<Int, String> {
        val catalogByName = musicRepository.fetchArtistCatalogByName()
        return coroutineScope {
            artistUsers.map { user ->
                async {
                    val resolved = musicRepository.resolveArtistProfileImageForUser(
                        userName = user.name,
                        linkedArtist = user.artist,
                        catalogByName = catalogByName
                    )
                    resolved?.let { user.id to it }
                }
            }.awaitAll().filterNotNull().toMap()
        }
    }
}