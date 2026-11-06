package com.docsalert.reminder.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class AppearanceMode {
    LIGHT,
    DARK,
    SYSTEM;

    companion object {
        fun fromName(name: String?): AppearanceMode =
            entries.firstOrNull { it.name == name } ?: SYSTEM
    }
}

val LocalThemePalette = staticCompositionLocalOf<ThemePalette> {
    error("ThemePalette not provided")
}

/** Lets screens write MaterialTheme.palette.successColor */
val MaterialTheme.palette: ThemePalette
    @Composable
    @ReadOnlyComposable
    get() = LocalThemePalette.current

@Composable
fun DocsAlertTheme(
    preset: ThemePreset = ThemePreset.DEFAULT_BLUE,
    appearance: AppearanceMode = AppearanceMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDarkTheme = when (appearance) {
        AppearanceMode.LIGHT -> false
        AppearanceMode.DARK -> true
        AppearanceMode.SYSTEM -> isSystemInDarkTheme()
    }

    val palette = remember(preset, isDarkTheme) {
        val definition = ThemePresets.get(preset)
        if (isDarkTheme) definition.darkPalette else definition.lightPalette
    }

    val colorScheme = remember(palette, isDarkTheme) {
        val base = if (isDarkTheme) darkColorScheme() else lightColorScheme()
        base.copy(
            primary = palette.primaryAccent,
            onPrimary = palette.onAccentText,
            primaryContainer = palette.accentContainer,
            onPrimaryContainer = palette.onAccentContainer,
            secondary = palette.secondaryAccent,
            onSecondary = palette.onAccentText,
            secondaryContainer = palette.secondaryCardBackground,
            onSecondaryContainer = palette.bodyText,
            tertiary = palette.tertiaryAccent,
            onTertiary = palette.onTertiaryAccent,
            tertiaryContainer = palette.tertiaryAccentContainer,
            onTertiaryContainer = palette.onTertiaryAccentContainer,
            background = palette.screenBackground,
            onBackground = palette.bodyText,
            surface = palette.cardBackground,
            onSurface = palette.bodyText,
            surfaceVariant = palette.secondaryCardBackground,
            onSurfaceVariant = palette.secondaryText,
            surfaceContainer = palette.cardBackground,
            inverseSurface = palette.inverseCardBackground,
            inverseOnSurface = palette.onInverseCardText,
            inversePrimary = palette.oppositeAccent,
            outline = palette.borderColor,
            outlineVariant = palette.dividerColor,
            error = palette.errorColor,
            errorContainer = palette.errorBackground,
            onErrorContainer = palette.onErrorContainer
        )
    }

    // Keep status bar / navigation bar icons readable
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity
            if (activity != null) {
                WindowCompat.getInsetsController(activity.window, view).apply {
                    isAppearanceLightStatusBars = !isDarkTheme
                    isAppearanceLightNavigationBars = !isDarkTheme
                }
            }
        }
    }

    CompositionLocalProvider(LocalThemePalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
