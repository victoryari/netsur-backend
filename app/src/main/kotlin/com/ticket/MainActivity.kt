package com.ticket

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalContext
import com.ticket.common.database.TiDBService
import com.ticket.common.notifications.NotificationRepository
import com.ticket.common.notifications.Notifier
import com.ticket.ui.*
import com.ticket.common.ui.TicketTheme
import com.ticket.common.ui.theme.*
import com.ticket.common.ui.theme.ThemePreference
import com.ticket.common.ui.theme.ThemePreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // El canal debe existir antes de la primera notificacion.
        Notifier.createChannel(this)

        setContent {
            val context = LocalContext.current
            val themePrefs = remember { ThemePreferenceManager(context) }
            var themePreference by remember { mutableStateOf(themePrefs.getThemePreference()) }
            val systemDarkTheme = isSystemInDarkTheme()
            val isDarkTheme = themePrefs.isDarkTheme(systemDarkTheme)
            val isHighContrast = themePrefs.isHighContrast()

            TicketTheme(
                darkTheme = isDarkTheme,
                highContrast = isHighContrast
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val context = LocalContext.current

                    // El usuario se observa de forma reactiva: en cuanto el login
                    // termina, todas las pantallas reciben el usuario con su rol.
                    val currentUser by TiDBService.currentUser.collectAsState()

                    // Avisos: se consultan mientras haya sesion. El scope se
                    // recrea por usuario para que un logout lo cancele de verdad.
                    val usuarioId = currentUser?.id
                    val avisosScope = remember(usuarioId) {
                        CoroutineScope(SupervisorJob() + Dispatchers.IO)
                    }
                    DisposableEffect(usuarioId) {
                        if (usuarioId != null) {
                            NotificationRepository.iniciarSondeo(context, avisosScope)
                        } else {
                            NotificationRepository.reset()
                        }
                        onDispose { avisosScope.cancel() }
                    }

                    // Permiso de notificaciones (solo Android 13+).
                    val pedirPermiso = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { }
                    var permisoPedido by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            !Notifier.hasPermission(context) &&
                            !permisoPedido
                        ) {
                            permisoPedido = true
                            pedirPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }

                    // Recuperar la sesion al abrir la app (por si el token sigue vivo).
                    LaunchedEffect(Unit) {
                        if (TiDBService.currentUser.value == null) {
                            TiDBService.getCurrentUser()
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = "splash"
                    ) {
                        composable("splash") {
                            SplashScreen(navController = navController)
                        }
                        composable("auth") {
                            AuthScreen(navController = navController)
                        }
                        composable("phone-verification") {
                            PhoneVerificationScreen(navController = navController)
                        }
                        composable("forgot-password") {
                            ForgotPasswordScreen(navController = navController)
                        }
                        composable("dashboard") {
                            DashboardScreen(navController = navController, user = currentUser)
                        }
                        composable("ticket-list") {
                            TicketListScreen(navController = navController, user = currentUser)
                        }
                        composable("ticket-create") {
                            TicketCreateScreen(navController = navController, user = currentUser)
                        }
                        composable("ticket-detail/{ticketId}") { backStackEntry ->
                            val ticketId = backStackEntry.arguments?.getString("ticketId") ?: ""
                            TicketDetailScreen(
                                ticketId = ticketId,
                                navController = navController,
                                user = currentUser
                            )
                        }
                        composable("user-management") {
                            UserManagementScreen(navController = navController, user = currentUser)
                        }
                        composable("notifications") {
                            NotificationsScreen(navController = navController)
                        }
                        composable("profile") {
                            ProfileScreen(navController = navController, user = currentUser)
                        }
                    }
                }
            }
        }
    }
}
