package com.ticket.common.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta de colores NETSUR.
 * Basada en la identidad de marca NETSUR: Naranja Calidóscopo (#EA580C / #F97316),
 * Coral Rojo (#E11D48) y Pizarra Profundo (#0F172A).
 */
object TicketColors {
    // Primary - Naranja NETSUR
    val Primary = Color(0xFFEA580C)           // Orange 600
    val PrimaryVariant = Color(0xFFF97316)    // Orange 500
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFFFFEDD5)   // Orange 100
    val OnPrimaryContainer = Color(0xFF7C2D12) // Orange 900

    // Secondary - Coral / Rojo NETSUR
    val Secondary = Color(0xFFE11D48)         // Rose 600
    val OnSecondary = Color(0xFFFFFFFF)
    val SecondaryContainer = Color(0xFFFFE4E6) // Rose 100
    val OnSecondaryContainer = Color(0xFF881337) // Rose 900

    // Status Colors
    val Error = Color(0xFFDC2626)             // Red 600
    val OnError = Color(0xFFFFFFFF)
    val ErrorContainer = Color(0xFFFEE2E2)     // Red 100
    val OnErrorContainer = Color(0xFF991B1B)   // Red 800

    val Success = Color(0xFF059669)           // Emerald 600
    val OnSuccess = Color(0xFFFFFFFF)
    val SuccessContainer = Color(0xFFD1FAE5)   // Emerald 100
    val OnSuccessContainer = Color(0xFF065F46) // Emerald 800

    val Warning = Color(0xFFD97706)           // Amber 600
    val OnWarning = Color(0xFFFFFFFF)
    val WarningContainer = Color(0xFFFEF3C7)   // Amber 100
    val OnWarningContainer = Color(0xFF92400E) // Amber 800

    val Info = Color(0xFF0284C7)              // Sky 600
    val OnInfo = Color(0xFFFFFFFF)
    val InfoContainer = Color(0xFFE0F2FE)      // Sky 100
    val OnInfoContainer = Color(0xFF075985)    // Sky 800

    // Surface & Background
    val Surface = Color(0xFFFFFFFF)
    val OnSurface = Color(0xFF181310)         // Dark Brown / Slate
    val SurfaceVariant = Color(0xFFF8F4F0)    // Warm Surface Tint
    val OnSurfaceVariant = Color(0xFF574E4A)   // Warm Gray
    val SurfaceTint = Color(0xFFEA580C)

    val Background = Color(0xFFFAFAFA)
    val OnBackground = Color(0xFF181310)

    // Borders & Outlines
    val Outline = Color(0xFFE5DCD6)           // Warm Outline
    val OutlineVariant = Color(0xFFF0E8E2)

    // Inverse Colors
    val InverseSurface = Color(0xFF181310)
    val InverseOnSurface = Color(0xFFFAFAFA)
    val InversePrimary = Color(0xFFFB923C)

    // Shadows & Scrim
    val Scrim = Color(0xFF000000)
    val Shadow = Color(0xFF181310)
}

/**
 * Colores para modo oscuro NETSUR.
 */
object TicketDarkColors {
    // Primary
    val Primary = Color(0xFFFB923C)           // Orange 400
    val PrimaryVariant = Color(0xFFF97316)    // Orange 500
    val OnPrimary = Color(0xFF431407)
    val PrimaryContainer = Color(0xFF7C2D12)   // Orange 900
    val OnPrimaryContainer = Color(0xFFFFEDD5) // Orange 100

    // Secondary
    val Secondary = Color(0xFFFB7185)         // Rose 400
    val OnSecondary = Color(0xFF4C0519)
    val SecondaryContainer = Color(0xFF881337)
    val OnSecondaryContainer = Color(0xFFFFE4E6)

    // Status Colors
    val Error = Color(0xFFF87171)
    val OnError = Color(0xFF450A0A)
    val ErrorContainer = Color(0xFF7F1D1D)
    val OnErrorContainer = Color(0xFFFEE2E2)

    val Success = Color(0xFF34D399)
    val OnSuccess = Color(0xFF064E3B)
    val SuccessContainer = Color(0xFF065F46)
    val OnSuccessContainer = Color(0xFFD1FAE5)

    val Warning = Color(0xFFFBBF24)
    val OnWarning = Color(0xFF451A03)
    val WarningContainer = Color(0xFF78350F)
    val OnWarningContainer = Color(0xFFFEF3C7)

    val Info = Color(0xFF38BDF8)
    val OnInfo = Color(0xFF0C4A6E)
    val InfoContainer = Color(0xFF075985)
    val OnInfoContainer = Color(0xFFE0F2FE)

    // Surface & Background
    val Surface = Color(0xFF1F1B18)           // Warm Midnight
    val OnSurface = Color(0xFFFAFAFA)
    val SurfaceVariant = Color(0xFF2D2723)
    val OnSurfaceVariant = Color(0xFFA89F99)
    val SurfaceTint = Color(0xFFFB923C)

    val Background = Color(0xFF120E0C)
    val OnBackground = Color(0xFFFAFAFA)

    // Borders & Outlines
    val Outline = Color(0xFF4A423C)
    val OutlineVariant = Color(0xFF332D28)

    // Inverse
    val InverseSurface = Color(0xFFFAFAFA)
    val InverseOnSurface = Color(0xFF181310)
    val InversePrimary = Color(0xFFEA580C)

    // Shadows & Scrim
    val Scrim = Color(0xFF000000)
    val Shadow = Color(0xFF000000)
}
