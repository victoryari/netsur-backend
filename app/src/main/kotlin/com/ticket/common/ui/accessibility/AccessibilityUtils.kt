package com.ticket.common.ui.accessibility

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.math.abs

/**
 * Utilidades de accesibilidad para verificar contraste y calcular ratios.
 * Basado en WCAG 2.1 (Web Content Accessibility Guidelines).
 */
object AccessibilityUtils {

    /**
     * Calcula el ratio de contraste entre dos colores.
     * Fórmula: (L1 + 0.05) / (L2 + 0.05) donde L1 es el color más claro.
     *
     * @return Ratio de contraste (1:1 a 21:1)
     */
    fun contrastRatio(color1: Color, color2: Color): Float {
        val luminance1 = color1.luminance()
        val luminance2 = color2.luminance()
        val lighter = maxOf(luminance1, luminance2)
        val darker = minOf(luminance1, luminance2)
        return (lighter + 0.05f) / (darker + 0.05f)
    }

    /**
     * Verifica si el contraste cumple con WCAG 2.1 AA.
     * Requiere 4.5:1 para texto normal y 3:1 para texto grande.
     *
     * @param isLargeText Si es texto grande (18pt+ o 14pt bold+)
     * @return true si cumple con el estándar
     */
    fun meetsWCAGAA(foreground: Color, background: Color, isLargeText: Boolean = false): Boolean {
        val ratio = contrastRatio(foreground, background)
        return if (isLargeText) ratio >= 3f else ratio >= 4.5f
    }

    /**
     * Verifica si el contraste cumple con WCAG 2.1 AAA.
     * Requiere 7:1 para texto normal y 4.5:1 para texto grande.
     *
     * @param isLargeText Si es texto grande (18pt+ o 14pt bold+)
     * @return true si cumple con el estándar
     */
    fun meetsWCAGAAA(foreground: Color, background: Color, isLargeText: Boolean = false): Boolean {
        val ratio = contrastRatio(foreground, background)
        return if (isLargeText) ratio >= 4.5f else ratio >= 7f
    }

    /**
     * Verifica si un color de texto es legible sobre un fondo.
     *
     * @param textColor Color del texto
     * @param backgroundColor Color del fondo
     * @param isLargeText Si es texto grande
     * @return true si es legible
     */
    fun isReadable(textColor: Color, backgroundColor: Color, isLargeText: Boolean = false): Boolean {
        return meetsWCAGAA(textColor, backgroundColor, isLargeText)
    }

    /**
     * Calcula el tamaño mínimo de toque recomendado.
     * WCAG recomienda al menos 44x44dp para elementos interactivos.
     *
     * @return Tamaño mínimo de toque en dp
     */
    fun minimumTouchTarget(): Int = 48

    /**
     * Verifica si un tamaño cumple con el mínimo de toque.
     *
     * @param widthDp Ancho en dp
     * @param heightDp Alto en dp
     * @return true si cumple con el mínimo
     */
    fun meetsMinimumTouchTarget(widthDp: Int, heightDp: Int): Boolean {
        return widthDp >= minimumTouchTarget() && heightDp >= minimumTouchTarget()
    }

    /**
     * Obtiene un color de texto legible para un fondo dado.
     * Elige entre blanco o negro según el luminance del fondo.
     *
     * @param backgroundColor Color del fondo
     * @return Color de texto legible (blanco o negro)
     */
    fun getReadableTextColor(backgroundColor: Color): Color {
        return if (backgroundColor.luminance() > 0.5f) {
            Color.Black
        } else {
            Color.White
        }
    }

    /**
     * Calcula el luminance relativo de un color.
     * Fórmula: 0.2126 * R + 0.7152 * G + 0.0722 * B
     *
     * @param color Color a evaluar
     * @return Luminance relativo (0.0 a 1.0)
     */
    fun relativeLuminance(color: Color): Float {
        return color.luminance()
    }

    /**
     * Verifica si un color es claro u oscuro.
     *
     * @param color Color a evaluar
     * @return true si es claro, false si es oscuro
     */
    fun isLightColor(color: Color): Boolean {
        return color.luminance() > 0.5f
    }

    /**
     * Ajusta la opacidad de un color para mejorar el contraste.
     *
     * @param color Color original
     * @param targetRatio Ratio de contraste objetivo
     * @param backgroundColor Color del fondo
     * @return Color ajustado
     */
    fun adjustForContrast(
        color: Color,
        targetRatio: Float,
        backgroundColor: Color
    ): Color {
        val currentRatio = contrastRatio(color, backgroundColor)
        if (currentRatio >= targetRatio) return color

        val isLight = isLightColor(backgroundColor)
        var adjustedColor = color
        var step = 0.1f

        repeat(10) {
            adjustedColor = if (isLight) {
                adjustedColor.copy(
                    red = (adjustedColor.red - step).coerceIn(0f, 1f),
                    green = (adjustedColor.green - step).coerceIn(0f, 1f),
                    blue = (adjustedColor.blue - step).coerceIn(0f, 1f)
                )
            } else {
                adjustedColor.copy(
                    red = (adjustedColor.red + step).coerceIn(0f, 1f),
                    green = (adjustedColor.green + step).coerceIn(0f, 1f),
                    blue = (adjustedColor.blue + step).coerceIn(0f, 1f)
                )
            }
            if (contrastRatio(adjustedColor, backgroundColor) >= targetRatio) {
                return adjustedColor
            }
        }

        return adjustedColor
    }
}
