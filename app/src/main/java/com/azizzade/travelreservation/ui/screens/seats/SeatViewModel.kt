package com.azizzade.travelreservation.ui.screens.seats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.azizzade.travelreservation.data.local.database.dao.SeatInfo
import com.azizzade.travelreservation.data.model.Trip
import com.azizzade.travelreservation.data.repository.ReservationRepository
import com.azizzade.travelreservation.data.repository.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SeatUiState(
    val trip: Trip? = null,
    val reservedSeatsWithGender: List<SeatInfo> = emptyList(),
    val selectedSeat: Int? = null,
    val isLoading: Boolean = true
)

class SeatViewModel(
    private val tripRepository: TripRepository,
    private val reservationRepository: ReservationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeatUiState())
    val uiState: StateFlow<SeatUiState> = _uiState.asStateFlow()

    fun loadTrip(tripId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val trip = tripRepository.getTripById(tripId)
            _uiState.value = _uiState.value.copy(
                trip = trip,
                isLoading = false
            )

            reservationRepository.getReservedSeatsWithGender(tripId).collect { seats ->
                _uiState.value = _uiState.value.copy(reservedSeatsWithGender = seats)
            }
        }
    }

    fun selectSeat(seatNumber: Int) {
        val isReserved = _uiState.value.reservedSeatsWithGender.any { it.seatNumber == seatNumber }
        if (!isReserved) {
            _uiState.value = _uiState.value.copy(selectedSeat = seatNumber)
        }
    }

    class Factory(
        private val tripRepository: TripRepository,
        private val reservationRepository: ReservationRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SeatViewModel(tripRepository, reservationRepository) as T
        }
    }
}