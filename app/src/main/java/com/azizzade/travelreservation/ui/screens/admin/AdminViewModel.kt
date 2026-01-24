package com.azizzade.travelreservation.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.azizzade.travelreservation.data.model.Trip
import com.azizzade.travelreservation.data.repository.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminUiState(
    val allTrips: List<Trip> = emptyList(),
    val filteredTrips: List<Trip> = emptyList(),
    val isLoading: Boolean = true,
    val selectedTypeFilter: String? = null,
    val adminName: String = "Admin Kullanıcı" // Burası normalde auth'dan gelir
)

class AdminViewModel(
    private val tripRepository: TripRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    fun loadAllTrips() {
        viewModelScope.launch {
            tripRepository.getAllTrips().collect { trips ->
                _uiState.value = _uiState.value.copy(
                    allTrips = trips,
                    filteredTrips = applyFilter(trips, _uiState.value.selectedTypeFilter),
                    isLoading = false
                )
            }
        }
    }

    fun filterByType(type: String?) {
        _uiState.value = _uiState.value.copy(
            selectedTypeFilter = type,
            filteredTrips = applyFilter(_uiState.value.allTrips, type)
        )
    }

    private fun applyFilter(trips: List<Trip>, type: String?): List<Trip> {
        return if (type == null) trips else trips.filter { it.vehicleType == type }
    }

    fun addTrip(trip: Trip) {
        viewModelScope.launch {
            tripRepository.insertTrip(trip)
        }
    }

    fun updateTrip(trip: Trip) {
        viewModelScope.launch {
            tripRepository.updateTrip(trip)
        }
    }

    fun deleteTrip(trip: Trip) {
        viewModelScope.launch {
            tripRepository.deleteTrip(trip)
        }
    }

    class Factory(
        private val tripRepository: TripRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AdminViewModel(tripRepository) as T
        }
    }
}