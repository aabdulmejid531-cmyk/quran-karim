package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.viewmodel.AppTheme

private val DarkColorScheme = darkColorScheme(
    primary = DarkEmeraldPrimary,
    secondary = DarkEmeraldSecondary,
    tertiary = DarkAmberAccent,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = DarkBackground,
    onSecondary = DarkBackground,
    onTertiary = DarkBackground,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary
)

private val BlackColorScheme = darkColorScheme(
    primary = Color(0xFF10B981),
    secondary = Color(0xFF34D399),
    tertiary = Color(0xFFD4AF37),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color(0xFF121212),
    onSecondary = Color(0xFF121212),
    onTertiary = Color(0xFF121212),
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    secondary = EmeraldSecondary,
    tertiary = AmberAccent,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = LightSurface,
    onSecondary = LightSurface,
    onTertiary = LightTextPrimary,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary
)

@Composable
fun MyApplicationTheme(
    appTheme: AppTheme = AppTheme.EMERALD_GREEN,
    content: @Composable () -> Unit
) {
    val colorScheme = when (appTheme) {
        AppTheme.EMERALD_GREEN -> DarkColorScheme
        AppTheme.DARK_BLACK -> BlackColorScheme
        AppTheme.LIGHT -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
