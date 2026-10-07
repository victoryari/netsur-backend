package com.ticket.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.ticket.common.Attachment
import com.ticket.common.Comment
import com.ticket.common.Ticket
import com.ticket.common.User
import com.ticket.common.UserRole
import com.ticket.common.database.TiDBService
import com.ticket.common.ui.components.*
import com.ticket.common.ui.theme.TicketShapes
import com.ticket.common.ui.theme.TicketSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailScreen(ticketId: String, navController: NavController, user: User?) {
    var ticket by remember { mutableStateOf<Ticket?>(null) }
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var attachments by remember { mutableStateOf<List<Attachment>>(emptyList()) }
    var newComment by remember { mutableStateOf("") }

    LaunchedEffect(ticketId) {
        val allTickets = TiDBService.getTickets()
        ticket = allTickets.find { it.id == ticketId }
        comments = TiDBService.getComments(ticketId)
        attachments = TiDBService.getAttachments(ticketId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Ticket") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (ticket != null) {
                // Tarjeta Principal
                TicketCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TicketStatusChip(status = ticket!!.status)
                            PriorityChip(priority = ticket!!.priority)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = ticket!!.title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = ticket!!.description ?: "Sin descripción adicional.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Adjuntos
                if (attachments.isNotEmpty()) {
                    Text(
                        text = "Archivos adjuntos (${attachments.size})",
                        style = MaterialTheme.typography.titleMedium
                    )

                    attachments.forEach { attach ->
                        TicketCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (attach.mimeType.startsWith("image/")) {
                                AsyncImage(
                                    model = attach.fileUrl,
                                    contentDescription = attach.fileName,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(TicketShapes.md),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.AttachFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(10.dp))
                                    Text(attach.fileName, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }

                // Acciones según rol
                if (user?.role == UserRole.ADMIN || user?.role == UserRole.AGENT) {
                    Text(
                        text = "Actualizar Estado",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("open", "in_progress", "resolved", "closed").forEach { status ->
                            FilterChip(
                                selected = ticket!!.status == status,
                                onClick = { /* Update status */ },
                                label = { Text(status, style = MaterialTheme.typography.labelMedium) },
                                shape = TicketShapes.full
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Comentarios
                Text(
                    text = "Comentarios",
                    style = MaterialTheme.typography.titleMedium
                )

                TicketTextField(
                    value = newComment,
                    onValueChange = { newComment = it },
                    label = "Escribe un comentario...",
                    singleLine = false,
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TicketButton(
                        text = "Enviar",
                        onClick = {
                            newComment = ""
                        },
                        enabled = newComment.isNotBlank(),
                        type = ButtonType.Primary,
                        size = ButtonSize.Medium,
                        icon = Icons.Filled.Send
                    )
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
