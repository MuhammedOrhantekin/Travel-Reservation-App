package com.mobil.travelreservation.ui.screens.seats

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.collections.find

// --- SABİT RENKLER ---
// Koltuk durumlarını temsil eden renkler
val SeatMale = Color(0xFF64B5F6)      // Erkek Dolu (Mavi)
val SeatFemale = Color(0xFFF48FB1)    // Kadın Dolu (Pembe)
val SeatAvailable = Color(0xFFE0E0E0) // Boş (Gri)
val SeatSelected = Color(0xFF4CAF50)  // Seçili (Yeşil)

/**
 * SeatSelectionScreen
 *
 * Kullanıcının sefere ait koltuk düzenini gördüğü ve koltuk seçtiği ekrandır.
 *
 * İşlevleri:
 * 1. Sefer Tipi Kontrolü: Otobüs ise 2+1 düzeni, Uçak ise 3+3 düzeni çizer.
 * 2. Cinsiyet Gösterimi: Dolu koltukları yolcunun cinsiyetine göre (Mavi/Pembe) boyar.
 * 3. Seçim Mantığı: Kullanıcı sadece boş koltukları seçebilir.
 * 4. Fiyat Onayı: Seçilen koltukla birlikte toplam tutarı gösterir.
 *
 * @param viewModel Koltuk verilerini ve seçim durumunu yöneten ViewModel.
 * @param tripId Hangi seferin koltuklarının yükleneceğini belirten ID.
 * @param onSeatConfirmed Koltuk seçilip "Devam Et" denildiğinde çalışacak fonksiyon (Ödeme ekranına gider).
 * @param onBackClick Geri tuşuna basıldığında çalışacak fonksiyon.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatSelectionScreen(
    viewModel: SeatViewModel,
    tripId: Long,
    onSeatConfirmed: (Long, Int) -> Unit,
    onBackClick: () -> Unit
) {
    // ViewModel'den UI durumunu dinliyoruz
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Ekran açıldığında sefer detaylarını ve dolu koltukları yükle
    LaunchedEffect(tripId) {
        viewModel.loadTrip(tripId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Koltuk Seçimi") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        // Yükleniyor durumu kontrolü
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // Sefer verisi yüklendiyse ekranı çiz
            uiState.trip?.let { trip ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(Color(0xFFF5F5F5)) // Hafif gri arka plan
                ) {
                    // 1. SEFER BİLGİ KARTI (Üst Kısım)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(trip.companyName, fontWeight = FontWeight.Bold)
                                Text(
                                    "${trip.departure} → ${trip.destination}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "${trip.date} • ${trip.time}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                            Text(
                                "${trip.price.toInt()} TL",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // 2. RENK AÇIKLAMALARI (Legend)
                    SeatLegend()

                    // 3. KOLTUK DÜZENİ (Scroll Edilebilir Alan)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f) // Kalan tüm alanı kapla
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Araç Tipi İkonu ve Düzen Bilgisi
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Icon(
                                    if (trip.vehicleType == "Uçak") Icons.Default.Flight else Icons.Default.DirectionsBus,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${trip.seatLayout} Düzen", color = Color.Gray)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Araç tipine göre uygun çizim fonksiyonunu çağır
                            if (trip.vehicleType == "Otobüs") {
                                // OTOBÜS 2+1 DÜZENİ
                                BusSeatLayout(
                                    totalSeats = trip.totalSeats,
                                    reservedSeats = uiState.reservedSeatsWithGender,
                                    selectedSeat = uiState.selectedSeat,
                                    onSeatClick = { viewModel.selectSeat(it) }
                                )
                            } else {
                                // UÇAK 3+3 DÜZENİ
                                PlaneSeatLayout(
                                    totalSeats = trip.totalSeats,
                                    reservedSeats = uiState.reservedSeatsWithGender,
                                    selectedSeat = uiState.selectedSeat,
                                    onSeatClick = { viewModel.selectSeat(it) }
                                )
                            }
                        }
                    }

                    // 4. ALT BUTON (Onayla ve Devam Et)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Seçilen koltuk bilgisini göster
                            if (uiState.selectedSeat != null) {
                                Text(
                                    "Seçilen Koltuk: ${uiState.selectedSeat}",
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            Button(
                                onClick = {
                                    uiState.selectedSeat?.let { seat ->
                                        onSeatConfirmed(tripId, seat)
                                    }
                                },
                                // Koltuk seçilmediyse buton pasif
                                enabled = uiState.selectedSeat != null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    if (uiState.selectedSeat != null)
                                        "Onayla ve Devam Et - ${uiState.trip?.price?.toInt()} TL"
                                    else
                                        "Lütfen koltuk seçin"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Renklerin ne anlama geldiğini gösteren açıklama satırı.
 */
@Composable
fun SeatLegend() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LegendItem(color = SeatMale, text = "Dolu - Erkek")
        LegendItem(color = SeatFemale, text = "Dolu - Kadın")
        LegendItem(color = SeatAvailable, text = "Boş Koltuk")
    }
}

@Composable
fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(color, RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}

/**
 * BusSeatLayout
 *
 * Otobüsler için özel 2+1 koltuk düzenini çizer.
 * Bu düzen genellikle: Sol tarafta ikili koltuk, sağ tarafta tekli koltuk şeklindedir.
 *
 * @param totalSeats Toplam koltuk sayısı.
 * @param reservedSeats Dolu koltukların listesi (Cinsiyet bilgisiyle).
 * @param selectedSeat Şu an seçili olan koltuk numarası.
 * @param onSeatClick Koltuğa tıklandığında çalışacak fonksiyon.
 */
@Composable
fun BusSeatLayout(
    totalSeats: Int,
    reservedSeats: List<com.mobil.travelreservation.data.local.database.dao.SeatInfo>,
    selectedSeat: Int?,
    onSeatClick: (Int) -> Unit
) {
    // Manuel olarak tanımlanmış Otobüs Koltuk Haritası (2+1 Düzen)
    // Listelerin içindeki sayılar koltuk numarasını, 'null' ise koridoru temsil eder.
    // Örnek: [3, 2, null, 1] -> 3 (Cam kenarı), 2 (Koridor yanı), BOŞLUK, 1 (Tekli koltuk)
    val rows = listOf(
        listOf(3, 2, null, 1),
        listOf(6, 5, null, 4),
        listOf(9, 8, null, 7),
        listOf(12, 11, null, 10),
        listOf(15, 14, null, 13),
        listOf(18, 17, null, 16),
        listOf(null, null, null, 19),  // Orta kapı boşluğu (Sadece sağda koltuk var)
        listOf(21, 20, null, 22),
        listOf(25, 24, null, 23),
        listOf(28, 27, null, 26),
        listOf(31, 30, null, 29),
        listOf(34, 33, null, 32),
        listOf(37, 36, null, 35),
        listOf(40, 39, null, 38)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Şoför Koltuğu (Görsel Amaçlı)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.DirectionsBus,
                    contentDescription = "Şoför",
                    tint = Color.Gray,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Koltuk Satırlarını Çiz
        rows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { seatNumber ->
                    if (seatNumber == null) {
                        // Koridor boşluğu için görünmez bir kutu koyuyoruz
                        Spacer(modifier = Modifier.size(44.dp))
                    } else {
                        // Gerçek koltuk kutusu
                        SeatItem(
                            seatNumber = seatNumber,
                            gender = reservedSeats.find { it.seatNumber == seatNumber }?.passengerGender,
                            isSelected = selectedSeat == seatNumber,
                            onClick = { onSeatClick(seatNumber) },
                            size = 44
                        )
                    }
                }
            }
        }
    }
}

/**
 * PlaneSeatLayout
 *
 * Uçaklar için standart 3+3 (ABC - DEF) koltuk düzenini çizer.
 * Otobüsün aksine bu düzen matematiksel döngü ile oluşturulur.
 */
@Composable
fun PlaneSeatLayout(
    totalSeats: Int,
    reservedSeats: List<com.mobil.travelreservation.data.local.database.dao.SeatInfo>,
    selectedSeat: Int?,
    onSeatClick: (Int) -> Unit
) {
    // Toplam koltuk sayısına göre kaç sıra olacağını hesapla (Her sıra 6 koltuk)
    val rows = (totalSeats + 5) / 6

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Üst Harfler (A B C   D E F)
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("A", "B", "C").forEach {
                Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                    Text(it, fontSize = 12.sp, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.width(24.dp)) // Koridor boşluğu
            listOf("D", "E", "F").forEach {
                Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                    Text(it, fontSize = 12.sp, color = Color.Gray)
                }
            }
        }

        // Satırları Döngüyle Oluştur
        for (row in 0 until rows) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sol Sıra Numarası (1, 2, 3...)
                Text("${row + 1}", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.width(16.dp))

                // Sol Blok (3 Koltuk)
                for (col in 0 until 3) {
                    val seatNum = row * 6 + col + 1
                    if (seatNum <= totalSeats) {
                        SeatItem(
                            seatNumber = seatNum,
                            gender = reservedSeats.find { it.seatNumber == seatNum }?.passengerGender,
                            isSelected = selectedSeat == seatNum,
                            onClick = { onSeatClick(seatNum) },
                            size = 36
                        )
                    } else {
                        // Eğer toplam koltuk sayısı bittiyse boşluk koy
                        Spacer(modifier = Modifier.size(36.dp))
                    }
                }

                // Koridor Boşluğu
                Spacer(modifier = Modifier.width(24.dp))

                // Sağ Blok (3 Koltuk)
                for (col in 3 until 6) {
                    val seatNum = row * 6 + col + 1
                    if (seatNum <= totalSeats) {
                        SeatItem(
                            seatNumber = seatNum,
                            gender = reservedSeats.find { it.seatNumber == seatNum }?.passengerGender,
                            isSelected = selectedSeat == seatNum,
                            onClick = { onSeatClick(seatNum) },
                            size = 36
                        )
                    } else {
                        Spacer(modifier = Modifier.size(36.dp))
                    }
                }

                // Sağ Sıra Numarası
                Text("${row + 1}", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.width(16.dp))
            }
        }
    }
}

/**
 * SeatItem
 *
 * Tek bir koltuğu temsil eden kutucuk.
 *
 * Durumlar:
 * 1. Dolu (Erkek): Mavi, tıklanamaz.
 * 2. Dolu (Kadın): Pembe, tıklanamaz.
 * 3. Seçili: Yeşil, beyaz çerçeveli.
 * 4. Boş: Gri, tıklanabilir.
 */
@Composable
fun SeatItem(
    seatNumber: Int,
    gender: String?, // null ise boş, "Erkek" veya "Kadın" ise dolu
    isSelected: Boolean,
    onClick: () -> Unit,
    size: Int = 44
) {
    val isReserved = gender != null

    // Duruma göre arka plan rengini belirle
    val backgroundColor = when {
        isSelected -> SeatSelected
        gender == "Erkek" -> SeatMale
        gender == "Kadın" -> SeatFemale
        else -> SeatAvailable
    }

    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color.White else Color.Gray.copy(alpha = 0.3f),
                shape = RoundedCornerShape(6.dp)
            )
            // Eğer rezerve edilmişse tıklamayı engelle
            .clickable(enabled = !isReserved, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = seatNumber.toString(),
            color = if (isSelected || isReserved) Color.White else Color.Black,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}