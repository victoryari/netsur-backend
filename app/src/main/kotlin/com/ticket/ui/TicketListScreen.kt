package com.ticket.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ticket.common.Ticket
import com.ticket.common.User
import com.ticket.common.UserRole
import com.ticket.common.database.TiDBService
import com.ticket.common.ui.components.*
import com.ticket.common.ui.theme.TicketShapes
import com.ticket.common.ui.theme.TicketSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketListScreen(navController: NavController, user: User?) {
    var tickets by remember { mutableStateOf<List<Ticket>>(emptyList()) }

    LaunchedEffect(user) {
        user?.let {
            tickets = when (it.role) {
                UserRole.ADMIN -> TiDBService.getTickets()
                UserRole.AGENT -> TiDBService.getTicketsByAssignee(it.id)
                UserRole.USER -> TiDBService.getTicketsByCreator(it.id)
            }
        }
    }

    val filteredTickets = remember(tickets, user) {
        when (user?.role) {
            UserRole.ADMIN -> tickets
            UserRole.AGENT -> tickets.filter { it.assignedTo == user.id }
            UserRole.USER -> tickets.filter { it.createdBy == user.id }
            null -> emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Listado de Tickets") },
                actions = {
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(
                            Icons.Filled.AccountCircle,
                            contentDescription = "Perfil"
                        )
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
                    selected = false,
                    onClick = { navController.navigate("dashboard") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.ConfirmationNumber, contentDescription = "Tickets") },
                    label = { Text("Tickets") },
                    selected = true,
                    onClick = {}
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
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate("ticket-create") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = TicketShapes.full,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nuevo Ticket", style = MaterialTheme.typography.labelLarge) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            val title = when (user?.role) {
                UserRole.ADMIN -> "Todos los Tickets"
                UserRole.AGENT -> "Mis Tickets Asignados"
                UserRole.USER -> "Mis Tickets"
                null -> "Tickets"
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "${filteredTickets.size} registramente activos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredTickets.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Surface(
                            shape = TicketShapes.xl,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Filled.ConfirmationNumber,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = when (user?.role) {
                                UserRole.AGENT -> "No tienes tickets asignados"
                                UserRole.USER -> "No has creado tickets aún"
                                else -> "No hay tickets registrados"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Presiona 'Nuevo Ticket' para crear una solicitud.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredTickets) { ticket ->
                        TicketListItemCard(
                            ticket = ticket,
                            onClick = {
                                navController.navigate("ticket-detail/${ticket.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TicketListItemCard(
    ticket: Ticket,
    onClick: () -> Unit
) {
    TicketCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TicketStatusChip(status = ticket.status)
                PriorityChip(priority = ticket.priority)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = ticket.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (!ticket.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = ticket.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!ticket.category.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Folder,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = ticket.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
