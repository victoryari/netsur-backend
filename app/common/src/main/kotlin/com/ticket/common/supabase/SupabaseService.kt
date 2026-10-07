package com.ticket.common.supabase

import supabase.kotlin.client.SupabaseClient
import supabase.kotlin.models.SupabaseUser
import kotlinx.coroutines.Task
import supabase.kotlin.client.supabaseClient
import com.ticket.common.User
import com.ticket.common.UserRole

object SupabaseService {

    @Volatile
    private var supabaseClient: SupabaseClient? = null

    val client: SupabaseClient
        get() = supabaseClient!!
        private set

    init {
        supabaseClient = SupabaseClient(
            url = "https://ksucmqvnzjqpkqardcgh.supabase.co",
            anonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImtzdWNtcXZuempxcGtxYXJkY2doIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTA2MDUxNTksImV4cCI6MjEwNjE4MTE1OX0.gXyoCw6ogj7P9g5VFgyN7_JXMcf7ult4VHYpf2f6yCc",
            debug = true
        )
    }

    // ==================== NOTIFICATIONS ====================

    fun subscribeToNewTickets(callback: (TicketDB) -> Unit) {
        client.realtime
            .channel("new_tickets")
            .on("postgres_change", { event ->
                if (event.table == "tickets" && event.type == "INSERT") {
                    val ticket = parseTicketFromJson(event.record)
                    callback(ticket)
                }
            })
            .subscribe()
    }

    fun subscribeToAssignedTickets(userId: String, callback: (TicketDB) -> Unit) {
        client.realtime
            .channel("assigned_tickets_$userId")
            .on("postgres_change", { event ->
                if (event.table == "tickets" && event.type == "UPDATE") {
                    val ticket = parseTicketFromJson(event.record)
                    if (ticket.assignedTo == userId) {
                        callback(ticket)
                    }
                }
            })
            .subscribe()
    }

    fun unsubscribeFromAll() {
        client.realtime.removeAllChannels()
    }

    private fun parseTicketFromJson(record: Map<String, Any?>): TicketDB {
        return TicketDB(
            id = record["id"] as? String ?: "",
            title = record["title"] as? String ?: "",
            description = record["description"] as? String,
            status = record["status"] as? String ?: "open",
            priority = record["priority"] as? String ?: "medium",
            category = record["category"] as? String,
            createdBy = record["created_by"] as? String ?: "",
            createdAt = (record["created_at"] as? Number)?.toFloat() ?: 0f,
            updatedAt = (record["updated_at"] as? Number)?.toFloat() ?: 0f,
            assignedTo = record["assigned_to"] as? String
        )
    }

    // ==================== CURRENT USER ====================

    fun getCurrentUserWithRole(): User? {
        val supaUser = client.auth.currentUser ?: return null
        return User(
            id = supaUser.id,
            email = supaUser.email ?: "",
            displayName = supaUser.user_metadata?.get("display_name") as? String,
            photoUrl = supaUser.user_metadata?.get("avatar_url") as? String,
            role = UserRole.USER, // Default role, can be updated from DB
            createdAt = (supaUser.createdAt?.time ?: 0L) / 1000
        )
    }

    // ==================== AUTH METHODS ====================

    fun signIn(email: String, password: String): Task<SupabaseUser> {
        return client.auth.signInWithPassword(email, password)
    }

    fun signUp(email: String, password: String, options: Map<String, Any>? = null): Task<SupabaseUser> {
        return client.auth.signUp(email, password, options)
    }

    fun signInWithGoogle(): Task<SupabaseUser> {
        return client.auth.signInWithOAuth(
            provider = "google",
            redirectTo = "com.ticket://callback"
        )
    }

    fun signInWithGitHub(): Task<SupabaseUser> {
        return client.auth.signInWithOAuth(
            provider = "github",
            redirectTo = "com.ticket://callback"
        )
    }

    fun signInWithMicrosoft(): Task<SupabaseUser> {
        return client.auth.signInWithOAuth(
            provider = "azure",
            redirectTo = "com.ticket://callback"
        )
    }

    fun signInWithApple(): Task<SupabaseUser> {
        return client.auth.signInWithOAuth(
            provider = "apple",
            redirectTo = "com.ticket://callback"
        )
    }

    fun sendMagicLink(email: String): Task<Void> {
        return client.auth.signInWithOtp(
            email = email,
            options = mapOf("email_redirect_to" to "com.ticket://callback")
        )
    }

    fun signInWithPhone(phone: String): Task<Void> {
        return client.auth.signInWithOtp(phone = phone)
    }

    fun verifyPhoneOtp(phone: String, token: String): Task<SupabaseUser> {
        return client.auth.verifyOtp(
            phone = phone,
            token = token,
            type = "sms"
        )
    }

    fun getCurrentUser(): SupabaseUser? {
        return client.auth.currentUser
    }

    fun signOut() {
        client.auth.signOut()
    }

    fun resetPassword(email: String): Task<Void> {
        return client.auth.resetPasswordForEmail(
            email = email,
            redirectTo = "com.ticket://reset-password"
        )
    }

    // ==================== TICKET OPERATIONS ====================

    fun getTickets(): Task<List<TicketDB>> = client
        .from("tickets")
        .select("*")
        .order("created_at", ascending = false)
        .execute()
        .map { response ->
            response.json.map { TicketDB(
                id = it.id,
                title = it.title,
                description = it.description,
                status = it.status,
                priority = it.priority,
                category = it.category,
                createdBy = it.created_by,
                createdAt = it.created_at?.toFloat() ?: 0f,
                updatedAt = it.updated_at?.toFloat() ?: 0f,
                assignedTo = it.assigned_to
            ) }
        }

    fun createTicket(ticket: TicketDB): Task<Void> = client
        .from("tickets")
        .insert(TicketDB.serializer().serialize(ticket))
        .execute()
        .map { _ -> Unit }

    fun updateTicket(ticket: TicketDB): Task<Void> = client
        .from("tickets")
        .update(TicketDB.serializer().serialize(ticket))
        .eq("id", ticket.id)
        .execute()
        .map { _ -> Unit }

    fun deleteTicket(ticketId: String): Task<Void> = client
        .from("tickets")
        .delete()
        .eq("id", ticketId)
        .execute()
        .map { _ -> Unit }

    // ==================== COMMENT OPERATIONS ====================

    fun addComment(comment: CommentDB): Task<Void> = client
        .from("comments")
        .insert(CommentDB.serializer().serialize(comment))
        .execute()
        .map { _ -> Unit }

    fun getComments(ticketId: String): Task<List<CommentDB>> = client
        .from("comments")
        .select("*")
        .eq("ticket_id", ticketId)
        .order("created_at", ascending = false)
        .execute()
        .map { response ->
            response.json.map { CommentDB(
                id = it.id,
                ticketId = it.ticket_id,
                text = it.text,
                author = it.author,
                createdAt = it.created_at?.toFloat() ?: 0f
            ) }
        }

    // ==================== USER OPERATIONS ====================

    fun getUsers(): Task<List<UserDB>> = client
        .from("users")
        .select("*")
        .execute()
        .map { response ->
            response.json.map { UserDB(
                id = it.id,
                email = it.email,
                appMetaData = it.app_meta_data ?: emptyMap(),
                userMetaData = it.user_meta_data ?: emptyMap(),
                aud = it.aud,
                role = it.role,
                lastSignInAt = it.last_sign_in_at?.toFloat() ?: 0f,
                updatedAt = it.updated_at?.toFloat() ?: 0f,
                phone = it.phone
            ) }
        }

    fun createUser(user: UserDB): Task<Void> = client
        .from("users")
        .insert(UserDB.serializer().serialize(user))
        .execute()
        .map { _ -> Unit }

    fun updateUser(user: UserDB): Task<Void> = client
        .from("users")
        .update(UserDB.serializer().serialize(user))
        .eq("id", user.id)
        .execute()
        .map { _ -> Unit }
}
