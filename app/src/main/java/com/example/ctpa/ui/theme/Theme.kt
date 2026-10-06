package com.example.ctpa.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

// Bordes de inputs: mismo gris neutro del diseño original en claro.
private val LightOutline = Color(0xFF79747E)
private val DarkOutline = Color(0xFF938F99)

private fun lightShiftPulseColors() = lightColorScheme(
    primary = Emerald500,
    onPrimary = White,
    primaryContainer = LightPalette.tintGreen,
    onPrimaryContainer = LightPalette.strongGreen,
    secondary = Emerald600,
    onSecondary = White,
    secondaryContainer = LightPalette.tintGreen,
    onSecondaryContainer = LightPalette.strongGreen,
    background = LightPalette.background,
    onBackground = LightPalette.textPrimary,
    surface = LightPalette.surface,
    onSurface = LightPalette.textPrimary,
    surfaceVariant = LightPalette.surfaceAlt,
    onSurfaceVariant = LightPalette.textMuted,
    outline = LightOutline,
    outlineVariant = LightPalette.border,
    error = Red500,
    onError = White,
    errorContainer = LightPalette.tintRed,
    onErrorContainer = Color(0xFF7F1D1D),
    inverseSurface = LightPalette.textPrimary,
    inverseOnSurface = LightPalette.background,
    surfaceBright = Color(0xFFFFFFFF),
    surfaceDim = LightPalette.background,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFCFCFD),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFF3F4F6),
    surfaceContainerHighest = Color(0xFFE5E7EB)
)

private fun darkShiftPulseColors() = darkColorScheme(
    primary = Color(0xFF34D399),
    onPrimary = Color(0xFF022C22),
    primaryContainer = DarkPalette.tintGreen,
    onPrimaryContainer = DarkPalette.strongGreen,
    secondary = Color(0xFF6EE7B7),
    onSecondary = Color(0xFF022C22),
    secondaryContainer = DarkPalette.tintGreen,
    onSecondaryContainer = DarkPalette.strongGreen,
    background = DarkPalette.background,
    onBackground = DarkPalette.textPrimary,
    surface = DarkPalette.surface,
    onSurface = DarkPalette.textPrimary,
    surfaceVariant = DarkPalette.surfaceAlt,
    onSurfaceVariant = DarkPalette.textMuted,
    outline = DarkOutline,
    outlineVariant = DarkPalette.border,
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
    errorContainer = DarkPalette.tintRed,
    onErrorContainer = Color(0xFFFCA5A5),
    inverseSurface = DarkPalette.textPrimary,
    inverseOnSurface = DarkPalette.background,
    surfaceBright = Color(0xFF273244),
    surfaceDim = DarkPalette.background,
    surfaceContainerLowest = Color(0xFF0D1421),
    surfaceContainerLow = DarkPalette.surface,
    surfaceContainer = Color(0xFF161F31),
    surfaceContainerHigh = DarkPalette.surfaceAlt,
    surfaceContainerHighest = Color(0xFF273244)
)

/**
 * Tema de la app. Aplica la paleta ShiftPulse (clara/oscura) a los alias de
 * [Color.kt] y a los esquemas de Material3, de modo que tarjetas, diálogos,
 * menús y campos de texto cambian de color con el modo oscuro.
 */
@Composable
fun CTPATheme(
    isDark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalShiftPulsePalette provides if (isDark) DarkPalette else LightPalette
    ) {
        MaterialTheme(
            colorScheme = if (isDark) darkShiftPulseColors() else lightShiftPulseColors(),
            content = content
        )
    }
}
