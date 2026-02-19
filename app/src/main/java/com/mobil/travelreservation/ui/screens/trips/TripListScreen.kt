package com.mobil.travelreservation.ui.screens.trips

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
import com.mobil.travelreservation.data.model.Trip
import com.mobil.travelreservation.ui.screens.home.formatDateTurkish

/**
 * TripListScreen
 *
 * Arama kriterlerine (Nereden, Nereye, Tarih) uygun seferlerin listelendiği ekrandır.
 *
 * İşlevleri:
 * 1. Liste Gösterimi: Seferleri LazyColumn ile listeler.
 * 2. Tarih Navigasyonu: Önceki/Sonraki gün butonları ile hızlı tarih değişimi sağlar.
 * 3. Sefer Detayı: "Sefer Detayları" butonuna basınca alttan açılan pencere (Bottom Sheet) gösterir.
 * 4. Boş Durum: Sefer yoksa kullanıcıya bilgi verir.
 *
 * @param viewModel Sefer verilerini yöneten TripViewModel.
 * @param from Kalkış şehri.
 * @param to Varış şehri.
 * @param date Seçilen tarih.
 * @param vehicleType Araç tipi (Otobüs/Uçak).
 * @param onTripSelect Sefer seçildiğinde çalışacak fonksiyon (Koltuk seçimine gider).
 */
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
    // ViewModel durumunu dinle (Liste ve Yüklenme durumu)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // BottomSheet kontrolü için yerel state
    var showDetailSheet by remember { mutableStateOf(false) }
    var selectedTrip by remember { mutableStateOf<Trip?>(null) }

    // Tarih değiştiğinde (Önceki/Sonraki gün) yeniden arama yap
    LaunchedEffect(uiState.currentDate) {
        viewModel.searchTrips(from, to, uiState.currentDate, vehicleType)
    }

    // Ekran ilk açıldığında ViewModel'i başlat
    LaunchedEffect(Unit) {
        viewModel.initSearch(from, to, date, vehicleType)
    }

    Scaffold(
        topBar = {
            Column {
                // Standart Üst Bar (Geri butonu ve Şehirler)
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

                // Özel Tarih Navigasyonu (Önceki - Bugün - Sonraki)
                DateNavigation(
                    currentDate = uiState.currentDate,
                    onPreviousClick = { viewModel.previousDay() },
                    onNextClick = { viewModel.nextDay() }
                )
            }
        }
    ) { padding ->
        // Duruma göre içerik gösterimi
        when {
            // 1. YÜKLENİYOR
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            // 2. BOŞ LİSTE
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
            // 3. SEFER LİSTESİ
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(Color(0xFFF5F5F5)), // Hafif gri arka plan
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Liste Başı (Kaç sefer bulundu?)
                    item {
                        Text(
                            "${uiState.trips.size} sefer bulundu",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }

                    // Sefer Kartları
                    items(
                        items = uiState.trips,
                        key = { it.id }, // Performans için ID anahtarı
                        contentType = { "trip_card" } // Render optimizasyonu
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

    // --- BOTTOM SHEET (Sefer Detayları) ---
    if (showDetailSheet && selectedTrip != null) {
        TripDetailSheet(
            trip = selectedTrip!!,
            onDismiss = { showDetailSheet = false }
        )
    }
}

/**
 * Tarih Navigasyonu Bileşeni.
 * Önceki ve Sonraki gün butonlarını içerir. Recomposition'ı azaltmak için ayrı fonksiyon yapıldı.
 */
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
        // Önceki Gün Butonu
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

        // Ortadaki Tarih Göstergesi
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

        // Sonraki Gün Butonu
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

/**
 * TripCard
 *
 * Listedeki her bir seferi gösteren kart tasarımı.
 * Saat, fiyat, süre ve firma bilgilerini içerir.
 */
@Composable
fun TripCard(
    trip: Trip,
    onSelectClick: () -> Unit,
    onDetailClick: () -> Unit
) {
    // --- OPTİMİZASYON ---
    // Varış saatini hesaplamak maliyetli olabilir. Liste kaydırılırken her karede tekrar hesaplanmasın diye
    // 'remember' kullanarak sonucu önbelleğe alıyoruz. Sadece saat veya süre değişirse tekrar hesaplanır.
    val arrivalTime = remember(trip.time, trip.duration) {
        calculateArrivalTime(trip.time, trip.duration)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Üst Kısım: Araç İkonu, Firma, Koltuk Düzeni, Fiyat
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

            // Orta Kısım: Kalkış Saati -> Süre -> Varış Saati
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Kalkış
                Column {
                    Text(
                        text = trip.time,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(trip.departure, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }

                // Süre Göstergesi (Ortadaki Çizgi ve İkon)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFE3F2FD)) // Açık mavi arka plan
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
                    // Çizgi ve Ok
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.width(40.dp).height(2.dp).background(Color.LightGray))
                        Icon(Icons.Default.ChevronRight, null, Modifier.size(16.dp), Color.LightGray)
                        Box(Modifier.width(40.dp).height(2.dp).background(Color.LightGray))
                    }
                }

                // Varış (Hesaplanan değer)
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = arrivalTime,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(trip.destination, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Alt Kısım: Detaylar ve Seç Butonları
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

/**
 * TripDetailSheet
 *
 * Sefer detaylarına (Özellikler, Güzergah) basıldığında alttan açılan pencere (Bottom Sheet).
 * Sekmeli yapı (TabRow) kullanır.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailSheet(
    trip: Trip,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    // OPTİMİZASYON: String parçalama işlemleri (split) maliyetlidir.
    // 'remember' kullanarak bu işlemi sadece trip değişirse yapıyoruz.
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
            // Başlık Çubuğu
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

            // Sekmeler (Özellikler / Güzergah)
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

            // Sekme İçeriği
            when (selectedTab) {
                0 -> {
                    // ÖZELLİKLER TAB
                    Column(modifier = Modifier.padding(16.dp)) {
                        features.forEach { feature ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Metne uygun ikon bulup göster
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
                    // GÜZERGAH TAB
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
                                // "08:00 İstanbul" metnini boşluktan böl
                                val parts = stop.trim().split(" ", limit = 2)
                                // Saat kısmı kalın, yer kısmı normal
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

/**
 * Metin içeriğine göre uygun ikonu döndüren yardımcı fonksiyon.
 * Örn: "WiFi" kelimesi geçiyorsa WiFi ikonu döner.
 */
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
        else -> Icons.Default.CheckCircle // Varsayılan ikon
    }
}

/**
 * Kalkış saati ve süreye ("5s 30dk") göre Varış saatini hesaplayan fonksiyon.
 * Örn: 10:00 + 2s 30dk = 12:30
 */
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
        return "--:--" // Hata durumunda
    }
}