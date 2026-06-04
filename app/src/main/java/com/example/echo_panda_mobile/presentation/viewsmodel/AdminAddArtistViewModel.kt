package com.example.echo_panda_mobile.presentation.viewsmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.echo_panda_mobile.data.repository.AdminRepository
import com.example.echo_panda_mobile.data.repository.AdminResult
import com.example.echo_panda_mobile.data.repository.TokenStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminAddArtistUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val slug: String = "",
    val bio: String = "",
    val verificationStatus: String = "pending",
    val verificationReason: String = "",
    val isActive: Boolean = true,
    val artistType: String = "Single",
    val gender: String = "Male",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val createdSuccessfully: Boolean = false
)

class AdminAddArtistViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AdminRepository(TokenStorage(application))

    private val _uiState = MutableStateFlow(AdminAddArtistUiState())
    val uiState: StateFlow<AdminAddArtistUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) {
        _uiState.value = _uiState.value.copy(name = value, errorMessage = null)
        // Auto-generate slug from name if slug is empty
        if (_uiState.value.slug.isBlank()) {
            _uiState.value = _uiState.value.copy(slug = value.lowercase().replace(" ", "-"))
        }
    }

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
    }

    fun onSlugChange(value: String) {
        _uiState.value = _uiState.value.copy(slug = value, errorMessage = null)
    }

    fun onBioChange(value: String) {
        _uiState.value = _uiState.value.copy(bio = value, errorMessage = null)
    }

    fun onVerificationStatusChange(status: String) {
        _uiState.value = _uiState.value.copy(verificationStatus = status, errorMessage = null)
    }

    fun onVerificationReasonChange(value: String) {
        _uiState.value = _uiState.value.copy(verificationReason = value, errorMessage = null)
    }

    fun onIsActiveChange(value: Boolean) {
        _uiState.value = _uiState.value.copy(isActive = value, errorMessage = null)
    }

    fun onArtistTypeChange(type: String) {
        val nextGender = if (type == "Group") "They" else {
            val current = _uiState.value.gender
            if (current == "They") "Male" else current
        }
        _uiState.value = _uiState.value.copy(
            artistType = type,
            gender = nextGender,
            errorMessage = null
        )
    }

    fun onGenderChange(gender: String) {
        _uiState.value = _uiState.value.copy(gender = gender, errorMessage = null)
    }

    fun createArtist() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Artist name is required")
            return
        }
        if (state.email.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Email is required")
            return
        }
        if (state.password.length < 8) {
            _uiState.value = state.copy(errorMessage = "Password must be at least 8 characters")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null, successMessage = null)

            when (
                val result = repository.createArtist(
                    name = state.name,
                    email = state.email,
                    password = state.password,
                    artistType = state.artistType,
                    gender = state.gender
                )
            ) {
                is AdminResult.Success -> {
                    val details = buildString {
                        append(result.data.message)
                        result.data.firebaseMessage?.let { append("\n").append(it) }
                    }
                    _uiState.value = state.copy(
                        isLoading = false,
                        successMessage = details,
                        createdSuccessfully = true
                    )
                }
                is AdminResult.Error -> {
                    _uiState.value = state.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun consumeSuccess() {
        _uiState.value = _uiState.value.copy(createdSuccessfully = false)
    }
}
