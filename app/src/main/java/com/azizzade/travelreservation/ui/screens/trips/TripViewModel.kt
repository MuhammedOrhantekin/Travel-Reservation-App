package com.azizzade.travelreservation.ui.screens.trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.azizzade.travelreservation.data.model.Trip
import com.azizzade.travelreservation.data.repository.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class TripListUiState(
    val trips: List<Trip> = emptyList(),
    val isLoading: Boolean = true,
    val from: String = "",
    val to: String = "",
    val currentDate: String = "",
    val vehicleType: String = ""
)

class TripViewModel(
    private val tripRepository: TripRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripListUiState())
    val uiState: StateFlow<TripListUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun initSearch(from: String, to: String, date: String, vehicleType: String) {
        _uiState.value = _uiState.value.copy(
            from = from,
            to = to,
            currentDate = date,
            vehicleType = vehicleType
        )
        searchTrips(from, to, date, vehicleType)
    }

    fun searchTrips(from: String, to: String, date: String, vehicleType: String) {
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            tripRepository.searchTripsWithVehicleType(from, to, date, vehicleType).collect { trips ->
                _uiState.value = _uiState.value.copy(
                    trips = trips,
                    isLoading = false
                )
            }
        }
    }

    fun previousDay() {
        val currentState = _uiState.value
        try {
            val currentDate = dateFormat.parse(currentState.currentDate) ?: return
            val calendar = Calendar.getInstance()
            calendar.time = currentDate
            calendar.add(Calendar.DAY_OF_YEAR, -1)

            // Bugünden önceye gidemez
            val today = Calendar.getInstance()
            today.set(Calendar.HOUR_OF_DAY, 0)
            today.set(Calendar.MINUTE, 0)
            today.set(Calendar.SECOND, 0)
            today.set(Calendar.MILLISECOND, 0)

            if (calendar.before(today)) return

            val newDate = dateFormat.format(calendar.time)
            _uiState.value = currentState.copy(currentDate = newDate)
        } catch (e: Exception) {
            // Hata durumunda bir şey yapma
        }
    }

    fun nextDay() {
        val currentState = _uiState.value
        try {
            val currentDate = dateFormat.parse(currentState.currentDate) ?: return
            val calendar = Calendar.getInstance()
            calendar.time = currentDate
            calendar.add(Calendar.DAY_OF_YEAR, 1)

            val newDate = dateFormat.format(calendar.time)
            _uiState.value = currentState.copy(currentDate = newDate)
        } catch (e: Exception) {
            // Hata durumunda bir şey yapma
        }
    }

    class Factory(
        private val tripRepository: TripRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TripViewModel(tripRepository) as T
        }
    }
}