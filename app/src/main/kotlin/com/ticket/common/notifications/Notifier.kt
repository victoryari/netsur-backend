package com.ticket.common.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ticket.MainActivity
import com.ticket.R
import com.ticket.common.AppNotification

/**
 * Publica los avisos como notificaciones del sistema Android.
 *
 * El canal se crea una sola vez; despues solo se notifica. Si el usuario no
 * concedio el permiso POST_NOTIFICATIONS, [show] no hace nada (y la app no
 * falla): los avisos siguen visibles dentro del centro de notificaciones.
 */
object Notifier {

    private const val CHANNEL_ID = "ticket_app_avisos"
    const val EXTRA_TICKET_ID = "ticketId"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Avisos de tickets",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Avisos de tickets nuevos y de tickets que te han sido asignados"
            enableLights(true)
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    fun hasPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun show(context: Context, item: AppNotification) {
        if (!hasPermission(context)) {
            Log.i("Notificador", "sin permiso POST_NOTIFICATIONS: aviso omitido")
            return
        }

        // Al tocar el aviso se abre la app; si el aviso trae ticket, en su detalle.
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TICKET_ID, item.ticketId)
        }
        val pending = PendingIntent.getActivity(
            context,
            item.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificacion = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setContentTitle(item.title)
            .setContentText(item.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(item.body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(item.id.hashCode(), notificacion)
            Log.i("Notificador", "aviso publicado: ${item.title}")
        }.onFailure { e ->
            Log.e("Notificador", "no se pudo publicar el aviso -> ${e.message}")
        }
    }
}
