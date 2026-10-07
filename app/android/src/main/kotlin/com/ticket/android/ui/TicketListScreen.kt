package com.ticket.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ticket.common.Ticket
import com.ticket.common.viewmodel.ticketViewModel
import com.ticket.android.MainActivity

@Composable
fun TicketListScreen(navController: NavController) {
    val viewModel: ticketViewModel = remember { ticketViewModel() }
    var filterStatus by remember { mutableStateOf("") }
    var filterPriority by remember { mutableStateOf("") }
    var searchText by remember { mutableStateOf("") }
    var refresh by remember { mutableStateOf(false) }

    var tickets by remember { mutableStateOf<List<Ticket>>(emptyList()) }

    SideEffect {
        viewModel.getTickets().collect { tickets ->
            this.tickets = tickets
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        // Filters and search
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            elevation = 2.dp
        ) {
            OutlinedTextField(
                value = filterStatus,
                onValueChange = { filterStatus = it },
                label = { Text("Estado") },
                modifier = Modifier.weight(0.5f),
                trailingIcon = {
                    if (filterStatus.isNotEmpty()) {
                        IconButton(onClick = { filterStatus = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Limpiar")
                        }
                    }
                }
            )
            OutlinedTextField(
                value = filterPriority,
                onValueChange = { filterPriority = it },
                label = { Text("Prioridad") },
                modifier = Modifier.weight(0.5f),
                trailingIcon = {
                    if (filterPriority.isNotEmpty()) {
                        IconButton(onClick = { filterPriority = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Limpiar")
                        }
                    }
                }
            )
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Buscar") },
                modifier = Modifier.weight(1f),
                keyboardOptions = androidx.foundation.text.KeyboardOptions(keyboardType = androidx.foundation.text.KeyboardType.Email),
                placeholder = { Text("Ingrese búsqueda") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Ticket list
        if (tickets.isEmpty()) {
            Text(
                text = "No hay tickets",
                modifier = Modifier.align(Alignment.CenterHorizontal),
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.onSurfaceVariant
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                items(tickets.size) { index ->
                    val ticket = tickets[index]
                    TicketCard(ticket = ticket, onNavigate = {
                        navController.navigate("ticket-detail") {
                            // Pass ticket ID
                        }
                    })
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        FloatingActionButton(
            onClick = {
                navController.navigate("ticket-create")
            }
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Crear ticket")
        }
    }
}

@Composable
fun TicketCard(
    ticket: Ticket,
    onNavigate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(4.dp),
        elevation = 2.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ticket.title,
                    style = MaterialTheme.typography.h6,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${ticket.priority?.toString() ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = when (ticket.priority) {
                        "HIGH" -> MaterialTheme.colors.error
                        "URGENT" -> MaterialTheme.colors.warning
                        else -> MaterialTheme.colors.onSurface
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Por: ${ticket.createdBy}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colors.onSurfaceVariant
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${ticket.createdAt?.let { it.format() }} ?: "N/A",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colors.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = ticket.description ?: "Sin descripción",
                style = MaterialTheme.typography.body2,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { onNavigate() },
                    modifier = Modifier.weight(0.5f)
                ) {
                    Text("Ver detalle")
                }

                OutlinedButton(
                    onClick = { /* Editar ticket */ },
                    modifier = Modifier.weight(0.5f)
                ) {
                    Text("Editar")
                }
            }
        }
    }
}