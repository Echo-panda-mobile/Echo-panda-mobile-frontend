package com.example.echo_panda_mobile.presentation.viewsmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.remote.NotificationDto
import com.example.echo_panda_mobile.data.remote.RetrofitClient
import com.example.echo_panda_mobile.data.repository.DashboardRepository
import com.example.echo_panda_mobile.data.repository.DashboardResult
import com.example.echo_panda_mobile.data.repository.TokenStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ArtistNotificationViewModel(
    private val repository: DashboardRepository
) : ViewModel() {

    sealed class NotificationUiState {
        object Loading : NotificationUiState()
        data class Success(val notifications: List<NotificationDto>) : NotificationUiState()
        data class Error(val message: String) : NotificationUiState()
    }

    private val _uiState = MutableStateFlow<NotificationUiState>(NotificationUiState.Loading)
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) _isRefreshing.value = true else _uiState.value = NotificationUiState.Loading
            val result = repository.getNotifications()
            _uiState.value = when (result) {
                is DashboardResult.Success -> NotificationUiState.Success(result.data)
                is DashboardResult.Error -> NotificationUiState.Error(result.message)
                DashboardResult.Loading -> NotificationUiState.Loading
            }
            if (isRefresh) _isRefreshing.value = false
        }
    }
}

class ArtistNotificationViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArtistNotificationViewModel::class.java)) {
            val tokenStorage = TokenStorage.getInstance(context.applicationContext)
            val apiService = RetrofitClient.getMusicService(tokenStorage)
            val repository = DashboardRepository(apiService)
            @Suppress("UNCHECKED_CAST")
            return ArtistNotificationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
