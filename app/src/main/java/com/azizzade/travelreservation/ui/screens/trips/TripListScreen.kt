package com.azizzade.travelreservation.ui.screens.trips

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azizzade.travelreservation.data.model.Trip
import com.azizzade.travelreservation.ui.screens.home.formatDateTurkish

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripListScreen(
    viewModel: TripViewModel,
    from: String,
    to: String,
    date: String,
    vehicleType: String,
    onTripSelect: (Long) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDetailSheet by remember { mutableStateOf(false) }
    var selectedTrip by remember { mutableStateOf<Trip?>(null) }

    LaunchedEffect(uiState.currentDate) {
        viewModel.searchTrips(from, to, uiState.currentDate, vehicleType)
    }

    LaunchedEffect(Unit) {
        viewModel.initSearch(from, to, date, vehicleType)
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(from, color = Color.White)
                            Icon(
                                Icons.Default.SwapHoriz,
                                contentDescription = null,
                                modifier = Modifier.padding(horizontal = 8.dp),
                                tint = Color.White
                            )
                            Text(to, color = Color.White)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                )
                // Tarih navigasyonu
                DateNavigation(
                    currentDate = uiState.currentDate,
                    onPreviousClick = { viewModel.previousDay() },
                    onNextClick = { viewModel.nextDay() }
                )
            }
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.trips.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            if (vehicleType == "Uçak") Icons.Default.Flight else Icons.Default.DirectionsBus,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Bu tarihte $vehicleType seferi bulunamadı")
                        Text(
                            "Farklı bir tarih deneyin",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(Color(0xFFF5F5F5)),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            "${uiState.trips.size} sefer bulundu",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }

                    items(
                        items = uiState.trips,
                        key = { it.id }, // Key kullanımı performans için kritiktir
                        contentType = { "trip_card" }
                    ) { trip ->
                        TripCard(
                            trip = trip,
                            onSelectClick = { onTripSelect(trip.id) },
                            onDetailClick = {
                                selectedTrip = trip
                                showDetailSheet = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Sefer Detayları Bottom Sheet
    if (showDetailSheet && selectedTrip != null) {
        TripDetailSheet(
            trip = selectedTrip!!,
            onDismiss = { showDetailSheet = false }
        )
    }
}

// Tarih Navigasyonunu ayırdık (Recomposition optimizasyonu)
@Composable
fun DateNavigation(
    currentDate: String,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onPreviousClick,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 4.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(Color.White.copy(alpha = 0.5f))
            )
        ) {
            Icon(Icons.Default.ChevronLeft, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(2.dp))
            Text("Önceki", fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }

        Card(
            modifier = Modifier.padding(horizontal = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    formatDateTurkish(currentDate),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1
                )
            }
        }

        OutlinedButton(
            onClick = onNextClick,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 4.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(Color.White.copy(alpha = 0.5f))
            )
        ) {
            Text("Sonraki", fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.width(2.dp))
            Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun TripCard(
    trip: Trip,
    onSelectClick: () -> Unit,
    onDetailClick: () -> Unit
) {
    // OPTİMİZASYON: Bu hesaplama sadece trip saati değişirse yapılır.
    // Listeyi kaydırırken tekrar tekrar hesaplanmaz.
    val arrivalTime = remember(trip.time, trip.duration) {
        calculateArrivalTime(trip.time, trip.duration)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Üst - Firma ve fiyat
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (trip.vehicleType == "Uçak") "✈" else "🚌",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = trip.companyName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AirlineSeatReclineNormal,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(trip.seatLayout, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }

                Text(
                    text = "${trip.price.toInt()} TL",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Orta - Saat ve süre
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = trip.time,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(trip.departure, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFE3F2FD))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = trip.duration,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.width(40.dp).height(2.dp).background(Color.LightGray))
                        Icon(Icons.Default.ChevronRight, null, Modifier.size(16.dp), Color.LightGray)
                        Box(Modifier.width(40.dp).height(2.dp).background(Color.LightGray))
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = arrivalTime, // remember'lanan değeri kullandık
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(trip.destination, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Alt - Butonlar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onDetailClick,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Sefer Detayları", style = MaterialTheme.typography.bodySmall)
                }

                Button(
                    onClick = onSelectClick,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Koltuk Seç")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ChevronRight, null, Modifier.size(18.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailSheet(
    trip: Trip,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    // OPTİMİZASYON: String parçalama işlemi sadece trip değişirse yapılır.
    val features = remember(trip.features) {
        trip.features.split(",").filter { it.isNotBlank() }
    }
    val routeStops = remember(trip.route) {
        trip.route.split(",").filter { it.isNotBlank() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White
    ) {
        Column(modifier = Modifier.padding(bottom = 32.dp)) {
            // Başlık
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "SEFER DETAYLARI",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                }
            }

            // Tab seçimi
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            if (trip.vehicleType == "Uçak") "Uçak Özellikleri" else "Otobüs Özellikleri"
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Güzergah Bilgisi") }
                )
            }

            // Tab içeriği
            when (selectedTab) {
                0 -> {
                    // Özellikler listesi
                    Column(modifier = Modifier.padding(16.dp)) {
                        features.forEach { feature ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    getFeatureIcon(feature.trim()),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(feature.trim())
                            }
                            HorizontalDivider()
                        }
                    }
                }
                1 -> {
                    // Güzergah bilgisi
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Belirtilen süreler firma tarafından iletilmekte olup tahminidir.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        routeStops.forEach { stop ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val parts = stop.trim().split(" ", limit = 2)
                                Text(
                                    text = parts.getOrElse(0) { "" },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(60.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(parts.getOrElse(1) { stop })
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

fun getFeatureIcon(feature: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when {
        feature.contains("WiFi", ignoreCase = true) -> Icons.Default.Wifi
        feature.contains("Priz", ignoreCase = true) || feature.contains("Şarj", ignoreCase = true) -> Icons.Default.Power
        feature.contains("TV", ignoreCase = true) -> Icons.Default.Tv
        feature.contains("Koltuk", ignoreCase = true) -> Icons.Default.AirlineSeatReclineExtra
        feature.contains("İkram", ignoreCase = true) || feature.contains("Yemek", ignoreCase = true) -> Icons.Default.Restaurant
        feature.contains("Klima", ignoreCase = true) -> Icons.Default.AcUnit
        feature.contains("Battaniye", ignoreCase = true) -> Icons.Default.Bed
        feature.contains("Bagaj", ignoreCase = true) -> Icons.Default.Luggage
        feature.contains("Eğlence", ignoreCase = true) -> Icons.Default.Headphones
        else -> Icons.Default.CheckCircle
    }
}

fun calculateArrivalTime(departureTime: String, duration: String): String {
    try {
        val parts = departureTime.split(":")
        var hours = parts[0].toInt()
        var minutes = parts[1].toInt()

        val durationRegex = """(\d+)s\s*(\d+)?dk?""".toRegex()
        val match = durationRegex.find(duration)
        if (match != null) {
            val durationHours = match.groupValues[1].toIntOrNull() ?: 0
            val durationMinutes = match.groupValues[2].toIntOrNull() ?: 0

            minutes += durationMinutes
            hours += durationHours + (minutes / 60)
            minutes %= 60
            hours %= 24
        }

        return String.format("%02d:%02d", hours, minutes)
    } catch (e: Exception) {
        return "--:--"
    }
}