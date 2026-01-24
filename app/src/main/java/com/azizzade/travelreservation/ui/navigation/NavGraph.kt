package com.azizzade.travelreservation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.azizzade.travelreservation.data.local.SessionManager
import com.azizzade.travelreservation.data.local.database.AppDatabase
import com.azizzade.travelreservation.data.repository.*
import com.azizzade.travelreservation.ui.screens.admin.AddEditTripScreen
import com.azizzade.travelreservation.ui.screens.admin.AdminPanelScreen
import com.azizzade.travelreservation.ui.screens.admin.AdminViewModel
import com.azizzade.travelreservation.ui.screens.auth.AuthViewModel
import com.azizzade.travelreservation.ui.screens.auth.LoginScreen
import com.azizzade.travelreservation.ui.screens.auth.RegisterScreen
import com.azizzade.travelreservation.ui.screens.main.MainScreen
import com.azizzade.travelreservation.ui.screens.payment.PaymentScreen
import com.azizzade.travelreservation.ui.screens.payment.PaymentViewModel
import com.azizzade.travelreservation.ui.screens.profile.*
import com.azizzade.travelreservation.ui.screens.seats.SeatSelectionScreen
import com.azizzade.travelreservation.ui.screens.seats.SeatViewModel
import com.azizzade.travelreservation.ui.screens.trips.TripListScreen
import com.azizzade.travelreservation.ui.screens.trips.TripViewModel

@Composable
fun NavGraph(navController: NavHostController) {
    val context = LocalContext.current

    val database = remember { AppDatabase.getDatabase(context) }
    val sessionManager = remember { SessionManager(context) }
    val userRepository = remember { UserRepository(database.userDao()) }
    val tripRepository = remember { TripRepository(database.tripDao()) }
    val reservationRepository = remember { ReservationRepository(database.reservationDao()) }
    val paymentCardRepository = remember { PaymentCardRepository(database.paymentCardDao()) }

    // Admin ise AdminPanel'e, değilse Main'e yönlendir
    val startDestination = when {
        !sessionManager.isLoggedIn() -> Screen.Login.route
        sessionManager.isAdmin() -> Screen.AdminPanel.route
        else -> Screen.Main.route
    }

    NavHost(navController = navController, startDestination = startDestination) {

        // Login
        composable(Screen.Login.route) {
            val viewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory(userRepository, sessionManager))
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    val destination = if (sessionManager.isAdmin()) Screen.AdminPanel.route else Screen.Main.route
                    navController.navigate(destination) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onRegisterClick = { navController.navigate(Screen.Register.route) }
            )
        }

        // Register
        composable(Screen.Register.route) {
            val viewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory(userRepository, sessionManager))
            RegisterScreen(
                viewModel = viewModel,
                onRegisterSuccess = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        // Main (Normal kullanıcı)
        composable(Screen.Main.route) {
            MainScreen(
                mainNavController = navController,
                database = database,
                sessionManager = sessionManager
            )
        }

        // Admin Panel
        composable(Screen.AdminPanel.route) {
            val viewModel: AdminViewModel = viewModel(factory = AdminViewModel.Factory(tripRepository))
            AdminPanelScreen(
                viewModel = viewModel,
                onAddTripClick = { navController.navigate(Screen.AddTrip.route) },
                onEditTripClick = { trip -> navController.navigate(Screen.EditTrip.createRoute(trip.id)) },
                onChangePassword = { navController.navigate(Screen.SecuritySettings.route) },
                onLogout = {
                    sessionManager.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Add Trip
        composable(Screen.AddTrip.route) {
            val viewModel: AdminViewModel = viewModel(factory = AdminViewModel.Factory(tripRepository))
            AddEditTripScreen(
                viewModel = viewModel,
                editTrip = null,
                onSaved = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        // Edit Trip
        composable(
            route = Screen.EditTrip.route,
            arguments = listOf(navArgument("tripId") { type = NavType.LongType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getLong("tripId") ?: 0L
            val viewModel: AdminViewModel = viewModel(factory = AdminViewModel.Factory(tripRepository))

            var trip by remember { mutableStateOf<com.azizzade.travelreservation.data.model.Trip?>(null) }
            LaunchedEffect(tripId) {
                trip = tripRepository.getTripById(tripId)
            }

            trip?.let {
                AddEditTripScreen(
                    viewModel = viewModel,
                    editTrip = it,
                    onSaved = { navController.popBackStack() },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        // Trip List
        composable(
            route = Screen.TripList.route,
            arguments = listOf(
                navArgument("from") { type = NavType.StringType },
                navArgument("to") { type = NavType.StringType },
                navArgument("date") { type = NavType.StringType },
                navArgument("vehicleType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
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

        // Seat Selection
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

        // Payment
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
                onPaymentComplete = { navController.popBackStack(Screen.Main.route, false) },
                onBackClick = { navController.popBackStack() }
            )
        }

        // Personal Info
        composable(Screen.PersonalInfo.route) {
            val viewModel: ProfileViewModel = viewModel(
                factory = ProfileViewModel.Factory(userRepository, paymentCardRepository, sessionManager)
            )
            PersonalInfoScreen(viewModel = viewModel, onBackClick = { navController.popBackStack() })
        }

        // Security Settings
        composable(Screen.SecuritySettings.route) {
            val viewModel: ProfileViewModel = viewModel(
                factory = ProfileViewModel.Factory(userRepository, paymentCardRepository, sessionManager)
            )
            SecuritySettingsScreen(viewModel = viewModel, onBackClick = { navController.popBackStack() })
        }

        // Payment Methods
        composable(Screen.PaymentMethods.route) {
            val viewModel: ProfileViewModel = viewModel(
                factory = ProfileViewModel.Factory(userRepository, paymentCardRepository, sessionManager)
            )
            PaymentMethodsScreen(viewModel = viewModel, onBackClick = { navController.popBackStack() })
        }

        // About
        composable(Screen.About.route) {
            AboutScreen(onBackClick = { navController.popBackStack() })
        }
    }
}