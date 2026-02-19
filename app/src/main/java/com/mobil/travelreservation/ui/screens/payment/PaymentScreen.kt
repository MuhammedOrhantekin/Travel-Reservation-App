package com.mobil.travelreservation.ui.screens.payment

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
import com.mobil.travelreservation.data.model.PaymentCard
import com.mobil.travelreservation.data.model.Trip

/**
 * PaymentScreen
 *
 * Kullanıcının yolcu bilgilerini ve ödeme bilgilerini girerek bilet satın aldığı ekrandır.
 *
 * İşlevleri:
 * 1. Yolcu Bilgileri Formu (Ad, TC, Telefon, Cinsiyet).
 * 2. Ödeme Formu (Kart Bilgileri veya Kayıtlı Kart Seçimi).
 * 3. Bilet Özeti Gösterimi.
 * 4. Başarılı Ödeme Sonrası Bilet Paylaşımı (Intent).
 *
 * @param viewModel Ödeme işlemlerini yöneten ViewModel.
 * @param tripId Satın alınacak seferin ID'si.
 * @param seatNumber Seçilen koltuk numarası.
 * @param onPaymentComplete Ödeme başarılı olunca çalışacak fonksiyon.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    viewModel: PaymentViewModel,
    tripId: Long,
    seatNumber: Int,
    onPaymentComplete: () -> Unit,
    onBackClick: () -> Unit
) {
    // ViewModel durumunu dinliyoruz (StateFlow)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Ekran açıldığında ilgili sefer ve koltuk bilgilerini veritabanından çek
    LaunchedEffect(tripId, seatNumber) {
        viewModel.loadTripAndSeat(tripId, seatNumber)
    }

    // Ödeme başarılı (paymentComplete = true) olduğunda çalışacak Yan Etki
    LaunchedEffect(uiState.paymentComplete) {
        if (uiState.paymentComplete) {
            // Bileti paylaşma ekranını aç (WhatsApp, Mail vb.)
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
            // --- ANA İÇERİK ---
            // Ekran taşarsa kaydırılabilir olsun (Vertical Scroll)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF5F5F5)) // Hafif gri arka plan
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. SEFER ÖZETİ (Hangi otobüs, saat kaçta, kaç para?)
                TripSummaryCard(trip = uiState.trip, seatNumber = seatNumber)

                // 2. YOLCU BİLGİLERİ (Ad, TC, Telefon)
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

                // 3. ÖDEME BİLGİLERİ (Kart Numarası veya Kayıtlı Kartlar)
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

                // 4. AKSİYON BUTONLARI VE HATA MESAJLARI
                PaymentActions(
                    errorMessage = uiState.errorMessage,
                    tripPrice = uiState.trip?.price,
                    isProcessing = uiState.isProcessing,
                    isPaymentValid = viewModel.isPaymentValid(), // Form dolu mu?
                    onProcessPayment = viewModel::processPayment
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Başarılı Ödeme Onay Penceresi (Dialog)
    if (uiState.showSuccessDialog) {
        PaymentSuccessDialog(
            trip = uiState.trip,
            seatNumber = seatNumber,
            passengerName = uiState.passengerName,
            onDismiss = { viewModel.completePayment() }
        )
    }
}

// --- MODÜLER BİLEŞENLER (COMPONENTS) ---

/**
 * Sefer ve koltuk bilgilerini özetleyen kart.
 */
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

/**
 * Yolcunun kişisel bilgilerini girdiği form alanı.
 */
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

            // Ad Soyad
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Ad Soyad *") },
                leadingIcon = { Icon(Icons.Default.Badge, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // TC Kimlik No (Sadece sayı ve max 11 hane)
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

            // E-posta
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

            // Telefon
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

            // Cinsiyet Seçimi (Chips)
            Text("Cinsiyet *", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ERKEK SEÇENEĞİ
                FilterChip(
                    selected = gender == "Erkek",
                    onClick = { onGenderChange("Erkek") },
                    label = { Text("Erkek", fontSize = 18.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = if (gender == "Erkek") Icons.Default.Check else Icons.Default.Man,
                            contentDescription = null,
                            modifier = Modifier.size(25.dp)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )

                // KADIN SEÇENEĞİ
                FilterChip(
                    selected = gender == "Kadın",
                    onClick = { onGenderChange("Kadın") },
                    label = { Text("Kadın", fontSize = 18.sp) },
                    leadingIcon = {
                        Icon(
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

/**
 * Ödeme Bilgileri Formu.
 * Kayıtlı kart varsa listeler, yoksa yeni kart giriş alanlarını gösterir.
 */
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

            // --- KAYITLI KARTLAR ---
            if (savedCards.isNotEmpty()) {
                Text("Kayıtlı Kartlar", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))

                savedCards.forEach { card ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            // Seçiliyse farklı renk yap
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
                // Yeni Kart Ekleme Seçeneği
                TextButton(onClick = { onSelectSavedCard(null) }) {
                    Text("+ Yeni kart ile öde")
                }
            }

            // --- YENİ KART GİRİŞ ALANI ---
            // Eğer kayıtlı bir kart seçilmediyse (selectedCardId == null) bu form görünür.
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
                    // Son Kullanma Tarihi (AA/YY)
                    OutlinedTextField(
                        value = cardExpiry,
                        onValueChange = {
                            if (it.length <= 5) {
                                // Sadece rakam ve / karakterine izin ver
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

                    // CVC Kodu
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

                // Kartı Kaydet Checkbox
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

/**
 * Ödeme Butonu, Hata Mesajları ve Güvenlik Bilgisi.
 */
@Composable
fun PaymentActions(
    errorMessage: String?,
    tripPrice: Double?,
    isProcessing: Boolean,
    isPaymentValid: Boolean,
    onProcessPayment: () -> Unit
) {
    Column {
        // Hata Mesajı Gösterimi
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

        // Ödeme Butonu
        Button(
            onClick = onProcessPayment,
            // İşlem sürüyorsa veya form eksikse butonu pasif yap
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

        // SSL Güvenlik Bilgisi
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

/**
 * Ödeme Başarılı Olduğunda Açılan Dialog Penceresi.
 */
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

                // Bilet Detayları Kartı
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        trip?.let {
                            // Firma Adı
                            Row {
                                Text(if (it.vehicleType == "Uçak") "✈️" else "🚌", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(it.companyName, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            // Güzergah
                            Row {
                                Icon(Icons.Default.TripOrigin, null, Modifier.size(20.dp), Color.Gray)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${it.departure} → ${it.destination}")
                            }
                            Spacer(modifier = Modifier.height(4.dp))

                            // Tarih ve Saat
                            Row {
                                Icon(Icons.Default.CalendarMonth, null, Modifier.size(20.dp), Color.Gray)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${it.date} • ${it.time}")
                            }
                            Spacer(modifier = Modifier.height(4.dp))

                            // Koltuk
                            Row {
                                Icon(Icons.Default.AirlineSeatReclineNormal, null, Modifier.size(20.dp), Color.Gray)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Koltuk: $seatNumber")
                            }
                            Spacer(modifier = Modifier.height(4.dp))

                            // Yolcu Adı
                            Row {
                                Icon(Icons.Default.Person, null, Modifier.size(20.dp), Color.Gray)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(passengerName)
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider() // Çizgi
                            Spacer(modifier = Modifier.height(8.dp))

                            // Toplam Fiyat
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

/**
 * Bileti paylaşmak için sistem paylaşım menüsünü (Share Sheet) açan yardımcı fonksiyon.
 * WhatsApp, Mail, Mesajlar vb. uygulamalara metin gönderir.
 */
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

    // Implicit Intent: "Bu metni paylaş, hangi uygulamayla paylaşacağını sen seç."
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, ticketInfo)
        putExtra(Intent.EXTRA_SUBJECT, "Seyahat Biletim - ${trip.companyName}")
    }
    context.startActivity(Intent.createChooser(intent, "Bileti Paylaş"))
}