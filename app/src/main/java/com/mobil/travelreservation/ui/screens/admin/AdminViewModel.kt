package com.mobil.travelreservation.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mobil.travelreservation.data.model.Trip
import com.mobil.travelreservation.data.repository.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * AdminUiState
 *
 * Admin ekranının o anki durumunu (Snapshot) tutan veri sınıfıdır.
 * @param allTrips Veritabanından gelen ham (tüm) sefer listesi.
 * @param filteredTrips Ekranda o an gösterilen (filtrelenmiş) liste.
 * @param isLoading Verilerin yüklenip yüklenmediği bilgisi.
 * @param selectedTypeFilter Hangi filtrenin seçili olduğu (Otobüs/Uçak/Null).
 */
data class AdminUiState(
    val allTrips: List<Trip> = emptyList(),
    val filteredTrips: List<Trip> = emptyList(),
    val isLoading: Boolean = true,
    val selectedTypeFilter: String? = null,
    val adminName: String = "Admin Kullanıcı"
)

/**
 * AdminViewModel
 *
 * AdminPanelScreen ile TripRepository arasındaki köprüdür.
 * İş Mantığı (Business Logic) burada döner:
 * 1. Veritabanından veriyi çeker ve UI State'i günceller.
 * 2. Kullanıcının filtreleme isteklerini işler.
 * 3. Ekleme, Silme ve Güncelleme işlemlerini Repository'ye iletir.
 */
class AdminViewModel(
    private val tripRepository: TripRepository
) : ViewModel() {

    // İçeriden güncellenebilir (Mutable) durum
    private val _uiState = MutableStateFlow(AdminUiState())
    // Dışarıya (UI'ya) açık, sadece okunabilir (Immutable) durum
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    /**
     * Tüm seferleri veritabanından çekip 'Flow'u dinlemeye başlar.
     * Veritabanında bir değişiklik olduğunda (Ekleme/Silme), liste otomatik güncellenir.
     */
    fun loadAllTrips() {
        viewModelScope.launch {
            tripRepository.getAllTrips().collect { trips ->
                _uiState.value = _uiState.value.copy(
                    allTrips = trips,
                    // Eğer aktif bir filtre varsa yeni gelen listeyi de ona göre filtrele
                    filteredTrips = applyFilter(trips, _uiState.value.selectedTypeFilter),
                    isLoading = false
                )
            }
        }
    }

    /**
     * Kullanıcı Tab'lara tıkladığında (Otobüs/Uçak) çalışır.
     * Listeyi filtreler ve UI State'i günceller.
     * @param type Filtrelenecek araç tipi (veya hepsi için null).
     */
    fun filterByType(type: String?) {
        _uiState.value = _uiState.value.copy(
            selectedTypeFilter = type,
            filteredTrips = applyFilter(_uiState.value.allTrips, type)
        )
    }

    // Yardımcı fonksiyon: Listeyi tipe göre süzer
    private fun applyFilter(trips: List<Trip>, type: String?): List<Trip> {
        return if (type == null) trips else trips.filter { it.vehicleType == type }
    }

    /**
     * Yeni bir sefer ekler (Asenkron).
     */
    fun addTrip(trip: Trip) {
        viewModelScope.launch {
            tripRepository.insertTrip(trip)
        }
    }

    /**
     * Mevcut bir seferi günceller.
     */
    fun updateTrip(trip: Trip) {
        viewModelScope.launch {
            tripRepository.updateTrip(trip)
        }
    }

    /**
     * Bir seferi siler.
     */
    fun deleteTrip(trip: Trip) {
        viewModelScope.launch {
            tripRepository.deleteTrip(trip)
        }
    }

    /**
     * Factory Class
     *
     * ViewModel'e parametre (TripRepository) geçebilmek için gereklidir.
     * Normalde ViewModel'ler parametresiz kurucu (constructor) bekler.
     * Bu Factory, Android sistemine "Bu ViewModel'i oluştururken şu Repository'yi içine koy" der.
     */
    class Factory(
        private val tripRepository: TripRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AdminViewModel(tripRepository) as T
        }
    }
}