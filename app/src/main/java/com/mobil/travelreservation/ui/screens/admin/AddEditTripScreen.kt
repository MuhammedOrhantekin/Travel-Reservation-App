package com.mobil.travelreservation.ui.screens.admin

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mobil.travelreservation.data.model.Trip
import java.util.*

/**
 * AddEditTripScreen
 *
 * Admin kullanıcısının yeni bir sefer eklediği veya mevcut bir seferi düzenlediği ekrandır.
 *
 * İşlevleri:
 * 1. Form Doğrulama: Boş alan bırakılmasını engeller.
 * 2. Mod Kontrolü: editTrip null ise "Ekle", dolu ise "Güncelle" modunda çalışır.
 * 3. Dinamik UI: Araç tipine (Otobüs/Uçak) göre varsayılan koltuk sayılarını ayarlar.
 * 4. Şehir Seçimi: Hatalı giriş olmaması için şehirleri listeden seçtirir.
 *
 * @param viewModel Veritabanı işlemlerini yapan AdminViewModel.
 * @param editTrip Düzenlenecek sefer nesnesi (Yeni eklenecekse null).
 * @param onSaved İşlem başarıyla bitince çalışacak fonksiyon.
 * @param onBackClick Geri butonuna basınca çalışacak fonksiyon.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTripScreen(
    viewModel: AdminViewModel,
    editTrip: Trip? = null,
    onSaved: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    // ViewModel durumunu dinle (Opsiyonel: Loading/Error durumu için)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // --- STATE TANIMLAMALARI (Form Alanları) ---
    // Eğer düzenleme modundaysak (editTrip != null), mevcut değerleri doldur.
    var vehicleType by remember { mutableStateOf(editTrip?.vehicleType ?: "Otobüs") }
    var companyName by remember { mutableStateOf(editTrip?.companyName ?: "") }
    var departure by remember { mutableStateOf(editTrip?.departure ?: "") }
    var destination by remember { mutableStateOf(editTrip?.destination ?: "") }
    var date by remember { mutableStateOf(editTrip?.date ?: "") }
    var time by remember { mutableStateOf(editTrip?.time ?: "") }
    var price by remember { mutableStateOf(editTrip?.price?.toString() ?: "") }
    var duration by remember { mutableStateOf(editTrip?.duration ?: "") }

    // Varsayılan koltuk sayısı: Otobüs ise 40, Uçak ise 180
    var totalSeats by remember { mutableStateOf(editTrip?.totalSeats?.toString() ?: if (vehicleType == "Otobüs") "40" else "180") }
    // Varsayılan düzen: Otobüs ise 2+1, Uçak ise 3+3
    var seatLayout by remember { mutableStateOf(editTrip?.seatLayout ?: if (vehicleType == "Otobüs") "2+1" else "3+3") }

    var features by remember { mutableStateOf(editTrip?.features ?: "") }
    var route by remember { mutableStateOf(editTrip?.route ?: "") }

    // Şehir seçimi dialoglarını kontrol eden bayraklar
    var showDepartureDialog by remember { mutableStateOf(false) }
    var showDestinationDialog by remember { mutableStateOf(false) }

    val isEditMode = editTrip != null

    // --- TARİH VE SAAT SEÇİCİLER (Native Android) ---
    val calendar = Calendar.getInstance()

    // DatePickerDialog (Tarih Seçimi)
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            // Format: YYYY-MM-DD (Veritabanı sıralaması için standart format)
            date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.minDate = System.currentTimeMillis() - 1000 // Geçmiş tarih seçilemez
    }

    // TimePickerDialog (Saat Seçimi)
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            // Format: HH:MM
            time = String.format("%02d:%02d", hourOfDay, minute)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true // 24 saat formatı
    )

    // --- YAN ETKİ (SIDE EFFECT) ---
    // Araç tipi değiştiğinde varsayılan koltuk sayılarını otomatik güncelle.
    // Sadece yeni kayıt modunda çalışır, düzenleme modunda veriyi bozmaz.
    LaunchedEffect(vehicleType) {
        if (!isEditMode) {
            totalSeats = if (vehicleType == "Otobüs") "40" else "180"
            seatLayout = if (vehicleType == "Otobüs") "2+1" else "3+3"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Sefer Düzenle" else "Sefer Ekle") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Geri", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()) // İçerik taşarsa kaydırılabilir yap
        ) {
            // 1. ARAÇ TİPİ SEÇİMİ
            Text("Araç Tipi", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = vehicleType == "Otobüs",
                    onClick = { vehicleType = "Otobüs" },
                    label = { Text("🚌 Otobüs") }
                )
                FilterChip(
                    selected = vehicleType == "Uçak",
                    onClick = { vehicleType = "Uçak" },
                    label = { Text("✈️ Uçak") }
                )
            }

            Spacer(Modifier.height(16.dp))

            // 2. FİRMA ADI
            OutlinedTextField(
                value = companyName,
                onValueChange = { companyName = it },
                label = { Text("Firma Adı *") },
                leadingIcon = { Icon(Icons.Default.Business, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            // 3. KALKIŞ YERİ (Tıklanabilir, Dialog Açar)
            OutlinedTextField(
                value = departure,
                onValueChange = { }, // Elle değiştirmeyi kapatıyoruz
                label = { Text("Kalkış *") },
                leadingIcon = { Icon(Icons.Default.TripOrigin, null) },
                trailingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDepartureDialog = true }, // Tıklayınca dialog aç
                enabled = false, // Klavye açılmasın diye disabled görünümlü
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.Black,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(Modifier.height(12.dp))

            // 4. VARIŞ YERİ (Tıklanabilir, Dialog Açar)
            OutlinedTextField(
                value = destination,
                onValueChange = { },
                label = { Text("Varış *") },
                leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                trailingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDestinationDialog = true },
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.Black,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.error
                )
            )

            Spacer(Modifier.height(12.dp))

            // 5. TARİH VE SAAT (Yan Yana)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tarih Alanı
                OutlinedTextField(
                    value = date,
                    onValueChange = { },
                    label = { Text("Tarih *") },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, null) },
                    modifier = Modifier
                        .weight(1f)
                        .clickable { datePickerDialog.show() },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Black,
                        disabledBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                // Saat Alanı
                OutlinedTextField(
                    value = time,
                    onValueChange = { },
                    label = { Text("Saat *") },
                    leadingIcon = { Icon(Icons.Default.Schedule, null) },
                    modifier = Modifier
                        .weight(1f)
                        .clickable { timePickerDialog.show() },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Black,
                        disabledBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }

            Spacer(Modifier.height(12.dp))

            // 6. FİYAT VE SÜRE (Yan Yana)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Fiyat Alanı (Sadece sayı ve nokta)
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Fiyat (TL) *") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                // Süre Alanı
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Süre *") },
                    placeholder = { Text("5s 30dk") },
                    leadingIcon = { Icon(Icons.Default.Timer, null) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(Modifier.height(12.dp))

            // 7. KOLTUK SAYISI VE DÜZEN (Yan Yana)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = totalSeats,
                    onValueChange = { totalSeats = it.filter { c -> c.isDigit() } },
                    label = { Text("Koltuk Sayısı") },
                    leadingIcon = { Icon(Icons.Default.AirlineSeatReclineNormal, null) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = seatLayout,
                    onValueChange = { seatLayout = it },
                    label = { Text("Düzen") },
                    placeholder = { Text("2+1 veya 3+3") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(Modifier.height(16.dp))

            // 8. ÖZELLİKLER
            Text("Özellikler", fontWeight = FontWeight.Bold)
            Text("Virgülle ayırarak yazın", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = features,
                onValueChange = { features = it },
                placeholder = { Text("WiFi,Priz,TV,İkram,Klima") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(Modifier.height(16.dp))

            // 9. GÜZERGAH
            Text("Güzergah Bilgisi", fontWeight = FontWeight.Bold)
            Text("Her durak: Saat Durak Adı (virgülle ayırın)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = route,
                onValueChange = { route = it },
                placeholder = { Text("08:00 İstanbul Esenler,10:30 Bolu,12:00 Ankara AŞTİ") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(Modifier.height(24.dp))

            // --- KAYDET BUTONU ---
            Button(
                onClick = {
                    // Formdaki verileri Trip nesnesine dönüştür
                    val trip = Trip(
                        id = editTrip?.id ?: 0,
                        departure = departure,
                        destination = destination,
                        date = date,
                        time = time,
                        price = price.toDoubleOrNull() ?: 0.0,
                        vehicleType = vehicleType,
                        totalSeats = totalSeats.toIntOrNull() ?: 40,
                        seatsPerRow = if (vehicleType == "Otobüs") 3 else 6,
                        companyName = companyName,
                        duration = duration,
                        seatLayout = seatLayout,
                        features = features,
                        route = route
                    )

                    // Mod'a göre işlemi yap
                    if (isEditMode) {
                        viewModel.updateTrip(trip)
                    } else {
                        viewModel.addTrip(trip)
                    }
                    onSaved()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                // Zorunlu alanlar dolmadan buton aktif olmaz (Validation)
                enabled = companyName.isNotBlank() && departure.isNotBlank() &&
                        destination.isNotBlank() && date.isNotBlank() &&
                        time.isNotBlank() && price.isNotBlank()
            ) {
                Icon(if (isEditMode) Icons.Default.Save else Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text(if (isEditMode) "Kaydet" else "Sefer Ekle")
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // --- DIALOGLARI GÖSTER/GİZLE ---
    if (showDepartureDialog) {
        CitySearchDialog(
            title = "Kalkış Şehri",
            selectedCity = departure,
            onCitySelected = { departure = it; showDepartureDialog = false },
            onDismiss = { showDepartureDialog = false }
        )
    }

    if (showDestinationDialog) {
        CitySearchDialog(
            title = "Varış Şehri",
            selectedCity = destination,
            onCitySelected = { destination = it; showDestinationDialog = false },
            onDismiss = { showDestinationDialog = false }
        )
    }
}

/**
 * CitySearchDialog
 *
 * İçinde arama çubuğu olan ve 81 ili listeleyen bir açılır pencere (Dialog).
 * Kullanıcı buradan il seçtiğinde ana ekrandaki alan otomatik dolar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitySearchDialog(
    title: String,
    selectedCity: String,
    onCitySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    // Tüm iller listesi
    val cities = listOf(
        "Adana", "Adıyaman", "Afyonkarahisar", "Ağrı", "Aksaray", "Amasya", "Ankara", "Antalya",
        "Artvin", "Aydın", "Balıkesir", "Bartın", "Batman", "Bayburt", "Bilecik", "Bingöl",
        "Bitlis", "Bolu", "Burdur", "Bursa", "Çanakkale", "Çankırı", "Çorum", "Denizli",
        "Diyarbakır", "Düzce", "Edirne", "Elazığ", "Erzincan", "Erzurum", "Eskişehir",
        "Gaziantep", "Giresun", "Gümüşhane", "Hakkari", "Hatay", "Iğdır", "Isparta",
        "İstanbul", "İzmir", "Kahramanmaraş", "Karabük", "Karaman", "Kars", "Kastamonu",
        "Kayseri", "Kırıkkale", "Kırklareli", "Kırşehir", "Kilis", "Kocaeli", "Konya",
        "Kütahya", "Malatya", "Manisa", "Mardin", "Mersin", "Muğla", "Muş", "Nevşehir",
        "Niğde", "Ordu", "Osmaniye", "Rize", "Sakarya", "Samsun", "Siirt", "Sinop",
        "Sivas", "Şanlıurfa", "Şırnak", "Tekirdağ", "Tokat", "Trabzon", "Tunceli",
        "Uşak", "Van", "Yalova", "Yozgat", "Zonguldak"
    )

    // Arama filtresi
    val filteredCities = cities.filter { it.contains(searchQuery, ignoreCase = true) }

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
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                // Kaydırılabilir Liste
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
                                null,
                                tint = if (city == selectedCity) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                city,
                                fontWeight = if (city == selectedCity) FontWeight.Bold else FontWeight.Normal,
                                color = if (city == selectedCity) MaterialTheme.colorScheme.primary else Color.Black
                            )
                            // Seçili olanın yanına tik koy
                            if (city == selectedCity) {
                                Spacer(Modifier.weight(1f))
                                Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
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