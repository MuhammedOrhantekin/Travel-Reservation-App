package com.mobil.travelreservation.ui.screens.seats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mobil.travelreservation.data.local.database.dao.SeatInfo
import com.mobil.travelreservation.data.model.Trip
import com.mobil.travelreservation.data.repository.ReservationRepository
import com.mobil.travelreservation.data.repository.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * SeatUiState
 *
 * Koltuk Seçimi ekranının anlık durumunu tutan veri sınıfı.
 *
 * @param trip Seçilen seferin detayları (Fiyat, kalkış saati vb. için).
 * @param reservedSeatsWithGender Dolu koltukların listesi (Koltuk No + Cinsiyet bilgisi içerir).
 * @param selectedSeat Kullanıcının o an seçtiği koltuk numarası.
 * @param isLoading Veriler yükleniyor mu?
 */
data class SeatUiState(
    val trip: Trip? = null,
    val reservedSeatsWithGender: List<SeatInfo> = emptyList(),
    val selectedSeat: Int? = null,
    val isLoading: Boolean = true
)

/**
 * SeatViewModel
 *
 * Koltuk seçimi ekranının iş mantığını yönetir.
 * 1. Sefer bilgilerini getirir.
 * 2. Dolu koltukları ve cinsiyetlerini getirir.
 * 3. Kullanıcının koltuk seçimini yönetir (Dolu koltuğu seçmeyi engeller).
 */
class SeatViewModel(
    private val tripRepository: TripRepository,
    private val reservationRepository: ReservationRepository
) : ViewModel() {

    // UI Durumunu tutan akış (Flow)
    private val _uiState = MutableStateFlow(SeatUiState())
    val uiState: StateFlow<SeatUiState> = _uiState.asStateFlow()

    /**
     * Ekran açıldığında çağrılır.
     * 1. Sefer detaylarını çeker.
     * 2. O sefere ait rezervasyonları dinler (Hangi koltukta kim oturuyor?).
     */
    fun loadTrip(tripId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Sefer bilgisini çek
            val trip = tripRepository.getTripById(tripId)
            _uiState.value = _uiState.value.copy(
                trip = trip,
                isLoading = false
            )

            // Dolu koltukları cinsiyet bilgisiyle beraber çek (Flow)
            // collect: Veritabanında bir değişiklik olursa burası tetiklenir ve ekran güncellenir.
            reservationRepository.getReservedSeatsWithGender(tripId).collect { seats ->
                _uiState.value = _uiState.value.copy(reservedSeatsWithGender = seats)
            }
        }
    }

    /**
     * Kullanıcı bir koltuğa tıkladığında çalışır.
     * Tıklanan koltuğun gerçekten boş olup olmadığını kontrol eder.
     */
    fun selectSeat(seatNumber: Int) {
        // Seçilen koltuk dolu mu? (Listede var mı?)
        val isReserved = _uiState.value.reservedSeatsWithGender.any { it.seatNumber == seatNumber }

        // Eğer dolu değilse seçimi güncelle
        if (!isReserved) {
            _uiState.value = _uiState.value.copy(selectedSeat = seatNumber)
        }
    }

    /**
     * Factory
     * ViewModel'e Repository bağımlılıklarını enjekte etmek için gereklidir.
     */
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