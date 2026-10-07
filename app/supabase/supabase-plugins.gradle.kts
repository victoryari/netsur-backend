plugins {
    id("com.github.commercebyte.supabase") version "1.5.0"
}

supabase {
    projectId = "tu-proyecto-supabase"
    headers = mapOf(
        "X-Client-Info" to "supabase/kotlin-client"
    )
    
    // Generate Kotlin types
    generateKotlinTypes = true
    kotlinTypesOutputDir = "app/common/src/main/kotlin/com/ticket/common/supabase"
    
    // Real-time configuration
    realtime = mapOf(
        "projects" to listOf("tickets", "comments", "users")
    )
}