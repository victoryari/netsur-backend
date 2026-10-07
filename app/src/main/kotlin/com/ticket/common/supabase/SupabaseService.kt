package com.ticket.common.supabase

import com.ticket.common.User
import com.ticket.common.UserRole
import com.ticket.common.Ticket
import com.ticket.common.Comment
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object SupabaseService {

    private const val SUPABASE_URL = "https://ksucmqvnzjqpkqardcgh.supabase.co"
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImtzdWNtcXZuempxcGtxYXJkY2doIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTA2MDUxNTksImV4cCI6MjEwNjE4MTE1OX0.gXyoCw6ogj7P9g5VFgyN7_JXMcf7ult4VHYpf2f6yCc"

    private var authToken: String? = null

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    // ==================== AUTH METHODS ====================

    suspend fun signIn(email: String, password: String): Boolean {
        return try {
            val response = client.post("$SUPABASE_URL/auth/v1/token?grant_type=password") {
                header("apikey", SUPABASE_ANON_KEY)
                header("Authorization", "Bearer $SUPABASE_ANON_KEY")
                contentType(ContentType.Application.Json)
                setBody(mapOf("email" to email, "password" to password))
            }
            
            if (response.status == HttpStatusCode.OK) {
                val body = response.body<Map<String, Any?>>()
                authToken = body["access_token"] as? String
                authToken != null
            } else {
                val errorBody = response.body<Map<String, Any?>>()
                println("Supabase auth error: ${response.status} - $errorBody")
                false
            }
        } catch (e: Exception) {
            println("Supabase auth exception: ${e.message}")
            false
        }
    }

    suspend fun signUp(email: String, password: String): Boolean {
        return try {
            val response = client.post("$SUPABASE_URL/auth/v1/signup") {
                header("apikey", SUPABASE_ANON_KEY)
                header("Authorization", "Bearer $SUPABASE_ANON_KEY")
                contentType(ContentType.Application.Json)
                setBody(mapOf("email" to email, "password" to password))
            }
            val body = response.body<Map<String, Any?>>()
            authToken = body["access_token"] as? String
            authToken != null
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getCurrentUserWithRole(): User? {
        val token = authToken ?: return getDemoUser()

        return try {
            // Obtener datos del usuario desde la tabla users
            val response = client.get("$SUPABASE_URL/rest/v1/users?select=*&limit=1") {
                header("apikey", SUPABASE_ANON_KEY)
                header("Authorization", "Bearer $token")
            }

            val users = response.body<List<Map<String, Any?>>>()
            if (users.isNotEmpty()) {
                val user = users[0]
                val roleStr = user["role"] as? String ?: "user"
                val role = when (roleStr) {
                    "admin" -> UserRole.ADMIN
                    "agent" -> UserRole.AGENT
                    else -> UserRole.USER
                }

                User(
                    id = user["id"] as? String ?: "",
                    email = user["email"] as? String ?: "",
                    displayName = user["display_name"] as? String,
                    photoUrl = user["photo_url"] as? String,
                    role = role,
                    createdAt = System.currentTimeMillis() / 1000
                )
            } else {
                getDemoUser()
            }
        } catch (e: Exception) {
            getDemoUser()
        }
    }

    private fun getDemoUser(): User {
        return User(
            id = "demo-user-id",
            email = "usuario@ticketapp.com",
            displayName = "Usuario Demo",
            photoUrl = null,
            role = UserRole.USER,
            createdAt = System.currentTimeMillis() / 1000
        )
    }

    fun signOut() {
        authToken = null
    }

    fun isLoggedIn(): Boolean = authToken != null

    fun setAuthToken(token: String) {
        authToken = token
    }

    // ==================== TICKET OPERATIONS ====================

    suspend fun getTickets(): List<Ticket> {
        val token = authToken ?: return emptyList()
        return try {
            val response = client.get("$SUPABASE_URL/rest/v1/tickets?order=created_at.desc") {
                header("apikey", SUPABASE_ANON_KEY)
                header("Authorization", "Bearer $token")
            }
            response.body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getTicketsByAssignee(assigneeId: String): List<Ticket> {
        val token = authToken ?: return emptyList()
        return try {
            val response = client.get("$SUPABASE_URL/rest/v1/tickets?assigned_to=eq.$assigneeId&order=created_at.desc") {
                header("apikey", SUPABASE_ANON_KEY)
                header("Authorization", "Bearer $token")
            }
            response.body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getTicketsByCreator(creatorId: String): List<Ticket> {
        val token = authToken ?: return emptyList()
        return try {
            val response = client.get("$SUPABASE_URL/rest/v1/tickets?created_by=eq.$creatorId&order=created_at.desc") {
                header("apikey", SUPABASE_ANON_KEY)
                header("Authorization", "Bearer $token")
            }
            response.body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun createTicket(ticket: Ticket): Boolean {
        val token = authToken ?: return false
        return try {
            client.post("$SUPABASE_URL/rest/v1/tickets") {
                header("apikey", SUPABASE_ANON_KEY)
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(ticket)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateTicket(ticket: Ticket): Boolean {
        val token = authToken ?: return false
        return try {
            client.patch("$SUPABASE_URL/rest/v1/tickets?id=eq.${ticket.id}") {
                header("apikey", SUPABASE_ANON_KEY)
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
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
            client.patch("$SUPABASE_URL/rest/v1/tickets?id=eq.$ticketId") {
                header("apikey", SUPABASE_ANON_KEY)
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
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
            val response = client.get("$SUPABASE_URL/rest/v1/comments?ticket_id=eq.$ticketId&order=created_at.desc") {
                header("apikey", SUPABASE_ANON_KEY)
                header("Authorization", "Bearer $token")
            }
            response.body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addComment(comment: Comment): Boolean {
        val token = authToken ?: return false
        return try {
            client.post("$SUPABASE_URL/rest/v1/comments") {
                header("apikey", SUPABASE_ANON_KEY)
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(comment)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    // ==================== USER OPERATIONS ====================

    suspend fun getUsers(): List<User> {
        val token = authToken ?: return emptyList()
        return try {
            val response = client.get("$SUPABASE_URL/rest/v1/users") {
                header("apikey", SUPABASE_ANON_KEY)
                header("Authorization", "Bearer $token")
            }
            response.body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateUserRole(userId: String, newRole: UserRole): Boolean {
        val token = authToken ?: return false
        return try {
            client.patch("$SUPABASE_URL/rest/v1/users?id=eq.$userId") {
                header("apikey", SUPABASE_ANON_KEY)
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(mapOf("role" to newRole.name.lowercase()))
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    // ==================== NOTIFICATIONS ====================

    suspend fun getNotifications(): List<Map<String, Any?>> {
        val token = authToken ?: return emptyList()
        return try {
            val response = client.get("$SUPABASE_URL/rest/v1/notifications?order=created_at.desc") {
                header("apikey", SUPABASE_ANON_KEY)
                header("Authorization", "Bearer $token")
            }
            response.body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun markNotificationAsRead(notificationId: String): Boolean {
        val token = authToken ?: return false
        return try {
            client.patch("$SUPABASE_URL/rest/v1/notifications?id=eq.$notificationId") {
                header("apikey", SUPABASE_ANON_KEY)
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(mapOf("is_read" to true))
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun subscribeToNewTickets(callback: (Ticket) -> Unit) {
        // TODO: Implementar con Supabase Realtime
    }

    fun subscribeToAssignedTickets(userId: String, callback: (Ticket) -> Unit) {
        // TODO: Implementar con Supabase Realtime
    }
}
