package com.mobil.travelreservation.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Color.kt
 *
 * Uygulamanın renk paletinin tanımlandığı dosyadır.
 * Renkler Hex kodu (0xFF...) olarak tanımlanır.
 */

// --- Material Design Varsayılan Renkleri (Mor/Pembe Tonları) ---
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

// --- Uygulama Özel Marka Renkleri ---
// Ana renk: Mavi (#1976D2) - Güven verir, seyahat uygulamaları için standarttır.
val PrimaryBlue = Color(0xFF1976D2)
val PrimaryBlueDark = Color(0xFF0D47A1)

// İkincil renk: Turuncu (#FF9800) - Dikkat çekici butonlar ve vurgular için.
val SecondaryOrange = Color(0xFFFF9800)

// Arka plan renkleri
val BackgroundLight = Color(0xFFF5F5F5) // Hafif gri, gözü yormaz
val SurfaceLight = Color(0xFFFFFFFF)    // Tam beyaz kartlar için

// --- Koltuk Durum Renkleri ---
// Koltuk seçim ekranında kullanılan mantıksal renkler
val SeatAvailable = Color(0xFF4CAF50) // Boş (Yeşil)
val SeatReserved = Color(0xFF9E9E9E)  // Dolu (Gri)
val SeatSelected = Color(0xFF2196F3)  // Seçili (Mavi)