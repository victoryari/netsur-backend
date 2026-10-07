package com.ticket.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ticket.R
import com.ticket.common.User
import com.ticket.common.UserRole
import com.ticket.common.database.TiDBService
import com.ticket.common.notifications.NotificationRepository
import com.ticket.common.ui.components.*
import com.ticket.common.ui.theme.TicketShapes
import com.ticket.common.ui.theme.TicketSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController, user: User?) {
    var tickets by remember { mutableStateOf<List<com.ticket.common.Ticket>>(emptyList()) }
    val unread by NotificationRepository.unreadCount.collectAsState()

    LaunchedEffect(user) {
        user?.let {
            tickets = when (it.role) {
                UserRole.ADMIN -> TiDBService.getTickets()
                UserRole.AGENT -> TiDBService.getTicketsByAssignee(it.id)
                UserRole.USER -> TiDBService.getTicketsByCreator(it.id)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = TicketShapes.sm,
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_netsur_logo),
                                contentDescription = "NETSUR",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(
                            Icons.Filled.AccountCircle,
                            contentDescription = "Perfil"
                        )
                    }
                    if (user?.role == UserRole.ADMIN || user?.role == UserRole.AGENT) {
                        Box {
                            IconButton(onClick = { navController.navigate("notifications") }) {
                                Icon(
                                    Icons.Filled.Notifications,
                                    contentDescription = "Notificaciones"
                                )
                            }
                            if (unread > 0) {
                                Badge(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(top = 6.dp, end = 6.dp),
                                    containerColor = MaterialTheme.colorScheme.error
                                ) {
                                    Text(
                                        if (unread > 99) "99+" else unread.toString(),
                                        color = MaterialTheme.colorScheme.onError
                                    )
                                }
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Dashboard, contentDescription = "Panel") },
                    label = { Text("Panel") },
                    selected = true,
                    onClick = {}
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
                    selected = false,
                    onClick = {
                        if (user?.role == UserRole.ADMIN) {
                            navController.navigate("user-management")
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tarjeta de Bienvenida
            TicketCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Bienvenido${user?.displayName?.let { ", $it" } ?: ""}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusChip(
                                label = when (user?.role) {
                                    UserRole.ADMIN -> "Administrador"
                                    UserRole.AGENT -> "Técnico Agente"
                                    UserRole.USER -> "Usuario"
                                    null -> "Invitado"
                                },
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Estadísticas
            val total = tickets.size
            val abiertos = tickets.count { it.status.equals("open", ignoreCase = true) }
            val resueltos = tickets.count {
                it.status.equals("resolved", ignoreCase = true) ||
                    it.status.equals("closed", ignoreCase = true)
            }
            val urgentes = tickets.count { it.priority.equals("urgent", ignoreCase = true) }

            Text(
                text = "Resumen de Tickets",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatsCard(
                    title = "Total",
                    value = total.toString(),
                    color = MaterialTheme.colorScheme.primary,
                    icon = Icons.Filled.ConfirmationNumber,
                    modifier = Modifier.weight(1f)
                )
                StatsCard(
                    title = "Abiertos",
                    value = abiertos.toString(),
                    color = MaterialTheme.colorScheme.tertiary,
                    icon = Icons.Filled.Dashboard,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatsCard(
                    title = "Resueltos",
                    value = resueltos.toString(),
                    color = MaterialTheme.colorScheme.primary,
                    icon = Icons.Filled.Shield,
                    modifier = Modifier.weight(1f)
                )
                StatsCard(
                    title = "Urgentes",
                    value = urgentes.toString(),
                    color = MaterialTheme.colorScheme.error,
                    icon = Icons.Filled.Notifications,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Acciones rápidas",
                style = MaterialTheme.typography.titleMedium
            )

            TicketButton(
                text = "Crear Nuevo Ticket",
                onClick = { navController.navigate("ticket-create") },
                type = ButtonType.Primary,
                size = ButtonSize.Large,
                icon = Icons.Filled.Add,
                modifier = Modifier.fillMaxWidth()
            )

            TicketButton(
                text = "Ver Todos los Tickets",
                onClick = { navController.navigate("ticket-list") },
                type = ButtonType.Outlined,
                size = ButtonSize.Medium,
                icon = Icons.Filled.ConfirmationNumber,
                modifier = Modifier.fillMaxWidth()
            )

            if (user?.role == UserRole.ADMIN) {
                TicketButton(
                    text = "Gestión de Usuarios",
                    onClick = { navController.navigate("user-management") },
                    type = ButtonType.Secondary,
                    size = ButtonSize.Medium,
                    icon = Icons.Filled.Person,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
