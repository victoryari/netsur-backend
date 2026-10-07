package com.ticket.common.viewmodel

import com.ticket.common.SupabaseRepository
import com.ticket.common.Ticket
import com.ticket.common.Ticket.TicketStatus
import com.ticket.common.Ticket.TicketPriority
import com.ticket.common.User
import com.ticket.common.di diModule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import supabase.kotlin.client.SupabaseClient
import supabase.kotlin.models.{SupabaseUser, SupabaseException}

class dashboardViewModel(override val korinInstance: Any = diModule()) : ViewModel() {
    private val repository = SupabaseRepository()

    var ticketStats by remember { mutableStateOf(TicketStats(0, 0, 0, 0)) }

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            try {
                val tickets = await repository.getTickets()
                val total = tickets.size
                val abiertos = tickets.count { it.status == TicketStatus.OPEN }
                val resueltos = tickets.count { it.status == TicketStatus.RESOLVED }
                val urgentes = tickets.count { it.priority == TicketPriority.URGENT }
                ticketStats = TicketStats(total, abiertos, resueltos, urgentes)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

data class TicketStats(
    val total: Int,
    val abiertos: Int,
    val resueltos: Int,
    val urgentes: Int
)

class authViewModel : ViewModel() {
    private val repository = SupabaseRepository()

    fun signIn(email: String, password: String) {
        repository.signIn(email, password).catch { exception ->
            when (exception) {
                is SupabaseException -> {
                    // Handle auth error
                }
            }
        }
    }

    fun signUp(email: String, password: String, displayName: String?) {
        repository.signUp(email, password, displayName).catch { exception ->
            when (exception) {
                is SupabaseException -> {
                    // Handle signup error
                }
            }
        }
    }
}

class ticketViewModel : ViewModel() {
    private val repository = SupabaseRepository()

    fun createTicket(ticket: Ticket) {
        repository.createTicket(ticket)
    }

    fun updateTicket(ticket: Ticket) {
        repository.updateTicket(ticket)
    }

    fun deleteTicket(ticketId: String) {
        repository.deleteTicket(ticketId)
    }

    fun getTickets(): Flow<List<Ticket>> {
        return flow {
            val tickets = await repository.getTickets()
            emit(tickets)
        }
    }

    fun getComments(ticketId: String): Flow<List<Comment>> {
        return flow {
            val comments = await repository.getComments(ticketId)
            emit(comments)
        }
    }
}