package com.docsalert.reminder.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.docsalert.reminder.R
import com.docsalert.reminder.data.preferences.ThemeSettings
import com.docsalert.reminder.data.preferences.UserPreferencesRepository
import com.docsalert.reminder.ui.components.ScreenHeader
import com.docsalert.reminder.ui.components.StaggeredEntrance
import com.docsalert.reminder.ui.components.pressScale
import com.docsalert.reminder.ui.theme.AppearanceMode
import com.docsalert.reminder.ui.theme.ThemePalette
import com.docsalert.reminder.ui.theme.ThemePresetDefinition
import com.docsalert.reminder.ui.theme.ThemePresets
import com.docsalert.reminder.ui.theme.palette
import kotlinx.coroutines.launch

@Composable
fun ThemeSettingsScreen(
    preferencesRepository: UserPreferencesRepository,
    onBackClick: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val settings by preferencesRepository.themeSettings
        .collectAsState(initial = ThemeSettings())

    // Each preview is drawn in the mode the user will actually see.
    val previewDark = when (settings.appearance) {
        AppearanceMode.LIGHT -> false
        AppearanceMode.DARK -> true
        AppearanceMode.SYSTEM -> isSystemInDarkTheme()
    }

    // No inner Scaffold: the app-level Scaffold already handles system bar spacing.
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Themes",
            subtitle = "Personalize your app appearance",
            onBackClick = onBackClick
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                StaggeredEntrance(0) {
                    Column {
                        SectionTitle(
                            title = "Appearance mode",
                            subtitle = "Choose how DocsAlert looks on your device."
                        )
                        Spacer(Modifier.height(14.dp))
                        AppearanceSelector(
                            selected = settings.appearance,
                            onSelect = { mode ->
                                scope.launch { preferencesRepository.saveAppearanceMode(mode) }
                            }
                        )
                    }
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                StaggeredEntrance(1) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        SectionTitle(
                            title = "Color themes",
                            subtitle = "Tap a theme to apply it instantly."
                        )
                    }
                }
            }

            itemsIndexed(
                items = ThemePresets.all,
                key = { _, definition -> definition.preset.id }
            ) { index, definition ->
                StaggeredEntrance(2 + index / 2) {
                    ThemePresetCard(
                        definition = definition,
                        previewDark = previewDark,
                        selected = settings.preset == definition.preset,
                        onClick = {
                            scope.launch { preferencesRepository.saveThemePreset(definition.preset) }
                        }
                    )
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                StaggeredEntrance(10) {
                    com.docsalert.reminder.ui.components.BannerAdView(modifier = Modifier.padding(top = 12.dp))
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Section title
// ---------------------------------------------------------------------------

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    val palette = MaterialTheme.palette

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 20.dp)
                .clip(RoundedCornerShape(50))
                .background(palette.primaryAccent)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
            color = palette.headingText
        )
    }
    Spacer(Modifier.height(4.dp))
    Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium,
        color = palette.secondaryText
    )
}

// ---------------------------------------------------------------------------
// Appearance mode (Light / Dark / System)
// ---------------------------------------------------------------------------

@Composable
private fun AppearanceSelector(
    selected: AppearanceMode,
    onSelect: (AppearanceMode) -> Unit
) {
    val palette = MaterialTheme.palette

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(palette.secondaryCardBackground)
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        AppearanceMode.entries.forEach { mode ->
            AppearanceOption(
                mode = mode,
                selected = mode == selected,
                modifier = Modifier.weight(1f),
                onClick = { onSelect(mode) }
            )
        }
    }
}

@Composable
private fun AppearanceOption(
    mode: AppearanceMode,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val palette = MaterialTheme.palette

    val label: String
    val icon: ImageVector
    when (mode) {
        AppearanceMode.LIGHT -> {
            label = "Light"
            icon = Icons.Default.LightMode
        }
        AppearanceMode.DARK -> {
            label = "Dark"
            icon = Icons.Default.DarkMode
        }
        AppearanceMode.SYSTEM -> {
            label = "System"
            icon = Icons.Default.BrightnessAuto
        }
    }

    val containerColor by animateColorAsState(
        targetValue = if (selected) palette.primaryAccent else palette.secondaryCardBackground,
        animationSpec = tween(durationMillis = 250),
        label = "optionContainer"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) palette.onAccentText else palette.bodyText,
        animationSpec = tween(durationMillis = 250),
        label = "optionContent"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(15.dp))
            .background(containerColor)
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick
            )
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            ),
            color = contentColor,
            maxLines = 1
        )
    }
}

// ---------------------------------------------------------------------------
// Theme card: a small "screenshot" of the app in that theme + name + check
// ---------------------------------------------------------------------------

@Composable
private fun ThemePresetCard(
    definition: ThemePresetDefinition,
    previewDark: Boolean,
    selected: Boolean,
    onClick: () -> Unit
) {
    val palette = MaterialTheme.palette
    val preview = if (previewDark) definition.darkPalette else definition.lightPalette
    val shape = RoundedCornerShape(22.dp)
    val interactionSource = remember { MutableInteractionSource() }

    val background by animateColorAsState(
        targetValue = if (selected) palette.selectedItemBackground else palette.cardBackground,
        animationSpec = tween(durationMillis = 250),
        label = "cardBackground"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) palette.selectedBorderColor else palette.borderColor,
        animationSpec = tween(durationMillis = 250),
        label = "cardBorderColor"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (selected) 2.dp else 1.dp,
        animationSpec = tween(durationMillis = 250),
        label = "cardBorderWidth"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interactionSource)
            .clip(shape)
            .background(background)
            .border(borderWidth, borderColor, shape)
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                role = Role.RadioButton,
                onClick = onClick
            )
            .padding(10.dp)
    ) {
        ThemeMiniPreview(preview = preview)

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .height(22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = definition.preset.displayName,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = palette.headingText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Signature theme palette swatches (Primary, Secondary, Tertiary, Opposite)
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(preview.primaryAccent)
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(preview.secondaryAccent)
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(preview.tertiaryAccent)
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(preview.oppositeAccent)
                )
            }

            Spacer(Modifier.width(6.dp))

            AnimatedVisibility(
                visible = selected,
                enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(palette.primaryAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected theme",
                        tint = palette.onAccentText,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/**
 * A tiny drawing of the Home screen using the given theme palette:
 * app logo + title lines, the gradient summary card, and the three status chips.
 */
@Composable
private fun ThemeMiniPreview(
    preview: ThemePalette,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(preview.screenBackground)
            .border(1.dp, preview.borderColor, shape)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Header: the real app logo on this theme's logo background + Opposite Accent chip
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(preview.appLogoBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = null,
                        modifier = Modifier
                            .size(21.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Box(
                        modifier = Modifier
                            .size(width = 38.dp, height = 6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(preview.headingText)
                    )
                    Box(
                        modifier = Modifier
                            .size(width = 24.dp, height = 4.dp)
                            .clip(RoundedCornerShape(50))
                            .background(preview.secondaryText)
                    )
                }
            }

            // Opposite Accent badge indicator (shows off contrast theme engine)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(preview.oppositeAccentContainer)
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 14.dp, height = 4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(preview.onOppositeAccentContainer)
                )
            }
        }

        // Summary card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.linearGradient(
                        listOf(preview.summaryCardStart, preview.summaryCardEnd)
                    )
                )
                .padding(horizontal = 8.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 34.dp, height = 7.dp)
                    .clip(RoundedCornerShape(50))
                    .background(preview.onSummaryCard)
            )
            Box(
                modifier = Modifier
                    .size(width = 52.dp, height = 4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(preview.onSummaryCard.copy(alpha = 0.6f))
            )
        }

        // Status & Opposite Accent chips
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            PreviewChip(preview.onSuccessContainer, preview.successBackground, Modifier.weight(1f))
            PreviewChip(preview.onWarningContainer, preview.warningBackground, Modifier.weight(1f))
            PreviewChip(preview.onErrorContainer, preview.errorBackground, Modifier.weight(1f))
            PreviewChip(preview.onOppositeAccentContainer, preview.oppositeAccentContainer, Modifier.weight(1f))
        }
    }
}

@Composable
private fun PreviewChip(
    content: Color,
    container: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(16.dp)
            .clip(RoundedCornerShape(50))
            .background(container),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 14.dp, height = 4.dp)
                .clip(RoundedCornerShape(50))
                .background(content)
        )
    }
}
