package com.mobil.travelreservation.ui.screens.trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mobil.travelreservation.data.model.Trip
import com.mobil.travelreservation.data.repository.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * TripListUiState
 *
 * Sefer Listesi ekranının anlık durumunu tutan veri sınıfı.
 *
 * @param trips Listelenecek seferlerin listesi.
 * @param isLoading Veriler yüklenirken gösterilecek progress bar durumu.
 * @param from Kalkış yeri.
 * @param to Varış yeri.
 * @param currentDate Seçili olan tarih (YYYY-MM-DD formatında).
 * @param vehicleType Araç tipi (Otobüs/Uçak).
 */
data class TripListUiState(
    val trips: List<Trip> = emptyList(),
    val isLoading: Boolean = true,
    val from: String = "",
    val to: String = "",
    val currentDate: String = "",
    val vehicleType: String = ""
)

/**
 * TripViewModel
 *
 * Sefer arama ve listeleme ekranının iş mantığını yönetir.
 * 1. Arama parametrelerini alır ve veritabanından sorgular.
 * 2. Tarih değiştirme (Önceki/Sonraki gün) işlemlerini hesaplar.
 * 3. Geçmiş tarihe gidilmesini engeller.
 */
class TripViewModel(
    private val tripRepository: TripRepository
) : ViewModel() {

    // UI Durumunu tutan akış (Flow)
    private val _uiState = MutableStateFlow(TripListUiState())
    val uiState: StateFlow<TripListUiState> = _uiState.asStateFlow()

    // Tarih formatlayıcı (Veritabanı formatı: YYYY-MM-DD)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * Ekran ilk açıldığında Home ekranından gelen parametrelerle aramyı başlatır.
     */
    fun initSearch(from: String, to: String, date: String, vehicleType: String) {
        _uiState.value = _uiState.value.copy(
            from = from,
            to = to,
            currentDate = date,
            vehicleType = vehicleType
        )
        // State güncellendikten sonra aramayı tetikle
        searchTrips(from, to, date, vehicleType)
    }

    /**
     * Veritabanında kriterlere uygun seferleri arar.
     * Coroutines (viewModelScope) kullanarak işlemi arka planda yapar.
     */
    fun searchTrips(from: String, to: String, date: String, vehicleType: String) {
        // Yükleniyor durumunu aç
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            // Repository'den veriyi akış (Flow) olarak al
            tripRepository.searchTripsWithVehicleType(from, to, date, vehicleType).collect { trips ->
                _uiState.value = _uiState.value.copy(
                    trips = trips,
                    isLoading = false
                )
            }
        }
    }

    /**
     * Seçili tarihi bir gün geri alır.
     * Ancak bugünden daha önceki bir tarihe gidilmesini engeller.
     */
    fun previousDay() {
        val currentState = _uiState.value
        try {
            val currentDate = dateFormat.parse(currentState.currentDate) ?: return
            val calendar = Calendar.getInstance()
            calendar.time = currentDate
            calendar.add(Calendar.DAY_OF_YEAR, -1) // 1 gün çıkar

            // --- KONTROL ---
            // Bugünden (saat 00:00:00) daha geriye gidilemez
            val today = Calendar.getInstance()
            today.set(Calendar.HOUR_OF_DAY, 0)
            today.set(Calendar.MINUTE, 0)
            today.set(Calendar.SECOND, 0)
            today.set(Calendar.MILLISECOND, 0)

            // Eğer hesaplanan tarih bugünden önceyse işlemi iptal et
            if (calendar.before(today)) return

            val newDate = dateFormat.format(calendar.time)
            // Sadece tarihi güncelle, UI'daki LaunchedEffect otomatik olarak aramayı tetikleyecek
            _uiState.value = currentState.copy(currentDate = newDate)
        } catch (e: Exception) {
            // Tarih format hatası olursa işlem yapma
        }
    }

    /**
     * Seçili tarihi bir gün ileri alır.
     */
    fun nextDay() {
        val currentState = _uiState.value
        try {
            val currentDate = dateFormat.parse(currentState.currentDate) ?: return
            val calendar = Calendar.getInstance()
            calendar.time = currentDate
            calendar.add(Calendar.DAY_OF_YEAR, 1) // 1 gün ekle

            val newDate = dateFormat.format(calendar.time)
            _uiState.value = currentState.copy(currentDate = newDate)
        } catch (e: Exception) {
            // Hata durumunda bir şey yapma
        }
    }

    /**
     * Factory
     * ViewModel'e TripRepository bağımlılığını enjekte etmek için gereklidir.
     */
    class Factory(
        private val tripRepository: TripRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TripViewModel(tripRepository) as T
        }
    }
}