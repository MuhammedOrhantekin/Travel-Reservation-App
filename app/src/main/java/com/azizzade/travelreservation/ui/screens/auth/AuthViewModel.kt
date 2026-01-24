package com.azizzade.travelreservation.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.azizzade.travelreservation.data.local.SessionManager
import com.azizzade.travelreservation.data.model.User
import com.azizzade.travelreservation.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            try {
                val user = userRepository.login(email, password)
                if (user != null) {
                    sessionManager.saveLoginSession(user.id, user.email, user.fullName, user.isAdmin)
                    _uiState.value = AuthUiState(isSuccess = true)
                } else {
                    _uiState.value = AuthUiState(errorMessage = "E-posta veya şifre hatalı")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState(errorMessage = "Bir hata oluştu: ${e.message}")
            }
        }
    }

    fun register(fullName: String, email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            // Validasyonlar
            when {
                fullName.isBlank() -> {
                    _uiState.value = AuthUiState(errorMessage = "Ad soyad gerekli")
                    return@launch
                }
                email.isBlank() -> {
                    _uiState.value = AuthUiState(errorMessage = "E-posta gerekli")
                    return@launch
                }
                !email.contains("@") -> {
                    _uiState.value = AuthUiState(errorMessage = "Geçerli bir e-posta girin")
                    return@launch
                }
                password.length < 6 -> {
                    _uiState.value = AuthUiState(errorMessage = "Şifre en az 6 karakter olmalı")
                    return@launch
                }
                password != confirmPassword -> {
                    _uiState.value = AuthUiState(errorMessage = "Şifreler eşleşmiyor")
                    return@launch
                }
            }

            try {
                val existingUser = userRepository.isEmailExists(email)
                if (existingUser) { // != null yerine doğrudan boolean kontrolü
                    _uiState.value = AuthUiState(errorMessage = "Bu e-posta zaten kayıtlı")
                } else {
                    userRepository.register(
                        User(
                            email = email,
                            password = password,
                            fullName = fullName,
                            isAdmin = false
                        )
                    )
                    _uiState.value = AuthUiState(isSuccess = true)
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState(errorMessage = "Bir hata oluştu: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState()
    }

    class Factory(
        private val userRepository: UserRepository,
        private val sessionManager: SessionManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(userRepository, sessionManager) as T
        }
    }
}