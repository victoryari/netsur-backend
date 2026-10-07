package com.ticket.common.ui.accessibility

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.ticket.common.ui.theme.TicketSpacing

/**
 * Botón accesible con etiqueta y tamaño mínimo de toque.
 *
 * @param text Texto del botón
 * @param onClick Acción al hacer clic
 * @param modifier Modificador de Compose
 * @param enabled Si el botón está habilitado
 * @param contentDescription Descripción para screen readers (opcional)
 */
@Composable
fun AccessibleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(
                minWidth = AccessibilityUtils.minimumTouchTarget().dp,
                minHeight = AccessibilityUtils.minimumTouchTarget().dp
            )
            .semantics {
                if (contentDescription != null) {
                    this.contentDescription = contentDescription
                }
                role = Role.Button
                if (!enabled) {
                    disabled()
                }
            },
        enabled = enabled
    ) {
        Text(text)
    }
}

/**
 * Campo de texto accesible con etiqueta y descripción.
 *
 * @param value Valor actual
 * @param onValueChange Callback cuando cambia
 * @param label Etiqueta visible
 * @param modifier Modificador de Compose
 * @param placeholder Placeholder (opcional)
 * @param isError Si tiene error
 * @param errorMessage Mensaje de error (opcional)
 * @param contentDescription Descripción para screen readers (opcional)
 */
@Composable
fun AccessibleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    contentDescription: String? = null
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = placeholder?.let { { Text(it) } },
            isError = isError,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(
                    minHeight = AccessibilityUtils.minimumTouchTarget().dp
                )
                .semantics {
                    if (contentDescription != null) {
                        this.contentDescription = contentDescription
                    }
                    if (isError && errorMessage != null) {
                        error(errorMessage)
                    }
                }
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(start = TicketSpacing.md, top = TicketSpacing.xs)
                    .semantics {
                        liveRegion = LiveRegionMode.Polite
                    }
            )
        }
    }
}

/**
 * IconButton accesible con descripción obligatoria.
 *
 * @param icon Icono del botón
 * @param contentDescription Descripción para screen readers (obligatorio)
 * @param onClick Acción al hacer clic
 * @param modifier Modificador de Compose
 * @param enabled Si el botón está habilitado
 */
@Composable
fun AccessibleIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(AccessibilityUtils.minimumTouchTarget().dp)
            .semantics {
                this.contentDescription = contentDescription
                role = Role.Button
                if (!enabled) {
                    disabled()
                }
            },
        enabled = enabled
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null // Ya se proporciona en el modifier
        )
    }
}

/**
 * Tarjeta accesible con rol y descripción.
 *
 * @param modifier Modificador de Compose
 * @param onClick Acción al hacer clic (opcional)
 * @param contentDescription Descripción para screen readers (opcional)
 * @param content Contenido de la tarjeta
 */
@Composable
fun AccessibleCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentDescription: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val semanticsModifier = if (contentDescription != null) {
        modifier.semantics {
            this.contentDescription = contentDescription
            role = Role.Button
        }
    } else {
        modifier
    }

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = semanticsModifier,
            content = content
        )
    } else {
        Card(
            modifier = semanticsModifier,
            content = content
        )
    }
}

/**
 * Texto accesible con live region para anuncios dinámicos.
 *
 * @param text Texto a mostrar
 * @param modifier Modificador de Compose
 * @param liveRegion Modo de live region (polite, assertive, off)
 */
@Composable
fun AccessibleText(
    text: String,
    modifier: Modifier = Modifier,
    liveRegion: LiveRegionMode = LiveRegionMode.Polite
) {
    Text(
        text = text,
        modifier = modifier.semantics {
            this.liveRegion = liveRegion
        }
    )
}

/**
 * Contenedor accesible con navegación por teclado.
 *
 * @param modifier Modificador de Compose
 * @param content Contenido
 */
@Composable
fun AccessibleFocusable(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.focusable()
    ) {
        content()
    }
}
