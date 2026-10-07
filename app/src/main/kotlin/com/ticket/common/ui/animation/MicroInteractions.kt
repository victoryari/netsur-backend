package com.ticket.common.ui.animation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * Efecto de presión para botones y elementos interactivos.
 * Escala el elemento cuando se presiona.
 */
fun Modifier.pressScale(): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) TicketAnimationValues.PRESS_SCALE else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "press_scale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    tryAwaitRelease()
                    isPressed = false
                }
            )
        }
}

/**
 * Efecto de elevación para tarjetas.
 * Eleva la tarjeta cuando se presiona.
 */
fun Modifier.cardElevation(): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) TicketAnimationValues.CARD_ELEVATION_SCALE else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_elevation"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    tryAwaitRelease()
                    isPressed = false
                }
            )
        }
}

/**
 * Efecto de brillo para elementos destacados.
 * Agrega un brillo sutil que se mueve.
 */
fun Modifier.shimmerEffect(): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = -200f,
        targetValue = 200f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = TicketEasing.EaseInOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    this.graphicsLayer {
        translationX = shimmerTranslate
    }
}

/**
 * Efecto de pulso para badges y notificaciones.
 */
fun Modifier.pulseEffect(): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = TicketEasing.EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Efecto de rebote para elementos que aparecen.
 */
fun Modifier.bounceIn(): Modifier = composed {
    var hasAppeared by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (hasAppeared) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bounce_in"
    )

    LaunchedEffect(Unit) {
        hasAppeared = true
    }

    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Efecto de desvanecimiento para elementos que desaparecen.
 */
fun Modifier.fadeOut(
    durationMillis: Int = TicketAnimations.NORMAL
): Modifier = composed {
    var isVisible by remember { mutableStateOf(true) }
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis, easing = TicketEasing.EaseInOut),
        label = "fade_out"
    )

    this.graphicsLayer {
        this.alpha = alpha
    }
}

/**
 * Efecto de deslizamiento desde abajo.
 */
fun Modifier.slideInFromBottom(
    durationMillis: Int = TicketAnimations.NORMAL
): Modifier = composed {
    var hasAppeared by remember { mutableStateOf(false) }
    val offsetY by animateFloatAsState(
        targetValue = if (hasAppeared) 0f else 100f,
        animationSpec = tween(durationMillis, easing = TicketEasing.EaseOut),
        label = "slide_in_bottom"
    )

    LaunchedEffect(Unit) {
        hasAppeared = true
    }

    this.graphicsLayer {
        translationY = offsetY
    }
}

/**
 * Efecto de deslizamiento desde arriba.
 */
fun Modifier.slideInFromTop(
    durationMillis: Int = TicketAnimations.NORMAL
): Modifier = composed {
    var hasAppeared by remember { mutableStateOf(false) }
    val offsetY by animateFloatAsState(
        targetValue = if (hasAppeared) 0f else -100f,
        animationSpec = tween(durationMillis, easing = TicketEasing.EaseOut),
        label = "slide_in_top"
    )

    LaunchedEffect(Unit) {
        hasAppeared = true
    }

    this.graphicsLayer {
        translationY = offsetY
    }
}

/**
 * Efecto de deslizamiento desde la izquierda.
 */
fun Modifier.slideInFromLeft(
    durationMillis: Int = TicketAnimations.NORMAL
): Modifier = composed {
    var hasAppeared by remember { mutableStateOf(false) }
    val offsetX by animateFloatAsState(
        targetValue = if (hasAppeared) 0f else -100f,
        animationSpec = tween(durationMillis, easing = TicketEasing.EaseOut),
        label = "slide_in_left"
    )

    LaunchedEffect(Unit) {
        hasAppeared = true
    }

    this.graphicsLayer {
        translationX = offsetX
    }
}

/**
 * Efecto de deslizamiento desde la derecha.
 */
fun Modifier.slideInFromRight(
    durationMillis: Int = TicketAnimations.NORMAL
): Modifier = composed {
    var hasAppeared by remember { mutableStateOf(false) }
    val offsetX by animateFloatAsState(
        targetValue = if (hasAppeared) 0f else 100f,
        animationSpec = tween(durationMillis, easing = TicketEasing.EaseOut),
        label = "slide_in_right"
    )

    LaunchedEffect(Unit) {
        hasAppeared = true
    }

    this.graphicsLayer {
        translationX = offsetX
    }
}
