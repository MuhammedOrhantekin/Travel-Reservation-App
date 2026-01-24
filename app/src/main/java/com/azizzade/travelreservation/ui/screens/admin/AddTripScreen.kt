//package com.azizzade.travelreservation.ui.screens.admin
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.runtime.saveable.rememberSaveable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AddTripScreen(
//    viewModel: AdminViewModel,
//    onTripAdded: () -> Unit,
//    onBackClick: () -> Unit
//) {
//    val cities = listOf("İstanbul", "Ankara", "İzmir", "Antalya", "Bursa", "Adana")
//    val vehicleTypes = listOf("Otobüs", "Uçak")
//
//    val addTripState by viewModel.addTripState.collectAsStateWithLifecycle()
//
//    var departureExpanded by rememberSaveable { mutableStateOf(false) }
//    var destinationExpanded by rememberSaveable { mutableStateOf(false) }
//    var vehicleExpanded by rememberSaveable { mutableStateOf(false) }
//
//    // Başarılı ekleme sonrası
//    LaunchedEffect(addTripState.isSuccess) {
//        if (addTripState.isSuccess) {
//            onTripAdded()
//            viewModel.resetAddTripState()
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Yeni Sefer Ekle") },
//                navigationIcon = {
//                    IconButton(onClick = onBackClick) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = MaterialTheme.colorScheme.primary,
//                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
//                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
//                )
//            )
//        }
//    ) { padding ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding)
//                .padding(24.dp)
//                .verticalScroll(rememberScrollState()),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            // Araç tipi seçimi
//            Text(
//                text = "Araç Tipi",
//                style = MaterialTheme.typography.titleMedium
//            )
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                vehicleTypes.forEach { type ->
//                    FilterChip(
//                        selected = addTripState.vehicleType == type,
//                        onClick = { viewModel.updateVehicleType(type) },
//                        label = { Text(type) },
//                        leadingIcon = {
//                            Text(if (type == "Uçak") "✈" else "🚌")
//                        },
//                        modifier = Modifier.weight(1f)
//                    )
//                }
//            }
//
//            HorizontalDivider()
//
//            // Kalkış şehri
//            ExposedDropdownMenuBox(
//                expanded = departureExpanded,
//                onExpandedChange = { departureExpanded = it }
//            ) {
//                OutlinedTextField(
//                    value = addTripState.departure,
//                    onValueChange = {},
//                    readOnly = true,
//                    label = { Text("Kalkış Şehri") },
//                    leadingIcon = { Icon(Icons.Default.FlightTakeoff, contentDescription = null) },
//                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = departureExpanded) },
//                    modifier = Modifier.fillMaxWidth().menuAnchor()
//                )
//                ExposedDropdownMenu(
//                    expanded = departureExpanded,
//                    onDismissRequest = { departureExpanded = false }
//                ) {
//                    cities.forEach { city ->
//                        DropdownMenuItem(
//                            text = { Text(city) },
//                            onClick = {
//                                viewModel.updateDeparture(city)
//                                departureExpanded = false
//                            }
//                        )
//                    }
//                }
//            }
//
//            // Varış şehri
//            ExposedDropdownMenuBox(
//                expanded = destinationExpanded,
//                onExpandedChange = { destinationExpanded = it }
//            ) {
//                OutlinedTextField(
//                    value = addTripState.destination,
//                    onValueChange = {},
//                    readOnly = true,
//                    label = { Text("Varış Şehri") },
//                    leadingIcon = { Icon(Icons.Default.FlightLand, contentDescription = null) },
//                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = destinationExpanded) },
//                    modifier = Modifier.fillMaxWidth().menuAnchor()
//                )
//                ExposedDropdownMenu(
//                    expanded = destinationExpanded,
//                    onDismissRequest = { destinationExpanded = false }
//                ) {
//                    cities.forEach { city ->
//                        DropdownMenuItem(
//                            text = { Text(city) },
//                            onClick = {
//                                viewModel.updateDestination(city)
//                                destinationExpanded = false
//                            }
//                        )
//                    }
//                }
//            }
//
//            // Tarih
//            OutlinedTextField(
//                value = addTripState.date,
//                onValueChange = { viewModel.updateDate(it) },
//                label = { Text("Tarih (YYYY-MM-DD)") },
//                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
//                singleLine = true,
//                modifier = Modifier.fillMaxWidth()
//            )
//
//            // Saat
//            OutlinedTextField(
//                value = addTripState.time,
//                onValueChange = { viewModel.updateTime(it) },
//                label = { Text("Saat (HH:MM)") },
//                leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
//                singleLine = true,
//                modifier = Modifier.fillMaxWidth()
//            )
//
//            // Fiyat
//            OutlinedTextField(
//                value = addTripState.price,
//                onValueChange = { viewModel.updatePrice(it) },
//                label = { Text("Fiyat (₺)") },
//                leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                singleLine = true,
//                modifier = Modifier.fillMaxWidth()
//            )
//
//            // Bilgi kartı
//            Card(
//                modifier = Modifier.fillMaxWidth(),
//                colors = CardDefaults.cardColors(
//                    containerColor = MaterialTheme.colorScheme.surfaceVariant
//                )
//            ) {
//                Row(
//                    modifier = Modifier.padding(16.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Icon(
//                        Icons.Default.Info,
//                        contentDescription = null,
//                        tint = MaterialTheme.colorScheme.primary
//                    )
//                    Spacer(modifier = Modifier.width(12.dp))
//                    Column {
//                        Text(
//                            text = "Koltuk Bilgisi",
//                            style = MaterialTheme.typography.titleSmall
//                        )
//                        Text(
//                            text = if (addTripState.vehicleType == "Uçak")
//                                "Uçak: 60 koltuk (3+3 düzen)"
//                            else
//                                "Otobüs: 40 koltuk (2+2 düzen)",
//                            style = MaterialTheme.typography.bodySmall
//                        )
//                    }
//                }
//            }
//
//            // Hata mesajı
//            addTripState.errorMessage?.let { error ->
//                Text(
//                    text = error,
//                    color = MaterialTheme.colorScheme.error,
//                    style = MaterialTheme.typography.bodySmall
//                )
//            }
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            // Ekle butonu
//            Button(
//                onClick = { viewModel.addTrip() },
//                enabled = !addTripState.isLoading,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(56.dp)
//            ) {
//                if (addTripState.isLoading) {
//                    CircularProgressIndicator(
//                        modifier = Modifier.size(24.dp),
//                        color = MaterialTheme.colorScheme.onPrimary
//                    )
//                } else {
//                    Icon(Icons.Default.Add, contentDescription = null)
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text("Sefer Ekle")
//                }
//            }
//        }
//    }
//}