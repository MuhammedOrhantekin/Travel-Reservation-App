package com.mobil.travelreservation.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mobil.travelreservation.data.local.SessionManager
import com.mobil.travelreservation.data.model.PaymentCard
import com.mobil.travelreservation.data.repository.PaymentCardRepository
import com.mobil.travelreservation.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ProfileUiState
 *
 * Profil ile ilgili ekranların (Kişisel, Güvenlik, Ödeme) anlık verilerini tutan sınıf.
 *
 * @param isLoading İşlem yapılırken dönen yükleniyor ifadesi.
 * @param fullName Kullanıcının adı soyadı.
 * @param email Kullanıcının e-posta adresi.
 * @param tcNumber Kullanıcının TC kimlik numarası.
 * @param phone Kullanıcının telefon numarası.
 * @param savedCards Kullanıcının kayıtlı kredi kartları listesi.
 */
data class ProfileUiState(
    val isLoading: Boolean = false,
    val fullName: String = "",
    val email: String = "",
    val tcNumber: String = "",
    val phone: String = "",
    val savedCards: List<PaymentCard> = emptyList()
)

/**
 * ProfileViewModel
 *
 * Profil, Güvenlik ve Ödeme Yöntemleri ekranlarını yöneten ViewModel.
 * Veritabanı (Repository) ile UI arasındaki köprüdür.
 *
 * Sorumlulukları:
 * 1. Kullanıcı bilgilerini getirme ve güncelleme.
 * 2. Şifre ve E-posta değiştirme.
 * 3. Kredi kartı ekleme, silme ve listeleme.
 */
class ProfileViewModel(
    private val userRepository: UserRepository,
    private val paymentCardRepository: PaymentCardRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    // UI Durumunu tutan akış (Flow)
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    /**
     * Kullanıcının güncel bilgilerini veritabanından çeker.
     * SessionManager'daki ID'yi kullanarak doğru kullanıcıyı bulur.
     */
    fun loadUserInfo() {
        viewModelScope.launch {
            val user = userRepository.getUserById(sessionManager.getUserId())
            user?.let {
                _uiState.value = _uiState.value.copy(
                    fullName = it.fullName,
                    email = it.email,
                    tcNumber = it.tcNumber,
                    phone = it.phone
                )
            }
        }
    }

    /**
     * Kullanıcının kayıtlı kredi kartlarını dinler (Flow).
     * Veritabanında bir kart eklendiğinde veya silindiğinde liste otomatik güncellenir.
     */
    fun loadPaymentCards() {
        viewModelScope.launch {
            paymentCardRepository.getUserCards(sessionManager.getUserId()).collect { cards ->
                _uiState.value = _uiState.value.copy(savedCards = cards)
            }
        }
    }

    // --- UI GÜNCELLEME FONKSİYONLARI (ANLIK YAZIM İÇİN) ---

    fun updateFullName(name: String) {
        _uiState.value = _uiState.value.copy(fullName = name)
    }

    fun updateTcNumber(tc: String) {
        // Sadece rakam girilmesini sağlar
        _uiState.value = _uiState.value.copy(tcNumber = tc.filter { it.isDigit() })
    }

    fun updatePhone(phone: String) {
        // Sadece rakam girilmesini sağlar
        _uiState.value = _uiState.value.copy(phone = phone.filter { it.isDigit() })
    }

    /**
     * Kişisel Bilgileri (Ad, TC, Tel) veritabanına kaydeder.
     * Ayrıca SessionManager'ı da günceller ki uygulamanın diğer yerlerinde (Ana Sayfa başlığı gibi)
     * isim anında değişsin.
     */
    fun savePersonalInfo() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val state = _uiState.value

            // Veritabanını güncelle
            userRepository.updateProfile(
                sessionManager.getUserId(),
                state.fullName,
                state.tcNumber,
                state.phone
            )

            // Oturumu güncelle (Uygulamayı kapatıp açmaya gerek kalmasın)
            sessionManager.saveUserName(state.fullName)

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    /**
     * Şifre değiştirme işlemi.
     */
    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            // Gerçek uygulamada burada önce eski şifrenin doğruluğu kontrol edilmelidir.
            userRepository.updatePassword(sessionManager.getUserId(), newPassword)
        }
    }

    /**
     * E-posta değiştirme işlemi.
     * Hem veritabanını hem de oturum bilgisini günceller.
     */
    fun changeEmail(newEmail: String) {
        viewModelScope.launch {
            userRepository.updateEmail(sessionManager.getUserId(), newEmail)
            sessionManager.saveUserEmail(newEmail)
            _uiState.value = _uiState.value.copy(email = newEmail)
        }
    }

    /**
     * Yeni bir kredi kartı ekler.
     * Kart numarasının ilk hanesine bakarak kart tipini (Visa/Master/Troy) otomatik algılar.
     */
    fun addPaymentCard(name: String, number: String, expiry: String) {
        viewModelScope.launch {
            // Kart Tipi Algılama Mantığı
            val cardType = when {
                number.startsWith("4") -> "Visa"
                number.startsWith("5") -> "Mastercard"
                number.startsWith("9") -> "Troy"
                else -> "Diğer"
            }

            paymentCardRepository.insertCard(
                PaymentCard(
                    userId = sessionManager.getUserId(),
                    cardName = name,
                    cardNumber = number,
                    lastFourDigits = number.takeLast(4), // Güvenlik için sadece son 4 hane gösterilir
                    expiryDate = expiry,
                    cardType = cardType
                )
            )
        }
    }

    /**
     * Seçilen bir kredi kartını siler.
     */
    fun deletePaymentCard(card: PaymentCard) {
        viewModelScope.launch {
            paymentCardRepository.deleteCard(card)
        }
    }

    /**
     * Factory
     * ViewModel'e birden fazla Repository ve SessionManager geçebilmek için gereklidir.
     */
    class Factory(
        private val userRepository: UserRepository,
        private val paymentCardRepository: PaymentCardRepository,
        private val sessionManager: SessionManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel(userRepository, paymentCardRepository, sessionManager) as T
        }
    }
}