package com.mobil.travelreservation.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Screen
 *
 * Uygulamadaki sayfaların rotalarını (URL benzeri adreslerini) tutan Sealed Class.
 * Sealed Class kullanmamızın sebebi Type-Safety (Tip Güvenliği) sağlamaktır.
 * Yanlış bir rota ismi yazmamızı derleme aşamasında engeller.
 */
sealed class Screen(val route: String) {
    // Parametre almayan sabit rotalar
    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main")
    object Home : Screen("home")
    object MyReservations : Screen("my_reservations")
    object Profile : Screen("profile")
    object Help : Screen("help")
    object AdminPanel : Screen("admin_panel")
    object AddTrip : Screen("add_trip")
    object PersonalInfo : Screen("personal_info")
    object SecuritySettings : Screen("security_settings")
    object PaymentMethods : Screen("payment_methods")
    object About : Screen("about")

    // Parametre alan dinamik rotalar (Örn: trip_list/İzmir/Ankara...)
    object TripList : Screen("trip_list/{from}/{to}/{date}/{vehicleType}") {
        fun createRoute(from: String, to: String, date: String, vehicleType: String) =
            "trip_list/$from/$to/$date/$vehicleType"
    }

    // Sefer ID'si ile koltuk seçimine gitmek için
    object SeatSelection : Screen("seat_selection/{tripId}") {
        fun createRoute(tripId: Long) = "seat_selection/$tripId"
    }

    // Ödeme ekranına sefer ID ve koltuk no ile gitmek için
    object Payment : Screen("payment/{tripId}/{seatNumber}") {
        fun createRoute(tripId: Long, seatNumber: Int) = "payment/$tripId/$seatNumber"
    }

    // Sefer düzenleme ekranı
    object EditTrip : Screen("edit_trip/{tripId}") {
        fun createRoute(tripId: Long) = "edit_trip/$tripId"
    }
}

/**
 * BottomNavItem
 *
 * Ana ekrandaki alt menü (Bottom Navigation Bar) elemanlarını tanımlar.
 * Her elemanın bir rotası, ikonu ve etiketi vardır.
 */
sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Search : BottomNavItem("home", Icons.Default.Search, "Ara")
    object Reservations : BottomNavItem("my_reservations", Icons.Default.ConfirmationNumber, "Seyahatlerim")
    object Help : BottomNavItem("help", Icons.Default.Help, "Yardım")
    object Profile : BottomNavItem("profile", Icons.Default.Person, "Hesabım")
}