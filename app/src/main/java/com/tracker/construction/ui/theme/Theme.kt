package com.tracker.construction.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.tracker.construction.util.ThemeMode

private val LightColors = lightColorScheme(
    primary = SafetyOrange,
    onPrimary = Color.White,
    secondary = ConcreteGray,
    tertiary = BlueprintBlue
)

private val DarkColors = darkColorScheme(
    primary = SafetyOrangeLight,
    secondary = ConcreteGrayLight,
    tertiary = BlueprintBlue,
    background = DarkBackground,
    surface = DarkSurface
)

@Composable
fun ConstructionTrackerTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val useDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val colors = if (useDark) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = ConstructionTypography,
        content = content
    )
}
