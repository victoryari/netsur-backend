package com.ticket.android.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ticket.common.supabase.SupabaseService

@Composable
fun AuthScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showRegister by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo
        Text(
            text = "🎫",
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Sistema de Soporte TI",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (showRegister) "Crear Cuenta" else "Iniciar Sesión",
                    style = MaterialTheme.typography.titleLarge
                )

                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo electrónico") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Password
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Error message
                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Login/Register button
                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        if (showRegister) {
                            // TODO: Implement register
                            isLoading = false
                        } else {
                            // TODO: Implement login
                            isLoading = false
                        }
                    },
                    enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(if (showRegister) "Registrarse" else "Iniciar Sesión")
                    }
                }

                // Toggle login/register
                TextButton(
                    onClick = {
                        showRegister = !showRegister
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (showRegister) "¿Ya tienes cuenta? Inicia sesión"
                        else "¿No tienes cuenta? Regístrate"
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = "O continúa con",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Google Sign-In
                OutlinedButton(
                    onClick = {
                        isLoading = true
                        try {
                            SupabaseService.signInWithGoogle()
                            navController.navigate("dashboard")
                        } catch (e: Exception) {
                            errorMessage = "Error al iniciar con Google: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Filled.Email,
                        contentDescription = "Google",
                        tint = Color(0xFFDB4437),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Continuar con Google")
                }

                // GitHub Sign-In
                OutlinedButton(
                    onClick = {
                        isLoading = true
                        try {
                            SupabaseService.signInWithGitHub()
                            navController.navigate("dashboard")
                        } catch (e: Exception) {
                            errorMessage = "Error al iniciar con GitHub: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Filled.Code,
                        contentDescription = "GitHub",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Continuar con GitHub")
                }

                // Magic Link (Passwordless)
                TextButton(
                    onClick = {
                        isLoading = true
                        try {
                            SupabaseService.sendMagicLink(email)
                            errorMessage = null
                            // Show success message
                        } catch (e: Exception) {
                            errorMessage = "Error al enviar enlace: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    },
                    enabled = !isLoading && email.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Filled.Link,
                        contentDescription = "Magic Link",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Enviar enlace mágico por correo")
                }

                // Phone Sign-In (optional)
                TextButton(
                    onClick = {
                        isLoading = true
                        try {
                            SupabaseService.sendMagicLink("+52$email")
                            errorMessage = null
                        } catch (e: Exception) {
                            errorMessage = "Error al enviar SMS: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    },
                    enabled = !isLoading && email.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Filled.PhoneIphone,
                        contentDescription = "Teléfono",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Enviar código por SMS")
                }
            }
        }
    }
}
