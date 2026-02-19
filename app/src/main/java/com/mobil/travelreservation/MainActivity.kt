package com.mobil.travelreservation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.mobil.travelreservation.ui.navigation.NavGraph
import com.mobil.travelreservation.ui.theme.TravelReservationTheme

/**
 * MainActivity
 *
 * Uygulamanın giriş kapısıdır. Android işletim sistemi uygulamayı başlattığında ilk bu sınıfı çağırır.
 *
 * Modern Android Geliştirme (Jetpack Compose) prensibi olan "Tek Aktivite Mimarisi"ni kullanır.
 * Yani uygulamada birden fazla Activity yoktur; sadece bu Activity vardır ve ekranlar (Screen)
 * bunun içinde birer Composable fonksiyon olarak değişir.
 */
class MainActivity : ComponentActivity() {

    /**
     * Aktivite oluşturulduğunda çalışan fonksiyon (Lifecycle Method).
     * @param savedInstanceState Eğer uygulama daha önce arka plana atılıp kapatıldıysa, eski durumunu buradan alır.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- EDGE-TO-EDGE ---
        // Uygulamanın ekranın en tepesinden (Status Bar arkası) en altına (Navigation Bar arkası)
        // kadar çizilmesini sağlar. Modern ve tam ekran bir görünüm için gereklidir.
        enableEdgeToEdge()

        // --- COMPOSE BAŞLANGICI ---
        // XML layout dosyası (setContentView) yerine Compose içeriği set edilir.
        setContent {
            // Uygulamanın Teması (Renkler, Fontlar)
            TravelReservationTheme {
                // Temel Zemin (Arka plan rengini temadan alır)
                Surface(
                    modifier = Modifier.fillMaxSize(), // Tüm ekranı kapla
                    color = MaterialTheme.colorScheme.background
                ) {
                    // --- NAVİGASYON ---
                    // Ekranlar arası geçişi yönetecek olan kontrolcü oluşturulur.
                    val navController = rememberNavController()

                    // Uygulamanın navigasyon haritasını (NavGraph) başlat.
                    // Tüm ekranlar (Login, Home, Payment vb.) bu fonksiyonun içindedir.
                    NavGraph(navController = navController)
                }
            }
        }
    }
}