package com.ticket.common.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ticket.common.ui.theme.TicketColors
import com.ticket.common.ui.theme.TicketShapes

/**
 * Chip de estado estilizado tipo píldora con color semántico.
 */
@Composable
fun StatusChip(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = if (onClick != null) modifier.then(Modifier.padding(0.dp)) else modifier,
        shape = TicketShapes.full,
        color = color.copy(alpha = 0.12f),
        contentColor = color,
        onClick = onClick ?: {}
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color, shape = CircleShape)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

/**
 * Chip de prioridad con colores semánticos predefinidos.
 */
@Composable
fun PriorityChip(
    priority: String,
    modifier: Modifier = Modifier
) {
    val (label, color) = when (priority.lowercase()) {
        "baja", "low" -> "Baja" to TicketColors.Info
        "media", "medium" -> "Media" to TicketColors.Success
        "alta", "high" -> "Alta" to TicketColors.Warning
        "urgente", "urgent" -> "Urgente" to TicketColors.Error
        else -> priority.uppercase() to TicketColors.OnSurfaceVariant
    }

    StatusChip(
        label = label,
        color = color,
        modifier = modifier
    )
}

/**
 * Chip de estado de ticket con colores predefinidos.
 */
@Composable
fun TicketStatusChip(
    status: String,
    modifier: Modifier = Modifier
) {
    val (label, color) = when (status.lowercase()) {
        "open", "abierto" -> "Abierto" to TicketColors.Info
        "in_progress", "en progreso", "en_proceso" -> "En Progreso" to TicketColors.Warning
        "resolved", "resuelto" -> "Resuelto" to TicketColors.Success
        "closed", "cerrado" -> "Cerrado" to TicketColors.OnSurfaceVariant
        else -> status to TicketColors.OnSurfaceVariant
    }

    StatusChip(
        label = label,
        color = color,
        modifier = modifier
    )
}

/**
 * Chip con icono.
 */
@Composable
fun IconChip(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    AssistChip(
        onClick = onClick ?: {},
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        shape = TicketShapes.full,
        modifier = modifier
    )
}
