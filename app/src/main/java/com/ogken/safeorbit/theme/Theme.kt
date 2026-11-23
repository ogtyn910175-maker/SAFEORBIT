package com.ogken.safeorbit.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SafeOrbitColorScheme = darkColorScheme(
    primary = SafeOrbitTeal,
    onPrimary = SafeOrbitDeepBackground,
    primaryContainer = SafeOrbitNavy,
    onPrimaryContainer = SafeOrbitMint,
    secondary = SafeOrbitMint,
    onSecondary = SafeOrbitDeepBackground,
    secondaryContainer = SafeOrbitNavyLight,
    onSecondaryContainer = SafeOrbitWhite,
    tertiary = SafeOrbitSecondaryText,
    onTertiary = SafeOrbitDeepBackground,
    background = SafeOrbitDeepBackground,
    onBackground = SafeOrbitWhite,
    surface = SafeOrbitSurface,
    onSurface = SafeOrbitWhite,
    surfaceVariant = SafeOrbitSurfaceVariant,
    onSurfaceVariant = SafeOrbitSecondaryText,
    outline = SafeOrbitBorder
)

@Composable
fun SafeOrbitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = SafeOrbitColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = SafeOrbitDeepBackground.toArgb()
                window.navigationBarColor = SafeOrbitDeepBackground.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
