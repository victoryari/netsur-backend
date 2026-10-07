package com.ticket.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ticket.common.Ticket
import com.ticket.common.Ticket.TicketStatus
import com.ticket.common.Ticket.TicketPriority

@Composable
fun TicketCreateScreen(navController: NavController) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(TicketPriority.MEDIUM) }
    var status by remember { mutableStateOf(TicketStatus.OPEN) }
    var category by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f).padding(16.dp),
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Crear Nuevo Ticket",
                    style = MaterialTheme.typography.h5,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Multiline),
                    softKeyboard = KeyboardActions(onDone = { }),
                    singleLine = false,
                    maxLines = 5
                )

                // Priority selector
                Text(
                    text = "Prioridad",
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )

                OutlinedButtonGroup(
                    value = priority,
                    onValueChange = { priority = it },
                    options = TicketPriority.values().map { priority ->
                        OutlinedButtonGroup.Option(
                            value = priority,
                            selected = priority == this.priority,
                            onClick = { /* handled by group */ },
                            text = { Text(priority.toString()) }
                        )
                    }
                )

                // Status selector
                Text(
                    text = "Status",
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )

                OutlinedButtonGroup(
                    value = status,
                    onValueChange = { status = it },
                    options = TicketStatus.values().map { status ->
                        OutlinedButtonGroup.Option(
                            value = status,
                            selected = status == this.status,
                            onClick = { /* handled by group */ },
                            text = { Text(status.toString()) }
                        )
                    }
                )

                // Category
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoría") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = "Categoría")
                    }
                )

                if (!error.isEmpty()) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colors.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Button(
                    onClick = async {
                        loading = true
                        error = ""
                        try {
                            val newTicket = Ticket(
                                id = java.util.UUID.randomUUID().toString(),
                                title = title,
                                description = if (description.isNotEmpty()) description else null,
                                status = status,
                                priority = priority,
                                category = if (category.isNotEmpty()) category else null,
                                createdBy = "current-user", // TODO: Get from auth
                                createdAt = System.currentTimeMillis() / 1000,
                                updatedAt = System.currentTimeMillis() / 1000,
                                assignedTo = null
                            )

                            await SupabaseRepository().createTicket(newTicket)
                            navController.navigate("ticket-list")
                        } catch (e: Exception) {
                            error = "Error al crear ticket: ${e.message}"
                        } finally {
                            loading = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading
                ) {
                    if (loading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterRight))
                    } else {
                        Text("Crear Ticket")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        navController.navigate("ticket-list")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancelar")
                }
            }
        }
    }
}