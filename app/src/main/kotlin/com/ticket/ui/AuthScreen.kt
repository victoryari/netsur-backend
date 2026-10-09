package com.ticket.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.navigation.NavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.ticket.R
import com.ticket.common.database.TiDBService
import com.ticket.common.ui.components.*
import com.ticket.common.ui.theme.TicketShapes
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isRegisterMode by remember { mutableStateOf(false) }
    var displayName by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

   // val webClientId = "500008577319-gvvr627ni0q5f4pbcmqi963s6mlrvj9i.apps.googleusercontent.com"
    val webClientId = "500008577319-66cfcat1ial6s7b6ncva5h6rs5c9iud1.apps.googleusercontent.com"
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo Oficial NETSUR
            Surface(
                modifier = Modifier
                    .size(110.dp)
                    .clip(TicketShapes.lg),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_netsur_logo),
                    contentDescription = "NETSUR Logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Sistema de Gestión de Soporte y Tickets",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Tarjeta Principal de Formulario
            TicketCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TabRow(
                        selectedTabIndex = if (isRegisterMode) 1 else 0,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        indicator = {},
                        divider = {}
                    ) {
                        Tab(
                            selected = !isRegisterMode,
                            onClick = {
                                isRegisterMode = false
                                errorMessage = null
                            },
                            text = {
                                Text(
                                    "Iniciar Sesión",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (!isRegisterMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                        Tab(
                            selected = isRegisterMode,
                            onClick = {
                                isRegisterMode = true
                                errorMessage = null
                            },
                            text = {
                                Text(
                                    "Crear Cuenta",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isRegisterMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    AnimatedVisibility(visible = isRegisterMode) {
                        TicketTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = "Nombre de usuario",
                            placeholder = "Ej. Juan Pérez",
                            leadingIcon = Icons.Filled.Person,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    TicketTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Correo electrónico",
                        placeholder = "ejemplo@correo.com",
                        leadingIcon = Icons.Filled.Email,
                        modifier = Modifier.fillMaxWidth()
                    )

                    TicketPasswordField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Contraseña",
                        leadingIcon = Icons.Filled.Lock,
                        modifier = Modifier.fillMaxWidth()
                    )

                    errorMessage?.let {
                        Surface(
                            shape = TicketShapes.sm,
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }

                    TicketButton(
                        text = if (isRegisterMode) "Crear Cuenta" else "Iniciar Sesión",
                        onClick = {
                            isLoading = true
                            errorMessage = null
                            scope.launch {
                                val success = if (isRegisterMode) {
                                    TiDBService.register(email, password, displayName.ifBlank { null })
                                } else {
                                    TiDBService.login(email, password)
                                }
                                isLoading = false
                                if (success) {
                                    navController.navigate("dashboard") {
                                        popUpTo("auth") { inclusive = true }
                                    }
                                } else {
                                    val detalle = TiDBService.lastError
                                    errorMessage = if (isRegisterMode) {
                                        if (detalle != null) "Error al crear la cuenta: $detalle"
                                        else "Error al crear la cuenta. Intenta nuevamente."
                                    } else {
                                        if (detalle != null) detalle
                                        else "Credenciales inválidas. Verifica tu correo y contraseña."
                                    }
                                }
                            }
                        },
                        loading = isLoading,
                        enabled = email.isNotBlank() && password.isNotBlank(),
                        type = ButtonType.Primary,
                        size = ButtonSize.Large,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                        Text(
                            text = "  o  ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                    }

                    TicketButton(
                        text = "Continuar con Google",
                        onClick = {
                            errorMessage = null
                            scope.launch {
                                try {
                                    val nonce = java.util.UUID.randomUUID().toString()
                                    val googleOption = try {
                                        com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption.Builder(webClientId)
                                            .setNonce(nonce)
                                            .build()
                                    } catch (e: Exception) {
                                        GetGoogleIdOption.Builder()
                                            .setFilterByAuthorizedAccounts(false)
                                            .setServerClientId(webClientId)
                                            .setNonce(nonce)
                                            .setAutoSelectEnabled(false)
                                            .build()
                                    }

                                    val request = GetCredentialRequest.Builder()
                                        .addCredentialOption(googleOption)
                                        .build()

                                    val credentialManager = CredentialManager.create(context)
                                    val result = credentialManager.getCredential(
                                        request = request,
                                        context = context
                                    )

                                    val credential = result.credential
                                    if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                        val gEmail = googleIdTokenCredential.id
                                        val gName = googleIdTokenCredential.displayName

                                        isLoading = true
                                        errorMessage = null
                                        val success = TiDBService.loginGoogle(
                                            email = gEmail,
                                            displayName = gName,
                                            googleId = googleIdTokenCredential.id
                                        )
                                        isLoading = false
                                        if (success) {
                                            navController.navigate("dashboard") {
                                                popUpTo("auth") { inclusive = true }
                                            }
                                        } else {
                                            errorMessage = TiDBService.lastError ?: "Error al iniciar sesión con Google"
                                        }
                                    } else {
                                        errorMessage = "No se obtuvo una credencial de tipo Google ID Token"
                                    }
                                } catch (e: GetCredentialCancellationException) {
                                    // El usuario canceló o cerró la ventana de selección
                                } catch (e: GetCredentialException) {
                                    errorMessage = "Google Sign-In (${e.javaClass.simpleName}): ${e.message ?: "Sin detalles. Revisa la SHA-1 en Google Cloud Console."}"
                                } catch (e: Exception) {
                                    errorMessage = "Error al conectar con Google: ${e.localizedMessage ?: e.javaClass.simpleName}"
                                }
                            }
                        },
                        type = ButtonType.Outlined,
                        size = ButtonSize.Medium,
                        icon = Icons.Filled.Email,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        TicketButton(
                            text = "¿Olvidaste tu contraseña?",
                            onClick = { navController.navigate("forgot-password") },
                            type = ButtonType.Text,
                            size = ButtonSize.Small
                        )
                    }
                }
            }
        }
    }
}
