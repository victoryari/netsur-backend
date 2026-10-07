package com.ticket.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ticket.common.User
import com.ticket.common.UserRole
import com.ticket.common.database.TiDBService
import com.ticket.common.ui.components.*
import com.ticket.common.ui.theme.TicketShapes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(navController: NavController, user: User?) {
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(user) {
        if (user?.role == UserRole.ADMIN) {
            users = TiDBService.getUsers()
        }
    }

    if (user?.role != UserRole.ADMIN) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No tienes permiso para ver esta sección",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
        }
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Dashboard, contentDescription = "Panel") },
                    label = { Text("Panel") },
                    selected = false,
                    onClick = { navController.navigate("dashboard") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.ConfirmationNumber, contentDescription = "Tickets") },
                    label = { Text("Tickets") },
                    selected = false,
                    onClick = { navController.navigate("ticket-list") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Usuarios") },
                    label = { Text("Usuarios") },
                    selected = true,
                    onClick = {}
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Gestión de Usuarios",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (users.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay usuarios para mostrar",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(users) { userItem ->
                        UserCardItem(
                            user = userItem,
                            currentUserId = user?.id,
                            onRoleChanged = {
                                scope.launch { users = TiDBService.getUsers() }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserCardItem(user: User, currentUserId: String?, onRoleChanged: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val esUnoMismo = user.id == currentUserId

    TicketCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = TicketShapes.full,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.displayName ?: user.email,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                StatusChip(
                    label = "Rol: ${user.role.name}",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Box {
                TicketButton(
                    text = "Cambiar rol",
                    onClick = { expanded = true },
                    type = ButtonType.Outlined,
                    size = ButtonSize.Small,
                    enabled = !esUnoMismo
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    UserRole.entries.forEach { rol ->
                        DropdownMenuItem(
                            text = { Text(rol.name, style = MaterialTheme.typography.bodyMedium) },
                            leadingIcon = {
                                if (rol == user.role) {
                                    Icon(Icons.Filled.Check, contentDescription = "Rol actual", tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            onClick = {
                                expanded = false
                                if (rol != user.role) {
                                    scope.launch {
                                        if (TiDBService.updateUserRole(user.id, rol)) {
                                            onRoleChanged()
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
