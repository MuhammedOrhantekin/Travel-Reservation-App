package com.azizzade.travelreservation.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.azizzade.travelreservation.data.local.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

data class HomeUiState(
    val userName: String = "",
    val isAdmin: Boolean = false,
    val fromCity: String = "",
    val toCity: String = "",
    val selectedDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val selectedVehicleType: String = "Otobüs"
)

class HomeViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Şehir listesini buraya aldık (Sadece bir kez oluşturulur)
    val cities = listOf(
        "İstanbul", "Ankara", "İzmir", "Antalya", "Bursa", "Adana",
        "Konya", "Gaziantep", "Mersin", "Kayseri", "Eskişehir", "Trabzon",
        "Samsun", "Denizli", "Muğla", "Balıkesir", "Manisa", "Aydın"
    )

    init {
        loadUserInfo()
    }

    private fun loadUserInfo() {
        _uiState.value = _uiState.value.copy(
            userName = sessionManager.getUserName(),
            isAdmin = sessionManager.isAdmin()
        )
    }

    fun updateFromCity(city: String) {
        _uiState.value = _uiState.value.copy(fromCity = city)
    }

    fun updateToCity(city: String) {
        _uiState.value = _uiState.value.copy(toCity = city)
    }

    fun updateDate(date: String) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
    }

    fun updateVehicleType(type: String) {
        _uiState.value = _uiState.value.copy(selectedVehicleType = type)
    }

    fun swapCities() {
        val current = _uiState.value
        _uiState.value = current.copy(
            fromCity = current.toCity,
            toCity = current.fromCity
        )
    }

    fun isSearchValid(): Boolean {
        val state = _uiState.value
        return state.fromCity.isNotBlank() &&
                state.toCity.isNotBlank() &&
                state.fromCity != state.toCity &&
                state.selectedDate.isNotBlank()
    }

    class Factory(
        private val sessionManager: SessionManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(sessionManager) as T
        }
    }
}