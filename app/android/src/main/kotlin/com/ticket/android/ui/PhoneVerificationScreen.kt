package com.ticket.android.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PhoneIphone
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
fun PhoneVerificationScreen(navController: NavController) {
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var codeSent by remember { mutableStateOf(false) }
    var countdown by remember { mutableStateOf(0) }

    // Countdown timer for resend
    LaunchedEffect(codeSent) {
        if (codeSent) {
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
                title = { Text("Verificación por Teléfono") },
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
                Icons.Filled.PhoneIphone,
                contentDescription = "Teléfono",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Verificación SMS",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (!codeSent) {
                    "Ingresa tu número de teléfono para recibir un código de verificación"
                } else {
                    "Ingresa el código de 6 dígitos enviado a +52$phoneNumber"
                },
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Phone number input
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it.filter { c -> c.isDigit() } },
                label = { Text("Número de teléfono") },
                placeholder = { Text("55 1234 5678") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                enabled = !codeSent,
                modifier = Modifier.fillMaxWidth(),
                prefix = { Text("+52 ") }
            )

            // OTP Code input (shown after code is sent)
            if (codeSent) {
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = otpCode,
                    onValueChange = { otpCode = it.filter { c -> c.isDigit() }.take(6) },
                    label = { Text("Código de verificación") },
                    placeholder = { Text("123456") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

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
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Send/Verify button
            Button(
                onClick = {
                    isLoading = true
                    errorMessage = null
                    successMessage = null

                    if (!codeSent) {
                        // Send OTP
                        try {
                            SupabaseService.signInWithPhone("+52$phoneNumber")
                            codeSent = true
                            successMessage = "Código enviado exitosamente"
                        } catch (e: Exception) {
                            errorMessage = "Error al enviar código: ${e.message}"
                        }
                    } else {
                        // Verify OTP
                        try {
                            SupabaseService.verifyPhoneOtp("+52$phoneNumber", otpCode)
                            navController.navigate("dashboard") {
                                popUpTo("auth") { inclusive = true }
                            }
                        } catch (e: Exception) {
                            errorMessage = "Código inválido: ${e.message}"
                        }
                    }
                    isLoading = false
                },
                enabled = !isLoading && (if (codeSent) otpCode.length == 6 else phoneNumber.length >= 10),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (codeSent) "Verificar Código" else "Enviar Código")
                }
            }

            // Resend code button
            if (codeSent) {
                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        try {
                            SupabaseService.signInWithPhone("+52$phoneNumber")
                            successMessage = "Código reenviado"
                        } catch (e: Exception) {
                            errorMessage = "Error al reenviar: ${e.message}"
                        }
                        isLoading = false
                    },
                    enabled = countdown == 0 && !isLoading
                ) {
                    Text(
                        if (countdown > 0) "Reenviar código en ${countdown}s"
                        else "Reenviar código"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Back to login
            TextButton(
                onClick = { navController.popBackStack() }
            ) {
                Text("Volver al inicio de sesión")
            }
        }
    }
}
