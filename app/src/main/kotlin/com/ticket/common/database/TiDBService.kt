package com.ticket.common.database

import com.ticket.common.AppNotification
import com.ticket.common.Attachment
import com.ticket.common.User
import com.ticket.common.UserRole
import com.ticket.common.Ticket
import com.ticket.common.Comment
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import android.util.Log
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import java.security.MessageDigest
import java.util.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object TiDBService {

    // IP del host en la red local (Laragon / Servidor local)
  //  private const val API_BASE_URL = "http://192.168.10.88:8080"
    private const val API_BASE_URL = "https://netsur-backend.onrender.com"

    private var authToken: String? = null

    /**
     * Usuario de la sesion actual. Es reactivo para que las pantallas se
     * actualicen solas en cuanto el login (o logout) termine.
     */
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    /** Se incrementa en cada login para forzar recargas de datos en la UI. */
    private val _sessionId = MutableStateFlow(0)
    val sessionId: StateFlow<Int> = _sessionId.asStateFlow()

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(HttpTimeout) {
            connectTimeoutMillis = 30_000
            requestTimeoutMillis = 60_000
            socketTimeoutMillis = 60_000
        }
    }

    private fun getBaseUrl(): String {
        return API_BASE_URL
    }

    private fun logError(op: String, e: Exception): String {
        val msg = if (e is HttpRequestTimeoutException || e is java.net.SocketTimeoutException || e is java.net.ConnectException) {
            "El servidor en la nube se está iniciando. Por favor, intenta de nuevo en unos segundos."
        } else {
            "${e::class.java.simpleName}: ${e.message}"
        }
        Log.e("TiDBService", "$op fallo -> $msg")
        return msg
    }

    // ==================== AUTH METHODS ====================

    /** Ultimo error ocurrido en una operacion, para poder mostrarlo en la UI. */
    var lastError: String? = null
        private set

    suspend fun register(email: String, password: String, displayName: String?): Boolean {
        return try {
            val userId = UUID.randomUUID().toString()
            val passwordHash = hashPassword(password)

            val response = client.post("${getBaseUrl()}/api/auth/register") {
                header("Content-Type", "application/json")
                setBody(mapOf(
                    "id" to userId,
                    "email" to email,
                    "password_hash" to passwordHash,
                    "display_name" to displayName
                ))
            }
            if (response.status != HttpStatusCode.OK) {
                lastError = "El servidor respondio ${response.status}"
            }
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            lastError = logError("register", e)
            false
        }
    }

    suspend fun login(email: String, password: String): Boolean {
        lastError = null
        return try {
            val passwordHash = hashPassword(password)
            Log.i("TiDBService", "login -> ${getBaseUrl()}/api/auth/login ($email)")

            val response = client.post("${getBaseUrl()}/api/auth/login") {
                header("Content-Type", "application/json")
                setBody(mapOf(
                    "email" to email,
                    "password_hash" to passwordHash
                ))
            }

            if (response.status == HttpStatusCode.OK) {
                val body = response.body<JsonObject>()
                authToken = body["token"]?.jsonPrimitive?.content
                if (authToken == null) {
                    lastError = "El servidor no devolvio un token"
                } else {
                    // El login tambien trae el usuario: lo publicamos de una vez
                    // para no depender de una segunda peticion a /api/auth/me.
                    _currentUser.value = body["user"]?.let { runCatching { bodyToUser(it.jsonObject) }.getOrNull() }
                    _sessionId.value = _sessionId.value + 1
                }
                Log.i("TiDBService", "login OK, token=${authToken != null}, user=${_currentUser.value?.role}")
            } else {
                val detalle = runCatching { response.body<JsonObject>()["error"]?.jsonPrimitive?.content }
                    .getOrNull()
                lastError = detalle ?: "Credenciales incorrectas (HTTP ${response.status.value})"
                Log.w("TiDBService", "login rechazado: ${response.status} $detalle")
            }
            authToken != null
        } catch (e: Exception) {
            lastError = logError("login", e)
            false
        }
    }

    suspend fun loginGoogle(email: String, displayName: String?, googleId: String): Boolean {
        lastError = null
        return try {
            Log.i("TiDBService", "loginGoogle -> ${getBaseUrl()}/api/auth/google ($email)")
            val response = client.post("${getBaseUrl()}/api/auth/google") {
                header("Content-Type", "application/json")
                setBody(mapOf(
                    "email" to email,
                    "display_name" to (displayName ?: ""),
                    "google_id" to googleId
                ))
            }

            if (response.status == HttpStatusCode.OK) {
                val body = response.body<JsonObject>()
                authToken = body["token"]?.jsonPrimitive?.content
                if (authToken == null) {
                    lastError = "El servidor no devolvió un token de sesión"
                } else {
                    _currentUser.value = body["user"]?.let { runCatching { bodyToUser(it.jsonObject) }.getOrNull() }
                    _sessionId.value = _sessionId.value + 1
                }
                Log.i("TiDBService", "loginGoogle OK, token=${authToken != null}")
            } else {
                val detalle = runCatching { response.body<JsonObject>()["error"]?.jsonPrimitive?.content }
                    .getOrNull()
                lastError = detalle ?: "Error al autenticar con Google (HTTP ${response.status.value})"
            }
            authToken != null
        } catch (e: Exception) {
            lastError = logError("loginGoogle", e)
            false
        }
    }

    fun logout() {
        authToken = null
        _currentUser.value = null
        _sessionId.value = _sessionId.value + 1
    }

    fun isLoggedIn(): Boolean = authToken != null

    fun setAuthToken(token: String) {
        authToken = token
    }

    suspend fun getCurrentUser(): User? {
        val token = authToken ?: return null

        return try {
            val response = client.get("${getBaseUrl()}/api/auth/me") {
                header("Authorization", "Bearer $token")
            }

            if (response.status == HttpStatusCode.OK) {
                bodyToUser(response.body<JsonObject>()).also { _currentUser.value = it }
            } else {
                Log.w("TiDBService", "getCurrentUser fallo: ${response.status}")
                null
            }
        } catch (e: Exception) {
            Log.w("TiDBService", "getCurrentUser fallo -> ${e.message}")
            null
        }
    }

    private fun bodyToUser(json: JsonObject): User {
        val roleStr = json["role"]?.jsonPrimitive?.content?.lowercase()
        val role = when (roleStr) {
            "admin" -> UserRole.ADMIN
            "agent" -> UserRole.AGENT
            else -> UserRole.USER
        }
        return User(
            id = json["id"]?.jsonPrimitive?.content ?: "",
            email = json["email"]?.jsonPrimitive?.content ?: "",
            displayName = json["displayName"]?.jsonPrimitive?.contentOrNull(),
            photoUrl = json["photoUrl"]?.jsonPrimitive?.contentOrNull(),
            role = role,
            createdAt = json["createdAt"]?.jsonPrimitive?.longOrNull ?: (System.currentTimeMillis() / 1000)
        )
    }

    private fun JsonElement?.contentOrNull(): String? =
        if (this == null || this is JsonNull) null else jsonPrimitive.content

    // ==================== TICKET OPERATIONS ====================

    /**
     * Deserializa la lista de tickets leyendo el JsonArray a mano.
     * `body<List<Ticket>>()` no resuelve el serializer de la lista y falla en
     * tiempo de ejecucion, dejando la pantalla vacia sin avisar.
     */
    private fun bodyToTickets(json: JsonArray): List<Ticket> = json.mapNotNull { element ->
        val o = element.jsonObject
        val id = o["id"]?.jsonPrimitive?.contentOrNull
        val titulo = o["title"]?.jsonPrimitive?.contentOrNull
        if (id == null || titulo == null) return@mapNotNull null

        Ticket(
            id = id,
            title = titulo,
            description = o["description"]?.jsonPrimitive?.contentOrNull,
            status = o["status"]?.jsonPrimitive?.contentOrNull ?: "open",
            priority = o["priority"]?.jsonPrimitive?.contentOrNull ?: "medium",
            category = o["category"]?.jsonPrimitive?.contentOrNull,
            createdBy = o["createdBy"]?.jsonPrimitive?.contentOrNull ?: "",
            createdAt = o["createdAt"]?.jsonPrimitive?.longOrNull ?: 0L,
            updatedAt = o["updatedAt"]?.jsonPrimitive?.longOrNull ?: 0L,
            assignedTo = o["assignedTo"]?.jsonPrimitive?.contentOrNull
        )
    }

    suspend fun getTickets(): List<Ticket> {
        val token = authToken ?: return emptyList()
        return try {
            val response = client.get("${getBaseUrl()}/api/tickets") {
                header("Authorization", "Bearer $token")
            }
            bodyToTickets(response.body<JsonArray>())
        } catch (e: Exception) {
            lastError = logError("getTickets", e)
            emptyList()
        }
    }

    suspend fun getTicketsByAssignee(assigneeId: String): List<Ticket> {
        val token = authToken ?: return emptyList()
        return try {
            val response = client.get("${getBaseUrl()}/api/tickets?assigned_to=$assigneeId") {
                header("Authorization", "Bearer $token")
            }
            bodyToTickets(response.body<JsonArray>())
        } catch (e: Exception) {
            lastError = logError("getTicketsByAssignee", e)
            emptyList()
        }
    }

    suspend fun getTicketsByCreator(creatorId: String): List<Ticket> {
        val token = authToken ?: return emptyList()
        return try {
            val response = client.get("${getBaseUrl()}/api/tickets?created_by=$creatorId") {
                header("Authorization", "Bearer $token")
            }
            bodyToTickets(response.body<JsonArray>())
        } catch (e: Exception) {
            lastError = logError("getTicketsByCreator", e)
            emptyList()
        }
    }

    suspend fun createTicket(ticket: Ticket): Boolean {
        lastError = null
        val token = authToken ?: run {
            lastError = "No hay sesión activa para crear el ticket"
            return false
        }
        return try {
            val bodyJson = buildJsonObject {
                put("id", ticket.id)
                put("title", ticket.title)
                if (!ticket.description.isNullOrBlank()) put("description", ticket.description)
                put("status", ticket.status)
                put("priority", ticket.priority)
                if (!ticket.category.isNullOrBlank()) put("category", ticket.category)
                put("createdBy", ticket.createdBy)
                put("createdAt", ticket.createdAt)
                put("updatedAt", ticket.updatedAt)
                if (!ticket.assignedTo.isNullOrBlank()) put("assignedTo", ticket.assignedTo)
            }

            val response = client.post("${getBaseUrl()}/api/tickets") {
                header("Authorization", "Bearer $token")
                header("Content-Type", "application/json")
                setBody(bodyJson)
            }
            if (response.status == HttpStatusCode.OK) {
                true
            } else {
                val err = runCatching { response.body<JsonObject>()["error"]?.jsonPrimitive?.content }.getOrNull()
                lastError = err ?: "Error del servidor al crear el ticket (HTTP ${response.status.value})"
                false
            }
        } catch (e: Exception) {
            lastError = logError("createTicket", e)
            false
        }
    }

    suspend fun updateTicket(ticket: Ticket): Boolean {
        val token = authToken ?: return false
        return try {
            client.put("${getBaseUrl()}/api/tickets/${ticket.id}") {
                header("Authorization", "Bearer $token")
                header("Content-Type", "application/json")
                setBody(ticket)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun assignTicket(ticketId: String, assigneeId: String): Boolean {
        val token = authToken ?: return false
        return try {
            client.patch("${getBaseUrl()}/api/tickets/$ticketId/assign") {
                header("Authorization", "Bearer $token")
                header("Content-Type", "application/json")
                setBody(mapOf("assigned_to" to assigneeId))
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    // ==================== COMMENT OPERATIONS ====================

    suspend fun getComments(ticketId: String): List<Comment> {
        val token = authToken ?: return emptyList()
        return try {
            val response = client.get("${getBaseUrl()}/api/tickets/$ticketId/comments") {
                header("Authorization", "Bearer $token")
            }
            response.body<JsonArray>().mapNotNull { element ->
                val o = element.jsonObject
                val id = o["id"]?.jsonPrimitive?.contentOrNull
                val contenido = o["content"]?.jsonPrimitive?.contentOrNull
                if (id == null || contenido == null) return@mapNotNull null

                Comment(
                    id = id,
                    ticketId = o["ticketId"]?.jsonPrimitive?.contentOrNull ?: ticketId,
                    authorId = o["authorId"]?.jsonPrimitive?.contentOrNull ?: "",
                    content = contenido,
                    createdAt = o["createdAt"]?.jsonPrimitive?.longOrNull ?: 0L
                )
            }
        } catch (e: Exception) {
            lastError = logError("getComments", e)
            emptyList()
        }
    }

    suspend fun addComment(comment: Comment): Boolean {
        val token = authToken ?: return false
        return try {
            val bodyJson = buildJsonObject {
                put("id", comment.id)
                put("ticketId", comment.ticketId)
                put("authorId", comment.authorId)
                put("content", comment.content)
                put("createdAt", comment.createdAt)
            }
            val response = client.post("${getBaseUrl()}/api/tickets/${comment.ticketId}/comments") {
                header("Authorization", "Bearer $token")
                header("Content-Type", "application/json")
                setBody(bodyJson)
            }
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            lastError = logError("addComment", e)
            false
        }
    }

    // ==================== USER OPERATIONS ====================

    suspend fun getUsers(): List<User> {
        val token = authToken ?: return emptyList()
        return try {
            val response = client.get("${getBaseUrl()}/api/users") {
                header("Authorization", "Bearer $token")
            }
            response.body<JsonArray>().map { bodyToUser(it.jsonObject) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Solo los usuarios con rol de tecnico, para el desplegable de asignacion. */
    suspend fun getTechnicians(): List<User> {
        val token = authToken ?: return emptyList()
        return try {
            val response = client.get("${getBaseUrl()}/api/users?role=agent") {
                header("Authorization", "Bearer $token")
            }
            response.body<JsonArray>().map { bodyToUser(it.jsonObject) }
        } catch (e: Exception) {
            lastError = logError("getTechnicians", e)
            emptyList()
        }
    }

    suspend fun getAttachments(ticketId: String): List<Attachment> {
        val token = authToken ?: return emptyList()
        return try {
            val response = client.get("${getBaseUrl()}/api/tickets/$ticketId/attachments") {
                header("Authorization", "Bearer $token")
            }
            response.body<JsonArray>().mapNotNull { element ->
                val o = element.jsonObject
                val id = o["id"]?.jsonPrimitive?.contentOrNull
                val ticket = o["ticketId"]?.jsonPrimitive?.contentOrNull
                val name = o["fileName"]?.jsonPrimitive?.contentOrNull
                if (id == null || ticket == null || name == null) return@mapNotNull null

                Attachment(
                    id = id,
                    ticketId = ticket,
                    fileName = name,
                    fileUrl = "${getBaseUrl()}/api/attachments/$id/download",
                    fileSize = o["fileSize"]?.jsonPrimitive?.longOrNull ?: 0L,
                    mimeType = o["mimeType"]?.jsonPrimitive?.contentOrNull ?: "",
                    uploadedBy = o["uploadedBy"]?.jsonPrimitive?.contentOrNull ?: "",
                    uploadedAt = o["uploadedAt"]?.jsonPrimitive?.longOrNull ?: 0L
                )
            }
        } catch (e: Exception) {
            lastError = logError("getAttachments", e)
            emptyList()
        }
    }

    private fun compressImageIfNeeded(bytes: ByteArray, maxDimension: Int = 1920, quality: Int = 80): ByteArray {
        return try {
            val options = android.graphics.BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            val w = options.outWidth
            val h = options.outHeight

            if (w <= 0 || h <= 0) return bytes

            var sampleSize = 1
            var currentW = w
            var currentH = h
            while (currentW > maxDimension || currentH > maxDimension) {
                sampleSize *= 2
                currentW /= 2
                currentH /= 2
            }

            val decodeOptions = android.graphics.BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions)
                ?: return bytes

            val stream = java.io.ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, stream)
            bitmap.recycle()
            stream.toByteArray()
        } catch (e: Exception) {
            bytes
        }
    }

    suspend fun uploadAttachment(ticketId: String, fileName: String, mimeType: String, bytes: ByteArray): Boolean {
        val token = authToken ?: return false
        return try {
            val payloadBytes = if (mimeType.startsWith("image/", ignoreCase = true)) {
                compressImageIfNeeded(bytes)
            } else {
                bytes
            }

            val boundary = "----TicketApp${System.currentTimeMillis()}"
            val body = buildString {
                append("--$boundary\r\n")
                append("Content-Disposition: form-data; name=\"file\"; filename=\"$fileName\"\r\n")
                append("Content-Type: $mimeType\r\n\r\n")
            }.toByteArray() + payloadBytes + "\r\n--$boundary--\r\n".toByteArray()

            val response = client.post("${getBaseUrl()}/api/tickets/$ticketId/attachments") {
                header("Authorization", "Bearer $token")
                header("Content-Type", "multipart/form-data; boundary=$boundary")
                timeout {
                    requestTimeoutMillis = 60_000
                    socketTimeoutMillis = 60_000
                }
                setBody(body)
            }
            if (response.status == HttpStatusCode.OK) {
                true
            } else {
                lastError = "Error del servidor al subir adjunto (${response.status.value})"
                false
            }
        } catch (e: Exception) {
            lastError = logError("uploadAttachment", e)
            false
        }
    }

    suspend fun updateUserRole(userId: String, newRole: UserRole): Boolean {
        val token = authToken ?: return false
        return try {
            client.patch("${getBaseUrl()}/api/users/$userId/role") {
                header("Authorization", "Bearer $token")
                header("Content-Type", "application/json")
                setBody(mapOf("role" to newRole.name.lowercase()))
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    // ==================== NOTIFICACIONES ====================

    /**
     * Descarga los avisos del usuario. Devuelve null si el servidor no pudo
     * responder, para que la UI pueda conservar lo que ya tenia.
     */
    suspend fun getNotifications(): Pair<List<AppNotification>, Int>? {
        val token = authToken ?: return null
        return try {
            val response = client.get("${getBaseUrl()}/api/notifications") {
                header("Authorization", "Bearer $token")
            }
            if (response.status != HttpStatusCode.OK) return null

            val body = response.body<JsonObject>()
            val items = body["items"]?.jsonArray?.map { it.jsonObject } ?: emptyList()
            val unread = body["unreadCount"]?.jsonPrimitive?.intOrNull ?: 0

            items.map { n ->
                AppNotification(
                    id = n["id"]?.jsonPrimitive?.content ?: "",
                    type = n["type"]?.jsonPrimitive?.content ?: "new_ticket",
                    title = n["title"]?.jsonPrimitive?.content ?: "",
                    body = n["body"]?.jsonPrimitive?.content ?: "",
                    ticketId = n["ticketId"]?.jsonPrimitive?.contentOrNull,
                    ticketTitle = n["ticketTitle"]?.jsonPrimitive?.content ?: "",
                    isRead = n["isRead"]?.jsonPrimitive?.booleanOrNull ?: false,
                    createdAt = n["createdAt"]?.jsonPrimitive?.longOrNull ?: 0L
                )
            } to unread
        } catch (e: Exception) {
            Log.w("TiDBService", "getNotifications fallo -> ${e.message}")
            null
        }
    }

    suspend fun markNotificationRead(id: String): Boolean {
        val token = authToken ?: return false
        return try {
            val response = client.patch("${getBaseUrl()}/api/notifications/$id/read") {
                header("Authorization", "Bearer $token")
            }
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            false
        }
    }

    suspend fun markAllNotificationsRead(): Boolean {
        val token = authToken ?: return false
        return try {
            val response = client.patch("${getBaseUrl()}/api/notifications/read-all") {
                header("Authorization", "Bearer $token")
            }
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            false
        }
    }

    // ==================== UTILITY ====================

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
