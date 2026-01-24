package com.azizzade.travelreservation.ui.screens.admin

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azizzade.travelreservation.data.model.Trip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: AdminViewModel,
    onAddTripClick: () -> Unit,
    onEditTripClick: (Trip) -> Unit,
    onChangePassword: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var tripToDelete by remember { mutableStateOf<Trip?>(null) }
    var selectedTab by remember { mutableStateOf(0) }
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadAllTrips()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Panel") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, "Menü", tint = Color.White)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Şifre Değiştir") },
                            onClick = { showMenu = false; onChangePassword() },
                            leadingIcon = { Icon(Icons.Default.Lock, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Çıkış Yap") },
                            onClick = { showMenu = false; onLogout() },
                            leadingIcon = { Icon(Icons.Default.Logout, null) }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTripClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Sefer Ekle")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab seçimi
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0; viewModel.filterByType("Otobüs") },
                    text = { Text("🚌 Otobüs") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1; viewModel.filterByType("Uçak") },
                    text = { Text("✈️ Uçak") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2; viewModel.filterByType(null) },
                    text = { Text("Tümü") }
                )
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.filteredTrips.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.DirectionsBus, null, Modifier.size(64.dp), Color.LightGray)
                        Spacer(Modifier.height(16.dp))
                        Text("Sefer bulunamadı")
                        Text("Yeni sefer eklemek için + butonuna tıklayın", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.filteredTrips, key = { it.id }) { trip ->
                        AdminTripCard(
                            trip = trip,
                            onEditClick = { onEditTripClick(trip) },
                            onDeleteClick = { tripToDelete = trip }
                        )
                    }
                }
            }
        }
    }

    // Silme dialogu
    tripToDelete?.let { trip ->
        AlertDialog(
            onDismissRequest = { tripToDelete = null },
            icon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Seferi Sil") },
            text = {
                Text("${trip.companyName}\n${trip.departure} → ${trip.destination}\n${trip.date} ${trip.time}\n\nBu seferi silmek istediğinize emin misiniz?")
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteTrip(trip); tripToDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Sil") }
            },
            dismissButton = {
                TextButton(onClick = { tripToDelete = null }) { Text("İptal") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTripCard(
    trip: Trip,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (trip.vehicleType == "Uçak") "✈️" else "🚌",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(trip.companyName, fontWeight = FontWeight.Bold)
                        Text(trip.seatLayout, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
                Text(
                    "${trip.price.toInt()} TL",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("${trip.departure} → ${trip.destination}", fontWeight = FontWeight.Medium)
                    Text("${trip.date} • ${trip.time}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    Text("Süre: ${trip.duration} • ${trip.totalSeats} koltuk", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Düzenle")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = onDeleteClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Sil")
                }
            }
        }
    }
}