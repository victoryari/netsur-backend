package com.ticket.common.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ticket.common.ui.theme.TicketSpacing

/**
 * Divisor con espaciado consistente.
 *
 * @param modifier Modificador de Compose
 */
@Composable
fun TicketDivider(
    modifier: Modifier = Modifier
) {
    HorizontalDivider(
        modifier = modifier.padding(vertical = TicketSpacing.md)
    )
}

/**
 * Divisor con texto centrado.
 *
 * @param text Texto del divisor
 * @param modifier Modificador de Compose
 */
@Composable
fun TicketDividerWithText(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = TicketSpacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = TicketSpacing.md)
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

/**
 * Divisor vertical.
 *
 * @param modifier Modificador de Compose
 */
@Composable
fun TicketVerticalDivider(
    modifier: Modifier = Modifier
) {
    VerticalDivider(
        modifier = modifier.padding(horizontal = TicketSpacing.md)
    )
}
