package com.azizzade.travelreservation.ui.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hakkında") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Logo / İkon
            Card(
                modifier = Modifier.size(100.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Row() {
                        Text("🚌", fontSize = 40.sp)
                        Text("✈️", fontSize = 40.sp)

                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Uygulama adı
            Text(
                text = "Seyahat Rezervasyon",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Versiyon 1.0.0",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Açıklama
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Uygulama Hakkında",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Seyahat Rezervasyon, otobüs ve uçak biletlerinizi kolayca aramanızı, karşılaştırmanızı ve rezervasyon yapmanızı sağlayan modern bir mobil uygulamadır.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Bu uygulama Ege Üniversitesi Mobil Programlama dersi kapsamında final projesi olarak geliştirilmiştir.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Özellikler
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Özellikler",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    FeatureItem(Icons.Default.Search, "Otobüs ve uçak seferi arama")
                    FeatureItem(Icons.Default.AirlineSeatReclineNormal, "Görsel koltuk seçimi")
                    FeatureItem(Icons.Default.Payment, "Güvenli ödeme sistemi")
                    FeatureItem(Icons.Default.ConfirmationNumber, "Rezervasyon yönetimi")
                    FeatureItem(Icons.Default.Share, "Bilet paylaşma")
                    FeatureItem(Icons.Default.AdminPanelSettings, "Admin paneli")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Teknolojiler
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Kullanılan Teknolojiler",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    TechItem("Kotlin", "Programlama dili")
                    TechItem("Jetpack Compose", "Modern UI toolkit")
                    TechItem("Room Database", "Yerel veritabanı")
                    TechItem("MVVM", "Mimari desen")
                    TechItem("Material Design 3", "Tasarım sistemi")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Geliştirici bilgileri
            Card(
//                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Code,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Geliştiriciler",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Amin Azizzade",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Muhammed Orhantekin",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Ahmet Murat Türkmen",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Nasrulla Emin",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Ege Üniversitesi",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Copyright
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "© 2025 Seyahat Rezervasyon",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Text(
                text = "Tüm hakları saklıdır.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Bu uygulama eğitim amaçlı geliştirilmiştir.\nTicari kullanım amaçlanmamaktadır.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun FeatureItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun TechItem(name: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color(0xFF4CAF50)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(name, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.width(8.dp))
        Text("- $description", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
    }
}