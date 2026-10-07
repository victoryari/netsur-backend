package com.ticket.common.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Colores de alto contraste para accesibilidad.
 * Cumple con WCAG 2.1 AAA (contraste 7:1 para texto normal).
 */
object HighContrastColors {
    // Primary - Azul oscuro con máximo contraste
    val Primary = Color(0xFF000000)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFF000000)
    val OnPrimaryContainer = Color(0xFFFFFFFF)

    // Secondary
    val Secondary = Color(0xFF000000)
    val OnSecondary = Color(0xFFFFFFFF)
    val SecondaryContainer = Color(0xFFFFFFFF)
    val OnSecondaryContainer = Color(0xFF000000)

    // Error - Rojo oscuro
    val Error = Color(0xFFB00020)
    val OnError = Color(0xFFFFFFFF)
    val ErrorContainer = Color(0xFFB00020)
    val OnErrorContainer = Color(0xFFFFFFFF)

    // Success - Verde oscuro
    val Success = Color(0xFF006400)
    val OnSuccess = Color(0xFFFFFFFF)
    val SuccessContainer = Color(0xFF006400)
    val OnSuccessContainer = Color(0xFFFFFFFF)

    // Warning - Naranja oscuro
    val Warning = Color(0xFF8B4513)
    val OnWarning = Color(0xFFFFFFFF)
    val WarningContainer = Color(0xFF8B4513)
    val OnWarningContainer = Color(0xFFFFFFFF)

    // Info - Azul oscuro
    val Info = Color(0xFF00008B)
    val OnInfo = Color(0xFFFFFFFF)
    val InfoContainer = Color(0xFF00008B)
    val OnInfoContainer = Color(0xFFFFFFFF)

    // Surface - Blanco puro
    val Surface = Color(0xFFFFFFFF)
    val OnSurface = Color(0xFF000000)
    val SurfaceVariant = Color(0xFFFFFFFF)
    val OnSurfaceVariant = Color(0xFF000000)
    val SurfaceTint = Color(0xFF000000)

    // Background - Blanco puro
    val Background = Color(0xFFFFFFFF)
    val OnBackground = Color(0xFF000000)

    // Outline - Negro
    val Outline = Color(0xFF000000)
    val OutlineVariant = Color(0xFF000000)

    // Inverse
    val InverseSurface = Color(0xFF000000)
    val InverseOnSurface = Color(0xFFFFFFFF)
    val InversePrimary = Color(0xFFFFFFFF)

    // Transparencias
    val Scrim = Color(0xFF000000)
    val Shadow = Color(0xFF000000)
}
