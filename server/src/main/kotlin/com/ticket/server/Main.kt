package com.ticket.server

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.util.UUID

// ---------- DTOs de entrada (el cliente Android envia estos nombres) ----------

@Serializable
data class LoginRequest(val email: String, val password_hash: String)

@Serializable
data class GoogleLoginRequest(val email: String, val display_name: String? = null, val google_id: String)

@Serializable
data class RegisterRequest(val id: String, val email: String, val password_hash: String, val display_name: String?)

@Serializable
data class TicketRequest(
    val id: String? = null,
    val title: String,
    val description: String? = null,
    val status: String = "open",
    val priority: String = "medium",
    val category: String? = null,
    @SerialName("createdBy") val created_by: String,
    @SerialName("assignedTo") val assigned_to: String? = null
)

@Serializable
data class TicketUpdateRequest(
    val title: String? = null,
    val description: String? = null,
    val status: String? = null,
    val priority: String? = null,
    val category: String? = null,
    @SerialName("assignedTo") val assigned_to: String? = null
)

@Serializable
data class CommentRequest(
    val id: String,
    @SerialName("ticketId") val ticket_id: String,
    @SerialName("authorId") val author_id: String,
    val content: String
)

@Serializable
data class AssignRequest(val assigned_to: String)

@Serializable
data class RoleUpdateRequest(val role: String)

// ---------- Helpers de respuesta JSON (kotlinx no serializa Map<String, Any?>) ----------

private fun jsonOf(vararg pairs: Pair<String, Any?>): JsonObject = buildJsonObject {
    for ((key, value) in pairs) put(key, value.toJsonElement())
}

private fun Map<String, Any?>.toJsonObject(): JsonObject = buildJsonObject {
    for ((key, value) in this@toJsonObject) put(key, value.toJsonElement())
}

private fun Any?.toJsonElement(): JsonElement = when (this) {
    null -> JsonNull
    is JsonElement -> this
    is Number -> JsonPrimitive(this)
    is Boolean -> JsonPrimitive(this)
    else -> JsonPrimitive(toString())
}

private fun List<Map<String, Any?>>.toJsonArray(): JsonArray = buildJsonArray {
    for (item in this@toJsonArray) add(item.toJsonObject())
}

private fun userJson(id: String, email: String, displayName: String?, role: String?, createdAt: Long? = null): JsonObject =
    buildJsonObject {
        put("id", id)
        put("email", email)
        put("displayName", displayName)
        put("photoUrl", JsonNull)
        put("role", role)
        if (createdAt != null) put("createdAt", createdAt) else put("createdAt", System.currentTimeMillis() / 1000)
    }

// ---------- Sesion y avisos ----------

/** Resuelve el usuario a partir de la cabecera Authorization. Null si no hay sesion valida. */
private fun ApplicationCall.requireUserId(dataSource: javax.sql.DataSource): String? {
    val header = request.header("Authorization") ?: return null
    if (!header.startsWith("Bearer ")) return null
    val token = header.removePrefix("Bearer ").trim()
    if (token.isEmpty()) return null

    return runCatching {
        dataSource.connection.use { conn ->
            conn.prepareStatement("SELECT user_id FROM sessions WHERE token = ?").use { stmt ->
                stmt.setString(1, token)
                stmt.executeQuery().use { rs -> if (rs.next()) rs.getString(1) else null }
            }
        }
    }.getOrNull()
}

/** Crea un aviso. Silencioso: un fallo al notificar nunca debe romper la operacion principal. */
private fun notifyUser(
    dataSource: javax.sql.DataSource,
    userId: String,
    type: String,
    title: String,
    body: String,
    ticketId: String?
) {
    runCatching {
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                "INSERT INTO notifications (id, user_id, type, title, body, ticket_id) VALUES (?, ?, ?, ?, ?, ?)"
            ).use { stmt ->
                stmt.setString(1, UUID.randomUUID().toString())
                stmt.setString(2, userId)
                stmt.setString(3, type)
                stmt.setString(4, title.take(160))
                stmt.setString(5, body.take(400))
                stmt.setString(6, ticketId)
                stmt.executeUpdate()
            }
        }
    }
}

/** Nombre visible de un usuario, o null si no existe. */
private fun displayNameOf(dataSource: javax.sql.DataSource, userId: String): String? = runCatching {
    dataSource.connection.use { conn ->
        conn.prepareStatement("SELECT display_name FROM users WHERE id = ?").use { stmt ->
            stmt.setString(1, userId)
            stmt.executeQuery().use { rs -> if (rs.next()) rs.getString(1) else null }
        }
    }
}.getOrNull()

/**
 * Ticket nuevo: avisa a tecnicos y administradores, excepto a quien lo creo
 * (a ese usuario no le sirve de nada un aviso de su propio ticket).
 */
private fun notifyNewTicket(dataSource: javax.sql.DataSource, ticketId: String, title: String, createdBy: String) {
    val creador = displayNameOf(dataSource, createdBy) ?: "Un usuario"

    val targets = runCatching {
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                "SELECT id FROM users WHERE role IN ('admin', 'agent') AND id <> ?"
            ).use { stmt ->
                stmt.setString(1, createdBy)
                stmt.executeQuery().use { rs ->
                    val out = mutableListOf<String>()
                    while (rs.next()) out.add(rs.getString(1))
                    out
                }
            }
        }
    }.getOrNull().orEmpty()

    targets.forEach { uid ->
        notifyUser(
            dataSource, uid, "new_ticket",
            "Nuevo ticket: $title",
            "$creador acaba de crear el ticket \"$title\".",
            ticketId
        )
    }
}

/**
 * Ticket asignado: avisa al tecnico que lo recibio, nombrando a quien se lo
 * asigno (no a el mismo, que es el destinatario del aviso).
 */
private fun notifyAssignment(
    dataSource: javax.sql.DataSource,
    ticketId: String,
    title: String?,
    assignedTo: String,
    assignedBy: String?
) {
    val por = assignedBy
        ?.takeIf { it.isNotBlank() }
        ?.let { displayNameOf(dataSource, it) }
        ?: "Un administrador"

    notifyUser(
        dataSource, assignedTo, "ticket_assigned",
        "Te asignaron un ticket",
        "$por te asigno el ticket ${title ?: "#$ticketId"}.",
        ticketId
    )
}

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }

        // Sin esto un JSON mal formado devolvia un 400 sin cuerpo, imposible
        // de depurar desde la app movil.
        install(StatusPages) {
            exception<io.ktor.server.plugins.BadRequestException> { call, cause ->
                call.respond(HttpStatusCode.BadRequest, jsonOf("error" to (cause.message ?: "Peticion invalida")))
            }
            exception<Throwable> { call, cause ->
                call.respond(HttpStatusCode.InternalServerError, jsonOf("error" to (cause.message ?: "Error interno")))
            }
        }

        val dataSource = createDataSource()

        routing {
            // ---------- Health ----------
            get("/health") {
                call.respond(jsonOf("status" to "ok"))
            }

            // ---------- Auth ----------
            post("/api/auth/register") {
                val params = call.receive<RegisterRequest>()
                try {
                    dataSource.connection.use { conn ->
                        conn.prepareStatement(
                            "INSERT INTO users (id, email, password_hash, display_name, role) VALUES (?, ?, ?, ?, 'user')"
                        ).use { stmt ->
                            stmt.setString(1, params.id)
                            stmt.setString(2, params.email)
                            stmt.setString(3, params.password_hash)
                            stmt.setString(4, params.display_name)
                            stmt.executeUpdate()
                        }
                    }
                    call.respond(HttpStatusCode.OK, jsonOf(
                        "id" to params.id,
                        "email" to params.email,
                        "displayName" to params.display_name,
                        "role" to "user"
                    ))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, jsonOf("error" to (e.message ?: "Error")))
                }
            }

            post("/api/auth/login") {
                val params = call.receive<LoginRequest>()
                try {
                    dataSource.connection.use { conn ->
                        conn.prepareStatement(
                            "SELECT id, email, display_name, role FROM users WHERE email = ? AND password_hash = ?"
                        ).use { stmt ->
                            stmt.setString(1, params.email)
                            stmt.setString(2, params.password_hash)
                            val rs = stmt.executeQuery()
                            if (rs.next()) {
                                val token = UUID.randomUUID().toString()
                                val userId = rs.getString("id")
                                val email = rs.getString("email")
                                val displayName = rs.getString("display_name")
                                val role = rs.getString("role")

                                conn.prepareStatement(
                                    "INSERT INTO sessions (id, user_id, token, expires_at) VALUES (?, ?, ?, DATE_ADD(NOW(), INTERVAL 24 HOUR))"
                                ).use { sessionStmt ->
                                    sessionStmt.setString(1, UUID.randomUUID().toString())
                                    sessionStmt.setString(2, userId)
                                    sessionStmt.setString(3, token)
                                    sessionStmt.executeUpdate()
                                }

                                call.respond(HttpStatusCode.OK, buildJsonObject {
                                    put("token", token)
                                    put("user", userJson(userId, email, displayName, role))
                                })
                            } else {
                                call.respond(HttpStatusCode.Unauthorized, jsonOf("error" to "Credenciales invalidas"))
                            }
                        }
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, jsonOf("error" to (e.message ?: "Error")))
                }
            }

            post("/api/auth/google") {
                val params = call.receive<GoogleLoginRequest>()
                try {
                    dataSource.connection.use { conn ->
                        var userId: String? = null
                        var displayName: String? = params.display_name
                        var role = "user"

                        conn.prepareStatement("SELECT id, display_name, role FROM users WHERE email = ?").use { stmt ->
                            stmt.setString(1, params.email)
                            stmt.executeQuery().use { rs ->
                                if (rs.next()) {
                                    userId = rs.getString("id")
                                    displayName = rs.getString("display_name") ?: params.display_name
                                    role = rs.getString("role")
                                }
                            }
                        }

                        if (userId == null) {
                            userId = UUID.randomUUID().toString()
                            conn.prepareStatement(
                                "INSERT INTO users (id, email, password_hash, display_name, role) VALUES (?, ?, ?, ?, 'user')"
                            ).use { stmt ->
                                stmt.setString(1, userId)
                                stmt.setString(2, params.email)
                                stmt.setString(3, "google_oauth_${params.google_id}")
                                stmt.setString(4, params.display_name)
                                stmt.executeUpdate()
                            }
                        }

                        val sessionToken = UUID.randomUUID().toString()
                        val sessionId = UUID.randomUUID().toString()
                        conn.prepareStatement(
                            "INSERT INTO sessions (id, user_id, token, expires_at) VALUES (?, ?, ?, DATE_ADD(NOW(), INTERVAL 24 HOUR))"
                        ).use { sessionStmt ->
                            sessionStmt.setString(1, sessionId)
                            sessionStmt.setString(2, userId)
                            sessionStmt.setString(3, sessionToken)
                            sessionStmt.executeUpdate()
                        }

                        call.respond(HttpStatusCode.OK, buildJsonObject {
                            put("token", sessionToken)
                            put("user", userJson(userId!!, params.email, displayName, role))
                        })
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, jsonOf("error" to (e.message ?: "Error")))
                }
            }

            get("/api/auth/me") {
                val authHeader = call.request.header("Authorization")
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    return@get call.respond(HttpStatusCode.Unauthorized, jsonOf("error" to "No token"))
                }
                val token = authHeader.removePrefix("Bearer ")

                try {
                    dataSource.connection.use { conn ->
                        conn.prepareStatement(
                            "SELECT u.id, u.email, u.display_name, u.role FROM sessions s JOIN users u ON s.user_id = u.id WHERE s.token = ? AND s.expires_at > NOW()"
                        ).use { stmt ->
                            stmt.setString(1, token)
                            val rs = stmt.executeQuery()
                            if (rs.next()) {
                                call.respond(HttpStatusCode.OK, userJson(
                                    id = rs.getString("id"),
                                    email = rs.getString("email"),
                                    displayName = rs.getString("display_name"),
                                    role = rs.getString("role")
                                ))
                            } else {
                                call.respond(HttpStatusCode.Unauthorized, jsonOf("error" to "Token invalido o expirado"))
                            }
                        }
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, jsonOf("error" to (e.message ?: "Error")))
                }
            }

            // ---------- Tickets ----------
            get("/api/tickets") {
                val authHeader = call.request.header("Authorization")
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    return@get call.respond(HttpStatusCode.Unauthorized, jsonOf("error" to "No token"))
                }

                val assignee = call.request.queryParameters["assigned_to"]
                val creator = call.request.queryParameters["created_by"]

                val where = StringBuilder("WHERE 1 = 1")
                if (!assignee.isNullOrBlank()) where.append(" AND assigned_to = ?")
                if (!creator.isNullOrBlank()) where.append(" AND created_by = ?")

                val sql = "SELECT id, title, description, status, priority, category, created_by, assigned_to, created_at, updated_at FROM tickets ${where} ORDER BY created_at DESC"

                try {
                    val tickets = mutableListOf<Map<String, Any?>>()
                    dataSource.connection.use { conn ->
                        conn.prepareStatement(sql).use { stmt ->
                            var i = 1
                            if (!assignee.isNullOrBlank()) stmt.setString(i++, assignee)
                            if (!creator.isNullOrBlank()) stmt.setString(i, creator)
                            val rs = stmt.executeQuery()
                            while (rs.next()) {
                                tickets.add(mapOf(
                                    "id" to rs.getString("id"),
                                    "title" to rs.getString("title"),
                                    "description" to rs.getString("description"),
                                    "status" to rs.getString("status"),
                                    "priority" to rs.getString("priority"),
                                    "category" to rs.getString("category"),
                                    "createdBy" to rs.getString("created_by"),
                                    "assignedTo" to rs.getString("assigned_to"),
                                    "createdAt" to (rs.getTimestamp("created_at")?.time ?: 0L) / 1000,
                                    "updatedAt" to (rs.getTimestamp("updated_at")?.time ?: 0L) / 1000
                                ))
                            }
                        }
                    }
                    call.respond(HttpStatusCode.OK, tickets.toJsonArray())
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, jsonOf("error" to (e.message ?: "Error")))
                }
            }

            post("/api/tickets") {
                val authHeader = call.request.header("Authorization")
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    return@post call.respond(HttpStatusCode.Unauthorized, jsonOf("error" to "No token"))
                }
                val params = call.receive<TicketRequest>()
                val ticketId = params.id?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()

                try {
                    dataSource.connection.use { conn ->
                        conn.prepareStatement(
                            "INSERT INTO tickets (id, title, description, status, priority, category, created_by, assigned_to) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
                        ).use { stmt ->
                            stmt.setString(1, ticketId)
                            stmt.setString(2, params.title)
                            stmt.setString(3, params.description)
                            stmt.setString(4, params.status)
                            stmt.setString(5, params.priority)
                            stmt.setString(6, params.category)
                            stmt.setString(7, params.created_by)
                            stmt.setString(8, params.assigned_to)
                            stmt.executeUpdate()
                        }
                    }
                    call.respond(HttpStatusCode.OK, jsonOf("id" to ticketId))

                    // Aviso a tecnicos y administradores (sin repetir al creador).
                    notifyNewTicket(dataSource, ticketId, params.title, params.created_by)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, jsonOf("error" to (e.message ?: "Error")))
                }
            }

            put("/api/tickets/{id}") {
                val authHeader = call.request.header("Authorization")
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    return@put call.respond(HttpStatusCode.Unauthorized, jsonOf("error" to "No token"))
                }
                val ticketId = call.parameters["id"]
                    ?: return@put call.respond(HttpStatusCode.BadRequest, jsonOf("error" to "Falta el ID del ticket"))
                val params = call.receive<TicketUpdateRequest>()

                val sets = mutableListOf<String>()
                if (params.title != null) sets.add("title = ?")
                if (params.description != null) sets.add("description = ?")
                if (params.status != null) sets.add("status = ?")
                if (params.priority != null) sets.add("priority = ?")
                if (params.category != null) sets.add("category = ?")
                if (params.assigned_to != null) sets.add("assigned_to = ?")

                if (sets.isEmpty()) {
                    return@put call.respond(HttpStatusCode.BadRequest, jsonOf("error" to "No hay campos para actualizar"))
                }

                try {
                    dataSource.connection.use { conn ->
                        conn.prepareStatement("UPDATE tickets SET ${sets.joinToString(", ")} WHERE id = ?").use { stmt ->
                            var i = 1
                            params.title?.let { stmt.setString(i++, it) }
                            params.description?.let { stmt.setString(i++, it) }
                            params.status?.let { stmt.setString(i++, it) }
                            params.priority?.let { stmt.setString(i++, it) }
                            params.category?.let { stmt.setString(i++, it) }
                            params.assigned_to?.let { stmt.setString(i++, it) }
                            stmt.setString(i, ticketId)
                            stmt.executeUpdate()
                        }
                    }
                    call.respond(HttpStatusCode.OK, jsonOf("id" to ticketId))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, jsonOf("error" to (e.message ?: "Error")))
                }
            }

            patch("/api/tickets/{id}/assign") {
                val userId = call.requireUserId(dataSource)
                    ?: return@patch call.respond(HttpStatusCode.Unauthorized, jsonOf("error" to "No token"))
                val ticketId = call.parameters["id"]
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, jsonOf("error" to "Falta el ID del ticket"))
                val params = call.receive<AssignRequest>()

                try {
                    val ticketTitle = dataSource.connection.use { conn ->
                        conn.prepareStatement("SELECT title FROM tickets WHERE id = ?").use { stmt ->
                            stmt.setString(1, ticketId)
                            stmt.executeQuery().use { rs -> if (rs.next()) rs.getString(1) else null }
                        }
                    }

                    dataSource.connection.use { conn ->
                        conn.prepareStatement("UPDATE tickets SET assigned_to = ? WHERE id = ?").use { stmt ->
                            stmt.setString(1, params.assigned_to)
                            stmt.setString(2, ticketId)
                            stmt.executeUpdate()
                        }
                    }
                    call.respond(HttpStatusCode.OK, jsonOf("id" to ticketId, "assignedTo" to params.assigned_to))

                    // Aviso al tecnico que recibe el ticket, nombrando a quien asigna.
                    notifyAssignment(dataSource, ticketId, ticketTitle, params.assigned_to, userId)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, jsonOf("error" to (e.message ?: "Error")))
                }
            }
            // ---------- Notificaciones ----------
            // Avisos del usuario autenticado, mas recientes primero.
            get("/api/notifications") {
                val userId = call.requireUserId(dataSource)
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, jsonOf("error" to "No token"))

                try {
                    val items = mutableListOf<JsonObject>()
                    dataSource.connection.use { conn ->
                        conn.prepareStatement(
                            """
                            SELECT n.id, n.type, n.title, n.body, n.ticket_id, n.is_read, n.created_at,
                                   COALESCE(t.title, '') AS ticket_title
                            FROM notifications n
                            LEFT JOIN tickets t ON t.id = n.ticket_id
                            WHERE n.user_id = ?
                            ORDER BY n.created_at DESC, n.id DESC
                            LIMIT 100
                            """.trimIndent()
                        ).use { stmt ->
                            stmt.setString(1, userId)
                            val rs = stmt.executeQuery()
                            while (rs.next()) {
                                items.add(buildJsonObject {
                                    put("id", rs.getString("id"))
                                    put("type", rs.getString("type"))
                                    put("title", rs.getString("title"))
                                    put("body", rs.getString("body"))
                                    put("ticketId", rs.getString("ticket_id").let { if (it == null) JsonNull else JsonPrimitive(it) })
                                    put("ticketTitle", rs.getString("ticket_title"))
                                    put("isRead", rs.getBoolean("is_read"))
                                    put("createdAt", (rs.getTimestamp("created_at")?.time ?: 0L) / 1000)
                                })
                            }
                        }
                    }

                    val unread = items.count { it["isRead"]?.jsonPrimitive?.booleanOrNull == false }
                    call.respond(
                        HttpStatusCode.OK,
                        buildJsonObject {
                            put("unreadCount", unread)
                            put("items", JsonArray(items))
                        }
                    )
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, jsonOf("error" to (e.message ?: "Error")))
                }
            }

            patch("/api/notifications/{id}/read") {
                val userId = call.requireUserId(dataSource)
                    ?: return@patch call.respond(HttpStatusCode.Unauthorized, jsonOf("error" to "No token"))
                val id = call.parameters["id"]
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, jsonOf("error" to "Falta el ID"))

                try {
                    dataSource.connection.use { conn ->
                        conn.prepareStatement("UPDATE notifications SET is_read = 1 WHERE id = ? AND user_id = ?")
                            .use { stmt ->
                                stmt.setString(1, id)
                                stmt.setString(2, userId)
                                stmt.executeUpdate()
                            }
                    }
                    call.respond(HttpStatusCode.OK, jsonOf("id" to id))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, jsonOf("error" to (e.message ?: "Error")))
                }
            }

            patch("/api/notifications/read-all") {
                val userId = call.requireUserId(dataSource)
                    ?: return@patch call.respond(HttpStatusCode.Unauthorized, jsonOf("error" to "No token"))

                try {
                    dataSource.connection.use { conn ->
                        conn.prepareStatement("UPDATE notifications SET is_read = 1 WHERE user_id = ? AND is_read = 0")
                            .use { stmt ->
                                stmt.setString(1, userId)
                                stmt.executeUpdate()
                            }
                    }
                    call.respond(HttpStatusCode.OK, jsonOf("ok" to true))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, jsonOf("error" to (e.message ?: "Error")))
                }
            }

            // ---------- Comments ----------
            get("/api/tickets/{id}/comments") {
                val authHeader = call.request.header("Authorization")
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    return@get call.respond(HttpStatusCode.Unauthorized, jsonOf("error" to "No token"))
                }
                val ticketId = call.parameters["id"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, jsonOf("error" to "Falta el ID del ticket"))

                try {
                    val comments = mutableListOf<Map<String, Any?>>()
                    dataSource.connection.use { conn ->
                        conn.prepareStatement(
                            "SELECT id, ticket_id, author_id, content, created_at FROM comments WHERE ticket_id = ? ORDER BY created_at DESC"
                        ).use { stmt ->
                            stmt.setString(1, ticketId)
                            val rs = stmt.executeQuery()
                            while (rs.next()) {
                                comments.add(mapOf(
                                    "id" to rs.getString("id"),
                                    "ticketId" to rs.getString("ticket_id"),
                                    "authorId" to rs.getString("author_id"),
                                    "content" to rs.getString("content"),
                                    "createdAt" to (rs.getTimestamp("created_at")?.time ?: 0L) / 1000
                                ))
                            }
                        }
                    }
                    call.respond(HttpStatusCode.OK, comments.toJsonArray())
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, jsonOf("error" to (e.message ?: "Error")))
                }
            }

            post("/api/tickets/{id}/comments") {
                val authHeader = call.request.header("Authorization")
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    return@post call.respond(HttpStatusCode.Unauthorized, jsonOf("error" to "No token"))
                }
                val ticketId = call.parameters["id"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, jsonOf("error" to "Falta el ID del ticket"))
                val params = call.receive<CommentRequest>()

                try {
                    dataSource.connection.use { conn ->
                        conn.prepareStatement(
                            "INSERT INTO comments (id, ticket_id, author_id, content) VALUES (?, ?, ?, ?)"
                        ).use { stmt ->
                            stmt.setString(1, params.id)
                            stmt.setString(2, ticketId)
                            stmt.setString(3, params.author_id)
                            stmt.setString(4, params.content)
                            stmt.executeUpdate()
                        }
                    }
                    call.respond(HttpStatusCode.OK, jsonOf("id" to params.id))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, jsonOf("error" to (e.message ?: "Error")))
                }
            }

            // ---------- Adjuntos ----------
            get("/api/tickets/{id}/attachments") {
                val userId = call.requireUserId(dataSource)
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, jsonOf("error" to "No token"))
                val ticketId = call.parameters["id"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, jsonOf("error" to "Falta el ID del ticket"))

                try {
                    val items = mutableListOf<JsonObject>()
                    dataSource.connection.use { conn ->
                        conn.prepareStatement(
                            "SELECT id, ticket_id, file_name, file_size, mime_type, uploaded_by, uploaded_at FROM attachments WHERE ticket_id = ? ORDER BY uploaded_at DESC"
                        ).use { stmt ->
                            stmt.setString(1, ticketId)
                            val rs = stmt.executeQuery()
                            while (rs.next()) {
                                items.add(buildJsonObject {
                                    put("id", rs.getString("id"))
                                    put("ticketId", rs.getString("ticket_id"))
                                    put("fileName", rs.getString("file_name"))
                                    put("fileSize", rs.getLong("file_size"))
                                    put("mimeType", rs.getString("mime_type"))
                                    put("uploadedBy", rs.getString("uploaded_by"))
                                    put("uploadedAt", (rs.getTimestamp("uploaded_at")?.time ?: 0L) / 1000)
                                })
                            }
                        }
                    }
                    call.respond(HttpStatusCode.OK, buildJsonArray { items.forEach { add(it) } })
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, jsonOf("error" to (e.message ?: "Error")))
                }
            }

            post("/api/tickets/{id}/attachments") {
                val userId = call.requireUserId(dataSource)
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, jsonOf("error" to "No token"))
                val ticketId = call.parameters["id"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, jsonOf("error" to "Falta el ID del ticket"))

                try {
                    val uploadDir = java.io.File("uploads").apply { mkdirs() }
                    var savedFile: java.io.File? = null
                    var fileName = "archivo"
                    var mimeType = "application/octet-stream"
                    var fileSize = 0L

                    val multipart = call.receiveMultipart()
                    multipart.forEachPart { part ->
                        when (part) {
                            is io.ktor.http.content.PartData.FileItem -> {
                                fileName = part.originalFileName ?: "archivo"
                                mimeType = part.contentType?.toString() ?: "application/octet-stream"
                                val ext = fileName.substringAfterLast('.', "")
                                val uniqueName = "${UUID.randomUUID()}${if (ext.isNotEmpty()) ".$ext" else ""}"
                                val target = java.io.File(uploadDir, uniqueName)
                                part.streamProvider().use { input ->
                                    target.outputStream().use { output -> input.copyTo(output) }
                                }
                                savedFile = target
                                fileSize = target.length()
                            }
                            else -> {}
                        }
                        part.dispose()
                    }

                    if (savedFile == null) {
                        return@post call.respond(HttpStatusCode.BadRequest, jsonOf("error" to "No se recibio ningun archivo"))
                    }

                    val attachmentId = UUID.randomUUID().toString()
                    dataSource.connection.use { conn ->
                        conn.prepareStatement(
                            "INSERT INTO attachments (id, ticket_id, file_name, file_path, file_size, mime_type, uploaded_by) VALUES (?, ?, ?, ?, ?, ?, ?)"
                        ).use { stmt ->
                            stmt.setString(1, attachmentId)
                            stmt.setString(2, ticketId)
                            stmt.setString(3, fileName)
                            stmt.setString(4, savedFile!!.name)
                            stmt.setLong(5, fileSize)
                            stmt.setString(6, mimeType)
                            stmt.setString(7, userId)
                            stmt.executeUpdate()
                        }
                    }

                    call.respond(HttpStatusCode.OK, buildJsonObject {
                        put("id", attachmentId)
                        put("fileName", fileName)
                        put("fileSize", fileSize)
                        put("mimeType", mimeType)
                    })
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, jsonOf("error" to (e.message ?: "Error")))
                }
            }

            get("/api/attachments/{id}/download") {
                val userId = call.requireUserId(dataSource)
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, jsonOf("error" to "No token"))
                val attachmentId = call.parameters["id"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, jsonOf("error" to "Falta el ID del adjunto"))

                try {
                    val filePath = dataSource.connection.use { conn ->
                        conn.prepareStatement("SELECT file_path, file_name, mime_type FROM attachments WHERE id = ?").use { stmt ->
                            stmt.setString(1, attachmentId)
                            stmt.executeQuery().use { rs ->
                                if (rs.next()) Triple(rs.getString("file_path"), rs.getString("file_name"), rs.getString("mime_type"))
                                else null
                            }
                        }
                    }

                    if (filePath == null) {
                        return@get call.respond(HttpStatusCode.NotFound, jsonOf("error" to "Adjunto no encontrado"))
                    }

                    val file = java.io.File("uploads", filePath.first)
                    if (!file.exists()) {
                        return@get call.respond(HttpStatusCode.NotFound, jsonOf("error" to "Archivo no encontrado en disco"))
                    }

                    call.respondOutputStream(
                        contentType = io.ktor.http.ContentType.parse(filePath.third),
                        status = HttpStatusCode.OK
                    ) {
                        file.inputStream().use { it.copyTo(this) }
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, jsonOf("error" to (e.message ?: "Error")))
                }
            }

            // ---------- Users ----------
            get("/api/users") {
                val authHeader = call.request.header("Authorization")
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    return@get call.respond(HttpStatusCode.Unauthorized, jsonOf("error" to "No token"))
                }

                val roleFilter = call.request.queryParameters["role"]

                try {
                    val users = mutableListOf<JsonObject>()
                    dataSource.connection.use { conn ->
                        val sql = if (roleFilter != null)
                            "SELECT id, email, display_name, role FROM users WHERE role = ?"
                        else
                            "SELECT id, email, display_name, role FROM users"

                        conn.prepareStatement(sql).use { stmt ->
                            if (roleFilter != null) stmt.setString(1, roleFilter.lowercase())
                            val rs = stmt.executeQuery()
                            while (rs.next()) {
                                users.add(userJson(
                                    id = rs.getString("id"),
                                    email = rs.getString("email"),
                                    displayName = rs.getString("display_name"),
                                    role = rs.getString("role")
                                ))
                            }
                        }
                    }
                    call.respond(HttpStatusCode.OK, buildJsonArray { users.forEach { add(it) } })
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, jsonOf("error" to (e.message ?: "Error")))
                }
            }

            patch("/api/users/{id}/role") {
                val authHeader = call.request.header("Authorization")
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    return@patch call.respond(HttpStatusCode.Unauthorized, jsonOf("error" to "No token"))
                }
                val userId = call.parameters["id"]
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, jsonOf("error" to "Falta el ID del usuario"))
                val params = call.receive<RoleUpdateRequest>()

                try {
                    dataSource.connection.use { conn ->
                        conn.prepareStatement("UPDATE users SET role = ? WHERE id = ?").use { stmt ->
                            stmt.setString(1, params.role.lowercase())
                            stmt.setString(2, userId)
                            stmt.executeUpdate()
                        }
                    }
                    call.respond(HttpStatusCode.OK, jsonOf("id" to userId, "role" to params.role.lowercase()))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, jsonOf("error" to (e.message ?: "Error")))
                }
            }
        }
    }.start(wait = true)
}

private fun createDataSource(): HikariDataSource {
    val config = HikariConfig().apply {
        jdbcUrl = System.getenv("DB_URL") 
            ?: "jdbc:mysql://gateway01.us-west-2.prod.aws.tidbcloud.com:4000/ticket_app?sslMode=VERIFY_IDENTITY&enabledTLSProtocols=TLSv1.2,TLSv1.3"
        username = System.getenv("DB_USER") ?: "HGsipoR4WGiR4io.root"
        password = System.getenv("DB_PASSWORD") ?: "Hnd9SD0J7wcav8o0"
        driverClassName = "com.mysql.cj.jdbc.Driver"
        maximumPoolSize = 10
    }
    return HikariDataSource(config)
}
