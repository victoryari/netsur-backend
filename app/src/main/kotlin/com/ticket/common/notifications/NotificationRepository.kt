package com.ticket.common.notifications

import android.content.Context
import android.util.Log
import com.ticket.common.AppNotification
import com.ticket.common.database.TiDBService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Mantiene los avisos del usuario al dia.
 *
 * El backend es local, asi que en vez de push se consulta cada pocos segundos
 * mientras la app esta abierta. Cada aviso nuevo que llega dispara ademas la
 * notificacion del sistema Android.
 */
object NotificationRepository {

    private const val POLL_MS = 10_000L

    private val _items = MutableStateFlow<List<AppNotification>>(emptyList())
    val items: StateFlow<List<AppNotification>> = _items.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    /** Ids ya mostrados como notificacion del sistema, para no repetir. */
    private var yaNotificados: Set<String> = emptySet()

    /** Cuando es false la pantalla de avisos va marcando lo leido al abrirla. */
    @Volatile
    var mostrarSistema: Boolean = true

    fun reset() {
        _items.value = emptyList()
        _unreadCount.value = 0
        yaNotificados = emptySet()
    }

    /** Descarga una vez. Util para refrescar al entrar en una pantalla. */
    suspend fun refrescar(context: Context, avisarSistema: Boolean = mostrarSistema) {
        val resultado = TiDBService.getNotifications() ?: return
        aplicar(context, resultado.first, resultado.second, avisarSistema)
    }

    private fun aplicar(
        context: Context,
        nuevos: List<AppNotification>,
        sinLeer: Int,
        avisarSistema: Boolean
    ) {
        val anteriores = _items.value
        val nuevosIds = nuevos.map { it.id }.toSet()
        val sonNuevos = nuevosIds - yaNotificados

        _items.value = nuevos
        _unreadCount.value = sinLeer

        if (avisarSistema) {
            // Solo los que no habiamos visto, para no reavisar en cada consulta.
            nuevos.filter { it.id in sonNuevos && !it.isRead }.forEach {
                Notifier.show(context, it)
            }
        }

        yaNotificados = if (yaNotificados.isEmpty()) {
            nuevosIds
        } else {
            (yaNotificados intersect nuevosIds) + nuevosIds
        }

        if (anteriores.isEmpty() && nuevos.isNotEmpty()) {
            Log.i("Notificaciones", "${nuevos.size} aviso(s), $sinLeer sin leer")
        }
    }

    /**
     * Arranca el sondeo periodico. Se cancela solo cuando el scope se cancela
     * (por ejemplo, al cerrar sesion).
     */
    fun iniciarSondeo(context: Context, scope: CoroutineScope) {
        val appContext = context.applicationContext
        scope.launch {
            while (isActive) {
                refrescar(appContext)
                delay(POLL_MS)
            }
        }
    }

    suspend fun marcarLeida(context: Context, id: String) {
        // Actualizacion optimista: el badge baja al instante y se confirma luego.
        _items.value = _items.value.map { if (it.id == id) it.copy(isRead = true) else it }
        _unreadCount.value = (_unreadCount.value - 1).coerceAtLeast(0)

        if (TiDBService.markNotificationRead(id)) {
            refrescar(context, avisarSistema = false)
        }
    }

    suspend fun marcarTodasLeidas(context: Context) {
        _items.value = _items.value.map { it.copy(isRead = true) }
        _unreadCount.value = 0

        if (TiDBService.markAllNotificationsRead()) {
            refrescar(context, avisarSistema = false)
        }
    }
}
