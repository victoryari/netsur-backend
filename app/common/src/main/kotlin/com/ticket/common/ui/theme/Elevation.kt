package com.ticket.common.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Sistema de elevación para tarjetas y superficies.
 * Define la jerarquía visual mediante sombras.
 */
object TicketElevation {
    /** Nivel 0 - 0dp: Superficie plana, sin sombra */
    val level0 = 0.dp

    /** Nivel 1 - 1dp: Superficie ligeramente elevada (inputs, chips) */
    val level1 = 1.dp

    /** Nivel 2 - 2dp: Tarjetas estándar */
    val level2 = 2.dp

    /** Nivel 3 - 4dp: Tarjetas elevadas, FABs */
    val level3 = 4.dp

    /** Nivel 4 - 8dp: Diálogos, bottom sheets */
    val level4 = 8.dp

    /** Nivel 5 - 16dp: Modales, drawers */
    val level5 = 16.dp
}
