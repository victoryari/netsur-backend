package com.ticket.common.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ticket.common.ui.theme.*

@Composable
fun TicketTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = TicketDarkColors.Primary,
            onPrimary = TicketDarkColors.OnPrimary,
            primaryContainer = TicketDarkColors.PrimaryContainer,
            onPrimaryContainer = TicketDarkColors.OnPrimaryContainer,
            secondary = TicketDarkColors.Secondary,
            onSecondary = TicketDarkColors.OnSecondary,
            secondaryContainer = TicketDarkColors.SecondaryContainer,
            onSecondaryContainer = TicketDarkColors.OnSecondaryContainer,
            tertiary = TicketDarkColors.Info,
            onTertiary = TicketDarkColors.OnInfo,
            tertiaryContainer = TicketDarkColors.InfoContainer,
            onTertiaryContainer = TicketDarkColors.OnInfoContainer,
            error = TicketDarkColors.Error,
            onError = TicketDarkColors.OnError,
            errorContainer = TicketDarkColors.ErrorContainer,
            onErrorContainer = TicketDarkColors.OnErrorContainer,
            background = TicketDarkColors.Background,
            onBackground = TicketDarkColors.OnBackground,
            surface = TicketDarkColors.Surface,
            onSurface = TicketDarkColors.OnSurface,
            surfaceVariant = TicketDarkColors.SurfaceVariant,
            onSurfaceVariant = TicketDarkColors.OnSurfaceVariant,
            outline = TicketDarkColors.Outline,
            outlineVariant = TicketDarkColors.OutlineVariant,
            scrim = TicketDarkColors.Scrim,
            inverseSurface = TicketDarkColors.InverseSurface,
            inverseOnSurface = TicketDarkColors.InverseOnSurface,
            inversePrimary = TicketDarkColors.InversePrimary,
            surfaceTint = TicketDarkColors.SurfaceTint,
        )
    } else {
        lightColorScheme(
            primary = TicketColors.Primary,
            onPrimary = TicketColors.OnPrimary,
            primaryContainer = TicketColors.PrimaryContainer,
            onPrimaryContainer = TicketColors.OnPrimaryContainer,
            secondary = TicketColors.Secondary,
            onSecondary = TicketColors.OnSecondary,
            secondaryContainer = TicketColors.SecondaryContainer,
            onSecondaryContainer = TicketColors.OnSecondaryContainer,
            tertiary = TicketColors.Info,
            onTertiary = TicketColors.OnInfo,
            tertiaryContainer = TicketColors.InfoContainer,
            onTertiaryContainer = TicketColors.OnInfoContainer,
            error = TicketColors.Error,
            onError = TicketColors.OnError,
            errorContainer = TicketColors.ErrorContainer,
            onErrorContainer = TicketColors.OnErrorContainer,
            background = TicketColors.Background,
            onBackground = TicketColors.OnBackground,
            surface = TicketColors.Surface,
            onSurface = TicketColors.OnSurface,
            surfaceVariant = TicketColors.SurfaceVariant,
            onSurfaceVariant = TicketColors.OnSurfaceVariant,
            outline = TicketColors.Outline,
            outlineVariant = TicketColors.OutlineVariant,
            scrim = TicketColors.Scrim,
            inverseSurface = TicketColors.InverseSurface,
            inverseOnSurface = TicketColors.InverseOnSurface,
            inversePrimary = TicketColors.InversePrimary,
            surfaceTint = TicketColors.SurfaceTint,
        )
    }

    val typography = Typography(
        displayLarge = TicketTypography.DisplayLarge,
        displayMedium = TicketTypography.DisplayMedium,
        displaySmall = TicketTypography.DisplaySmall,
        headlineLarge = TicketTypography.HeadlineLarge,
        headlineMedium = TicketTypography.HeadlineMedium,
        headlineSmall = TicketTypography.HeadlineSmall,
        titleLarge = TicketTypography.TitleLarge,
        titleMedium = TicketTypography.TitleMedium,
        titleSmall = TicketTypography.TitleSmall,
        bodyLarge = TicketTypography.BodyLarge,
        bodyMedium = TicketTypography.BodyMedium,
        bodySmall = TicketTypography.BodySmall,
        labelLarge = TicketTypography.LabelLarge,
        labelMedium = TicketTypography.LabelMedium,
        labelSmall = TicketTypography.LabelSmall,
    )

    val shapes = Shapes(
        extraSmall = TicketShapes.xs,
        small = TicketShapes.sm,
        medium = TicketShapes.md,
        large = TicketShapes.lg,
        extraLarge = TicketShapes.xl,
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = shapes,
        content = content
    )
}

/**
 * Proporciona acceso a los tokens de espaciado y elevación
 * que no están incluidos en MaterialTheme por defecto.
 */
object TicketThemeTokens {
    val spacing: TicketSpacing
        @Composable
        @ReadOnlyComposable
        get() = TicketSpacing

    val elevation: TicketElevation
        @Composable
        @ReadOnlyComposable
        get() = TicketElevation
}
