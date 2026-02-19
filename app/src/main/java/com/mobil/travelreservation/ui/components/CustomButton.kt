package com.mobil.travelreservation.ui.components

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * CustomButton
 * Uygulama genelinde kullanılan standart buton bileşenidir.
 * @param onClick Butona basıldığında çalışacak fonksiyon.
 * @param text Butonun üzerinde yazacak metin.
 */
@Composable
fun CustomButton(onClick: () -> Unit, text: String, modifier: Modifier = Modifier) {
    Button(onClick = onClick, modifier = modifier) {
        Text(text = text)
    }
}
