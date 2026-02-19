package com.mobil.travelreservation.ui.screens.home

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.*

/**
 * HomeScreen
 *
 * Kullanıcının sefer araması yaptığı Ana Sayfa ekranıdır.
 *
 * İşlevleri:
 * 1. Araç Tipi Seçimi (Otobüs/Uçak)
 * 2. Rota Seçimi (Nereden - Nereye)
 * 3. Tarih Seçimi (Takvim veya Hızlı Butonlar)
 * 4. Arama İşlemi (Validasyon sonrası)
 *
 * Performans Notu:
 * Ekran küçük parçalara (Composable) bölünmüştür. Bu sayede örneğin sadece
 * "Araç Tipi" değiştiğinde tüm ekran değil, sadece o kart yeniden çizilir (Recomposition).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onSearchClick: (from: String, to: String, date: String, vehicleType: String) -> Unit
) {
    // UI Durumunu dinliyoruz (StateFlow)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Dialogların görünürlük durumları (Ekran dönse bile korunur: rememberSaveable)
    var showFromDialog by rememberSaveable { mutableStateOf(false) }
    var showToDialog by rememberSaveable { mutableStateOf(false) }

    // --- DATE PICKER (Tarih Seçici) ---
    // 'remember' kullanarak Calendar ve Dialog nesnelerini sadece bir kez oluşturuyoruz.
    // Her çizimde (recomposition) tekrar oluşturulmaz, performans artar.
    val calendar = remember { Calendar.getInstance() }
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                // Seçilen tarihi formatlayıp ViewModel'e gönderiyoruz
                val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                viewModel.updateDate(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            // Geçmiş tarihlerin seçilmesini engelliyoruz
            datePicker.minDate = System.currentTimeMillis() - 1000
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary) // Arka plan rengi (Mavi)
    ) {
        // Üst Başlık (Merhaba, Kullanıcı!)
        HomeHeader(userName = uiState.userName)

        // Beyaz İçerik Alanı (Köşeleri yuvarlatılmış)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()) // Kaydırma özelliği
        ) {
            // 1. MODÜL: Araç Seçimi
            VehicleSelectionSection(
                selectedVehicleType = uiState.selectedVehicleType,
                onVehicleSelect = viewModel::updateVehicleType
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2. MODÜL: Rota Seçimi (Nereden - Nereye)
            RouteSelectionCard(
                fromCity = uiState.fromCity,
                toCity = uiState.toCity,
                onFromClick = { showFromDialog = true },
                onToClick = { showToDialog = true },
                onSwapClick = viewModel::swapCities // Şehirleri yer değiştir
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. MODÜL: Tarih Seçimi
            DateSelectionCard(
                selectedDate = uiState.selectedDate,
                onDateClick = { datePickerDialog.show() },
                onTodayClick = { viewModel.updateDate(getTodayDate()) },
                onTomorrowClick = { viewModel.updateDate(getTomorrowDate()) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4. MODÜL: Arama Butonu
            SearchButtonSection(
                vehicleType = uiState.selectedVehicleType,
                isEnabled = viewModel.isSearchValid(), // Validasyon kontrolü
                onClick = {
                    onSearchClick(
                        uiState.fromCity,
                        uiState.toCity,
                        uiState.selectedDate,
                        uiState.selectedVehicleType
                    )
                }
            )

            // Hata Mesajı (Şehirler aynıysa)
            if (uiState.fromCity == uiState.toCity && uiState.fromCity.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Kalkış ve varış şehri aynı olamaz",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bilgilendirme Kartı (Statik İçerik)
            InfoCard()
        }
    }

    // --- DİALOGLAR ---
    // Eğer durum true ise ekrana çizilirler
    if (showFromDialog) {
        CitySelectionDialog(
            title = "Nereden",
            cities = viewModel.cities,
            selectedCity = uiState.fromCity,
            onCitySelected = {
                viewModel.updateFromCity(it)
                showFromDialog = false
            },
            onDismiss = { showFromDialog = false }
        )
    }

    if (showToDialog) {
        CitySelectionDialog(
            title = "Nereye",
            cities = viewModel.cities,
            selectedCity = uiState.toCity,
            onCitySelected = {
                viewModel.updateToCity(it)
                showToDialog = false
            },
            onDismiss = { showToDialog = false }
        )
    }
}

// --- MODÜLER BİLEŞENLER (COMPONENTS) ---

/**
 * Kullanıcı ismini gösteren basit başlık bileşeni.
 */
@Composable
fun HomeHeader(userName: String) {
    Text(
        text = "Merhaba, $userName!",
        style = MaterialTheme.typography.titleMedium,
        color = Color.White,
        modifier = Modifier.padding(16.dp)
    )
}

/**
 * Otobüs ve Uçak seçeneklerini yan yana gösteren alan.
 */
@Composable
fun VehicleSelectionSection(
    selectedVehicleType: String,
    onVehicleSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        VehicleTypeCard(
            icon = "🚌",
            label = "Otobüs",
            isSelected = selectedVehicleType == "Otobüs",
            onClick = { onVehicleSelect("Otobüs") },
            modifier = Modifier.weight(1f)
        )
        VehicleTypeCard(
            icon = "✈️",
            label = "Uçak",
            isSelected = selectedVehicleType == "Uçak",
            onClick = { onVehicleSelect("Uçak") },
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Nereden ve Nereye şehirlerinin seçildiği, aralarında değiştirme (Swap) butonunun olduğu kart.
 */
@Composable
fun RouteSelectionCard(
    fromCity: String,
    toCity: String,
    onFromClick: () -> Unit,
    onToClick: () -> Unit,
    onSwapClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            // Nereden Satırı
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onFromClick)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.TripOrigin,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("NEREDEN", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(
                        text = fromCity.ifEmpty { "Şehir seçin" },
                        style = MaterialTheme.typography.titleMedium,
                        color = if (fromCity.isEmpty()) Color.Gray else Color.Black
                    )
                }
                // Swap (Yer Değiştirme) Butonu
                IconButton(
                    onClick = onSwapClick,
                    modifier = Modifier.border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Default.SwapVert, "Yer Değiştir", tint = MaterialTheme.colorScheme.primary)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Nereye Satırı
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToClick)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("NEREYE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(
                        text = toCity.ifEmpty { "Şehir seçin" },
                        style = MaterialTheme.typography.titleMedium,
                        color = if (toCity.isEmpty()) Color.Gray else Color.Black
                    )
                }
            }
        }
    }
}

/**
 * Takvim ikonuna tıklayarak veya "Bugün/Yarın" butonları ile tarih seçilen alan.
 */
@Composable
fun DateSelectionCard(
    selectedDate: String,
    onDateClick: () -> Unit,
    onTodayClick: () -> Unit,
    onTomorrowClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Tarih Gösterimi
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onDateClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("GİDİŞ TARİHİ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(text = formatDateTurkish(selectedDate), style = MaterialTheme.typography.titleMedium)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Hızlı Seçim Butonları (Bugün / Yarın)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickDateButton(
                    text = "Bugün",
                    isSelected = selectedDate == getTodayDate(),
                    onClick = onTodayClick,
                    modifier = Modifier.weight(1f)
                )
                QuickDateButton(
                    text = "Yarın",
                    isSelected = selectedDate == getTomorrowDate(),
                    onClick = onTomorrowClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun SearchButtonSection(
    vehicleType: String,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = isEnabled, // Şehirler ve tarih seçilmeden aktif olmaz
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text = "$vehicleType Ara", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun InfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Kesintisiz iade hakkı ve 0 komisyon ile güvenli rezervasyon",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/**
 * Araç tipi seçiminde kullanılan tekil kart bileşeni.
 */
@Composable
fun VehicleTypeCard(
    icon: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.White
        ),
        // Seçili değilse gri kenarlık ekle
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = icon, fontSize = 40.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
            )
        }
    }
}

@Composable
fun QuickDateButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.primary
        )
    ) {
        Text(text)
    }
}

/**
 * CitySelectionDialog
 *
 * Şehir arama ve seçme penceresi.
 * Liste çok uzun olduğu için arama çubuğu ve filtrelenebilir liste içerir.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitySelectionDialog(
    title: String,
    cities: List<String>,
    selectedCity: String,
    onCitySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }

    // --- OPTİMİZASYON ---
    // 'remember' kullanarak filtreleme işleminin her çizimde (recomposition) tekrar yapılmasını engelliyoruz.
    // Sadece 'searchQuery' veya 'cities' değiştiğinde liste yeniden filtrelenir.
    val filteredCities = remember(searchQuery, cities) {
        cities.filter { it.contains(searchQuery, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                // Arama Kutusu
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Şehir ara...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Şehir Listesi
                Column(
                    modifier = Modifier
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    filteredCities.forEach { city ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCitySelected(city) } // Seç ve Kapat
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationCity,
                                contentDescription = null,
                                tint = if (city == selectedCity) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = city,
                                fontWeight = if (city == selectedCity) FontWeight.Bold else FontWeight.Normal,
                                color = if (city == selectedCity) MaterialTheme.colorScheme.primary else Color.Black
                            )
                            // Seçili olanın yanına tik koy
                            if (city == selectedCity) {
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Kapat") }
        }
    )
}

// --- YARDIMCI FONKSİYONLAR ---

fun formatDateTurkish(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("d MMMM EEEE", Locale("tr"))
        val date = inputFormat.parse(dateString)
        outputFormat.format(date!!)
    } catch (e: Exception) {
        dateString
    }
}

fun getTodayDate(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date())
}

fun getTomorrowDate(): String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, 1)
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(calendar.time)
}