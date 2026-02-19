package com.mobil.travelreservation.ui.screens.reservations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mobil.travelreservation.data.local.SessionManager
import com.mobil.travelreservation.data.model.Reservation
import com.mobil.travelreservation.data.model.Trip
import com.mobil.travelreservation.data.repository.ReservationRepository
import com.mobil.travelreservation.data.repository.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ReservationWithTrip
 *
 * Rezervasyon tablosu ile Sefer (Trip) tablosunu birleştiren yardımcı veri sınıfı.
 * UI'da hem rezervasyon bilgilerini (koltuk no, yolcu adı) hem de
 * sefer bilgilerini (kalkış saati, firma adı) göstermek için kullanılır.
 *
 * @param reservation Rezervasyon detayları.
 * @param trip İlişkili sefer detayları.
 */
data class ReservationWithTrip(
    val reservation: Reservation,
    val trip: Trip?
)

/**
 * MyReservationsUiState
 *
 * Seyahatlerim ekranının anlık durumunu tutar.
 *
 * @param reservations Ekranda listelenecek birleştirilmiş veriler.
 * @param isLoading Veriler yükleniyor mu?
 * @param showCancelDialog İptal onay penceresi açık mı?
 * @param selectedReservation İşlem yapılmak üzere seçilen (iptal edilecek) rezervasyon.
 */
data class MyReservationsUiState(
    val reservations: List<ReservationWithTrip> = emptyList(),
    val isLoading: Boolean = true,
    val showCancelDialog: Boolean = false,
    val selectedReservation: ReservationWithTrip? = null
)

/**
 * ReservationViewModel
 *
 * Rezervasyon işlemlerini (Listeleme, İptal Etme) yöneten ViewModel.
 * İki farklı repository (Reservation ve Trip) ile çalışır.
 */
class ReservationViewModel(
    private val reservationRepository: ReservationRepository,
    private val tripRepository: TripRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    // UI Durumunu tutan akış (Flow)
    private val _uiState = MutableStateFlow(MyReservationsUiState())
    val uiState: StateFlow<MyReservationsUiState> = _uiState.asStateFlow()

    init {
        // ViewModel oluşturulduğu an verileri çekmeye başla
        loadReservations()
    }

    /**
     * Kullanıcının rezervasyonlarını çeker ve her bir rezervasyon için
     * ilgili sefer (Trip) bilgisini bulup birleştirir.
     */
    private fun loadReservations() {
        viewModelScope.launch {
            // Flow'u dinle (collect): Veritabanında değişiklik olursa burası tetiklenir
            reservationRepository.getUserReservations(sessionManager.getUserId()).collect { reservations ->

                // MAPLEME İŞLEMİ: Her rezervasyon için Trip bilgisini çekiyoruz
                val reservationsWithTrips = reservations.map { reservation ->
                    val trip = tripRepository.getTripById(reservation.tripId)
                    ReservationWithTrip(reservation, trip)
                }

                // UI State'i güncelle
                _uiState.value = _uiState.value.copy(
                    reservations = reservationsWithTrips,
                    isLoading = false
                )
            }
        }
    }

    /**
     * İptal onay dialogunu açar ve hangi rezervasyonun silineceğini seçer.
     */
    fun showCancelDialog(reservationWithTrip: ReservationWithTrip) {
        _uiState.value = _uiState.value.copy(
            showCancelDialog = true,
            selectedReservation = reservationWithTrip
        )
    }

    /**
     * İptal onay dialogunu kapatır.
     */
    fun hideCancelDialog() {
        _uiState.value = _uiState.value.copy(
            showCancelDialog = false,
            selectedReservation = null
        )
    }

    /**
     * Seçili rezervasyonu veritabanından siler.
     */
    fun cancelReservation() {
        val reservation = _uiState.value.selectedReservation?.reservation ?: return

        viewModelScope.launch {
            reservationRepository.deleteReservation(reservation)
            hideCancelDialog() // İşlem bitince dialogu kapat
            // Not: loadReservations() içindeki 'collect' sayesinde liste otomatik güncellenir.
        }
    }

    /**
     * Factory
     * ViewModel'e 3 farklı bağımlılığı (Repo'lar ve Session) enjekte etmek için gereklidir.
     */
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