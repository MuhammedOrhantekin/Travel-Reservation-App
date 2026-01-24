package com.azizzade.travelreservation.ui.screens.payment

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azizzade.travelreservation.data.model.PaymentCard
import com.azizzade.travelreservation.data.model.Trip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    viewModel: PaymentViewModel,
    tripId: Long,
    seatNumber: Int,
    onPaymentComplete: () -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(tripId, seatNumber) {
        viewModel.loadTripAndSeat(tripId, seatNumber)
    }

    LaunchedEffect(uiState.paymentComplete) {
        if (uiState.paymentComplete) {
            uiState.trip?.let { trip ->
                shareTicket(context, trip, seatNumber, uiState.passengerName)
            }
            onPaymentComplete()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ödeme") },
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
            // Ana içerik: Parçalanmış yapı sayesinde input girişleri tüm ekranı etkilemez
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF5F5F5))
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. PARÇA: SEFER ÖZETİ (Statik - Inputlardan etkilenmez)
                TripSummaryCard(trip = uiState.trip, seatNumber = seatNumber)

                // 2. PARÇA: YOLCU BİLGİLERİ FORM (Sadece burası kendi içinde güncellenir)
                PassengerInfoForm(
                    name = uiState.passengerName,
                    tc = uiState.passengerTc,
                    email = uiState.passengerEmail,
                    phone = uiState.passengerPhone,
                    gender = uiState.passengerGender,
                    onNameChange = viewModel::updatePassengerName,
                    onTcChange = viewModel::updatePassengerTc,
                    onEmailChange = viewModel::updatePassengerEmail,
                    onPhoneChange = viewModel::updatePassengerPhone,
                    onGenderChange = viewModel::updatePassengerGender
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3. PARÇA: ÖDEME BİLGİLERİ FORM (Ayrı bileşen)
                PaymentInfoForm(
                    savedCards = uiState.savedCards,
                    selectedCardId = uiState.selectedCardId,
                    cardNumber = uiState.cardNumber,
                    cardHolderName = uiState.cardHolderName,
                    cardExpiry = uiState.cardExpiry,
                    cardCvc = uiState.cardCvc,
                    saveCard = uiState.saveCard,
                    onSelectSavedCard = viewModel::selectSavedCard,
                    onCardNumberChange = viewModel::updateCardNumber,
                    onCardHolderNameChange = viewModel::updateCardHolderName,
                    onCardExpiryChange = viewModel::updateCardExpiry,
                    onCardCvcChange = viewModel::updateCardCvc,
                    onSaveCardChange = viewModel::updateSaveCard
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 4. PARÇA: BUTONLAR VE HATA MESAJLARI
                PaymentActions(
                    errorMessage = uiState.errorMessage,
                    tripPrice = uiState.trip?.price,
                    isProcessing = uiState.isProcessing,
                    isPaymentValid = viewModel.isPaymentValid(),
                    onProcessPayment = viewModel::processPayment
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Başarılı ödeme dialog
    if (uiState.showSuccessDialog) {
        PaymentSuccessDialog(
            trip = uiState.trip,
            seatNumber = seatNumber,
            passengerName = uiState.passengerName,
            onDismiss = { viewModel.completePayment() }
        )
    }
}

// --- MODÜLER COMPOSABLE PARÇALAR ---

@Composable
fun TripSummaryCard(trip: Trip?, seatNumber: Int) {
    trip?.let {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(it.companyName, fontWeight = FontWeight.Bold)
                    Text("${it.departure} → ${it.destination}")
                    Text("${it.date} • ${it.time}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Koltuk: $seatNumber", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Text(
                    "${it.price.toInt()} TL",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun PassengerInfoForm(
    name: String,
    tc: String,
    email: String,
    phone: String,
    gender: String,
    onNameChange: (String) -> Unit,
    onTcChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onGenderChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Yolcu Bilgileri", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Ad Soyad *") },
                leadingIcon = { Icon(Icons.Default.Badge, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = tc,
                onValueChange = { if (it.length <= 11) onTcChange(it) },
                label = { Text("T.C. Kimlik No *") },
                leadingIcon = { Icon(Icons.Default.CreditCard, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("E-posta *") },
                leadingIcon = { Icon(Icons.Default.Email, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { if (it.length <= 11) onPhoneChange(it) },
                label = { Text("Telefon *") },
                leadingIcon = { Icon(Icons.Default.Phone, null) },
                placeholder = { Text("05XX XXX XX XX") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Cinsiyet *", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ERKEK CHIP
                FilterChip(
                    selected = gender == "Erkek",
                    onClick = { onGenderChange("Erkek") },
                    label = { Text("Erkek", fontSize = 18.sp) },
                    leadingIcon = {
                        Icon(
                            // Eğer seçiliyse TİK, değilse ERKEK sembolü göster
                            imageVector = if (gender == "Erkek") Icons.Default.Check else Icons.Default.Man,
                            contentDescription = null,
                            modifier = Modifier.size(25.dp)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )

                // KADIN CHIP
                FilterChip(
                    selected = gender == "Kadın",
                    onClick = { onGenderChange("Kadın") },
                    label = { Text("Kadın", fontSize = 18.sp) },
                    leadingIcon = {
                        Icon(
                            // Seçiliyse TİK, değilse KADIN figürü
                            imageVector = if (gender == "Kadın") Icons.Default.Check else Icons.Default.Woman,
                            contentDescription = null,
                            modifier = Modifier.size(25.dp)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun PaymentInfoForm(
    savedCards: List<PaymentCard>,
    selectedCardId: Long?,
    cardNumber: String,
    cardHolderName: String,
    cardExpiry: String,
    cardCvc: String,
    saveCard: Boolean,
    onSelectSavedCard: (Long?) -> Unit,
    onCardNumberChange: (String) -> Unit,
    onCardHolderNameChange: (String) -> Unit,
    onCardExpiryChange: (String) -> Unit,
    onCardCvcChange: (String) -> Unit,
    onSaveCardChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Payment, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ödeme Bilgileri", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Kayıtlı Kartlar Mantığı
            if (savedCards.isNotEmpty()) {
                Text("Kayıtlı Kartlar", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))

                savedCards.forEach { card ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedCardId == card.id)
                                MaterialTheme.colorScheme.primaryContainer
                            else Color.White
                        ),
                        onClick = { onSelectSavedCard(card.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CreditCard, null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(card.cardName, fontWeight = FontWeight.Medium)
                                Text("**** ${card.lastFourDigits}", color = Color.Gray)
                            }
                            if (selectedCardId == card.id) {
                                Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { onSelectSavedCard(null) }) {
                    Text("+ Yeni kart ile öde")
                }
            }

            // Yeni Kart Giriş Alanı
            if (selectedCardId == null) {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { if (it.length <= 16) onCardNumberChange(it) },
                    label = { Text("Kart Numarası *") },
                    leadingIcon = { Icon(Icons.Default.CreditCard, null) },
                    placeholder = { Text("XXXX XXXX XXXX XXXX") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = cardHolderName,
                    onValueChange = onCardHolderNameChange,
                    label = { Text("Kart Üzerindeki İsim *") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = cardExpiry,
                        onValueChange = {
                            if (it.length <= 5) {
                                val filtered = it.filter { c -> c.isDigit() || c == '/' }
                                onCardExpiryChange(filtered)
                            }
                        },
                        label = { Text("Son Kullanma *") },
                        placeholder = { Text("AA/YY") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    OutlinedTextField(
                        value = cardCvc,
                        onValueChange = { if (it.length <= 3) onCardCvcChange(it) },
                        label = { Text("CVC *") },
                        placeholder = { Text("XXX") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = saveCard,
                        onCheckedChange = onSaveCardChange
                    )
                    Text("Bu kartı kaydet", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun PaymentActions(
    errorMessage: String?,
    tripPrice: Double?,
    isProcessing: Boolean,
    isPaymentValid: Boolean,
    onProcessPayment: () -> Unit
) {
    Column {
        if (errorMessage != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(errorMessage, color = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = onProcessPayment,
            enabled = isPaymentValid && !isProcessing,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("İşleniyor...")
            } else {
                Icon(Icons.Default.Lock, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ödemeyi Tamamla - ${tripPrice?.toInt() ?: 0} TL")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Security, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                "256-bit SSL ile güvenli ödeme",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun PaymentSuccessDialog(
    trip: Trip?,
    seatNumber: Int,
    passengerName: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(64.dp)
            )
        },
        title = {
            Text(
                "Ödeme Başarılı!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Rezervasyonunuz tamamlandı",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        trip?.let {
                            Row {
                                Text("🚌", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(it.companyName, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row {
                                Icon(Icons.Default.TripOrigin, null, Modifier.size(20.dp), Color.Gray)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${it.departure} → ${it.destination}")
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row {
                                Icon(Icons.Default.CalendarMonth, null, Modifier.size(20.dp), Color.Gray)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${it.date} • ${it.time}")
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row {
                                Icon(Icons.Default.AirlineSeatReclineNormal, null, Modifier.size(20.dp), Color.Gray)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Koltuk: $seatNumber")
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row {
                                Icon(Icons.Default.Person, null, Modifier.size(20.dp), Color.Gray)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(passengerName)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))
                            Row {
                                Text("Toplam:", fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    "${it.price.toInt()} TL",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Share, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Bileti Paylaş ve Kapat")
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
        appendLine("⏱ Süre: ${trip.duration}")
        appendLine("💺 Koltuk: $seat")
        appendLine("👤 Yolcu: $passenger")
        appendLine("━━━━━━━━━━━━━━━━━━")
        appendLine("💰 ${trip.price.toInt()} TL")
        appendLine()
        appendLine("İyi yolculuklar! 🚀")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, ticketInfo)
        putExtra(Intent.EXTRA_SUBJECT, "Seyahat Biletim - ${trip.companyName}")
    }
    context.startActivity(Intent.createChooser(intent, "Bileti Paylaş"))
}