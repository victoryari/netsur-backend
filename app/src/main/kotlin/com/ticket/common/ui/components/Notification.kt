package com.ticket.common.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ticket.common.ui.theme.TicketColors
import com.ticket.common.ui.theme.TicketSpacing
import kotlinx.coroutines.delay

/**
 * Tipos de notificación.
 */
enum class NotificationType {
    Success,
    Error,
    Warning,
    Info
}

/**
 * Notificación con título, mensaje y tipo.
 *
 * @param title Título de la notificación
 * @param message Mensaje de la notificación
 * @param type Tipo de notificación
 * @param onDismiss Callback para cerrar
 * @param modifier Modificador de Compose
 */
@Composable
fun TicketNotification(
    title: String,
    message: String,
    type: NotificationType,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null
) {
    val (icon, color) = when (type) {
        NotificationType.Success -> Icons.Filled.CheckCircle to TicketColors.Success
        NotificationType.Error -> Icons.Filled.Error to TicketColors.Error
        NotificationType.Warning -> Icons.Filled.Warning to TicketColors.Warning
        NotificationType.Info -> Icons.Filled.Info to TicketColors.Info
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(TicketSpacing.md),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(TicketSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = color
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (onDismiss != null) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Error,
                        contentDescription = "Cerrar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Snackbar con acción opcional.
 *
 * @param message Mensaje del snackbar
 * @param actionLabel Etiqueta de la acción (opcional)
 * @param onAction Callback de la acción (opcional)
 * @param onDismiss Callback para cerrar
 * @param duration Duración del snackbar
 */
@Composable
fun TicketSnackbar(
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    duration: SnackbarDuration = SnackbarDuration.Short
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        val result = snackbarHostState.showSnackbar(
            message = message,
            actionLabel = actionLabel,
            duration = duration
        )
        if (result == SnackbarResult.ActionPerformed) {
            onAction?.invoke()
        }
        onDismiss?.invoke()
    }

    SnackbarHost(
        hostState = snackbarHostState,
        modifier = modifier
    )
}

/**
 * Notificación flotante con auto-cierre.
 *
 * @param visible Si la notificación es visible
 * @param title Título de la notificación
 * @param message Mensaje de la notificación
 * @param type Tipo de notificación
 * @param onDismiss Callback para cerrar
 * @param autoDismissDelay Tiempo de auto-cierre en ms
 */
@Composable
fun FloatingNotification(
    visible: Boolean,
    title: String,
    message: String,
    type: NotificationType,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    autoDismissDelay: Long = 3000L
) {
    if (visible) {
        LaunchedEffect(visible) {
            delay(autoDismissDelay)
            onDismiss()
        }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(TicketSpacing.md)
        ) {
            TicketNotification(
                title = title,
                message = message,
                type = type,
                onDismiss = onDismiss
            )
        }
    }
}
