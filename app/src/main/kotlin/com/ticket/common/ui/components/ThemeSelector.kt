package com.ticket.common.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.ticket.common.ui.theme.ThemePreference
import com.ticket.common.ui.theme.TicketSpacing

/**
 * Selector de tema con opciones de sistema, claro, oscuro y alto contraste.
 *
 * @param currentPreference Preferencia actual
 * @param onPreferenceChange Callback cuando cambia la preferencia
 * @param modifier Modificador de Compose
 */
@Composable
fun ThemeSelector(
    currentPreference: ThemePreference,
    onPreferenceChange: (ThemePreference) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        ThemeOption(
            icon = Icons.Filled.SettingsBrightness,
            label = "Sistema",
            description = "Usar configuración del dispositivo",
            selected = currentPreference == ThemePreference.SYSTEM,
            onClick = { onPreferenceChange(ThemePreference.SYSTEM) }
        )
        ThemeOption(
            icon = Icons.Filled.LightMode,
            label = "Claro",
            description = "Tema claro siempre",
            selected = currentPreference == ThemePreference.LIGHT,
            onClick = { onPreferenceChange(ThemePreference.LIGHT) }
        )
        ThemeOption(
            icon = Icons.Filled.DarkMode,
            label = "Oscuro",
            description = "Tema oscuro siempre",
            selected = currentPreference == ThemePreference.DARK,
            onClick = { onPreferenceChange(ThemePreference.DARK) }
        )
        ThemeOption(
            icon = Icons.Filled.Visibility,
            label = "Alto contraste",
            description = "Mejor visibilidad y accesibilidad",
            selected = currentPreference == ThemePreference.HIGH_CONTRAST,
            onClick = { onPreferenceChange(ThemePreference.HIGH_CONTRAST) }
        )
    }
}

/**
 * Opción de tema individual.
 */
@Composable
private fun ThemeOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = TicketSpacing.sm, horizontal = TicketSpacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Spacer(Modifier.width(TicketSpacing.md))
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.width(TicketSpacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Diálogo de selección de tema.
 *
 * @param visible Si el diálogo es visible
 * @param currentPreference Preferencia actual
 * @param onPreferenceChange Callback cuando cambia la preferencia
 * @param onDismiss Callback para cerrar
 */
@Composable
fun ThemeSelectorDialog(
    visible: Boolean,
    currentPreference: ThemePreference,
    onPreferenceChange: (ThemePreference) -> Unit,
    onDismiss: () -> Unit
) {
    if (visible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Seleccionar tema") },
            text = {
                ThemeSelector(
                    currentPreference = currentPreference,
                    onPreferenceChange = onPreferenceChange
                )
            },
            confirmButton = {
                TicketButton(
                    text = "Cerrar",
                    onClick = onDismiss,
                    type = ButtonType.Text
                )
            }
        )
    }
}
