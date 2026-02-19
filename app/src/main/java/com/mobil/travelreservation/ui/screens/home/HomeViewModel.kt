package com.mobil.travelreservation.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mobil.travelreservation.data.local.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

/**
 * Ana Sayfa ekranının anlık durumunu tutan veri sınıfı.
 *
 * @param userName Giriş yapan kullanıcının adı (Başlıkta göstermek için).
 * @param fromCity Seçilen kalkış şehri.
 * @param toCity Seçilen varış şehri.
 * @param selectedDate Seçilen tarih (Varsayılan: Bugün).
 * @param selectedVehicleType Seçilen araç tipi (Otobüs/Uçak).
 */
data class HomeUiState(
    val userName: String = "",
    val isAdmin: Boolean = false,
    val fromCity: String = "",
    val toCity: String = "",
    val selectedDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val selectedVehicleType: String = "Otobüs"
)

/**
 * HomeViewModel
 *
 * Ana Sayfa (HomeScreen) için veri ve mantık yönetimini sağlar.
 * Kullanıcı seçimlerini yönetir ve arama butonunun aktiflik durumunu kontrol eder.
 */
class HomeViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    // UI durumunu tutan akış (Flow)
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Şehir listesi (Performans için statik liste kullanıldı, veritabanından da çekilebilirdi)
    // 81 İlin Tam Listesi (Alfabetik Sıralı)
    val cities = listOf(
        "Adana", "Adıyaman", "Afyonkarahisar", "Ağrı", "Aksaray", "Amasya", "Ankara", "Antalya", "Ardahan", "Artvin",
        "Aydın", "Balıkesir", "Bartın", "Batman", "Bayburt", "Bilecik", "Bingöl", "Bitlis", "Bolu", "Burdur",
        "Bursa", "Çanakkale", "Çankırı", "Çorum", "Denizli", "Diyarbakır", "Düzce", "Edirne", "Elazığ", "Erzincan",
        "Erzurum", "Eskişehir", "Gaziantep", "Giresun", "Gümüşhane", "Hakkari", "Hatay", "Iğdır", "Isparta", "İstanbul",
        "İzmir", "Kahramanmaraş", "Karabük", "Karaman", "Kars", "Kastamonu", "Kayseri", "Kırıkkale", "Kırklareli", "Kırşehir",
        "Kilis", "Kocaeli", "Konya", "Kütahya", "Malatya", "Manisa", "Mardin", "Mersin", "Muğla", "Muş",
        "Nevşehir", "Niğde", "Ordu", "Osmaniye", "Rize", "Sakarya", "Samsun", "Siirt", "Sinop", "Sivas",
        "Şanlıurfa", "Şırnak", "Tekirdağ", "Tokat", "Trabzon", "Tunceli", "Uşak", "Van", "Yalova", "Yozgat", "Zonguldak"
    )

    init {
        loadUserInfo()
    }

    /**
     * SessionManager'dan giriş yapan kullanıcının adını çeker.
     */
    private fun loadUserInfo() {
        _uiState.value = _uiState.value.copy(
            userName = sessionManager.getUserName(),
            isAdmin = sessionManager.isAdmin()
        )
    }

    // --- GÜNCELLEME FONKSİYONLARI ---

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

    /**
     * Kalkış ve Varış şehirlerini birbiriyle değiştirir (Swap).
     * Kullanıcı deneyimi (UX) için pratik bir özelliktir.
     */
    fun swapCities() {
        val current = _uiState.value
        _uiState.value = current.copy(
            fromCity = current.toCity,
            toCity = current.fromCity
        )
    }

    /**
     * "Sefer Ara" butonunun aktif olup olmayacağına karar verir.
     * Şehirler seçili mi? Şehirler birbirinden farklı mı? Tarih var mı?
     * @return Tüm şartlar sağlanıyorsa true döner.
     */
    fun isSearchValid(): Boolean {
        val state = _uiState.value
        return state.fromCity.isNotBlank() &&
                state.toCity.isNotBlank() &&
                state.fromCity != state.toCity && // Aynı şehirden aynı şehre gidilemez
                state.selectedDate.isNotBlank()
    }

    /**
     * ViewModel'e SessionManager bağımlılığını enjekte etmek için Factory.
     */
    class Factory(
        private val sessionManager: SessionManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(sessionManager) as T
        }
    }
}