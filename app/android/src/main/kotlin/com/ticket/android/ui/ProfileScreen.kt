package com.ticket.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }

    // Load user profile
    SideEffect {
        // TODO: Load user data from Supabase auth
        name = "Juan Pérez"
        email = "juan@ticketapp.com"
        phone = "+52 55 1234 5678"
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("dashboard")
                    }) {
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(0.9f).padding(16.dp),
                    elevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // User info
                        CircleAvatar(
                            userPhoto = null,
                            size = 80.dp,
                            contentDescription = name
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = name,
                            style = MaterialTheme.typography.h6,
                            modifier = Modifier.align(Alignment.CenterHorizontal)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = email,
                            style = MaterialTheme.typography.body1,
                            color = MaterialTheme.colors.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.CenterHorizontal)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Teléfono") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(0.8f),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {},
                                modifier = Modifier.weight(0.5f)
                            ) {
                                Text("Editar perfil")
                            }

                            OutlinedButton(
                                onClick = {},
                                modifier = Modifier.weight(0.5f)
                            ) {
                                Text("Cambiar contraseña")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = {
                                // Sign out
                                navController.navigate("auth")
                            },
                            modifier = Modifier.fillMaxWidth(0.9f),
                            colors = OutlinedButtonColors(
                                containerColor = MaterialTheme.colors.error
                            )
                        ) {
                            Text("Cerrar sesión", color = MaterialTheme.colors.onError)
                        }
                    }
                }
            }
        }
    }
}