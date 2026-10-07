package com.ticket.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ticket.common.User

@Composable
fun UserManagementScreen(navController: NavController) {
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    SideEffect {
        // Load users from Supabase
        isLoading = true
        // TODO: Call repository to get users
        isLoading = false
    }

    Scaffold(
        bottomBar = {
            BottomNavigation {
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    selected = true,
                    onClick = {}
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.Ticket, contentDescription = "Tickets") },
                    label = { Text("Tickets") },
                    onClick = {
                        navController.navigate("ticket-list")
                    }
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Usuarios") },
                    label = { Text("Usuarios") },
                    selected = false,
                    onClick = {}
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    elevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.Start,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Gestión de Usuarios",
                            style = MaterialTheme.typography.h5,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        if (users.isEmpty()) {
                            Text(
                                text = "No hay usuarios registrados",
                                style = MaterialTheme.typography.body1,
                                color = MaterialTheme.colors.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.CenterHorizontal)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                items(users.size) { index ->
                                    val user = users[index]
                                    UserRow(user = user)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                navController.navigate("user-create")
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Crear usuario")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserRow(user: User, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircleAvatar(
            userPhoto = user.photoUrl,
            size = 32.dp,
            contentDescription = user.displayName
        )

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = user.displayName ?: user.email,
                style = MaterialTheme.typography.subtitle1,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
            Text(
                text = user.role.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colors.onSurfaceVariant
            )
        }

        Spacer(Modifier.width(8.dp))

        OutlinedButton(
            onClick = {
                // Ver/editar usuario
            },
            modifier = Modifier.padding(end = 4.dp)
        ) {
            Text("Ver")
        }
    }
}