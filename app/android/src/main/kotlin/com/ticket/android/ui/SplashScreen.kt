package com.ticket.ui

import androidx.compose.animation.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.foundation.clickable.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.unit.dp;
import com.ticket.android.MainActivity;

@Composable
fun SplashScreen(navController: NavController) {
    var showLogin by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.primary),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!showLogin) {
            // Logo and brand
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(CircleShape, Color.White)
                    .padding(vertical = 50.dp)
            ) {
                ImageVector(
                    painter = rememberImagePainter(
                        data = androidx.compose.runtime.remember("ticket-logo")
                    ),
                    contentDescription = "Ticket App Logo",
                    modifier = Modifier.size(100.dp)
                )
            }

            Text(
                text = "Gestión de Tickets TI",
                style = MaterialTheme.typography.h4,
                color = MaterialTheme.colors.onPrimary,
                modifier = Modifier.padding(vertical = 32.dp)
            )

            // Loading indicator
            CircularProgressIndicator(
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colors.onPrimary
            )

            Button(
                onClick = {
                    showLogin = true
                },
                modifier = Modifier.padding(vertical = 24.dp)
            ) {
                Text("Iniciar Sesión")
            }
        } else {
            // Login form will appear
            AuthScreen(navController) {
                navController.navigate("auth")
            }
        }
    }

    // Auto-navigate after 3 seconds
    SideEffect(Unit) {
        Thread.sleep(3000)
        showLogin = true
    }
}