package com.mobil.travelreservation.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * LoginScreen
 *
 * Kullanıcının e-posta ve şifre ile sisteme giriş yaptığı ekrandır.
 *
 * Özellikleri:
 * 1. State Hoisting: Input değerlerini (email/pass) kendi içinde tutar.
 * 2. Side Effect: Giriş başarılı olduğunda (isSuccess) sayfayı yönlendirir.
 * 3. Feedback: Hatalı girişte uyarı, yüklenirken loading gösterir.
 *
 * @param viewModel Giriş işlemini yöneten AuthViewModel.
 * @param onLoginSuccess Giriş başarılı olduğunda çalışacak navigasyon fonksiyonu.
 * @param onRegisterClick Kayıt ol ekranına geçiş fonksiyonu.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit
) {
    // --- YEREL DURUMLAR (Local States) ---
    // rememberSaveable: Ekran döndürüldüğünde (Rotation) yazılanlar silinmesin diye kullanılır.
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) } // Şifre göz/gizle ikonu için

    // ViewModel'den gelen UI durumunu (Loading, Success, Error) dinliyoruz.
    // collectAsStateWithLifecycle: Uygulama arka plana atıldığında gereksiz kaynak tüketimini durdurur.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // --- YAN ETKİ (SIDE EFFECT) ---
    // uiState.isSuccess değeri 'true' olduğu an bu blok çalışır.
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onLoginSuccess() // Ana ekrana yönlendir
            viewModel.resetState() // State'i temizle ki geri gelince tekrar tetiklenmesin
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Giriş Yap") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()), // Küçük ekranlarda taşmayı önlemek için kaydırma
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo ve Başlık
            Text(
                text = "🚌 ✈️",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Seyahat Rezervasyon",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            // E-posta Alanı
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-posta") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Şifre Alanı (Göz ikonlu)
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Şifre") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Şifreyi gizle" else "Şifreyi göster"
                        )
                    }
                },
                // Şifreyi yıldızlı (***) veya açık gösterme mantığı
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Hata Mesajı Gösterimi (Varsa)
            uiState.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Giriş Butonu
            Button(
                onClick = { viewModel.login(email, password) },
                // Yükleniyorsa veya alanlar boşsa butonu kilitle
                enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Giriş Yap")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Kayıt Ol Linki
            TextButton(onClick = onRegisterClick) {
                Text("Hesabınız yok mu? Kayıt olun")
            }

            // --- TEST AMAÇLI BİLGİ KARTI ---
            // Sunumda hocaya kolaylık sağlamak için eklenmiştir.
            Spacer(modifier = Modifier.height(32.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Test Hesapları:",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Admin: admin@test.com / 123456",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Kullanıcı: user@test.com / 123456",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}