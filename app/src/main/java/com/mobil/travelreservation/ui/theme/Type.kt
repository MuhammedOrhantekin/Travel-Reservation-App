package com.mobil.travelreservation.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Type.kt
 *
 * Uygulamanın yazı stillerini (Typography) tanımlar.
 * Material Design 3 standartlarına göre başlık, gövde, etiket stilleri burada özelleştirilir.
 */

val Typography = Typography(
    // Genel gövde metni stili (Varsayılan Text stili)
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default, // Sistem yazı tipi
        fontWeight = FontWeight.Normal,  // Normal kalınlık
        fontSize = 16.sp,                // 16sp boyut
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )




    /* * İstenirse Başlık (Title) veya Etiket (Label) stilleri de
     * buradan özelleştirilebilir:
     *
     * titleLarge = TextStyle(...),
     * labelSmall = TextStyle(...)
     */
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)