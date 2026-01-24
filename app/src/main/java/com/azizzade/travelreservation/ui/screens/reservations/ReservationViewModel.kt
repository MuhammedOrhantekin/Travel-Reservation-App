package com.azizzade.travelreservation.ui.screens.reservations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.azizzade.travelreservation.data.local.SessionManager
import com.azizzade.travelreservation.data.model.Reservation
import com.azizzade.travelreservation.data.model.Trip
import com.azizzade.travelreservation.data.repository.ReservationRepository
import com.azizzade.travelreservation.data.repository.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReservationWithTrip(
    val reservation: Reservation,
    val trip: Trip?
)

data class MyReservationsUiState(
    val reservations: List<ReservationWithTrip> = emptyList(),
    val isLoading: Boolean = true,
    val showCancelDialog: Boolean = false,
    val selectedReservation: ReservationWithTrip? = null
)

class ReservationViewModel(
    private val reservationRepository: ReservationRepository,
    private val tripRepository: TripRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyReservationsUiState())
    val uiState: StateFlow<MyReservationsUiState> = _uiState.asStateFlow()

    init {
        loadReservations()
    }

    private fun loadReservations() {
        viewModelScope.launch {
            reservationRepository.getUserReservations(sessionManager.getUserId()).collect { reservations ->
                val reservationsWithTrips = reservations.map { reservation ->
                    val trip = tripRepository.getTripById(reservation.tripId)
                    ReservationWithTrip(reservation, trip)
                }
                _uiState.value = _uiState.value.copy(
                    reservations = reservationsWithTrips,
                    isLoading = false
                )
            }
        }
    }

    fun showCancelDialog(reservationWithTrip: ReservationWithTrip) {
        _uiState.value = _uiState.value.copy(
            showCancelDialog = true,
            selectedReservation = reservationWithTrip
        )
    }

    fun hideCancelDialog() {
        _uiState.value = _uiState.value.copy(
            showCancelDialog = false,
            selectedReservation = null
        )
    }

    fun cancelReservation() {
        val reservation = _uiState.value.selectedReservation?.reservation ?: return

        viewModelScope.launch {
            reservationRepository.deleteReservation(reservation)
            hideCancelDialog()
        }
    }

    class Factory(
        private val reservationRepository: ReservationRepository,
        private val tripRepository: TripRepository,
        private val sessionManager: SessionManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReservationViewModel(reservationRepository, tripRepository, sessionManager) as T
        }
    }
}