package com.mobil.travelreservation.ui.screens.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mobil.travelreservation.data.local.SessionManager
import com.mobil.travelreservation.data.model.PaymentCard
import com.mobil.travelreservation.data.model.Reservation
import com.mobil.travelreservation.data.model.Trip
import com.mobil.travelreservation.data.repository.PaymentCardRepository
import com.mobil.travelreservation.data.repository.ReservationRepository
import com.mobil.travelreservation.data.repository.TripRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * PaymentUiState
 *
 * Ödeme ekranının o anki tüm verilerini tutan sınıf.
 * Hem yolcu bilgilerini, hem ödeme bilgilerini hem de işlem durumunu barındırır.
 */
data class PaymentUiState(
    val trip: Trip? = null,
    val seatNumber: Int = 0,
    val isLoading: Boolean = true,
    val isProcessing: Boolean = false,

    // Yolcu bilgileri
    val passengerName: String = "",
    val passengerTc: String = "",
    val passengerEmail: String = "",
    val passengerPhone: String = "",
    val passengerGender: String = "Erkek",

    // Ödeme bilgileri
    val paymentMethod: String = "Kredi Kartı",
    val cardNumber: String = "",
    val cardHolderName: String = "",
    val cardExpiry: String = "",
    val cardCvc: String = "",
    val saveCard: Boolean = false,

    // Kayıtlı kartlar
    val savedCards: List<PaymentCard> = emptyList(),
    val selectedCardId: Long? = null,

    // İşlem Durumu
    val showSuccessDialog: Boolean = false,
    val paymentComplete: Boolean = false,
    val errorMessage: String? = null
)

/**
 * PaymentViewModel
 *
 * Ödeme ekranının beyin takımıdır.
 * 1. Sefer ve koltuk bilgisini getirir.
 * 2. Kullanıcı girdilerini (Input) yönetir ve doğrular (Validation).
 * 3. Rezervasyon işlemini gerçekleştirir (Database Insert).
 * 4. İsteğe bağlı olarak kredi kartını kaydeder.
 */
class PaymentViewModel(
    private val tripRepository: TripRepository,
    private val reservationRepository: ReservationRepository,
    private val paymentCardRepository: PaymentCardRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    // UI Durumunu tutan akış
    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    /**
     * Ekran açıldığında çağrılır.
     * Sefer detaylarını ve kullanıcının kayıtlı kartlarını yükler.
     * Ayrıca otomatik olarak kullanıcının adını ve mailini doldurur.
     */
    fun loadTripAndSeat(tripId: Long, seatNumber: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val trip = tripRepository.getTripById(tripId)

            // Kayıtlı kartları Flow ile dinle
            paymentCardRepository.getUserCards(sessionManager.getUserId()).collect { cards ->
                _uiState.value = _uiState.value.copy(
                    trip = trip,
                    seatNumber = seatNumber,
                    isLoading = false,
                    savedCards = cards,
                    passengerName = sessionManager.getUserName(),
                    passengerEmail = sessionManager.getUserEmail()
                )
            }
        }
    }

    // --- INPUT GÜNCELLEME FONKSİYONLARI ---

    fun updatePassengerName(name: String) {
        _uiState.value = _uiState.value.copy(passengerName = name)
    }

    fun updatePassengerTc(tc: String) {
        // Sadece rakam girilmesini garanti altına alıyoruz
        _uiState.value = _uiState.value.copy(passengerTc = tc.filter { it.isDigit() })
    }

    fun updatePassengerEmail(email: String) {
        _uiState.value = _uiState.value.copy(passengerEmail = email)
    }

    fun updatePassengerPhone(phone: String) {
        _uiState.value = _uiState.value.copy(passengerPhone = phone.filter { it.isDigit() })
    }

    fun updatePassengerGender(gender: String) {
        _uiState.value = _uiState.value.copy(passengerGender = gender)
    }

    fun updatePaymentMethod(method: String) {
        _uiState.value = _uiState.value.copy(paymentMethod = method)
    }

    fun updateCardNumber(number: String) {
        _uiState.value = _uiState.value.copy(cardNumber = number.filter { it.isDigit() })
    }

    fun updateCardHolderName(name: String) {
        _uiState.value = _uiState.value.copy(cardHolderName = name)
    }

    fun updateCardExpiry(expiry: String) {
        _uiState.value = _uiState.value.copy(cardExpiry = expiry)
    }

    fun updateCardCvc(cvc: String) {
        _uiState.value = _uiState.value.copy(cardCvc = cvc.filter { it.isDigit() })
    }

    fun updateSaveCard(save: Boolean) {
        _uiState.value = _uiState.value.copy(saveCard = save)
    }

    fun selectSavedCard(cardId: Long?) {
        _uiState.value = _uiState.value.copy(selectedCardId = cardId)
    }

    /**
     * Form Doğrulama (Validation).
     * Tüm alanların kurallara uygun olup olmadığını kontrol eder.
     * @return Eğer her şey uygunsa true döner ve "Ödeme Yap" butonu aktif olur.
     */
    fun isPaymentValid(): Boolean {
        val state = _uiState.value

        // Yolcu bilgileri kontrolü
        if (state.passengerName.isBlank()) return false
        if (state.passengerTc.length != 11) return false
        if (state.passengerEmail.isBlank() || !state.passengerEmail.contains("@")) return false
        if (state.passengerPhone.length < 10) return false

        // Ödeme bilgileri kontrolü (Eğer kayıtlı kart seçili DEĞİLSE)
        if (state.selectedCardId == null) {
            if (state.cardNumber.length != 16) return false
            if (state.cardHolderName.isBlank()) return false
            if (state.cardExpiry.length < 4) return false // Örn: 12/25
            if (state.cardCvc.length != 3) return false
        }

        return true
    }

    /**
     * Ödeme İşlemi (Simülasyon).
     * 1. 2 saniye bekle (Banka işlemi gibi).
     * 2. Rezervasyonu veritabanına ekle.
     * 3. "Kartı Kaydet" seçildiyse kartı veritabanına ekle.
     * 4. Başarılı olursa dialog göster.
     */
    fun processPayment() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, errorMessage = null)

            // Banka ile iletişim kuruyormuş gibi yapıyoruz (UX için)
            delay(2000)

            val state = _uiState.value
            val trip = state.trip ?: return@launch

            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                // Rezervasyonu Kaydet
                reservationRepository.insertReservation(
                    Reservation(
                        userId = sessionManager.getUserId(),
                        tripId = trip.id,
                        seatNumber = state.seatNumber,
                        passengerName = state.passengerName,
                        passengerGender = state.passengerGender,
                        passengerTc = state.passengerTc,
                        passengerEmail = state.passengerEmail,
                        passengerPhone = state.passengerPhone,
                        reservationDate = dateFormat.format(Date()),
                        paymentMethod = state.paymentMethod
                    )
                )

                // Kartı Kaydet (Eğer seçildiyse ve yeni kart girildiyse)
                if (state.saveCard && state.selectedCardId == null) {
                    paymentCardRepository.insertCard(
                        PaymentCard(
                            userId = sessionManager.getUserId(),
                            cardName = state.cardHolderName,
                            cardNumber = state.cardNumber,
                            lastFourDigits = state.cardNumber.takeLast(4), // Güvenlik için sadece son 4 hane
                            expiryDate = state.cardExpiry,
                            cardType = detectCardType(state.cardNumber)
                        )
                    )
                }

                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    showSuccessDialog = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    errorMessage = "Ödeme işlemi başarısız: ${e.message}"
                )
            }
        }
    }

    /**
     * Ödeme tamamlandıktan sonra dialog kapatılınca çağrılır.
     * Ekranı kapatmak için state'i günceller.
     */
    fun completePayment() {
        _uiState.value = _uiState.value.copy(
            showSuccessDialog = false,
            paymentComplete = true
        )
    }

    // Kart numarasının başına bakarak tipini (Visa/Master) tahmin eder
    private fun detectCardType(cardNumber: String): String {
        return when {
            cardNumber.startsWith("4") -> "Visa"
            cardNumber.startsWith("5") -> "Mastercard"
            cardNumber.startsWith("9") -> "Troy"
            else -> "Diğer"
        }
    }

    class Factory(
        private val tripRepository: TripRepository,
        private val reservationRepository: ReservationRepository,
        private val paymentCardRepository: PaymentCardRepository,
        private val sessionManager: SessionManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PaymentViewModel(tripRepository, reservationRepository, paymentCardRepository, sessionManager) as T
        }
    }
}