package com.docsalert.reminder.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Defines the semantic colors used throughout DocsAlert.
 *
 * Each theme preset provides its own light and dark palettes with high-contrast
 * primary, secondary, tertiary, and opposite color properties for enhanced UX.
 */
@Immutable
data class ThemePalette(

    // Backgrounds
    val screenBackground: Color,
    val cardBackground: Color,
    val secondaryCardBackground: Color,
    val topBarBackground: Color,
    val bottomBarBackground: Color,
    val dialogBackground: Color,
    val inputBackground: Color,
    val disabledBackground: Color,

    // Text
    val headingText: Color,
    val subheadingText: Color,
    val bodyText: Color,
    val secondaryText: Color,
    val hintText: Color,
    val onAccentText: Color,
    val disabledText: Color,
    val linkText: Color,

    // Borders and dividers
    val borderColor: Color,
    val inputBorderColor: Color,
    val focusedBorderColor: Color,
    val dividerColor: Color,
    val selectedBorderColor: Color,

    // Interactive elements & Primary Accents
    val primaryAccent: Color,
    val primaryAccentPressed: Color,
    val secondaryAccent: Color,
    val accentContainer: Color,
    val onAccentContainer: Color,
    val selectedItemBackground: Color,
    val selectedItemContent: Color,
    val unselectedItemContent: Color,

    // Opposite & Complementary Accents (Opposite color wheel tones for high visual contrast)
    val oppositeAccent: Color,
    val oppositeAccentPressed: Color,
    val onOppositeAccent: Color,
    val oppositeAccentContainer: Color,
    val onOppositeAccentContainer: Color,

    // Tertiary Accents (For badges, multi-category tags, and highlight elements)
    val tertiaryAccent: Color,
    val tertiaryAccentContainer: Color,
    val onTertiaryAccent: Color,
    val onTertiaryAccentContainer: Color,

    // Inverse Surfaces & Floating Overlays
    val inverseScreenBackground: Color,
    val inverseCardBackground: Color,
    val onInverseCardText: Color,

    // Document status colors & dedicated container content colors
    val successColor: Color,
    val successBackground: Color,
    val onSuccessContainer: Color,
    val warningColor: Color,
    val warningBackground: Color,
    val onWarningContainer: Color,
    val errorColor: Color,
    val errorBackground: Color,
    val onErrorContainer: Color,
    val infoColor: Color,
    val infoBackground: Color,
    val onInfoContainer: Color,

    // Special components & Badges
    val summaryCardStart: Color,
    val summaryCardEnd: Color,
    val onSummaryCard: Color,
    val categoryIconBackground: Color,
    val appLogoBackground: Color,
    val adPlaceholderBackground: Color,
    val adPlaceholderText: Color,
    val badgeBackground: Color,
    val onBadgeText: Color,
    val highlightBackground: Color,
    val onHighlightText: Color,
    val glowColor: Color
)
