package com.ticket.common.ui.accessibility

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp

/**
 * Modificadores de accesibilidad reutilizables.
 * Usa solo APIs estables de Compose.
 */

/**
 * Agrega una etiqueta de accesibilidad al elemento.
 */
fun Modifier.accessibleLabel(label: String): Modifier = composed {
    this.semantics {
        contentDescription = label
    }
}

/**
 * Marca el elemento como deshabilitado para screen readers.
 */
fun Modifier.accessibleDisabled(): Modifier = composed {
    this.semantics {
        disabled()
    }
}

/**
 * Establece el rol del elemento para accesibilidad.
 */
fun Modifier.accessibleRole(role: Role): Modifier = composed {
    this.semantics {
        this.role = role
    }
}

/**
 * Agrega un live region para anuncios dinámicos.
 */
fun Modifier.liveRegion(mode: LiveRegionMode): Modifier = composed {
    this.semantics {
        liveRegion = mode
    }
}

/**
 * Hace el elemento focusable para navegación por teclado.
 */
fun Modifier.accessibleFocusable(): Modifier = composed {
    this.focusable()
}

/**
 * Establece el tamaño mínimo de toque (48dp).
 */
fun Modifier.minimumTouchTarget(): Modifier = composed {
    this.defaultMinSize(
        minWidth = AccessibilityUtils.minimumTouchTarget().dp,
        minHeight = AccessibilityUtils.minimumTouchTarget().dp
    )
}

/**
 * Agrega un estado de seleccionado para screen readers.
 */
fun Modifier.accessibleSelected(selected: Boolean): Modifier = composed {
    this.semantics {
        this.selected = selected
    }
}

/**
 * Oculta el elemento de screen readers.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.hiddenFromAccessibility(): Modifier = composed {
    this.semantics {
        invisibleToUser()
    }
}

/**
 * Marca el elemento como encabezado para screen readers.
 */
fun Modifier.accessibleHeading(): Modifier = composed {
    this.semantics {
        heading()
    }
}

/**
 * Agrega una descripción de texto alternativo para imágenes.
 */
fun Modifier.accessibleImage(description: String): Modifier = composed {
    this.semantics {
        contentDescription = description
        role = Role.Image
    }
}

/**
 * Combina múltiples modificadores de accesibilidad comunes para botones.
 */
fun Modifier.accessibleButton(
    label: String,
    enabled: Boolean = true
): Modifier = composed {
    this
        .minimumTouchTarget()
        .semantics {
            contentDescription = label
            role = Role.Button
            if (!enabled) {
                disabled()
            }
        }
}

/**
 * Combina múltiples modificadores de accesibilidad comunes para campos de texto.
 */
fun Modifier.accessibleTextField(
    label: String,
    isError: Boolean = false,
    errorMessage: String? = null
): Modifier = composed {
    this
        .minimumTouchTarget()
        .semantics {
            contentDescription = label
            if (isError && errorMessage != null) {
                error(errorMessage)
            }
        }
}
