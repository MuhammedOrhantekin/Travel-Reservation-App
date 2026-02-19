package com.mobil.travelreservation.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mobil.travelreservation.data.local.SessionManager
import com.mobil.travelreservation.data.local.database.AppDatabase
import com.mobil.travelreservation.data.repository.*
import com.mobil.travelreservation.ui.screens.admin.AddEditTripScreen
import com.mobil.travelreservation.ui.screens.admin.AdminPanelScreen
import com.mobil.travelreservation.ui.screens.admin.AdminViewModel
import com.mobil.travelreservation.ui.screens.auth.AuthViewModel
import com.mobil.travelreservation.ui.screens.auth.LoginScreen
import com.mobil.travelreservation.ui.screens.auth.RegisterScreen
import com.mobil.travelreservation.ui.screens.main.MainScreen
import com.mobil.travelreservation.ui.screens.payment.PaymentScreen
import com.mobil.travelreservation.ui.screens.payment.PaymentViewModel
import com.mobil.travelreservation.ui.screens.profile.*
import com.mobil.travelreservation.ui.screens.seats.SeatSelectionScreen
import com.mobil.travelreservation.ui.screens.seats.SeatViewModel
import com.mobil.travelreservation.ui.screens.trips.TripListScreen
import com.mobil.travelreservation.ui.screens.trips.TripViewModel

/**
 * NavGraph
 *
 * Uygulamanın navigasyon ağacını ve bağımlılıklarını (Dependencies) yöneten ana fonksiyondur.
 * Tüm ekranlar (Composable fonksiyonlar) burada tanımlanır.
 *
 * Görevleri:
 * 1. Dependency Injection: Veritabanı ve Repository'ler burada oluşturulup alt ekranlara dağıtılır.
 * 2. Routing: Hangi ekranın (Composable) hangi adreste (Route) açılacağı belirlenir.
 * 3. Auth Control: Uygulama açılışında kullanıcının Admin mi, normal üye mi olduğu kontrol edilir.
 */
@Composable
fun NavGraph(navController: NavHostController) {
    val context = LocalContext.current

    // --- BAĞIMLILIK ENJEKSİYONU (Dependency Injection) ---
    // Veritabanı ve Repository nesneleri, uygulama yaşam döngüsü boyunca bir kez oluşturulur (remember).
    // Bu nesneler, ihtiyaç duyan ViewModel'lere parametre olarak geçilir.
    val database = remember { AppDatabase.getDatabase(context) }
    val sessionManager = remember { SessionManager(context) }
    val userRepository = remember { UserRepository(database.userDao()) }
    val tripRepository = remember { TripRepository(database.tripDao()) }
    val reservationRepository = remember { ReservationRepository(database.reservationDao()) }
    val paymentCardRepository = remember { PaymentCardRepository(database.paymentCardDao()) }

    // --- BAŞLANGIÇ EKRANI MANTIĞI ---
    // Kullanıcı giriş yapmamışsa -> Login Ekranı
    // Admin ise -> Admin Paneli
    // Normal kullanıcı ise -> Ana Sayfa (Main)
    val startDestination = when {
        !sessionManager.isLoggedIn() -> Screen.Login.route
        sessionManager.isAdmin() -> Screen.AdminPanel.route
        else -> Screen.Main.route
    }

    NavHost(navController = navController, startDestination = startDestination) {

        // --- GİRİŞ EKRANI ---
        composable(Screen.Login.route) {
            // ViewModel Factory ile ViewModel oluşturuluyor
            val viewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory(userRepository, sessionManager))
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    // Giriş başarılıysa yönlendir
                    val destination = if (sessionManager.isAdmin()) Screen.AdminPanel.route else Screen.Main.route
                    navController.navigate(destination) {
                        // Geri tuşuna basınca tekrar Login ekranına dönmesin diye Login'i yığından (stack) siliyoruz.
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onRegisterClick = { navController.navigate(Screen.Register.route) }
            )
        }

        // --- KAYIT EKRANI ---
        composable(Screen.Register.route) {
            val viewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory(userRepository, sessionManager))
            RegisterScreen(
                viewModel = viewModel,
                onRegisterSuccess = { navController.popBackStack() }, // Kayıt başarılı olunca geri dön
                onBackClick = { navController.popBackStack() }
            )
        }

        // --- ANA EKRAN (Normal Kullanıcı) ---
        composable(Screen.Main.route) {
            // MainScreen içinde kendi alt navigasyonu (BottomBar) olduğu için veritabanını oraya paslıyoruz.
            MainScreen(
                mainNavController = navController,
                database = database,
                sessionManager = sessionManager
            )
        }

        // --- ADMİN PANELİ ---
        composable(Screen.AdminPanel.route) {
            val viewModel: AdminViewModel = viewModel(factory = AdminViewModel.Factory(tripRepository))
            AdminPanelScreen(
                viewModel = viewModel,
                onAddTripClick = { navController.navigate(Screen.AddTrip.route) },
                onEditTripClick = { trip -> navController.navigate(Screen.EditTrip.createRoute(trip.id)) },
                onChangePassword = { navController.navigate(Screen.SecuritySettings.route) },
                onLogout = {
                    sessionManager.logout()
                    // Çıkış yapınca Login ekranına at ve tüm geçmişi sil
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // --- SEFER EKLEME (Admin) ---
        composable(Screen.AddTrip.route) {
            val viewModel: AdminViewModel = viewModel(factory = AdminViewModel.Factory(tripRepository))
            AddEditTripScreen(
                viewModel = viewModel,
                editTrip = null, // Yeni ekleme olduğu için null
                onSaved = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        // --- SEFER DÜZENLEME (Admin) ---
        composable(
            route = Screen.EditTrip.route,
            arguments = listOf(navArgument("tripId") { type = NavType.LongType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getLong("tripId") ?: 0L
            val viewModel: AdminViewModel = viewModel(factory = AdminViewModel.Factory(tripRepository))

            // Düzenlenecek seferi veritabanından çekmek için state kullanıyoruz
            var trip by remember { mutableStateOf<com.mobil.travelreservation.data.model.Trip?>(null) }
            LaunchedEffect(tripId) {
                trip = tripRepository.getTripById(tripId)
            }

            // Veri geldikten sonra ekranı çiziyoruz
            trip?.let {
                AddEditTripScreen(
                    viewModel = viewModel,
                    editTrip = it, // Mevcut sefer verisini gönderiyoruz
                    onSaved = { navController.popBackStack() },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        // --- SEFER LİSTELEME EKRANI (Filtreli) ---
        composable(
            route = Screen.TripList.route,
            arguments = listOf(
                navArgument("from") { type = NavType.StringType },
                navArgument("to") { type = NavType.StringType },
                navArgument("date") { type = NavType.StringType },
                navArgument("vehicleType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            // URL'den (Route) gelen parametreleri okuyoruz
            val from = backStackEntry.arguments?.getString("from") ?: ""
            val to = backStackEntry.arguments?.getString("to") ?: ""
            val date = backStackEntry.arguments?.getString("date") ?: ""
            val vehicleType = backStackEntry.arguments?.getString("vehicleType") ?: "Otobüs"

            val viewModel: TripViewModel = viewModel(factory = TripViewModel.Factory(tripRepository))
            TripListScreen(
                viewModel = viewModel,
                from = from, to = to, date = date, vehicleType = vehicleType,
                onTripSelect = { tripId -> navController.navigate(Screen.SeatSelection.createRoute(tripId)) },
                onBackClick = { navController.popBackStack() }
            )
        }

        // --- KOLTUK SEÇİMİ ---
        composable(
            route = Screen.SeatSelection.route,
            arguments = listOf(navArgument("tripId") { type = NavType.LongType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getLong("tripId") ?: 0L
            val viewModel: SeatViewModel = viewModel(factory = SeatViewModel.Factory(tripRepository, reservationRepository))
            SeatSelectionScreen(
                viewModel = viewModel,
                tripId = tripId,
                onSeatConfirmed = { tId, seatNumber ->
                    navController.navigate(Screen.Payment.createRoute(tId, seatNumber))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // --- ÖDEME EKRANI ---
        composable(
            route = Screen.Payment.route,
            arguments = listOf(
                navArgument("tripId") { type = NavType.LongType },
                navArgument("seatNumber") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getLong("tripId") ?: 0L
            val seatNumber = backStackEntry.arguments?.getInt("seatNumber") ?: 0
            val viewModel: PaymentViewModel = viewModel(
                factory = PaymentViewModel.Factory(tripRepository, reservationRepository, paymentCardRepository, sessionManager)
            )
            PaymentScreen(
                viewModel = viewModel,
                tripId = tripId,
                seatNumber = seatNumber,
                onPaymentComplete = { navController.popBackStack(Screen.Main.route, false) }, // Ana ekrana dön
                onBackClick = { navController.popBackStack() }
            )
        }

        // --- PROFİL ALT EKRANLARI ---

        // Kişisel Bilgiler
        composable(Screen.PersonalInfo.route) {
            val viewModel: ProfileViewModel = viewModel(
                factory = ProfileViewModel.Factory(userRepository, paymentCardRepository, sessionManager)
            )
            PersonalInfoScreen(viewModel = viewModel, onBackClick = { navController.popBackStack() })
        }

        // Güvenlik Ayarları
        composable(Screen.SecuritySettings.route) {
            val viewModel: ProfileViewModel = viewModel(
                factory = ProfileViewModel.Factory(userRepository, paymentCardRepository, sessionManager)
            )
            SecuritySettingsScreen(viewModel = viewModel, onBackClick = { navController.popBackStack() })
        }

        // Kayıtlı Kartlar
        composable(Screen.PaymentMethods.route) {
            val viewModel: ProfileViewModel = viewModel(
                factory = ProfileViewModel.Factory(userRepository, paymentCardRepository, sessionManager)
            )
            PaymentMethodsScreen(viewModel = viewModel, onBackClick = { navController.popBackStack() })
        }

        // Hakkında
        composable(Screen.About.route) {
            AboutScreen(onBackClick = { navController.popBackStack() })
        }
    }
}