package com.ticket.common

import com.ticket.supabase.SupabaseConfig
import com.ticket.common.Ticket
import com.ticket.common.Comment
import com.ticket.common.User
import com.ticket.common.Ticket.TicketStatus
import com.ticket.common.Ticket.TicketPriority
import supabase.kotlin.client.SupabaseClient
import supabase.kotlin.models.{SupabaseUser, SupabaseException}
import kotlinx.serialization.json.decodeFromString
import kotlinx.serialization.json.encodeToString
import java.util.*

class SupabaseRepository(private val client: SupabaseClient) {

    fun signIn(email: String, password: String): Task<SupabaseUser> {
        return client.auth.signInWithPassword(email, password)
    }

    fun signUp(email: String, password: String, displayName: String?): Task<SupabaseUser> {
        return client.auth.signUp(email, password, dataOf("display_name", displayName))
    }

    fun signInWithGoogle(): Task<SupabaseUser> {
        return client.auth.signInWithOAuth(
            provider = "google",
            options = redirectTo = "com.ticket.callback"
        )
    }

    fun getCurrentUser(): Option<SupabaseUser> {
        return client.auth.currentUser
    }

    fun getTickets(): Task<List<Ticket>> {
        return client
            .from("tickets")
            .select("*")
            .order("created_at", ascending = false)
            .execute()
            .map { response ->
                Json.decodeFromString<List<Ticket>>(encodeToString(response.json)) ?: emptyList()
            }
    }

    fun createTicket(ticket: Ticket): Task<Void> {
        return client
            .from("tickets")
            .insert(encodeToString(ticket))
            .execute()
            .map { _ -> Unit }
    }

    fun updateTicket(ticket: Ticket): Task<Void> {
        return client
            .from("tickets")
            .update(encodeToString(ticket))
            .eq("id", ticket.id)
            .execute()
            .map { _ -> Unit }
    }

    fun deleteTicket(ticketId: String): Task<Void> {
        return client
            .from("tickets")
            .delete()
            .eq("id", ticketId)
            .execute()
            .map { _ -> Unit }
    }

    fun getUsers(): Task<List<User>> {
        return client
            .from("users")
            .select("*")
            .execute()
            .map { response ->
                Json.decodeFromString<List<User>>(encodeToString(response.json)) ?: emptyList()
            }
    }

    fun createUser(user: User): Task<Void> {
        return client
            .from("users")
            .insert(encodeToString(user))
            .execute()
            .map { _ -> Unit }
    }

    fun updateUser(user: User): Task<Void> {
        return client
            .from("users")
            .update(encodeToString(user))
            .eq("id", user.id)
            .execute()
            .map { _ -> Unit }
    }

    fun addComment(ticketId: String, comment: Comment): Task<Void> {
        return client
            .from("comments")
            .insert(encodeToString(mapOf(
                "ticket_id" to ticketId,
                "text" to comment.text,
                "author" to comment.author,
                "created_at" to comment.createdAt
            )))
            .execute()
            .map { _ -> Unit }
    }

    fun getComments(ticketId: String): Task<List<Comment>> {
        return client
            .from("comments")
            .select("*")
            .eq("ticket_id", ticketId)
            .order("created_at", ascending = false)
            .execute()
            .map { response ->
                Json.decodeFromString<List<Comment>>(encodeToString(response.json)) ?: emptyList()
            }
    }
}