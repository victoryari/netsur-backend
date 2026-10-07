package com.ticket.android.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ticket.common.supabase.SupabaseService
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var countdown by remember { mutableStateOf(0) }

    // Countdown timer for resend
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            countdown = 60
            while (countdown > 0) {
                delay(1000)
                countdown--
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recuperar Contraseña") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.LockReset,
                contentDescription = "Recuperar",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "¿Olvidaste tu contraseña?",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ingresa tu correo electrónico y te enviaremos un enlace para restablecer tu contraseña.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Email input
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                placeholder = { Text("tu@correo.com") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Filled.Email, contentDescription = "Email")
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Error/Success messages
            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            successMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Send reset link button
            Button(
                onClick = {
                    isLoading = true
                    errorMessage = null
                    successMessage = null

                    try {
                        SupabaseService.resetPassword(email)
                        successMessage = "Enlace enviado. Revisa tu correo electrónico."
                    } catch (e: Exception) {
                        errorMessage = "Error: ${e.message}"
                    }
                    isLoading = false
                },
                enabled = !isLoading && email.contains("@"),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Enviar Enlace de Recuperación")
                }
            }

            // Resend button
            if (successMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        try {
                            SupabaseService.resetPassword(email)
                            successMessage = "Enlace reenviado. Revisa tu correo."
                        } catch (e: Exception) {
                            errorMessage = "Error: ${e.message}"
                        }
                        isLoading = false
                    },
                    enabled = countdown == 0 && !isLoading
                ) {
                    Text(
                        if (countdown > 0) "Reenviar en ${countdown}s"
                        else "Reenviar enlace"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Back to login
            TextButton(
                onClick = { navController.popBackStack() }
            ) {
                Text("Volver al inicio de sesión")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Register link
            TextButton(
                onClick = {
                    navController.navigate("auth") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            ) {
                Text("¿No tienes cuenta? Regístrate")
            }
        }
    }
}
