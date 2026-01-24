package com.azizzade.travelreservation.ui.screens.admin


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
import com.azizzade.travelreservation.data.model.Trip
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTripScreen(
    viewModel: AdminViewModel,
    editTrip: Trip? = null,
    onSaved: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var vehicleType by remember { mutableStateOf(editTrip?.vehicleType ?: "Otobüs") }
    var companyName by remember { mutableStateOf(editTrip?.companyName ?: "") }
    var departure by remember { mutableStateOf(editTrip?.departure ?: "") }
    var destination by remember { mutableStateOf(editTrip?.destination ?: "") }
    var date by remember { mutableStateOf(editTrip?.date ?: "") }
    var time by remember { mutableStateOf(editTrip?.time ?: "") }
    var price by remember { mutableStateOf(editTrip?.price?.toString() ?: "") }
    var duration by remember { mutableStateOf(editTrip?.duration ?: "") }
    var totalSeats by remember { mutableStateOf(editTrip?.totalSeats?.toString() ?: if (vehicleType == "Otobüs") "40" else "180") }
    var seatLayout by remember { mutableStateOf(editTrip?.seatLayout ?: if (vehicleType == "Otobüs") "2+1" else "3+3") }
    var features by remember { mutableStateOf(editTrip?.features ?: "") }
    var route by remember { mutableStateOf(editTrip?.route ?: "") }

    var showDepartureDialog by remember { mutableStateOf(false) }
    var showDestinationDialog by remember { mutableStateOf(false) }

    val isEditMode = editTrip != null

    // DatePicker
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.minDate = System.currentTimeMillis() - 1000
    }

    // TimePicker
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            time = String.format("%02d:%02d", hourOfDay, minute)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    // Araç tipi değiştiğinde varsayılanları güncelle
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
                .verticalScroll(rememberScrollState())
        ) {
            // Araç Tipi
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

            // Firma adı
            OutlinedTextField(
                value = companyName,
                onValueChange = { companyName = it },
                label = { Text("Firma Adı *") },
                leadingIcon = { Icon(Icons.Default.Business, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            // Kalkış
            OutlinedTextField(
                value = departure,
                onValueChange = { },
                label = { Text("Kalkış *") },
                leadingIcon = { Icon(Icons.Default.TripOrigin, null) },
                trailingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDepartureDialog = true },
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.Black,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(Modifier.height(12.dp))

            // Varış
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

            // Tarih ve Saat
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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

            // Fiyat ve Süre
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Fiyat (TL) *") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
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

            // Koltuk sayısı ve düzeni
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

            // Özellikler
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

            // Güzergah
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

            // Kaydet butonu
            Button(
                onClick = {
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

    // Şehir seçim dialogları
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitySearchDialog(
    title: String,
    selectedCity: String,
    onCitySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

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

    val filteredCities = cities.filter { it.contains(searchQuery, ignoreCase = true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Şehir ara...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    filteredCities.forEach { city ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCitySelected(city) }
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