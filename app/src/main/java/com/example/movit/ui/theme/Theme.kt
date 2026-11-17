package com.example.movit.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightScheme = lightColorScheme(
    primary = Emerald700,
    onPrimary = Color.White,
    primaryContainer = LightMovitColors.accentSoft,
    onPrimaryContainer = LightMovitColors.accent,
    secondary = Emerald500,
    onSecondary = Color.White,
    tertiary = LightMovitColors.orange,
    onTertiary = Color.White,
    background = LightMovitColors.background,
    onBackground = LightMovitColors.text,
    surface = LightMovitColors.surface,
    onSurface = LightMovitColors.text,
    surfaceVariant = LightMovitColors.subtle,
    onSurfaceVariant = LightMovitColors.muted,
    outline = LightMovitColors.border,
    error = Color(0xFFDC2626),
    onError = Color.White,
)

private val DarkScheme = darkColorScheme(
    primary = Emerald700,
    onPrimary = Color.White,
    primaryContainer = DarkMovitColors.accentSoft,
    onPrimaryContainer = DarkMovitColors.accent,
    secondary = Emerald500,
    onSecondary = Color.White,
    tertiary = DarkMovitColors.orange,
    onTertiary = Color.Black,
    background = DarkMovitColors.background,
    onBackground = DarkMovitColors.text,
    surface = DarkMovitColors.surface,
    onSurface = DarkMovitColors.text,
    surfaceVariant = DarkMovitColors.subtle,
    onSurfaceVariant = DarkMovitColors.muted,
    outline = DarkMovitColors.border,
    error = Color(0xFFF87171),
    onError = Color.Black,
)

// Radius mengikuti desain: tombol 16, kartu 20-22, panel bawah 28
val MovitShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),   // tombol, input
    large = RoundedCornerShape(20.dp),    // kartu
    extraLarge = RoundedCornerShape(28.dp), // panel bawah, dialog
)

val LocalMovitColors = staticCompositionLocalOf { LightMovitColors }

/** Akses warna khusus MOVIT: MovitTheme.colors.accentSoft, MovitTheme.colors.orange, dst. */
object MovitTheme {
    val colors: MovitColors
        @Composable
        @ReadOnlyComposable
        get() = LocalMovitColors.current
}

@Composable
fun MovitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // Dynamic color sengaja tidak dipakai supaya emerald MOVIT konsisten di semua HP
    val movitColors = if (darkTheme) DarkMovitColors else LightMovitColors
    val scheme = if (darkTheme) DarkScheme else LightScheme

    CompositionLocalProvider(LocalMovitColors provides movitColors) {
        MaterialTheme(
            colorScheme = scheme,
            typography = MovitTypography,
            shapes = MovitShapes,
            content = content,
        )
    }
}