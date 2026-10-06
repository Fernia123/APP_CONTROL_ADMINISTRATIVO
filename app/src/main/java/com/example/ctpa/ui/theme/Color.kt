package com.example.ctpa.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// ============================================================
// ACENTOS ESTÁTICOS
// Se usan sobre fondos de color (iconos, texto sobre acento,
// rellenos de botones) y son correctos en claro y en oscuro.
// ============================================================
val Emerald500 = Color(0xFF10B981)      // Verde principal (botones, avatares)
val Emerald600 = Color(0xFF059669)      // Verde para texto/enlaces (sobre claro y oscuro)
val Emerald700Container = Color(0xFF047857) // Contenedor fijo (botón "Trabajador", texto blanco)
val White = Color(0xFFFFFFFF)           // Texto/iconos SOBRE acentos y paneles oscuros
val Red500 = Color(0xFFEF4444)          // Peligro (siempre sobre fondo claro/tinte)

// ============================================================
// PALETA TEMÁTICA (cambia con el modo oscuro)
// ============================================================
data class ShiftPulsePalette(
    val background: Color,     // Fondo de pantalla
    val surface: Color,        // Tarjetas y superficies
    val surfaceAlt: Color,     // Chips, campos sutiles, fondos internos
    val border: Color,         // Bordes y separadores
    val borderStrong: Color,   // Bordes con más presencia
    val textPrimary: Color,    // Texto principal
    val textSecondary: Color,  // Texto secundario
    val textMuted: Color,      // Texto atenuado
    val hint: Color,           // Iconos/pistas de texto
    val tintGreen: Color,      // Fondo tintado verde (badges, chips)
    val tintAmber: Color,      // Fondo tintado ámbar (alertas suaves)
    val tintRed: Color,        // Fondo tintado rojo (errores, alertas)
    val strongGreen: Color     // Verde intenso para texto
)

val LightPalette = ShiftPulsePalette(
    background = Color(0xFFF9FAFB),
    surface = Color(0xFFFFFFFF),
    surfaceAlt = Color(0xFFF3F4F6),
    border = Color(0xFFE5E7EB),
    borderStrong = Color(0xFFD1D5DB),
    textPrimary = Color(0xFF111827),
    textSecondary = Color(0xFF374151),
    textMuted = Color(0xFF6B7280),
    hint = Color(0xFF9CA3AF),
    tintGreen = Color(0xFFECFDF5),
    tintAmber = Color(0xFFFEF3C7),
    tintRed = Color(0xFFFEE2E2),
    strongGreen = Color(0xFF047857)
)

val DarkPalette = ShiftPulsePalette(
    background = Color(0xFF0A0F1A),
    surface = Color(0xFF111827),
    surfaceAlt = Color(0xFF1F2937),
    border = Color(0xFF374151),
    borderStrong = Color(0xFF4B5563),
    textPrimary = Color(0xFFF9FAFB),
    textSecondary = Color(0xFFD1D5DB),
    textMuted = Color(0xFF9CA3AF),
    hint = Color(0xFF6B7280),
    tintGreen = Color(0xFF0B3B2E),
    tintAmber = Color(0xFF3A2E10),
    tintRed = Color(0xFF301414),
    strongGreen = Color(0xFF6EE7B7)
)

val LocalShiftPulsePalette = compositionLocalOf { LightPalette }

// ============================================================
// ALIASES TEMÁTICOS
// Mismos nombres de siempre, pero resuelven según el modo oscuro.
// Úsalos dentro de composables (son getters @Composable).
// ============================================================

/** Fondo de pantalla. */
val Gray50: Color @Composable get() = LocalShiftPulsePalette.current.background

/** Fondos sutiles: chips, teclado, campos internos. */
val Gray100: Color @Composable get() = LocalShiftPulsePalette.current.surfaceAlt

/** Bordes y separadores. */
val Gray200: Color @Composable get() = LocalShiftPulsePalette.current.border

/** Bordes con más presencia (inputs, botones secundarios). */
val Gray300: Color @Composable get() = LocalShiftPulsePalette.current.borderStrong

/** Iconos y pistas atenuadas. */
val Gray400: Color @Composable get() = LocalShiftPulsePalette.current.hint

/** Texto terciario. */
val Gray500: Color @Composable get() = LocalShiftPulsePalette.current.textMuted

/** Texto secundario. */
val Gray700: Color @Composable get() = LocalShiftPulsePalette.current.textSecondary

/** Texto principal. */
val Gray900: Color @Composable get() = LocalShiftPulsePalette.current.textPrimary

/** Fondo tintado verde (badges y chips de estado). */
val Emerald50: Color @Composable get() = LocalShiftPulsePalette.current.tintGreen

/** Verde intenso para texto sobre fondos claros/tintados. */
val Emerald700: Color @Composable get() = LocalShiftPulsePalette.current.strongGreen

/** Fondo tintado ámbar (en proceso, pausas). */
val Amber50: Color @Composable get() = LocalShiftPulsePalette.current.tintAmber

/** Fondo tintado rojo (vencidos, errores). */
val Red50: Color @Composable get() = LocalShiftPulsePalette.current.tintRed
