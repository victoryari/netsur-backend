package com.ticket.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ticket.common.Ticket
import com.ticket.common.Comment
import com.ticket.common.viewmodel.ticketViewModel
import com.ticket.android.MainActivity

@Composable
fun TicketDetailScreen(
    ticketId: String,
    navController: NavController
) {
    val viewModel: ticketViewModel = remember { ticketViewModel() }
    var ticket by remember { mutableStateOf<Ticket?>(null) }
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var textComment by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    // Load ticket and comments
    SideEffect {
        isLoading = true
        viewModel.getTickets().collect { tickets ->
            ticket = tickets.firstOrNull { it.id == ticketId }
        }
        viewModel.getComments(ticketId).collect { commentsList ->
            comments = commentsList
        }
        isLoading = false
    }

    Scaffold(
        bottomBar = {
            BottomNavigation {
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    onClick = {
                        navController.navigate("dashboard")
                    }
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.Ticket, contentDescription = "Tickets") },
                    label = { Text("Tickets") },
                    onClick = {
                        navController.navigate("ticket-list")
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            verticalArrangement = Arrangement.Top
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (ticket != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    elevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.Start
                    ) {
                        // Ticket header info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ticket.title,
                                style = MaterialTheme.typography.h4,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                            Spacer(Modifier.width(8.dp))
                            Chip(
                                label = { Text(ticket.status.toString()) },
                                style = ChipStyle.Filled
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {},
                                modifier = Modifier.weight(0.5f)
                            ) {
                                Text("Asignar")
                            }
                            OutlinedButton(
                                onClick = {},
                                modifier = Modifier.weight(0.5f)
                            ) {
                                Text("Comentar")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Description
                        Text(
                            text = "Descripción:",
                            style = MaterialTheme.typography.subtitle1,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = ticket.description ?: "Sin descripción",
                            style = MaterialTheme.typography.body1,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Comments section
 Card(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            elevation = 2.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.Start
                            ) {
                                Text(
                                    text = "Comentarios (${comments.size})",
                                    style = MaterialTheme.typography.subtitle2,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                if (comments.isEmpty()) {
                                    Text(
                                        text = "No hay comentarios aún",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.align(Alignment.CenterHorizontal)
                                    )
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        items(comments.size) { index ->
                                            val comment = comments[index]
                                            CommentCard(comment = comment)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Add comment form
                                OutlinedTextField(
                                    value = textComment,
                                    onValueChange = { textComment = it },
                                    label = { Text("Nuevo comentario") },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                    softKeyboard = KeyboardActions(onCommit = {
                                        addComment(ticketId, textComment)
                                        textComment = ""
                                    })
                                )

                                Button(
                                    onClick = {
                                        addComment(ticketId, textComment)
                                        textComment = ""
                                    },
                                    modifier = Modifier.align(Alignment.CenterEnd)
                                ) {
                                    Text("Publicar")
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "Ticket no encontrado",
                    modifier = Modifier.align(Alignment.CenterHorizontal),
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.error
                )
            }
        }
    }
}

@Composable
fun CommentCard(comment: Comment, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleAvatar(
                    size = 20.dp,
                    contentDescription = "Comentario"
                )
                Spacer(Modifier.width(8.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = comment.author,
                        style = MaterialTheme.typography.bodySmall,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                    Text(
                        text = comment.text,
                        style = MaterialTheme.typography.caption,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${comment.createdAt?.let { it.format() }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colors.onSurfaceVariant
            )
        }
    }
}

private fun addComment(ticketId: String, commentText: String) {
    // TODO: Implement comment creation
}