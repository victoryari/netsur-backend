package com.ticket.common.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ticket.common.ui.theme.TicketShapes

enum class ButtonType {
    Primary,
    Secondary,
    Outlined,
    Text,
    Error
}

enum class ButtonSize {
    Small,
    Medium,
    Large
}

/**
 * Botón moderno y refinado de TicketApp con elevación, bordes e indicadores de carga.
 */
@Composable
fun TicketButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: ButtonType = ButtonType.Primary,
    size: ButtonSize = ButtonSize.Medium,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    val buttonShape = when (size) {
        ButtonSize.Small -> TicketShapes.sm
        ButtonSize.Medium -> TicketShapes.md
        ButtonSize.Large -> TicketShapes.md
    }

    val heightModifier = when (size) {
        ButtonSize.Small -> Modifier.heightIn(min = 38.dp)
        ButtonSize.Medium -> Modifier.heightIn(min = 48.dp)
        ButtonSize.Large -> Modifier.heightIn(min = 54.dp)
    }

    val contentPadding = when (size) {
        ButtonSize.Small -> PaddingValues(horizontal = 14.dp, vertical = 6.dp)
        ButtonSize.Medium -> PaddingValues(horizontal = 20.dp, vertical = 12.dp)
        ButtonSize.Large -> PaddingValues(horizontal = 28.dp, vertical = 16.dp)
    }

    val textStyle = when (size) {
        ButtonSize.Small -> MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
        ButtonSize.Medium -> MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
        ButtonSize.Large -> MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
    }

    val buttonContent: @Composable RowScope.() -> Unit = {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.5.dp,
                color = when (type) {
                    ButtonType.Primary, ButtonType.Error -> MaterialTheme.colorScheme.onPrimary
                    ButtonType.Secondary -> MaterialTheme.colorScheme.onSecondaryContainer
                    else -> MaterialTheme.colorScheme.primary
                }
            )
            Spacer(Modifier.width(10.dp))
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
        }
        Text(text = text, style = textStyle)
    }

    when (type) {
        ButtonType.Primary -> {
            Button(
                onClick = onClick,
                modifier = modifier.then(heightModifier),
                enabled = enabled && !loading,
                shape = buttonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = contentPadding,
                content = buttonContent
            )
        }
        ButtonType.Secondary -> {
            FilledTonalButton(
                onClick = onClick,
                modifier = modifier.then(heightModifier),
                enabled = enabled && !loading,
                shape = buttonShape,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                contentPadding = contentPadding,
                content = buttonContent
            )
        }
        ButtonType.Outlined -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier.then(heightModifier),
                enabled = enabled && !loading,
                shape = buttonShape,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.25.dp,
                    color = if (enabled) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outlineVariant
                ),
                contentPadding = contentPadding,
                content = buttonContent
            )
        }
        ButtonType.Text -> {
            TextButton(
                onClick = onClick,
                modifier = modifier.then(heightModifier),
                enabled = enabled && !loading,
                shape = buttonShape,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = contentPadding,
                content = buttonContent
            )
        }
        ButtonType.Error -> {
            Button(
                onClick = onClick,
                modifier = modifier.then(heightModifier),
                enabled = enabled && !loading,
                shape = buttonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                contentPadding = contentPadding,
                content = buttonContent
            )
        }
    }
}

/**
 * Botón de icono estilizado.
 */
@Composable
fun TicketIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint
        )
    }
}
