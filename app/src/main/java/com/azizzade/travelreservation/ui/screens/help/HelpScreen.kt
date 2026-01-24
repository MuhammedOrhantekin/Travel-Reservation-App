package com.azizzade.travelreservation.ui.screens.help

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yardım") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Sıkça Sorulan Sorular",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            FAQItem(
                icon = Icons.Default.ConfirmationNumber,
                question = "Biletimi nasıl iptal edebilirim?",
                answer = "Seyahatlerim bölümünden ilgili biletinizi bulup 'İptal Et' butonuna tıklayarak iptal edebilirsiniz."
            )

            FAQItem(
                icon = Icons.Default.SwapHoriz,
                question = "Biletimi değiştirebilir miyim?",
                answer = "Mevcut biletinizi iptal edip yeni bir bilet alabilirsiniz."
            )

            FAQItem(
                icon = Icons.Default.Payment,
                question = "Ödeme yöntemleri nelerdir?",
                answer = "Kredi kartı, banka kartı ve havale ile ödeme yapabilirsiniz."
            )

            FAQItem(
                icon = Icons.Default.AirlineSeatReclineNormal,
                question = "Koltuk seçimi nasıl yapılır?",
                answer = "Sefer seçtikten sonra boş koltuklar arasından istediğinizi seçebilirsiniz. Mavi erkek, pembe kadın yolcuları gösterir."
            )

            FAQItem(
                icon = Icons.Default.Phone,
                question = "İletişim bilgileri",
                answer = "Destek hattı: 0850 123 45 67\nE-posta: destek@seyahat.com"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // İletişim kartı
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.SupportAgent,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "7/24 Müşteri Desteği",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Size yardımcı olmaktan mutluluk duyarız",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { /* Arama */ }) {
                        Icon(Icons.Default.Call, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Bizi Arayın")
                    }
                }
            }
        }
    }
}

@Composable
fun FAQItem(
    icon: ImageVector,
    question: String,
    answer: String
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = question,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}