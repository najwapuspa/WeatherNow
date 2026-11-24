package com.example.weathernow.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.weathernow.data.model.ThemeMode

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = White,
    primaryContainer = LightBlue,
    onPrimaryContainer = DeepNavy,
    secondary = DarkPrimary,
    onSecondary = White,
    background = WeatherBackgroundLight,
    onBackground = TextPrimaryLight,
    surface = White,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightBlue,
    onSurfaceVariant = TextSecondaryLight,
    outline = Color(0xFFD9E2EC),
    error = ErrorRed,
    errorContainer = ErrorContainerLight,
    onError = White
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DeepNavy,
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = TextPrimaryDark,
    secondary = DarkSecondary,
    onSecondary = DarkBackground,
    background = DarkBackground,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = Color(0xFF243B53),
    error = ErrorRed,
    errorContainer = ErrorContainerDark,
    onError = White
)

@Composable
fun WeatherNowTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            val insets = WindowCompat.getInsetsController(window, view)
            insets.isAppearanceLightStatusBars = !darkTheme
            insets.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
