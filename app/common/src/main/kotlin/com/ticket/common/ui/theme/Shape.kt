package com.ticket.common.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Sistema de formas y redondez.
 * Define la consistencia de bordes en toda la aplicación.
 */
object TicketShapes {
    /** Extra pequeño - 4dp: Chips, badges */
    val xs = RoundedCornerShape(4.dp)

    /** Pequeño - 8dp: Botones, inputs, tarjetas pequeñas */
    val sm = RoundedCornerShape(8.dp)

    /** Medio - 12dp: Tarjetas estándar */
    val md = RoundedCornerShape(12.dp)

    /** Grande - 16dp: Tarjetas grandes, bottom sheets */
    val lg = RoundedCornerShape(16.dp)

    /** Extra grande - 24dp: Diálogos, modales */
    val xl = RoundedCornerShape(24.dp)

    /** Completo - 50dp: Avatares, FABs circulares */
    val full = RoundedCornerShape(50)
}
