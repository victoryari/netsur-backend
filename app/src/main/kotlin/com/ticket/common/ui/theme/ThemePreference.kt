package com.ticket.common.ui.theme

import android.content.Context
import android.content.SharedPreferences

/**
 * Preferencias de tema de la aplicación.
 */
enum class ThemePreference {
    /** Usar la configuración del sistema */
    SYSTEM,

    /** Siempre tema claro */
    LIGHT,

    /** Siempre tema oscuro */
    DARK,

    /** Alto contraste para accesibilidad */
    HIGH_CONTRAST
}

/**
 * Manejador de preferencias de tema.
 * Persiste la selección del usuario en SharedPreferences.
 */
class ThemePreferenceManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    /**
     * Obtiene la preferencia de tema actual.
     */
    fun getThemePreference(): ThemePreference {
        val value = prefs.getString(KEY_THEME, ThemePreference.SYSTEM.name)
        return try {
            ThemePreference.valueOf(value ?: ThemePreference.SYSTEM.name)
        } catch (e: IllegalArgumentException) {
            ThemePreference.SYSTEM
        }
    }

    /**
     * Establece la preferencia de tema.
     */
    fun setThemePreference(preference: ThemePreference) {
        prefs.edit().putString(KEY_THEME, preference.name).apply()
    }

    /**
     * Verifica si el tema oscuro está activo basado en la preferencia y el sistema.
     */
    fun isDarkTheme(systemIsDark: Boolean): Boolean {
        return when (getThemePreference()) {
            ThemePreference.SYSTEM -> systemIsDark
            ThemePreference.LIGHT -> false
            ThemePreference.DARK -> true
            ThemePreference.HIGH_CONTRAST -> false // Alto contraste usa tema claro
        }
    }

    /**
     * Verifica si el alto contraste está activo.
     */
    fun isHighContrast(): Boolean {
        return getThemePreference() == ThemePreference.HIGH_CONTRAST
    }

    companion object {
        private const val KEY_THEME = "theme_preference"
    }
}
