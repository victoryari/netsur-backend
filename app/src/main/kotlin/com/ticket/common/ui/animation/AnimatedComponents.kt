package com.ticket.common.ui.animation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.ticket.common.ui.theme.TicketSpacing

/**
 * Contenedor animado con fade in/out.
 *
 * @param visible Si el contenido es visible
 * @param modifier Modificador de Compose
 * @param content Contenido animado
 */
@Composable
fun AnimatedFadeVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(TicketAnimations.NORMAL)) +
                expandVertically(animationSpec = tween(TicketAnimations.NORMAL)),
        exit = fadeOut(animationSpec = tween(TicketAnimations.FAST)) +
                shrinkVertically(animationSpec = tween(TicketAnimations.FAST)),
        modifier = modifier,
        content = content
    )
}

/**
 * Contenedor animado con slide in/out.
 *
 * @param visible Si el contenido es visible
 * @param modifier Modificador de Compose
 * @param content Contenido animado
 */
@Composable
fun AnimatedSlideVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = tween(TicketAnimations.NORMAL, easing = TicketEasing.EaseOut)
        ) + fadeIn(animationSpec = tween(TicketAnimations.NORMAL)),
        exit = slideOutVertically(
            targetOffsetY = { -it / 2 },
            animationSpec = tween(TicketAnimations.FAST, easing = TicketEasing.EaseIn)
        ) + fadeOut(animationSpec = tween(TicketAnimations.FAST)),
        modifier = modifier,
        content = content
    )
}

/**
 * Botón con animación de presión.
 *
 * @param text Texto del botón
 * @param onClick Acción al hacer clic
 * @param modifier Modificador de Compose
 * @param enabled Si el botón está habilitado
 */
@Composable
fun AnimatedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) TicketAnimationValues.PRESS_SCALE else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "button_scale"
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        enabled = enabled
    ) {
        Text(text)
    }
}

/**
 * Tarjeta con animación de elevación al presionar.
 *
 * @param modifier Modificador de Compose
 * @param onClick Acción al hacer clic
 * @param content Contenido de la tarjeta
 */
@Composable
fun AnimatedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) TicketAnimationValues.CARD_ELEVATION_SCALE else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_scale"
    )

    Card(
        onClick = onClick ?: {},
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        content = content
    )
}

/**
 * Indicador de carga animado.
 *
 * @param modifier Modificador de Compose
 * @param color Color del indicador
 */
@Composable
fun AnimatedLoadingIndicator(
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = TicketEasing.EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loading_alpha"
    )

    CircularProgressIndicator(
        modifier = modifier,
        color = color.copy(alpha = alpha)
    )
}

/**
 * Badge animado con pulso.
 *
 * @param count Contador del badge
 * @param modifier Modificador de Compose
 */
@Composable
fun AnimatedBadge(
    count: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "badge_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = TicketEasing.EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badge_scale"
    )

    Badge(
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        Text(
            text = if (count > 99) "99+" else count.toString()
        )
    }
}

/**
 * Barra de progreso animada.
 *
 * @param progress Progreso (0.0 a 1.0)
 * @param modifier Modificador de Compose
 * @param color Color de la barra
 */
@Composable
fun AnimatedProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(TicketAnimations.NORMAL, easing = TicketEasing.EaseInOut),
        label = "progress"
    )

    LinearProgressIndicator(
        progress = animatedProgress,
        modifier = modifier,
        color = color
    )
}

/**
 * Contenedor con animación de entrada escalonada.
 *
 * @param visible Si el contenido es visible
 * @param modifier Modificador de Compose
 * @param content Contenido animado
 */
@Composable
fun AnimatedStaggeredVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(TicketAnimations.SLOW)) +
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(TicketAnimations.SLOW, easing = TicketEasing.EaseOut)
                ),
        exit = fadeOut(animationSpec = tween(TicketAnimations.FAST)) +
                slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = tween(TicketAnimations.FAST, easing = TicketEasing.EaseIn)
                ),
        modifier = modifier,
        content = content
    )
}
