package com.ticket.common.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Sistema de formas refinado de TicketApp.
 * Curvas suaves y esquinas redondeadas modernas.
 */
object TicketShapes {
    /** Extra pequeño - 6dp: Badges, chips pequeños */
    val xs = RoundedCornerShape(6.dp)

    /** Pequeño - 10dp: Chips, botones compactos */
    val sm = RoundedCornerShape(10.dp)

    /** Medio - 14dp: Botones, campos de texto, tarjetas estándar */
    val md = RoundedCornerShape(14.dp)

    /** Grande - 20dp: Tarjetas destacadas, diálogos */
    val lg = RoundedCornerShape(20.dp)

    /** Extra grande - 28dp: Modales, bottom sheets */
    val xl = RoundedCornerShape(28.dp)

    /** Completo - 50dp: Píldoras, FABs, avatares */
    val full = RoundedCornerShape(50)
}
