package com.mobil.travelreservation.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable

/**
 * TopBar
 *
 * Sayfaların en üstünde görünen başlıktır.
 * Material3 tasarım standartlarına uygun TopAppBar kullanır.
 *
 * @param title Başlıkta yazacak metin (Örn: "Sefer Listesi").
 */
@OptIn(ExperimentalMaterial3Api::class) // Material3 API'si henüz deneysel olduğu için bu annotation gerekli.
@Composable
fun TopBar(title: String) {
    TopAppBar(title = { Text(text = title) })
}