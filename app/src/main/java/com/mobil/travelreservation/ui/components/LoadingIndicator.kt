package com.mobil.travelreservation.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * LoadingIndicator
 *
 * Veritabanından veri çekilirken veya işlem yapılırken
 * ekranın tam ortasında dönen bir yükleme çubuğu gösterir.
 * Kullanıcıya uygulamanın donmadığını, çalıştığını hissettirir (UX).
 */
@Composable
fun LoadingIndicator() {
    // Box: İçindeki elemanları üst üste veya hizalı koymaya yarayan kutu.
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}