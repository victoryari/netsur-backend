package com.ticket.common.ui.animation

import androidx.compose.animation.core.*
import androidx.compose.ui.unit.dp

/**
 * Duraciones de animación estándar.
 */
object TicketAnimations {
    /** Rápido - 150ms: Micro-interacciones, cambios de estado */
    const val FAST = 150

    /** Normal - 300ms: Transiciones estándar */
    const val NORMAL = 300

    /** Lento - 500ms: Transiciones de pantalla */
    const val SLOW = 500

    /** Muy lento - 800ms: Animaciones complejas */
    const val VERY_SLOW = 800
}

/**
 * Easing functions para animaciones naturales.
 */
object TicketEasing {
    /** Ease In - Acelera desde el inicio */
    val EaseIn = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)

    /** Ease Out - Desacelera hacia el final */
    val EaseOut = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)

    /** Ease In Out - Acelera y desacelera */
    val EaseInOut = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)

    /** Ease In Back - Efecto de rebote al inicio */
    val EaseInBack = CubicBezierEasing(0.6f, -0.28f, 0.735f, 0.045f)

    /** Ease Out Back - Efecto de rebote al final */
    val EaseOutBack = CubicBezierEasing(0.175f, 0.885f, 0.32f, 1.275f)

    /** Ease In Out Back - Efecto de rebote en ambos extremos */
    val EaseInOutBack = CubicBezierEasing(0.68f, -0.55f, 0.265f, 1.55f)

    /** Linear - Velocidad constante */
    val Linear = LinearEasing
}

/**
 * Valores de animación comunes.
 */
object TicketAnimationValues {
    /** Escala para efecto de presión en botones */
    const val PRESS_SCALE = 0.95f

    /** Escala para efecto de elevación en tarjetas */
    const val CARD_ELEVATION_SCALE = 1.02f

    /** Opacidad para elementos deshabilitados */
    const val DISABLED_OPACITY = 0.6f

    /** Opacidad para elementos semitransparentes */
    const val SEMI_TRANSPARENT = 0.8f

    /** Distancia de deslizamiento para transiciones */
    const val SLIDE_DISTANCE = 20f

    /** Distancia de deslizamiento pequeña */
    const val SLIDE_DISTANCE_SMALL = 10f
}

/**
 * Transición de fade estándar.
 */
fun <T> fadeTransition(): TweenSpec<T> = tween(
    durationMillis = TicketAnimations.NORMAL,
    easing = TicketEasing.EaseInOut
)

/**
 * Transición de slide estándar.
 */
fun <T> slideTransition(): TweenSpec<T> = tween(
    durationMillis = TicketAnimations.NORMAL,
    easing = TicketEasing.EaseInOut
)

/**
 * Transición de escala estándar.
 */
fun <T> scaleTransition(): TweenSpec<T> = tween(
    durationMillis = TicketAnimations.NORMAL,
    easing = TicketEasing.EaseOutBack
)
