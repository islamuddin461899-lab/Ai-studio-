package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.model.AccentThemeOption
import com.example.data.model.DarkModeOption

@Composable
fun PromptStudioTheme(
    darkModeOption: DarkModeOption = DarkModeOption.SYSTEM,
    accentTheme: AccentThemeOption = AccentThemeOption.VIOLET,
    content: @Composable () -> Unit
) {
    val isDark = when (darkModeOption) {
        DarkModeOption.SYSTEM -> isSystemInDarkTheme()
        DarkModeOption.LIGHT -> false
        DarkModeOption.DARK -> true
    }

    val primaryColor = Color(accentTheme.hexPrimary)
    val secondaryColor = Color(accentTheme.hexSecondary)

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = primaryColor.copy(alpha = 0.2f),
            onPrimaryContainer = Color.White,
            secondary = secondaryColor,
            onSecondary = Color.White,
            secondaryContainer = secondaryColor.copy(alpha = 0.2f),
            onSecondaryContainer = Color.White,
            background = DarkBackground,
            onBackground = DarkTextPrimary,
            surface = DarkSurface,
            onSurface = DarkTextPrimary,
            surfaceVariant = DarkSurfaceElevated,
            onSurfaceVariant = DarkTextSecondary,
            outline = DarkBorder,
            outlineVariant = DarkSurfaceHighlight
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = primaryColor.copy(alpha = 0.12f),
            onPrimaryContainer = primaryColor,
            secondary = secondaryColor,
            onSecondary = Color.White,
            secondaryContainer = secondaryColor.copy(alpha = 0.12f),
            onSecondaryContainer = secondaryColor,
            background = LightBackground,
            onBackground = LightTextPrimary,
            surface = LightSurface,
            onSurface = LightTextPrimary,
            surfaceVariant = LightSurfaceElevated,
            onSurfaceVariant = LightTextSecondary,
            outline = LightBorder,
            outlineVariant = LightSurfaceHighlight
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
