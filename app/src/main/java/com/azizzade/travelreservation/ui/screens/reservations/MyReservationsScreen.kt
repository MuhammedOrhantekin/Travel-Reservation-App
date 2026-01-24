package com.azizzade.travelreservation.ui.screens.reservations

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azizzade.travelreservation.data.model.Trip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReservationsScreen(
    viewModel: ReservationViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seyahatlerim") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
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
            uiState.reservations.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.EventBusy,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Henüz rezervasyonunuz yok",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )
                        Text(
                            "Sefer arayarak ilk rezervasyonunuzu yapın",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            "${uiState.reservations.size} rezervasyonunuz var",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }

                    items(
                        items = uiState.reservations,
                        key = { it.reservation.id }
                    ) { reservationWithTrip ->
                        ReservationCard(
                            reservationWithTrip = reservationWithTrip,
                            onShare = {
                                reservationWithTrip.trip?.let { trip ->
                                    shareTicket(
                                        context = context,
                                        trip = trip,
                                        seat = reservationWithTrip.reservation.seatNumber,
                                        passenger = reservationWithTrip.reservation.passengerName
                                    )
                                }
                            },
                            onCancel = {
                                viewModel.showCancelDialog(reservationWithTrip)
                            }
                        )
                    }
                }
            }
        }

        // İptal dialog
        if (uiState.showCancelDialog && uiState.selectedReservation != null) {
            CancelReservationDialog(
                reservationWithTrip = uiState.selectedReservation!!,
                onConfirm = { viewModel.cancelReservation() },
                onDismiss = { viewModel.hideCancelDialog() }
            )
        }
    }
}

@Composable
fun ReservationCard(
    reservationWithTrip: ReservationWithTrip,
    onShare: () -> Unit,
    onCancel: () -> Unit
) {
    val reservation = reservationWithTrip.reservation
    val trip = reservationWithTrip.trip

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Üst kısım
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (trip?.vehicleType == "Uçak") "✈" else "🚌",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = trip?.companyName ?: "Bilinmiyor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = trip?.let { "${it.departure} → ${it.destination}" } ?: "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                trip?.let {
                    Text(
                        text = "${it.price.toInt()} TL",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            // Detaylar
            Row {
                Column(modifier = Modifier.weight(1f)) {
                    DetailRow(Icons.Default.CalendarMonth, trip?.date ?: "")
                    DetailRow(Icons.Default.Schedule, trip?.time ?: "")
                }
                Column(modifier = Modifier.weight(1f)) {
                    DetailRow(Icons.Default.AirlineSeatReclineNormal, "Koltuk: ${reservation.seatNumber}")
                    DetailRow(Icons.Default.Person, reservation.passengerName)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Butonlar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onShare,
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Paylaş")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("İptal")
                }
            }
        }
    }
}

@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun CancelReservationDialog(
    reservationWithTrip: ReservationWithTrip,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val trip = reservationWithTrip.trip
    val reservation = reservationWithTrip.reservation

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, contentDescription = null) },
        title = { Text("Rezervasyon İptali") },
        text = {
            Column {
                Text("Bu rezervasyonu iptal etmek istediğinize emin misiniz?")
                Spacer(modifier = Modifier.height(12.dp))
                trip?.let {
                    Text("${it.companyName}")
                    Text("${it.departure} → ${it.destination}")
                    Text("${it.date} - ${it.time}")
                }
                Text("Koltuk: ${reservation.seatNumber}")
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("İptal Et")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Vazgeç")
            }
        }
    )
}

fun shareTicket(context: Context, trip: Trip, seat: Int, passenger: String) {
    val ticketInfo = buildString {
        appendLine("🎫 SEYAHAT BİLETİ")
        appendLine("━━━━━━━━━━━━━━━━━━")
        appendLine("🚌 ${trip.companyName}")
        appendLine("📍 ${trip.departure} → ${trip.destination}")
        appendLine("📅 ${trip.date} - ${trip.time}")
        appendLine("⏱ ${trip.duration}")
        appendLine("💺 Koltuk: $seat")
        appendLine("👤 $passenger")
        appendLine("━━━━━━━━━━━━━━━━━━")
        appendLine("💰 ${trip.price.toInt()} TL")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, ticketInfo)
    }
    context.startActivity(Intent.createChooser(intent, "Bileti Paylaş"))
}