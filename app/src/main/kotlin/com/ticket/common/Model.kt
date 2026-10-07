package com.ticket.common

import kotlinx.serialization.Serializable

@Serializable
data class Ticket(
    val id: String,
    val title: String,
    val description: String? = null,
    val status: String = "open",
    val priority: String = "medium",
    val category: String? = null,
    val createdBy: String,
    val createdAt: Long = System.currentTimeMillis() / 1000,
    val updatedAt: Long = System.currentTimeMillis() / 1000,
    val assignedTo: String? = null
)

@Serializable
data class Comment(
    val id: String,
    val ticketId: String,
    val authorId: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis() / 1000
)

@Serializable
data class Attachment(
    val id: String,
    val ticketId: String,
    val fileName: String,
    val fileUrl: String,
    val fileSize: Long = 0,
    val mimeType: String = "",
    val uploadedBy: String,
    val uploadedAt: Long = System.currentTimeMillis() / 1000
)

@Serializable
data class User(
    val id: String,
    val email: String,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val role: UserRole = UserRole.USER,
    val createdAt: Long = System.currentTimeMillis() / 1000
)

/** Aviso para tecnicos y administradores: ticket nuevo o ticket asignado. */
@Serializable
data class AppNotification(
    val id: String,
    val type: String = "new_ticket",
    val title: String = "",
    val body: String = "",
    val ticketId: String? = null,
    val ticketTitle: String = "",
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis() / 1000
) {
    /** "hoy", "ayer" o la fecha corta en espanol. */
    fun relativeDate(nowSeconds: Long = System.currentTimeMillis() / 1000): String {
        val diff = nowSeconds - createdAt
        return when {
            diff < 60 -> "hace un momento"
            diff < 3600 -> "hace ${diff / 60} min"
            diff < 86_400 -> "hace ${diff / 3600} h"
            diff < 172_800 -> "ayer"
            else -> {
                val dias = diff / 86_400
                "hace $dias dias"
            }
        }
    }
}

enum class UserRole {
    ADMIN, AGENT, USER
}
