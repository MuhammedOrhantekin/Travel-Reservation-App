package com.mobil.travelreservation.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * SecuritySettingsScreen
 *
 * Kullanıcının hesap güvenliği ile ilgili ayarları yaptığı ekrandır.
 *
 * İşlevleri:
 * 1. Şifre Değiştirme: Mevcut şifre doğrulaması ve yeni şifre belirleme.
 * 2. E-posta Değiştirme: Mevcut e-postayı güncelleme.
 * 3. Anlık Geri Bildirim: İşlem sonucunu (Başarılı/Hatalı) aynı ekranda gösterme.
 *
 * @param viewModel Profil ve güvenlik işlemlerini yöneten ViewModel.
 * @param onBackClick Geri tuşuna basıldığında çalışacak fonksiyon.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(
    viewModel: ProfileViewModel,
    onBackClick: () -> Unit
) {
    // ViewModel'deki kullanıcı verilerini dinliyoruz (Mevcut e-postayı göstermek için)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // --- YEREL DURUMLAR (Form Girdileri) ---
    // Bu değişkenler sadece bu ekranda lazım olduğu için ViewModel yerine burada tutuyoruz.
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var newEmail by remember { mutableStateOf("") }

    // İşlem sonucu mesajları (Hata veya Başarı)
    var passwordMessage by remember { mutableStateOf<String?>(null) }
    var emailMessage by remember { mutableStateOf<String?>(null) }

    // Ekran açıldığında güncel bilgileri çek
    LaunchedEffect(Unit) { viewModel.loadUserInfo() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Güvenlik") },
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
                .verticalScroll(rememberScrollState()) // İçerik taşarsa kaydır
        ) {

            // --- 1. KART: ŞİFRE DEĞİŞTİRME ---
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Başlık
                    Row {
                        Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Şifre Değiştir", style = MaterialTheme.typography.titleMedium)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mevcut Şifre
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("Mevcut Şifre") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        // Şifreyi yıldızlı (***) göstermek için
                        visualTransformation = PasswordVisualTransformation()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Yeni Şifre
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Yeni Şifre") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Yeni Şifre Tekrar
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Yeni Şifre (Tekrar)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Şifre Değiştir Butonu
                    Button(
                        onClick = {
                            // Basit Validasyonlar
                            if (newPassword != confirmPassword) {
                                passwordMessage = "Şifreler eşleşmiyor"
                            } else if (newPassword.length < 6) {
                                passwordMessage = "Şifre en az 6 karakter olmalı"
                            } else {
                                // ViewModel'e isteği gönder
                                viewModel.changePassword(currentPassword, newPassword)
                                passwordMessage = "Şifre başarıyla değiştirildi"
                                // Formu temizle
                                currentPassword = ""
                                newPassword = ""
                                confirmPassword = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        // Alanlar boşsa buton pasif olsun
                        enabled = currentPassword.isNotBlank() && newPassword.isNotBlank()
                    ) {
                        Text("Şifreyi Değiştir")
                    }

                    // Durum Mesajı Gösterimi (Başarı: Yeşil, Hata: Kırmızı)
                    passwordMessage?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = it,
                            color = if (it.contains("başarı")) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 2. KART: E-POSTA DEĞİŞTİRME ---
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Başlık
                    Row {
                        Icon(Icons.Default.Email, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("E-posta Değiştir", style = MaterialTheme.typography.titleMedium)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Mevcut E-postayı göster
                    Text(
                        text = "Mevcut: ${uiState.email}",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Yeni E-posta Girişi
                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = { Text("Yeni E-posta") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // E-posta Değiştir Butonu
                    Button(
                        onClick = {
                            if (!newEmail.contains("@")) {
                                emailMessage = "Geçerli bir e-posta girin"
                            } else {
                                viewModel.changeEmail(newEmail)
                                emailMessage = "E-posta başarıyla değiştirildi"
                                newEmail = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = newEmail.isNotBlank()
                    ) {
                        Text("E-postayı Değiştir")
                    }

                    // Durum Mesajı
                    emailMessage?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = it,
                            color = if (it.contains("başarı")) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}