package com.azizzade.travelreservation.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.azizzade.travelreservation.data.local.SessionManager
import com.azizzade.travelreservation.data.model.PaymentCard
import com.azizzade.travelreservation.data.repository.PaymentCardRepository
import com.azizzade.travelreservation.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val fullName: String = "",
    val email: String = "",
    val tcNumber: String = "",
    val phone: String = "",
    val savedCards: List<PaymentCard> = emptyList()
)

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val paymentCardRepository: PaymentCardRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

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

    fun loadPaymentCards() {
        viewModelScope.launch {
            paymentCardRepository.getUserCards(sessionManager.getUserId()).collect { cards ->
                _uiState.value = _uiState.value.copy(savedCards = cards)
            }
        }
    }

    fun updateFullName(name: String) {
        _uiState.value = _uiState.value.copy(fullName = name)
    }

    fun updateTcNumber(tc: String) {
        _uiState.value = _uiState.value.copy(tcNumber = tc.filter { it.isDigit() })
    }

    fun updatePhone(phone: String) {
        _uiState.value = _uiState.value.copy(phone = phone.filter { it.isDigit() })
    }

    fun savePersonalInfo() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val state = _uiState.value
            userRepository.updateProfile(
                sessionManager.getUserId(),
                state.fullName,
                state.tcNumber,
                state.phone
            )
            sessionManager.saveUserName(state.fullName)
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            userRepository.updatePassword(sessionManager.getUserId(), newPassword)
        }
    }

    fun changeEmail(newEmail: String) {
        viewModelScope.launch {
            userRepository.updateEmail(sessionManager.getUserId(), newEmail)
            sessionManager.saveUserEmail(newEmail)
            _uiState.value = _uiState.value.copy(email = newEmail)
        }
    }

    fun addPaymentCard(name: String, number: String, expiry: String) {
        viewModelScope.launch {
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
                    lastFourDigits = number.takeLast(4),
                    expiryDate = expiry,
                    cardType = cardType
                )
            )
        }
    }

    fun deletePaymentCard(card: PaymentCard) {
        viewModelScope.launch {
            paymentCardRepository.deleteCard(card)
        }
    }

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