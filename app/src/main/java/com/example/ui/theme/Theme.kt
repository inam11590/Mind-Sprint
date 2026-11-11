package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalAppThemePreset = staticCompositionLocalOf { AppThemePreset.COSMIC_INDIGO }

@Composable
fun MindSprintTheme(
    themePreset: AppThemePreset = AppThemePreset.COSMIC_INDIGO,
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = themePreset.buttonContainerColor,
        onPrimary = themePreset.buttonContentColor,
        primaryContainer = themePreset.primaryColor,
        onPrimaryContainer = themePreset.buttonContentColor,
        secondary = themePreset.secondaryButtonColor,
        onSecondary = Color.White,
        tertiary = themePreset.accentColor,
        onTertiary = Color(0xFF0F172A),
        background = themePreset.gradientColors.first(),
        onBackground = Color.White,
        surface = themePreset.cardBackground,
        onSurface = Color.White,
        surfaceVariant = themePreset.cardBackground.copy(alpha = 0.85f),
        onSurfaceVariant = Color(0xFFE2E8F0),
        outline = themePreset.buttonBorderColor
    )

    CompositionLocalProvider(LocalAppThemePreset provides themePreset) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
