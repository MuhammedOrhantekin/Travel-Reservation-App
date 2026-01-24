package com.azizzade.travelreservation.ui.screens.main

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
import com.azizzade.travelreservation.data.local.SessionManager
import com.azizzade.travelreservation.data.local.database.AppDatabase
import com.azizzade.travelreservation.data.repository.ReservationRepository
import com.azizzade.travelreservation.data.repository.TripRepository
import com.azizzade.travelreservation.ui.navigation.BottomNavItem
import com.azizzade.travelreservation.ui.navigation.Screen
import com.azizzade.travelreservation.ui.screens.help.HelpScreen
import com.azizzade.travelreservation.ui.screens.home.HomeScreen
import com.azizzade.travelreservation.ui.screens.home.HomeViewModel
import com.azizzade.travelreservation.ui.screens.profile.ProfileScreen
import com.azizzade.travelreservation.ui.screens.reservations.MyReservationsScreen
import com.azizzade.travelreservation.ui.screens.reservations.ReservationViewModel

@Composable
fun MainScreen(
    mainNavController: NavHostController,
    database: AppDatabase,
    sessionManager: SessionManager
) {
    val bottomNavController = rememberNavController()
    val bottomNavItems = listOf(
        BottomNavItem.Search,
        BottomNavItem.Reservations,
        BottomNavItem.Help,
        BottomNavItem.Profile
    )

    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            // NavigationBar yerine BottomAppBar kullanıyoruz
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer, // Arkaplan rengi
                contentPadding = PaddingValues(horizontal = 12.dp) // Kenarlardan biraz boşluk
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    // ÖNEMLİ: SpaceBetween veya SpaceAround kullanarak öğeleri yayıyoruz.
                    // Bu sayede her öğe kendi içeriği kadar yer kaplar,
                    // "Ara" az yer kaplarken, "Seyahatlerim" rahatça sığar.
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentRoute == item.route

                        // Standart NavigationBarItem yerine kendi özel tasarımımız
                        // Bu sayede "weight" zorlaması kalkar
                        CustomBottomNavItem(
                            item = item,
                            isSelected = isSelected,
                            onClick = {
                                bottomNavController.navigate(item.route) {
                                    popUpTo(bottomNavController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavItem.Search.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(BottomNavItem.Search.route) {
                val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory(sessionManager))
                HomeScreen(
                    viewModel = viewModel,
                    onSearchClick = { from, to, date, vehicleType ->
                        mainNavController.navigate(Screen.TripList.createRoute(from, to, date, vehicleType))
                    }
                )
            }

            composable(BottomNavItem.Reservations.route) {
                val tripRepository = remember { TripRepository(database.tripDao()) }
                val reservationRepository = remember { ReservationRepository(database.reservationDao()) }
                val viewModel: ReservationViewModel = viewModel(
                    factory = ReservationViewModel.Factory(reservationRepository, tripRepository, sessionManager)
                )
                MyReservationsScreen(viewModel = viewModel)
            }

            composable(BottomNavItem.Help.route) {
                HelpScreen()
            }

            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    sessionManager = sessionManager,
                    isAdmin = sessionManager.isAdmin(),
                    onPersonalInfoClick = { mainNavController.navigate(Screen.PersonalInfo.route) },
                    onSecurityClick = { mainNavController.navigate(Screen.SecuritySettings.route) },
                    onPaymentMethodsClick = { mainNavController.navigate(Screen.PaymentMethods.route) },
                    onAboutClick = { mainNavController.navigate(Screen.About.route) },
                    onAdminClick = { mainNavController.navigate(Screen.AdminPanel.route) },
                    onLogout = {
                        sessionManager.logout()
                        mainNavController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CustomBottomNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent

    Column(
        modifier = Modifier
            // Tıklama alanı ve yuvarlatılmış köşeler
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 6.dp), // Tıklama alanının iç boşluğu
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // İkonun arkasındaki renkli kutu (seçiliyse görünür)
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

        Text(
            text = item.label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            maxLines = 1
        )
    }
}