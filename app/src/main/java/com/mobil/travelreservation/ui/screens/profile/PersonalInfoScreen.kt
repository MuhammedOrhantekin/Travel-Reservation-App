package com.mobil.travelreservation.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * PersonalInfoScreen
 *
 * Kullanıcının kişisel bilgilerini (Ad Soyad, TC, Telefon) görüntülediği ve
 * güncelleyebildiği profil alt ekranıdır.
 *
 * İşlevleri:
 * 1. Mevcut Bilgileri Yükleme: Ekran açılınca veritabanından bilgileri çeker.
 * 2. Veri Girişi Kısıtlaması: TC ve Telefon için sadece sayı ve max karakter kontrolü yapar.
 * 3. Salt Okunur Alan: E-posta adresi buradan değiştirilemez (Güvenlik).
 * 4. Kayıt İşlemi: Değişiklikleri veritabanına yazar.
 *
 * @param viewModel Profil işlemlerini yöneten ViewModel.
 * @param onBackClick Geri tuşuna basıldığında çalışacak fonksiyon.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoScreen(
    viewModel: ProfileViewModel,
    onBackClick: () -> Unit
) {
    // ViewModel'deki verileri (UI State) dinliyoruz
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Kayıt başarılı mesajını göstermek için yerel durum
    var showSuccessMessage by remember { mutableStateOf(false) }

    // Ekran ilk açıldığında kullanıcı bilgilerini veritabanından çek
    LaunchedEffect(Unit) {
        viewModel.loadUserInfo()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kişisel Bilgiler") },
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
                .verticalScroll(rememberScrollState()) // Ekran taşarsa kaydır
        ) {
            // --- AD SOYAD ---
            OutlinedTextField(
                value = uiState.fullName,
                onValueChange = { viewModel.updateFullName(it) },
                label = { Text("Ad Soyad") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- E-POSTA (Salt Okunur / Read Only) ---
            // enabled = false yapılarak değiştirilmesi engellenmiştir.
            OutlinedTextField(
                value = uiState.email,
                onValueChange = { },
                label = { Text("E-posta") },
                leadingIcon = { Icon(Icons.Default.Email, null) },
                modifier = Modifier.fillMaxWidth(),
                enabled = false, // Buradan değiştirilemez
                singleLine = true
            )
            Text(
                "E-posta değiştirmek için Güvenlik bölümünü kullanın",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- TC KİMLİK NO ---
            OutlinedTextField(
                value = uiState.tcNumber,
                onValueChange = {
                    // Sadece 11 haneye kadar izin ver
                    if (it.length <= 11) viewModel.updateTcNumber(it)
                },
                label = { Text("T.C. Kimlik No") },
                leadingIcon = { Icon(Icons.Default.CreditCard, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- TELEFON NUMARASI ---
            OutlinedTextField(
                value = uiState.phone,
                onValueChange = {
                    // Sadece 11 haneye kadar izin ver
                    if (it.length <= 11) viewModel.updatePhone(it)
                },
                label = { Text("Telefon") },
                leadingIcon = { Icon(Icons.Default.Phone, null) },
                placeholder = { Text("05XX XXX XX XX") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- KAYDET BUTONU ---
            Button(
                onClick = {
                    viewModel.savePersonalInfo()
                    showSuccessMessage = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !uiState.isLoading // Yüklenirken tıklamayı engelle
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(Modifier.size(24.dp), Color.White)
                } else {
                    Icon(Icons.Default.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Kaydet")
                }
            }

            // --- BAŞARI MESAJI ---
            if (showSuccessMessage) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)) // Yeşil arka plan
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Bilgileriniz başarıyla kaydedildi",
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }
        }
    }
}