package com.azizzade.travelreservation.ui.screens.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.azizzade.travelreservation.data.local.SessionManager
import com.azizzade.travelreservation.data.model.PaymentCard
import com.azizzade.travelreservation.data.model.Reservation
import com.azizzade.travelreservation.data.model.Trip
import com.azizzade.travelreservation.data.repository.PaymentCardRepository
import com.azizzade.travelreservation.data.repository.ReservationRepository
import com.azizzade.travelreservation.data.repository.TripRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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

    // Durum
    val showSuccessDialog: Boolean = false,
    val paymentComplete: Boolean = false,
    val errorMessage: String? = null
)

class PaymentViewModel(
    private val tripRepository: TripRepository,
    private val reservationRepository: ReservationRepository,
    private val paymentCardRepository: PaymentCardRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    fun loadTripAndSeat(tripId: Long, seatNumber: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val trip = tripRepository.getTripById(tripId)

            // Kayıtlı kartları yükle
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

    fun updatePassengerName(name: String) {
        _uiState.value = _uiState.value.copy(passengerName = name)
    }

    fun updatePassengerTc(tc: String) {
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

    fun isPaymentValid(): Boolean {
        val state = _uiState.value

        if (state.passengerName.isBlank()) return false
        if (state.passengerTc.length != 11) return false
        if (state.passengerEmail.isBlank() || !state.passengerEmail.contains("@")) return false
        if (state.passengerPhone.length < 10) return false

        if (state.selectedCardId == null) {
            if (state.cardNumber.length != 16) return false
            if (state.cardHolderName.isBlank()) return false
            if (state.cardExpiry.length < 4) return false
            if (state.cardCvc.length != 3) return false
        }

        return true
    }

    fun processPayment() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, errorMessage = null)

            delay(2000)

            val state = _uiState.value
            val trip = state.trip ?: return@launch

            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
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

                if (state.saveCard && state.selectedCardId == null) {
                    paymentCardRepository.insertCard(
                        PaymentCard(
                            userId = sessionManager.getUserId(),
                            cardName = state.cardHolderName,
                            cardNumber = state.cardNumber,
                            lastFourDigits = state.cardNumber.takeLast(4),
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

    fun completePayment() {
        _uiState.value = _uiState.value.copy(
            showSuccessDialog = false,
            paymentComplete = true
        )
    }

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