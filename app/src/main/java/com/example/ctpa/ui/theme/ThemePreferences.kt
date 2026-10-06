package com.example.ctpa.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persiste la preferencia de modo oscuro.
 * `null` significa "seguir al sistema".
 */
@Singleton
class ThemePreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("shift_pulse_theme", Context.MODE_PRIVATE)

    private val _darkMode = MutableStateFlow(
        prefs.getString(KEY_DARK_MODE, null)?.toBooleanStrictOrNull()
    )
    val darkMode: StateFlow<Boolean?> = _darkMode.asStateFlow()

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putString(KEY_DARK_MODE, enabled.toString()).apply()
        _darkMode.value = enabled
    }

    private companion object {
        const val KEY_DARK_MODE = "dark_mode"
    }
}

/**
 * Estado del modo oscuro compartido por todas las pantallas
 * (todas apuntan a la misma [ThemePreferences] singleton).
 */
@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themePreferences: ThemePreferences
) : ViewModel() {

    val darkMode: StateFlow<Boolean?> = themePreferences.darkMode

    /** Preferencia explícita o, si el usuario no eligió nada, el sistema. */
    @Composable
    fun isDarkMode(): Boolean =
        darkMode.collectAsStateWithLifecycle().value ?: isSystemInDarkTheme()

    fun setDarkMode(enabled: Boolean) = themePreferences.setDarkMode(enabled)

    /** Cambia al estado opuesto al [isDark] efectivo que se le indique. */
    fun toggle(isDark: Boolean) = themePreferences.setDarkMode(!isDark)
}
