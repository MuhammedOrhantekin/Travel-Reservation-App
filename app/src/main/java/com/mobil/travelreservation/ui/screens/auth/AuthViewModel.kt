package com.mobil.travelreservation.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mobil.travelreservation.data.local.SessionManager
import com.mobil.travelreservation.data.model.User
import com.mobil.travelreservation.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Giriş ve Kayıt ekranlarının anlık durumunu (Yükleniyor, Başarılı, Hata) tutar.
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Kullanıcı giriş (Login) ve kayıt (Register) işlemlerini yöneten ViewModel.
 * Veritabanı ve Oturum Yöneticisi (SessionManager) ile iletişim kurar.
 */
class AuthViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    // UI durumunu tutan ve güncelleyen akış (Flow)
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /**
     * Kullanıcı girişi yapar. Başarılıysa oturumu kaydeder, başarısızsa hata mesajı döner.
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            try {
                val user = userRepository.login(email, password)
                if (user != null) {
                    // Giriş başarılı, oturumu telefona kaydet
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

    /**
     * Yeni kullanıcı kaydeder. Önce input doğrulaması (Validation) yapar,
     * ardından e-posta kontrolü yapıp kullanıcıyı oluşturur.
     */
    fun register(fullName: String, email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)

            // --- VALIDATION (Doğrulama) ---
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
                // Aynı mail adresi var mı kontrolü
                val existingUser = userRepository.isEmailExists(email)
                if (existingUser) {
                    _uiState.value = AuthUiState(errorMessage = "Bu e-posta zaten kayıtlı")
                } else {
                    // Kayıt işlemini gerçekleştir
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

    /**
     * UI durumunu sıfırlar (Örn: Hata mesajı gösterildikten sonra temizlemek için).
     */
    fun resetState() {
        _uiState.value = AuthUiState()
    }

    /**
     * ViewModel'e Repository ve SessionManager'ı parametre olarak geçmek için Factory.
     */
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