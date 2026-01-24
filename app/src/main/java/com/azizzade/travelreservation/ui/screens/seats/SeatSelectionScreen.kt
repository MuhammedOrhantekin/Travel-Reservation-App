package com.azizzade.travelreservation.ui.screens.seats

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
import com.azizzade.travelreservation.data.model.Trip
import kotlin.collections.find

val SeatMale = Color(0xFF64B5F6)
val SeatFemale = Color(0xFFF48FB1)
val SeatAvailable = Color(0xFFE0E0E0)
val SeatSelected = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatSelectionScreen(
    viewModel: SeatViewModel,
    tripId: Long,
    onSeatConfirmed: (Long, Int) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            uiState.trip?.let { trip ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(Color(0xFFF5F5F5))
                ) {
                    // Sefer bilgisi
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

                    // Legend
                    SeatLegend()

                    // Koltuk düzeni
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
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
                            // Şoför
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

                    // Alt buton
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
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

@Composable
fun BusSeatLayout(
    totalSeats: Int,
    reservedSeats: List<com.azizzade.travelreservation.data.local.database.dao.SeatInfo>,
    selectedSeat: Int?,
    onSeatClick: (Int) -> Unit
) {
    // Görseldeki düzen: 40 koltuk, 2+1 format (dikey)
    // Her satırda: [sol2] [sol1] [koridor] [sağ1]
    // Satır 1: 3, 2, _, 1
    // Satır 2: 6, 5, _, 4
    // ... devam eder

    // Satırlar: Her satırda 3 koltuk (2 sol + 1 sağ)
    val rows = listOf(
        listOf(3, 2, null, 1),
        listOf(6, 5, null, 4),
        listOf(9, 8, null, 7),
        listOf(12, 11, null, 10),
        listOf(15, 14, null, 13),
        listOf(18, 17, null, 16),
        listOf(null, null, null, 19),  // Koridor geçişi, sadece sağda 19
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
        // Şoför alanı
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

        // Koltuklar
        rows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { seatNumber ->
                    if (seatNumber == null) {
                        // Koridor boşluğu
                        Spacer(modifier = Modifier.size(44.dp))
                    } else {
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

@Composable
fun PlaneSeatLayout(
    totalSeats: Int,
    reservedSeats: List<com.azizzade.travelreservation.data.local.database.dao.SeatInfo>,
    selectedSeat: Int?,
    onSeatClick: (Int) -> Unit
) {
    // 3+3 düzen: Sol 3 koltuk, koridor, sağ 3 koltuk

    val rows = (totalSeats + 5) / 6

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Sıra numaraları
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("A", "B", "C").forEach {
                Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                    Text(it, fontSize = 12.sp, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.width(24.dp))
            listOf("D", "E", "F").forEach {
                Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                    Text(it, fontSize = 12.sp, color = Color.Gray)
                }
            }
        }

        for (row in 0 until rows) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sıra numarası
                Text("${row + 1}", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.width(16.dp))

                // Sol 3 koltuk
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
                        Spacer(modifier = Modifier.size(36.dp))
                    }
                }

                // Koridor
                Spacer(modifier = Modifier.width(24.dp))

                // Sağ 3 koltuk
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

                // Sıra numarası
                Text("${row + 1}", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.width(16.dp))
            }
        }
    }
}

@Composable
fun SeatItem(
    seatNumber: Int,
    gender: String?,
    isSelected: Boolean,
    onClick: () -> Unit,
    size: Int = 44
) {
    val isReserved = gender != null

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