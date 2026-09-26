package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LighthouseLightColorScheme = lightColorScheme(
    primary = PrimaryActionBlue,
    onPrimary = PureWhiteCard,
    primaryContainer = MistBlue,
    onPrimaryContainer = DeepSlateText,
    secondary = VerifiedGreen,
    onSecondary = PureWhiteCard,
    secondaryContainer = SoftSage,
    onSecondaryContainer = DeepSlateText,
    tertiary = SlateMuted,
    onTertiary = PureWhiteCard,
    tertiaryContainer = SoftLavender,
    onTertiaryContainer = DeepSlateText,
    background = WarmCloudBackground,
    onBackground = DeepSlateText,
    surface = PureWhiteCard,
    onSurface = DeepSlateText,
    surfaceVariant = MistBlue,
    onSurfaceVariant = SlateMuted,
    error = EmergencyRose,
    onError = PureWhiteCard,
    errorContainer = EmergencyRoseContainer,
    onErrorContainer = EmergencyRose,
    outline = BorderCanvas,
    outlineVariant = BorderMist
)

private val LighthouseDarkColorScheme = darkColorScheme(
    primary = PrimaryActionBlue,
    onPrimary = PureWhiteCard,
    primaryContainer = DeepSlateDark,
    onPrimaryContainer = PureWhiteCard,
    secondary = VerifiedGreen,
    onSecondary = PureWhiteCard,
    secondaryContainer = DeepSlateDark,
    onSecondaryContainer = SoftSage,
    background = DeepSlateDark,
    onBackground = WarmCloudBackground,
    surface = DeepSlateText,
    onSurface = WarmCloudBackground,
    surfaceVariant = DeepSlateDark,
    onSurfaceVariant = SlateLight,
    error = EmergencyRose,
    onError = PureWhiteCard,
    outline = BorderCanvas
)

@Composable
fun LighthouseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) LighthouseDarkColorScheme else LighthouseLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
