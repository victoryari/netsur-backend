package com.ticket.common.di

import com.ticket.common.SupabaseRepository
import com.ticket.common.SupabaseRepository(client = { SupabaseClient(
    SUPABASE_URL,
    SUPABASE_ANON_KEY,
    debug = true
)})
import org.koin.dsl.module
import org.koin.java.inject

val diModule = module {
    single { SupabaseRepository() }
}