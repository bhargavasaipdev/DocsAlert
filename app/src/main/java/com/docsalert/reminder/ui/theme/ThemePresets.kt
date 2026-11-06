package com.docsalert.reminder.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/** Six DocsAlert themes. Each theme has distinct primary, secondary and tertiary accents. */
enum class ThemePreset(val id: String, val displayName: String) {
    DEFAULT_BLUE("default_blue", "Default Blue"),
    FOREST_GREEN("forest_green", "Forest Green"),
    MIDNIGHT_DARK("midnight_dark", "Midnight Dark"),
    SUNSET_ORANGE("sunset_orange", "Sunset Orange"),
    ROYAL_PURPLE("royal_purple", "Royal Purple"),
    OCEAN_TEAL("ocean_teal", "Ocean Teal");

    companion object {
        fun fromId(id: String?): ThemePreset = entries.firstOrNull { it.id == id } ?: DEFAULT_BLUE
    }
}

@Immutable
data class ThemePresetDefinition(
    val preset: ThemePreset,
    val lightPalette: ThemePalette,
    val darkPalette: ThemePalette
)

object ThemePresets {
    private data class ThemeColors(
        val lightPrimary: Color,
        val lightSecondary: Color,
        val lightTertiary: Color,
        val lightOpposite: Color,
        val lightScreen: Color,
        val lightCard: Color,
        val lightSecondaryCard: Color,
        val lightHeading: Color,
        val lightBody: Color,
        val lightMuted: Color,
        val lightBorder: Color,
        val lightSelected: Color,
        val lightGradientStart: Color,
        val lightGradientEnd: Color,
        val darkPrimary: Color,
        val darkSecondary: Color,
        val darkTertiary: Color,
        val darkOpposite: Color,
        val darkScreen: Color,
        val darkCard: Color,
        val darkSecondaryCard: Color,
        val darkHeading: Color,
        val darkBody: Color,
        val darkMuted: Color,
        val darkBorder: Color,
        val darkSelected: Color,
        val darkGradientStart: Color,
        val darkGradientEnd: Color
    )

    private fun makePalette(c: ThemeColors, dark: Boolean): ThemePalette {
        val primary = if (dark) c.darkPrimary else c.lightPrimary
        val secondary = if (dark) c.darkSecondary else c.lightSecondary
        val tertiary = if (dark) c.darkTertiary else c.lightTertiary
        val opposite = if (dark) c.darkOpposite else c.lightOpposite
        val screen = if (dark) c.darkScreen else c.lightScreen
        val card = if (dark) c.darkCard else c.lightCard
        val secondaryCard = if (dark) c.darkSecondaryCard else c.lightSecondaryCard
        val heading = if (dark) c.darkHeading else c.lightHeading
        val body = if (dark) c.darkBody else c.lightBody
        val muted = if (dark) c.darkMuted else c.lightMuted
        val border = if (dark) c.darkBorder else c.lightBorder
        val selected = if (dark) c.darkSelected else c.lightSelected
        val summaryStart = if (dark) c.darkGradientStart else c.lightGradientStart
        val summaryEnd = if (dark) c.darkGradientEnd else c.lightGradientEnd

        val success = if (dark) Color(0xFF4ADE80) else Color(0xFF15803D)
        val successBg = if (dark) Color(0xFF14532D) else Color(0xFFDCFCE7)
        val warning = if (dark) Color(0xFFFBBF24) else Color(0xFFB45309)
        val warningBg = if (dark) Color(0xFF451A03) else Color(0xFFFEF3C7)
        val error = if (dark) Color(0xFFFB7185) else Color(0xFFBE123C)
        val errorBg = if (dark) Color(0xFF4C0519) else Color(0xFFFFE4E6)
        val info = if (dark) Color(0xFF60A5FA) else Color(0xFF1D4ED8)
        val infoBg = if (dark) Color(0xFF1E3A8A) else Color(0xFFDBEAFE)

        val oppositeContainer = opposite.copy(alpha = if (dark) 0.22f else 0.14f)
        val tertiaryContainer = tertiary.copy(alpha = if (dark) 0.22f else 0.14f)
        val accentContainer = primary.copy(alpha = if (dark) 0.22f else 0.12f)
        val onAccent = if (dark) Color(0xFF0F172A) else Color.White

        return ThemePalette(
            screenBackground = screen,
            cardBackground = card,
            secondaryCardBackground = secondaryCard,
            topBarBackground = screen,
            bottomBarBackground = card,
            dialogBackground = card,
            inputBackground = secondaryCard,
            disabledBackground = secondaryCard,
            headingText = heading,
            subheadingText = body,
            bodyText = body,
            secondaryText = muted,
            hintText = muted,
            onAccentText = onAccent,
            disabledText = muted.copy(alpha = 0.60f),
            linkText = primary,
            borderColor = border,
            inputBorderColor = border,
            focusedBorderColor = primary,
            dividerColor = border,
            selectedBorderColor = primary,
            primaryAccent = primary,
            primaryAccentPressed = primary.copy(alpha = 0.82f),
            secondaryAccent = secondary,
            accentContainer = accentContainer,
            onAccentContainer = heading,
            selectedItemBackground = selected,
            selectedItemContent = primary,
            unselectedItemContent = muted,
            oppositeAccent = opposite,
            oppositeAccentPressed = opposite.copy(alpha = 0.82f),
            onOppositeAccent = onAccent,
            oppositeAccentContainer = oppositeContainer,
            onOppositeAccentContainer = heading,
            tertiaryAccent = tertiary,
            tertiaryAccentContainer = tertiaryContainer,
            onTertiaryAccent = onAccent,
            onTertiaryAccentContainer = heading,
            inverseScreenBackground = if (dark) Color(0xFFF8FAFC) else Color(0xFF0F172A),
            inverseCardBackground = if (dark) Color.White else Color(0xFF1E293B),
            onInverseCardText = if (dark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
            successColor = success,
            successBackground = successBg,
            onSuccessContainer = if (dark) Color(0xFF86EFAC) else Color(0xFF166534),
            warningColor = warning,
            warningBackground = warningBg,
            onWarningContainer = if (dark) Color(0xFFFDE68A) else Color(0xFF92400E),
            errorColor = error,
            errorBackground = errorBg,
            onErrorContainer = if (dark) Color(0xFFFECDD3) else Color(0xFF9F1239),
            infoColor = info,
            infoBackground = infoBg,
            onInfoContainer = if (dark) Color(0xFFBFDBFE) else Color(0xFF1E40AF),
            summaryCardStart = summaryStart,
            summaryCardEnd = summaryEnd,
            onSummaryCard = Color.White,
            categoryIconBackground = accentContainer,
            appLogoBackground = primary,
            adPlaceholderBackground = secondaryCard,
            adPlaceholderText = muted,
            badgeBackground = opposite,
            onBadgeText = onAccent,
            highlightBackground = selected,
            onHighlightText = heading,
            glowColor = primary.copy(alpha = if (dark) 0.25f else 0.16f)
        )
    }

    private fun colors(
        lightPrimary: Color, lightSecondary: Color, lightTertiary: Color, lightOpposite: Color,
        lightScreen: Color, lightCard: Color, lightSecondaryCard: Color,
        lightHeading: Color, lightBody: Color, lightMuted: Color, lightBorder: Color,
        lightSelected: Color, lightGradientStart: Color, lightGradientEnd: Color,
        darkPrimary: Color, darkSecondary: Color, darkTertiary: Color, darkOpposite: Color,
        darkScreen: Color, darkCard: Color, darkSecondaryCard: Color,
        darkHeading: Color, darkBody: Color, darkMuted: Color, darkBorder: Color,
        darkSelected: Color, darkGradientStart: Color, darkGradientEnd: Color
    ) = ThemeColors(
        lightPrimary, lightSecondary, lightTertiary, lightOpposite,
        lightScreen, lightCard, lightSecondaryCard, lightHeading, lightBody, lightMuted,
        lightBorder, lightSelected, lightGradientStart, lightGradientEnd,
        darkPrimary, darkSecondary, darkTertiary, darkOpposite,
        darkScreen, darkCard, darkSecondaryCard, darkHeading, darkBody, darkMuted,
        darkBorder, darkSelected, darkGradientStart, darkGradientEnd
    )

    private val defaultBlue = colors(
        // Default Blue: Button Accent = Warm Amber
        // Light: Primary #2563EB · Secondary #0284C7 · Tertiary #6366F1 · Opposite #D97706
        Color(0xFF2563EB), Color(0xFF0284C7), Color(0xFF6366F1), Color(0xFFD97706),
        Color(0xFFF8FAFC), Color(0xFFFFFFFF), Color(0xFFF1F5F9), Color(0xFF0F172A), Color(0xFF334155), Color(0xFF64748B), Color(0xFFE2E8F0), Color(0xFFDBEAFE), Color(0xFF2563EB), Color(0xFF4F46E5),
        // Dark: True Black Screen #000000
        Color(0xFF3B82F6), Color(0xFF38BDF8), Color(0xFF818CF8), Color(0xFFF59E0B),
        Color(0xFF000000), Color(0xFF121212), Color(0xFF1E1E22), Color(0xFFFFFFFF), Color(0xFFE2E8F0), Color(0xFF94A3B8), Color(0xFF27272A), Color(0xFF27272A), Color(0xFF1D4ED8), Color(0xFF3730A3)
    )

    private val forestGreen = colors(
        // Forest Green: Button Accent = Cobalt Sky Blue (Unique for Green!)
        // Light: Primary #059669 · Secondary #0284C7 · Tertiary #0D9488 · Opposite #0284C7
        Color(0xFF059669), Color(0xFF0284C7), Color(0xFF0D9488), Color(0xFF0284C7),
        Color(0xFFF8FAFC), Color(0xFFFFFFFF), Color(0xFFF1F5F9), Color(0xFF0F172A), Color(0xFF334155), Color(0xFF64748B), Color(0xFFE2E8F0), Color(0xFFD1FAE5), Color(0xFF059669), Color(0xFF0D9488),
        // Dark: True Black Screen #000000
        Color(0xFF10B981), Color(0xFF38BDF8), Color(0xFF2DD4BF), Color(0xFF38BDF8),
        Color(0xFF000000), Color(0xFF121212), Color(0xFF1E1E22), Color(0xFFFFFFFF), Color(0xFFE2E8F0), Color(0xFF94A3B8), Color(0xFF27272A), Color(0xFF27272A), Color(0xFF047857), Color(0xFF0F766E)
    )

    private val midnightDark = colors(
        // Midnight Dark: Button Accent = Teal Cyan (Unique for Indigo!)
        // Light: Primary #4F46E5 · Secondary #7C3AED · Tertiary #0284C7 · Opposite #06B6D4
        Color(0xFF4F46E5), Color(0xFF7C3AED), Color(0xFF0284C7), Color(0xFF06B6D4),
        Color(0xFFF8FAFC), Color(0xFFFFFFFF), Color(0xFFF1F5F9), Color(0xFF0F172A), Color(0xFF334155), Color(0xFF64748B), Color(0xFFE2E8F0), Color(0xFFE0E7FF), Color(0xFF4F46E5), Color(0xFF7C3AED),
        // Dark: True Black Screen #000000
        Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFF38BDF8), Color(0xFF22D3EE),
        Color(0xFF000000), Color(0xFF121212), Color(0xFF1E1E22), Color(0xFFFFFFFF), Color(0xFFE2E8F0), Color(0xFF94A3B8), Color(0xFF27272A), Color(0xFF27272A), Color(0xFF3730A3), Color(0xFF5B21B6)
    )

    private val sunsetOrange = colors(
        // Sunset Orange: Button Accent = Slate Indigo (Unique for Orange!)
        // Light: Primary #EA580C · Secondary #D97706 · Tertiary #C026D3 · Opposite #4F46E5
        Color(0xFFEA580C), Color(0xFFD97706), Color(0xFFC026D3), Color(0xFF4F46E5),
        Color(0xFFF8FAFC), Color(0xFFFFFFFF), Color(0xFFF1F5F9), Color(0xFF0F172A), Color(0xFF334155), Color(0xFF64748B), Color(0xFFE2E8F0), Color(0xFFFFEDD5), Color(0xFFEA580C), Color(0xFFE11D48),
        // Dark: True Black Screen #000000
        Color(0xFFF97316), Color(0xFFF59E0B), Color(0xFFE879F9), Color(0xFF818CF8),
        Color(0xFF000000), Color(0xFF121212), Color(0xFF1E1E22), Color(0xFFFFFFFF), Color(0xFFE2E8F0), Color(0xFF94A3B8), Color(0xFF27272A), Color(0xFF27272A), Color(0xFFC2410C), Color(0xFF9F1239)
    )

    private val royalPurple = colors(
        // Royal Purple: Button Accent = Emerald Green (Unique for Purple!)
        // Light: Primary #7C3AED · Secondary #C026D3 · Tertiary #0284C7 · Opposite #059669
        Color(0xFF7C3AED), Color(0xFFC026D3), Color(0xFF0284C7), Color(0xFF059669),
        Color(0xFFF8FAFC), Color(0xFFFFFFFF), Color(0xFFF1F5F9), Color(0xFF0F172A), Color(0xFF334155), Color(0xFF64748B), Color(0xFFE2E8F0), Color(0xFFEDE9FE), Color(0xFF7C3AED), Color(0xFFC026D3),
        // Dark: True Black Screen #000000
        Color(0xFF8B5CF6), Color(0xFFE879F9), Color(0xFF38BDF8), Color(0xFF34D399),
        Color(0xFF000000), Color(0xFF121212), Color(0xFF1E1E22), Color(0xFFFFFFFF), Color(0xFFE2E8F0), Color(0xFF94A3B8), Color(0xFF27272A), Color(0xFF27272A), Color(0xFF5B21B6), Color(0xFF86198F)
    )

    private val oceanTeal = colors(
        // Ocean Teal: Button Accent = Muted Orchid Magenta (Unique for Teal!)
        // Light: Primary #0D9488 · Secondary #2563EB · Tertiary #059669 · Opposite #C026D3
        Color(0xFF0D9488), Color(0xFF2563EB), Color(0xFF059669), Color(0xFFC026D3),
        Color(0xFFF8FAFC), Color(0xFFFFFFFF), Color(0xFFF1F5F9), Color(0xFF0F172A), Color(0xFF334155), Color(0xFF64748B), Color(0xFFE2E8F0), Color(0xFFCCFBF1), Color(0xFF0D9488), Color(0xFF2563EB),
        // Dark: True Black Screen #000000
        Color(0xFF14B8A6), Color(0xFF3B82F6), Color(0xFF10B981), Color(0xFFE879F9),
        Color(0xFF000000), Color(0xFF121212), Color(0xFF1E1E22), Color(0xFFFFFFFF), Color(0xFFE2E8F0), Color(0xFF94A3B8), Color(0xFF27272A), Color(0xFF27272A), Color(0xFF0F766E), Color(0xFF1D4ED8)
    )

    private fun definition(preset: ThemePreset, colors: ThemeColors) = ThemePresetDefinition(
        preset = preset,
        lightPalette = makePalette(colors, dark = false),
        darkPalette = makePalette(colors, dark = true)
    )

    // Keep this list below every palette definition to avoid initialization-order issues.
    val all: List<ThemePresetDefinition> = listOf(
        definition(ThemePreset.DEFAULT_BLUE, defaultBlue),
        definition(ThemePreset.FOREST_GREEN, forestGreen),
        definition(ThemePreset.MIDNIGHT_DARK, midnightDark),
        definition(ThemePreset.SUNSET_ORANGE, sunsetOrange),
        definition(ThemePreset.ROYAL_PURPLE, royalPurple),
        definition(ThemePreset.OCEAN_TEAL, oceanTeal)
    )

    fun get(preset: ThemePreset): ThemePresetDefinition = all.first { it.preset == preset }
}
