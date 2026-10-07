package com.ticket.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ticket.android.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "splash"
                    ) {
                        // Splash Screen
                        composable("splash") {
                            SplashScreen(navController = navController)
                        }

                        // Auth Screen
                        composable("auth") {
                            AuthScreen(navController = navController)
                        }

                        // Phone Verification Screen
                        composable("phone-verification") {
                            PhoneVerificationScreen(navController = navController)
                        }

                        // Forgot Password Screen
                        composable("forgot-password") {
                            ForgotPasswordScreen(navController = navController)
                        }

                        // Dashboard
                        composable("dashboard") {
                            DashboardScreen(navController = navController)
                        }

                        // Ticket Screens
                        composable("ticket-list") {
                            TicketListScreen(navController = navController)
                        }

                        composable("ticket-create") {
                            TicketCreateScreen(navController = navController)
                        }

                        composable("ticket-detail/{ticketId}") { backStackEntry ->
                            val ticketId = backStackEntry.arguments?.getString("ticketId") ?: ""
                            TicketDetailScreen(
                                ticketId = ticketId,
                                navController = navController
                            )
                        }

                        // User Management
                        composable("user-management") {
                            UserManagementScreen(navController = navController)
                        }

                        composable("user-create") {
                            // TODO: UserCreateScreen
                        }

                        // Profile
                        composable("profile") {
                            ProfileScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }
}
