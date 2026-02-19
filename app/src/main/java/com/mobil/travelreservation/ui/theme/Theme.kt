package com.mobil.travelreservation.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --- Karanlık Tema Renk Şeması ---
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

// --- Aydınlık Tema Renk Şeması (Varsayılan) ---
// Bizim tanımladığımız Mavi ve Turuncu renkleri buraya bağlıyoruz.
private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    secondary = SecondaryOrange,
    tertiary = Pink40,
    background = BackgroundLight,
    surface = SurfaceLight
)

/**
 * TravelReservationTheme
 *
 * Uygulamanın ana tema sarmalayıcısıdır (Wrapper).
 * Tüm ekranlar bu fonksiyonun içinde çalışır.
 *
 * İşlevleri:
 * 1. Sistem temasını (Karanlık/Aydınlık) algılar.
 * 2. Android 12+ cihazlarda Dinamik Renk (Wallpaper rengi) desteğini yönetir.
 * 3. Status Bar (Bildirim Çubuğu) rengini uygulamanın ana rengine boyar.
 *
 * @param darkTheme Sistem karanlık modda mı? (Otomatik algılar).
 * @param dynamicColor Dinamik renk açık mı? (Android 12+ için true).
 * @param content Temanın uygulanacağı ekran içeriği.
 */
@Composable
fun TravelReservationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true, // Android 12+ (Material You) için
    content: @Composable () -> Unit
) {
    // Hangi renk şemasının kullanılacağına karar ver
    val colorScheme = when {
        // Durum 1: Android 12 ve üzeri ise ve dinamik renk açıksa -> Sistem renklerini kullan
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // Durum 2: Karanlık mod ise -> DarkColorScheme kullan
        darkTheme -> DarkColorScheme
        // Durum 3: Diğer tüm durumlarda -> Bizim özel LightColorScheme'i kullan
        else -> LightColorScheme
    }

    // --- YAN ETKİ (SIDE EFFECT): STATUS BAR RENGİ ---
    // Compose sadece UI çizer, ancak Status Bar Android penceresine (Window) aittir.
    // Bu yüzden SideEffect bloğu içinde Window özelliklerine erişip rengi değiştiriyoruz.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Status barı, temanın primary rengine (Mavi) boya
            window.statusBarColor = colorScheme.primary.toArgb()

            // Status bar ikonlarının rengini ayarla (Karanlık modda açık, aydınlık modda koyu ikon)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    // MaterialTheme bileşeni, renkleri ve tipografiyi alt bileşenlere dağıtır
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}