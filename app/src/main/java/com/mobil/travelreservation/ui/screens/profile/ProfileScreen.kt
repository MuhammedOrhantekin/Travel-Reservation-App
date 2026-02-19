package com.mobil.travelreservation.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mobil.travelreservation.data.local.SessionManager

/**
 * ProfileScreen
 *
 * Kullanıcının profil özetini gördüğü ve hesap ayarlarına eriştiği ana ekrandır.
 *
 * İşlevleri:
 * 1. Profil Başlığı: Kullanıcı adı ve baş harflerinden oluşan avatarı gösterir.
 * 2. Menü Listesi: Kişisel bilgiler, güvenlik, ödeme gibi alt ekranlara yönlendirir.
 * 3. Admin Kontrolü: Eğer kullanıcı Admin ise, "Admin Panel" seçeneğini görünür yapar.
 * 4. Çıkış İşlemi: Kullanıcıyı uygulamadan güvenli bir şekilde çıkarır.
 *
 * @param sessionManager Kullanıcı verilerini (Ad, Adminlik durumu) okumak için.
 * @param isAdmin Kullanıcı yönetici mi? (Menüde admin panelini göstermek için).
 * @param onPersonalInfoClick "Kişisel Bilgiler"e tıklandığında çalışır.
 * @param onSecurityClick "Güvenlik"e tıklandığında çalışır.
 * @param onPaymentMethodsClick "Ödeme Yöntemleri"ne tıklandığında çalışır.
 * @param onAboutClick "Hakkında"ya tıklandığında çalışır.
 * @param onAdminClick "Admin Panel"e tıklandığında çalışır.
 * @param onLogout Çıkış onayı verildiğinde çalışır.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    sessionManager: SessionManager,
    isAdmin: Boolean,
    onPersonalInfoClick: () -> Unit,
    onSecurityClick: () -> Unit,
    onPaymentMethodsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onAdminClick: () -> Unit,
    onLogout: () -> Unit
) {
    // Çıkış onayı için dialog kontrolü
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hesabım") },
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
                .verticalScroll(rememberScrollState()) // Küçük ekranlarda taşmayı önler
        ) {
            // --- PROFİL BAŞLIĞI (HEADER) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Yuvarlak Avatar
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape) // Yuvarlak kesim
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        // İsmin ilk 2 harfini alıp büyük harfle gösteriyoruz
                        Text(
                            text = sessionManager.getUserName().take(2).uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // Kullanıcı Adı
                    Text(
                        text = sessionManager.getUserName(),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    // Admin Rozeti (Sadece Adminse Görünür)
                    if (isAdmin) {
                        Spacer(modifier = Modifier.height(8.dp))
                        AssistChip(
                            onClick = {},
                            label = { Text("Admin") },
                            leadingIcon = { Icon(Icons.Default.AdminPanelSettings, null, Modifier.size(16.dp)) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color.White.copy(alpha = 0.2f),
                                labelColor = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- MENÜ LİSTESİ ---
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                ProfileMenuItem(
                    Icons.Default.Person,
                    "Kişisel Bilgiler",
                    "Ad, TC, telefon bilgilerinizi düzenleyin",
                    onPersonalInfoClick
                )
                ProfileMenuItem(
                    Icons.Default.Security,
                    "Güvenlik",
                    "Şifre ve e-posta değiştirme",
                    onSecurityClick
                )
                ProfileMenuItem(
                    Icons.Default.CreditCard,
                    "Ödeme Yöntemleri",
                    "Kayıtlı kartlarınızı yönetin",
                    onPaymentMethodsClick
                )

                // Admin Paneli Linki (Koşullu Gösterim)
                if (isAdmin) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    ProfileMenuItem(
                        Icons.Default.AdminPanelSettings,
                        "Admin Panel",
                        "Sefer yönetimi",
                        onAdminClick,
                        MaterialTheme.colorScheme.primary
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                ProfileMenuItem(
                    Icons.Default.Info,
                    "Hakkında",
                    "Uygulama bilgileri",
                    onAboutClick
                )

                // Çıkış Butonu (Kırmızı Renkli)
                ProfileMenuItem(
                    Icons.Default.Logout,
                    "Çıkış Yap",
                    "Hesabınızdan çıkış yapın",
                    { showLogoutDialog = true }, // Tıklanınca dialog aç
                    MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Sürüm 1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // --- ÇIKIŞ ONAY DİALOGU ---
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.Default.Logout, null) },
            title = { Text("Çıkış Yap") },
            text = { Text("Hesabınızdan çıkış yapmak istediğinize emin misiniz?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout() // Asıl çıkış işlemini tetikle
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Çıkış Yap") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("İptal") }
            }
        )
    }
}

/**
 * ProfileMenuItem
 *
 * Profil ekranındaki her bir menü satırı için kullanılan özelleştirilmiş kart bileşeni.
 * Kod tekrarını önlemek için ayrı bir fonksiyon olarak yazılmıştır.
 *
 * @param icon Sol taraftaki ikon.
 * @param title Menü başlığı.
 * @param subtitle Alt açıklama metni.
 * @param onClick Tıklama olayı.
 * @param tint İkon ve başlık rengi (Varsayılan: Siyah/Tema Rengi).
 */
@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = tint)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
        }
    }
}