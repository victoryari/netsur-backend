package com.ticket.common.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ticket.common.ui.theme.TicketSpacing

/**
 * Diálogo de confirmación con título, mensaje y acciones.
 *
 * @param visible Si el diálogo es visible
 * @param title Título del diálogo
 * @param message Mensaje del diálogo
 * @param confirmText Texto del botón de confirmación
 * @param cancelText Texto del botón de cancelación
 * @param onConfirm Callback para confirmar
 * @param onCancel Callback para cancelar
 * @param modifier Modificador de Compose
 */
@Composable
fun ConfirmDialog(
    visible: Boolean,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    confirmText: String = "Confirmar",
    cancelText: String = "Cancelar",
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    if (visible) {
        AlertDialog(
            onDismissRequest = onCancel,
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = {
                TicketButton(
                    text = confirmText,
                    onClick = onConfirm,
                    type = ButtonType.Primary
                )
            },
            dismissButton = {
                TicketButton(
                    text = cancelText,
                    onClick = onCancel,
                    type = ButtonType.Text
                )
            },
            modifier = modifier
        )
    }
}

/**
 * Diálogo de información con título, mensaje y botón de cierre.
 *
 * @param visible Si el diálogo es visible
 * @param title Título del diálogo
 * @param message Mensaje del diálogo
 * @param closeText Texto del botón de cierre
 * @param onClose Callback para cerrar
 * @param modifier Modificador de Compose
 */
@Composable
fun InfoDialog(
    visible: Boolean,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    closeText: String = "Cerrar",
    onClose: () -> Unit
) {
    if (visible) {
        AlertDialog(
            onDismissRequest = onClose,
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = {
                TicketButton(
                    text = closeText,
                    onClick = onClose,
                    type = ButtonType.Primary
                )
            },
            modifier = modifier
        )
    }
}

/**
 * Diálogo de carga con mensaje.
 *
 * @param visible Si el diálogo es visible
 * @param message Mensaje de carga
 * @param modifier Modificador de Compose
 */
@Composable
fun LoadingDialog(
    visible: Boolean,
    message: String = "Cargando...",
    modifier: Modifier = Modifier
) {
    if (visible) {
        Dialog(onDismissRequest = {}) {
            Card(
                modifier = modifier.padding(TicketSpacing.lg)
            ) {
                Column(
                    modifier = Modifier.padding(TicketSpacing.xl),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(TicketSpacing.md))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
