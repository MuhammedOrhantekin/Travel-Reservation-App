package com.mobil.travelreservation.ui.screens.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mobil.travelreservation.data.local.SessionManager
import com.mobil.travelreservation.data.local.database.AppDatabase
import com.mobil.travelreservation.data.repository.ReservationRepository
import com.mobil.travelreservation.data.repository.TripRepository
import com.mobil.travelreservation.ui.navigation.BottomNavItem
import com.mobil.travelreservation.ui.navigation.Screen
import com.mobil.travelreservation.ui.screens.help.HelpScreen
import com.mobil.travelreservation.ui.screens.home.HomeScreen
import com.mobil.travelreservation.ui.screens.home.HomeViewModel
import com.mobil.travelreservation.ui.screens.profile.ProfileScreen
import com.mobil.travelreservation.ui.screens.reservations.MyReservationsScreen
import com.mobil.travelreservation.ui.screens.reservations.ReservationViewModel

/**
 * MainScreen
 *
 * Uygulamanın ana ekran konteyneridir. Alt tarafta bir menü çubuğu (Bottom Navigation)
 * ve üstte seçilen menüye göre değişen içerik alanını barındırır.
 *
 * Navigasyon Yapısı:
 * 1. mainNavController: Genel uygulama gezinmesi (Login -> Main -> TripList vb.)
 * 2. bottomNavController: Sadece alt menü sekmeleri arasındaki gezinme (Ara <-> Profil)
 *
 * @param mainNavController Ana navigasyon kontrolcüsü (Alt ekranlardan dışarı çıkmak için).
 * @param database Veritabanı nesnesi (Alt ekranlara iletmek için).
 * @param sessionManager Oturum yöneticisi.
 */
@Composable
fun MainScreen(
    mainNavController: NavHostController,
    database: AppDatabase,
    sessionManager: SessionManager
) {
    // Alt menü için ayrı bir navigasyon kontrolcüsü oluşturuyoruz (Nested Navigation)
    val bottomNavController = rememberNavController()

    // Alt menüde gösterilecek sekmeler
    val bottomNavItems = listOf(
        BottomNavItem.Search,
        BottomNavItem.Reservations,
        BottomNavItem.Help,
        BottomNavItem.Profile
    )

    // Şu an hangi sekmede olduğumuzu takip ediyoruz (İkonu boyamak için)
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            // Standart NavigationBar yerine BottomAppBar kullanarak özelleştiriyoruz.
            // Sebep: "Seyahatlerim" gibi uzun metinlerin sığması için esnek yerleşim (SpaceBetween) gerekiyor.
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer, // Arka plan rengi
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    // Öğeleri ekrana eşit yaymak yerine, aralarındaki boşluğu ayarlayarak diziyoruz.
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentRoute == item.route

                        // Özel tasarım navigasyon öğesi
                        CustomBottomNavItem(
                            item = item,
                            isSelected = isSelected,
                            onClick = {
                                bottomNavController.navigate(item.route) {
                                    // Geri tuşuna basınca her zaman 'Ara' sekmesine dönsün
                                    popUpTo(bottomNavController.graph.startDestinationId) { saveState = true }
                                    // Aynı sekmeye tekrar basılırsa sayfa yenilenmesin (Single Top)
                                    launchSingleTop = true
                                    // Sekme değiştirince scroll durumunu hatırla
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        // --- İÇERİK ALANI (NavHost) ---
        // Alt menüye basıldığında değişen ekranlar burada tanımlanır.
        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavItem.Search.route,
            modifier = Modifier.padding(padding)
        ) {
            // 1. ARA (Ana Sayfa)
            composable(BottomNavItem.Search.route) {
                val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory(sessionManager))
                HomeScreen(
                    viewModel = viewModel,
                    onSearchClick = { from, to, date, vehicleType ->
                        // Buradan "TripList" ekranına gitmek istiyoruz.
                        // TripList alt menüde olmadığı için 'mainNavController' kullanıyoruz.
                        mainNavController.navigate(Screen.TripList.createRoute(from, to, date, vehicleType))
                    }
                )
            }

            // 2. SEYAHATLERİM
            composable(BottomNavItem.Reservations.route) {
                val tripRepository = remember { TripRepository(database.tripDao()) }
                val reservationRepository = remember { ReservationRepository(database.reservationDao()) }
                val viewModel: ReservationViewModel = viewModel(
                    factory = ReservationViewModel.Factory(reservationRepository, tripRepository, sessionManager)
                )
                MyReservationsScreen(viewModel = viewModel)
            }

            // 3. YARDIM
            composable(BottomNavItem.Help.route) {
                HelpScreen()
            }

            // 4. PROFİL
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    sessionManager = sessionManager,
                    isAdmin = sessionManager.isAdmin(),
                    // Profil altındaki butonlara tıklanınca ana navigasyonu kullan
                    onPersonalInfoClick = { mainNavController.navigate(Screen.PersonalInfo.route) },
                    onSecurityClick = { mainNavController.navigate(Screen.SecuritySettings.route) },
                    onPaymentMethodsClick = { mainNavController.navigate(Screen.PaymentMethods.route) },
                    onAboutClick = { mainNavController.navigate(Screen.About.route) },
                    onAdminClick = { mainNavController.navigate(Screen.AdminPanel.route) },
                    onLogout = {
                        sessionManager.logout()
                        // Çıkış yapınca Login ekranına at ve geçmişi temizle
                        mainNavController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

/**
 * CustomBottomNavItem
 *
 * Alt menüdeki her bir öğe için özel tasarım bileşeni.
 * Standart 'NavigationBarItem' kullanmak yerine bunu yazdık çünkü:
 * 1. "weight" zorlaması olmadan, öğelerin kendi genişliğinde olmasını sağlar.
 * 2. Uzun metinlerin (Seyahatlerim) kesilmesini önler.
 *
 * @param item Menü öğesi bilgileri (İkon, Etiket, Rota).
 * @param isSelected Seçili olup olmadığı.
 * @param onClick Tıklama olayı.
 */
@Composable
fun CustomBottomNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Seçili ise tema rengini, değilse gri renk kullan
    val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp)) // Tıklama efekti oval olsun
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // İkonun arkasındaki renkli kutu (Sadece seçiliyken görünür)
        Surface(
            color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = contentColor,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            )
        }

        // Menü etiketi
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            maxLines = 1
        )
    }
}