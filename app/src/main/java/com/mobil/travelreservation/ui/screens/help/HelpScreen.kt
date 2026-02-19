package com.mobil.travelreservation.ui.screens.help

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

/**
 * HelpScreen
 *
 * Kullanıcıya uygulama hakkında bilgi veren ve sıkça sorulan soruları (SSS) listeleyen ekrandır.
 *
 * Özellikleri:
 * 1. Statik Bilgi Gösterimi: Kullanıcıya hazır cevaplar sunar.
 * 2. Genişletilebilir Liste (Accordion): Sorulara tıklandığında cevabın açılmasını sağlar.
 * 3. İletişim Kartı: Müşteri hizmetlerine yönlendirme butonu içerir.
 */
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
                .verticalScroll(rememberScrollState()) // İçerik uzun olursa kaydırma
        ) {
            Text(
                "Sıkça Sorulan Sorular",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- SSS MADDELERİ ---
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

            // --- İLETİŞİM KARTI ---
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
                    Button(onClick = { /* Arama intent'i eklenebilir */ }) {
                        Icon(Icons.Default.Call, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Bizi Arayın")
                    }
                }
            }
        }
    }
}

/**
 * FAQItem (Sıkça Sorulan Soru Öğesi)
 *
 * Tıklandığında genişleyip cevabı gösteren özel bir kart bileşenidir.
 * Kendi içindeki 'expanded' durumunu (Local State) yönetir.
 *
 * @param icon Sorunun yanındaki ikon.
 * @param question Soru metni.
 * @param answer Cevap metni.
 */
@Composable
fun FAQItem(
    icon: ImageVector,
    question: String,
    answer: String
) {
    // Kartın açık/kapalı durumunu tutan yerel değişken
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = { expanded = !expanded } // Tıklayınca durumu tersine çevir
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Başlık Satırı (İkon + Soru + Ok İşareti)
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
                // Duruma göre aşağı veya yukarı ok göster
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }
            // Eğer expanded true ise cevabı göster
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