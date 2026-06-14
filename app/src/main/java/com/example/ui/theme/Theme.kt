package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.viewmodel.AuraTheme

private val ObsidianColorScheme = darkColorScheme(
    primary = GoldPremium,
    secondary = GoldPremiumAccent,
    tertiary = Color(0xFFE8DEF8),
    background = ObsidianDark,
    surface = DarkGreySurface,
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5),
    onPrimary = Color(0xFF381E72),
    onSecondary = Color(0xFF381E72),
    surfaceVariant = LightGreySurface,
    onSurfaceVariant = Color(0xFFCAC4D0)
)

private val AmethystColorScheme = darkColorScheme(
    primary = AmethystSecondary,
    secondary = AmethystPrimary,
    tertiary = Color(0xFFE6E6FA),
    background = AmethystDark,
    surface = AmethystSurface,
    onBackground = Color.White,
    onSurface = Color.White,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    surfaceVariant = Color(0xFF3B1E5B),
    onSurfaceVariant = Color.LightGray
)

private val OceanColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    secondary = EmeraldSecondary,
    tertiary = Color(0xFFE0F7F4),
    background = EmeraldDark,
    surface = EmeraldSurface,
    onBackground = Color.White,
    onSurface = Color.White,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    surfaceVariant = Color(0xFF144D42),
    onSurfaceVariant = Color.LightGray
)

private val EmberColorScheme = darkColorScheme(
    primary = EmberPrimary,
    secondary = EmberSecondary,
    tertiary = Color(0xFFFFEAEB),
    background = EmberDark,
    surface = EmberSurface,
    onBackground = Color.White,
    onSurface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    surfaceVariant = Color(0xFF421C11),
    onSurfaceVariant = Color.LightGray
)

private val LightMinimalColorScheme = lightColorScheme(
    primary = Color.Black,
    secondary = Color(0xFF4A4A4A),
    tertiary = Color(0xFF888888),
    background = Color(0xFFF9F9FB),
    surface = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onPrimary = Color.White,
    onSecondary = Color.White,
    surfaceVariant = Color(0xFFF0F0F2),
    onSurfaceVariant = Color.DarkGray
)

@Composable
fun AuraThemeLayout(
    theme: AuraTheme = AuraTheme.OBSIDIAN_LUXURY,
    content: @Composable () -> Unit
) {
    val colorScheme = when (theme) {
        AuraTheme.OBSIDIAN_LUXURY -> ObsidianColorScheme
        AuraTheme.AMETHYST_SUNSET -> AmethystColorScheme
        AuraTheme.OCEAN_EMERALD -> OceanColorScheme
        AuraTheme.AMBER_EMBER -> EmberColorScheme
        AuraTheme.LIGHT_MINIMAL -> LightMinimalColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
