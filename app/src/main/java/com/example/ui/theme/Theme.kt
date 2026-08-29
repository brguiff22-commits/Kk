package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.repository.AppThemeStyle

private val MidnightColorScheme = darkColorScheme(
    primary = MidnightPrimary,
    onPrimary = Color.White,
    primaryContainer = MidnightPrimaryContainer,
    onPrimaryContainer = Color(0xFFDDD6FE),
    secondary = MidnightSecondary,
    onSecondary = Color.Black,
    tertiary = MidnightTertiary,
    background = MidnightBackground,
    onBackground = TextWhite,
    surface = MidnightSurface,
    onSurface = TextWhite,
    surfaceVariant = MidnightSurfaceVariant,
    onSurfaceVariant = TextMuted,
    outline = BorderDark,
    error = ErrorColor
)

private val OledColorScheme = darkColorScheme(
    primary = OledPrimary,
    onPrimary = Color.White,
    primaryContainer = OledPrimaryContainer,
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = OledSecondary,
    onSecondary = Color.Black,
    background = OledBackground,
    onBackground = TextWhite,
    surface = OledSurface,
    onSurface = TextWhite,
    surfaceVariant = OledSurfaceVariant,
    onSurfaceVariant = TextMuted,
    outline = Color(0xFF2E2E2E),
    error = ErrorColor
)

private val CyberColorScheme = darkColorScheme(
    primary = CyberPrimary,
    onPrimary = Color.Black,
    primaryContainer = CyberPrimaryContainer,
    onPrimaryContainer = Color(0xFFE0F7FA),
    secondary = CyberSecondary,
    onSecondary = Color.White,
    background = CyberBackground,
    onBackground = TextWhite,
    surface = CyberSurface,
    onSurface = TextWhite,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = TextMuted,
    outline = Color(0xFF1E3A5F),
    error = ErrorColor
)

private val EmeraldColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.Black,
    primaryContainer = EmeraldPrimaryContainer,
    onPrimaryContainer = Color(0xFFD1FAE5),
    secondary = EmeraldSecondary,
    onSecondary = Color.Black,
    background = EmeraldBackground,
    onBackground = TextWhite,
    surface = EmeraldSurface,
    onSurface = TextWhite,
    surfaceVariant = EmeraldSurfaceVariant,
    onSurfaceVariant = TextMuted,
    outline = Color(0xFF134E39),
    error = ErrorColor
)

@Composable
fun MinhaIATheme(
    themeStyle: AppThemeStyle = AppThemeStyle.MIDNIGHT,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeStyle) {
        AppThemeStyle.MIDNIGHT -> MidnightColorScheme
        AppThemeStyle.OLED_BLACK -> OledColorScheme
        AppThemeStyle.CYBER_NEON -> CyberColorScheme
        AppThemeStyle.EMERALD_DARK -> EmeraldColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
