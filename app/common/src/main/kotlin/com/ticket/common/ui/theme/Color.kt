package com.ticket.common.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta de colores semántica de TicketApp.
 * Todos los colores deben ser accesibles con contraste mínimo 4.5:1 para texto normal
 * y 3:1 para texto grande (18pt+ o 14pt bold+).
 */
object TicketColors {
    // Primary - Azul corporativo
    val Primary = Color(0xFF1E3A8A)
    val PrimaryVariant = Color(0xFF3B82F6)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFFDBEAFE)
    val OnPrimaryContainer = Color(0xFF1E3A8A)

    // Secondary - Azul claro
    val Secondary = Color(0xFF60A5FA)
    val OnSecondary = Color(0xFFFFFFFF)
    val SecondaryContainer = Color(0xFFEFF6FF)
    val OnSecondaryContainer = Color(0xFF1E40AF)

    // Error - Rojo
    val Error = Color(0xFFDC2626)
    val OnError = Color(0xFFFFFFFF)
    val ErrorContainer = Color(0xFFFEE2E2)
    val OnErrorContainer = Color(0xFF991B1B)

    // Success - Verde
    val Success = Color(0xFF16A34A)
    val OnSuccess = Color(0xFFFFFFFF)
    val SuccessContainer = Color(0xFFDCFCE7)
    val OnSuccessContainer = Color(0xFF166534)

    // Warning - Ámbar
    val Warning = Color(0xFFF59E0B)
    val OnWarning = Color(0xFFFFFFFF)
    val WarningContainer = Color(0xFFFEF3C7)
    val OnWarningContainer = Color(0xFF92400E)

    // Info - Azul informativo
    val Info = Color(0xFF0EA5E9)
    val OnInfo = Color(0xFFFFFFFF)
    val InfoContainer = Color(0xFFE0F2FE)
    val OnInfoContainer = Color(0xFF075985)

    // Surface - Superficies
    val Surface = Color(0xFFFFFFFF)
    val OnSurface = Color(0xFF1F2937)
    val SurfaceVariant = Color(0xFFF3F4F6)
    val OnSurfaceVariant = Color(0xFF6B7280)
    val SurfaceTint = Color(0xFF1E3A8A)

    // Background - Fondo
    val Background = Color(0xFFF9FAFB)
    val OnBackground = Color(0xFF1F2937)

    // Outline - Bordes
    val Outline = Color(0xFFD1D5DB)
    val OutlineVariant = Color(0xFFE5E7EB)

    // Inverse - Colores inversos
    val InverseSurface = Color(0xFF1F2937)
    val InverseOnSurface = Color(0xFFF9FAFB)
    val InversePrimary = Color(0xFF60A5FA)

    // Transparencias
    val Scrim = Color(0xFF000000)
    val Shadow = Color(0xFF000000)
}

/**
 * Colores para modo oscuro.
 * Asegurar contraste adecuado en fondos oscuros.
 */
object TicketDarkColors {
    // Primary
    val Primary = Color(0xFF60A5FA)
    val PrimaryVariant = Color(0xFF3B82F6)
    val OnPrimary = Color(0xFF1E3A8A)
    val PrimaryContainer = Color(0xFF1E40AF)
    val OnPrimaryContainer = Color(0xFFDBEAFE)

    // Secondary
    val Secondary = Color(0xFF93C5FD)
    val OnSecondary = Color(0xFF1E3A8A)
    val SecondaryContainer = Color(0xFF1E40AF)
    val OnSecondaryContainer = Color(0xFFEFF6FF)

    // Error
    val Error = Color(0xFFF87171)
    val OnError = Color(0xFF7F1D1D)
    val ErrorContainer = Color(0xFF991B1B)
    val OnErrorContainer = Color(0xFFFEE2E2)

    // Success
    val Success = Color(0xFF4ADE80)
    val OnSuccess = Color(0xFF14532D)
    val SuccessContainer = Color(0xFF166534)
    val OnSuccessContainer = Color(0xFFDCFCE7)

    // Warning
    val Warning = Color(0xFFFBBF24)
    val OnWarning = Color(0xFF78350F)
    val WarningContainer = Color(0xFF92400E)
    val OnWarningContainer = Color(0xFFFEF3C7)

    // Info
    val Info = Color(0xFF38BDF8)
    val OnInfo = Color(0xFF0C4A6E)
    val InfoContainer = Color(0xFF075985)
    val OnInfoContainer = Color(0xFFE0F2FE)

    // Surface
    val Surface = Color(0xFF1F2937)
    val OnSurface = Color(0xFFF9FAFB)
    val SurfaceVariant = Color(0xFF374151)
    val OnSurfaceVariant = Color(0xFFD1D5DB)
    val SurfaceTint = Color(0xFF60A5FA)

    // Background
    val Background = Color(0xFF111827)
    val OnBackground = Color(0xFFF9FAFB)

    // Outline
    val Outline = Color(0xFF4B5563)
    val OutlineVariant = Color(0xFF374151)

    // Inverse
    val InverseSurface = Color(0xFFF9FAFB)
    val InverseOnSurface = Color(0xFF1F2937)
    val InversePrimary = Color(0xFF1E3A8A)

    // Transparencias
    val Scrim = Color(0xFF000000)
    val Shadow = Color(0xFF000000)
}
