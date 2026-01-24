package com.azizzade.travelreservation.ui.screens.profile

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
import com.azizzade.travelreservation.data.local.SessionManager

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
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = sessionManager.getUserName().take(2).uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = sessionManager.getUserName(),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
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

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                ProfileMenuItem(Icons.Default.Person, "Kişisel Bilgiler", "Ad, TC, telefon bilgilerinizi düzenleyin", onPersonalInfoClick)
                ProfileMenuItem(Icons.Default.Security, "Güvenlik", "Şifre ve e-posta değiştirme", onSecurityClick)
                ProfileMenuItem(Icons.Default.CreditCard, "Ödeme Yöntemleri", "Kayıtlı kartlarınızı yönetin", onPaymentMethodsClick)

                if (isAdmin) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    ProfileMenuItem(Icons.Default.AdminPanelSettings, "Admin Panel", "Sefer yönetimi", onAdminClick, MaterialTheme.colorScheme.primary)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                ProfileMenuItem(Icons.Default.Info, "Hakkında", "Uygulama bilgileri", onAboutClick)
                ProfileMenuItem(Icons.Default.Logout, "Çıkış Yap", "Hesabınızdan çıkış yapın", { showLogoutDialog = true }, MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Sürüm 1.0.0", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.Default.Logout, null) },
            title = { Text("Çıkış Yap") },
            text = { Text("Hesabınızdan çıkış yapmak istediğinize emin misiniz?") },
            confirmButton = {
                Button(onClick = { showLogoutDialog = false; onLogout() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Çıkış Yap") }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("İptal") } }
        )
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit, tint: Color = MaterialTheme.colorScheme.onSurface) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), onClick = onClick) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
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