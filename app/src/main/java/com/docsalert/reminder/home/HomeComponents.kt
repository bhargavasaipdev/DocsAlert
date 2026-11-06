package com.docsalert.reminder.home

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.docsalert.reminder.R
import com.docsalert.reminder.data.model.FamilyMember
import com.docsalert.reminder.data.model.Item
import com.docsalert.reminder.data.model.ItemCategory
import com.docsalert.reminder.data.model.formattedNumber
import com.docsalert.reminder.ui.components.MemberProfileAvatar
import com.docsalert.reminder.ui.components.bellSwing
import com.docsalert.reminder.ui.components.pressScale
import com.docsalert.reminder.ui.components.rememberCountUp
import com.docsalert.reminder.ui.theme.palette
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// ---------------------------------------------------------------------------
// Theme colors for categories and expiry levels
// ---------------------------------------------------------------------------

@Immutable
internal data class ColorPair(val content: Color, val container: Color)

@Composable
internal fun ItemCategory.colors(): ColorPair {
    val p = MaterialTheme.palette
    return when (this) {
        ItemCategory.VEHICLE -> ColorPair(p.onAccentContainer, p.accentContainer)
        ItemCategory.INSURANCE -> ColorPair(p.secondaryAccent, p.secondaryCardBackground)
        ItemCategory.IDENTITY -> ColorPair(p.onTertiaryAccentContainer, p.tertiaryAccentContainer)
        ItemCategory.TRAVEL -> ColorPair(p.onOppositeAccentContainer, p.oppositeAccentContainer)
        ItemCategory.CERTIFICATES -> ColorPair(p.onSuccessContainer, p.successBackground)
        ItemCategory.OTHER -> ColorPair(p.onInfoContainer, p.infoBackground)
    }
}

internal enum class ExpiryLevel { SAFE, SOON, URGENT }

internal fun expiryLevelFor(daysLeft: Long): ExpiryLevel = when {
    daysLeft <= 7 -> ExpiryLevel.URGENT
    daysLeft <= 30 -> ExpiryLevel.SOON
    else -> ExpiryLevel.SAFE
}

@Composable
internal fun ExpiryLevel.colors(): ColorPair {
    val p = MaterialTheme.palette
    return when (this) {
        ExpiryLevel.SAFE -> ColorPair(p.onSuccessContainer, p.successBackground)
        ExpiryLevel.SOON -> ColorPair(p.onWarningContainer, p.warningBackground)
        ExpiryLevel.URGENT -> ColorPair(p.onErrorContainer, p.errorBackground)
    }
}

private fun remainingText(daysLeft: Long): String = when {
    daysLeft < 0 -> "Expired"
    daysLeft == 0L -> "Due today"
    daysLeft == 1L -> "1 day left"
    else -> "$daysLeft days left"
}

private fun greetingTime(): String {
    val hour = LocalTime.now().hour
    return when {
        hour in 5..11 -> "Good Morning 👋"
        hour in 12..16 -> "Good Afternoon ☀️"
        hour in 17..21 -> "Good Evening 🌙"
        else -> "Welcome Back ✨"
    }
}

// ---------------------------------------------------------------------------
// Modern Dashboard Header
// ---------------------------------------------------------------------------

@Composable
internal fun HomeHeader(
    familyMembers: List<FamilyMember> = emptyList(),
    selectedMemberId: String? = null,
    onMemberSelected: (String?) -> Unit = {}
) {
    val palette = MaterialTheme.palette
    var menuExpanded by remember { mutableStateOf(false) }

    val selectedName = remember(selectedMemberId, familyMembers) {
        if (selectedMemberId == null) {
            "All Members"
        } else {
            familyMembers.find { it.id == selectedMemberId }?.fullName ?: "All Members"
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(14.dp),
            color = palette.appLogoBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderColor.copy(alpha = 0.5f)),
            shadowElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "DocsAlert logo",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "DocsAlert",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = palette.headingText
            )
            Text(
                text = greetingTime(),
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                color = palette.oppositeAccent
            )
        }

        if (familyMembers.isNotEmpty()) {
            val selectedMember = familyMembers.find { it.id == selectedMemberId }
            Box {
                Surface(
                    onClick = { menuExpanded = true },
                    shape = RoundedCornerShape(50),
                    color = palette.oppositeAccent,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MemberProfileAvatar(
                            photoUrl = selectedMember?.profilePhotoUrl,
                            modifier = Modifier.size(26.dp)
                        )
                        Text(
                            text = selectedName,
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                            color = palette.onOppositeAccent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Select member",
                            tint = palette.onOppositeAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("All Members", fontWeight = FontWeight.Bold) },
                        leadingIcon = {
                            MemberProfileAvatar(photoUrl = null, modifier = Modifier.size(24.dp))
                        },
                        onClick = {
                            onMemberSelected(null)
                            menuExpanded = false
                        }
                    )
                    familyMembers.forEach { member ->
                        DropdownMenuItem(
                            text = { Text(member.fullName) },
                            leadingIcon = {
                                MemberProfileAvatar(photoUrl = member.profilePhotoUrl, modifier = Modifier.size(24.dp))
                            },
                            onClick = {
                                onMemberSelected(member.id)
                                menuExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Hero Summary Card with Embedded Status Boxes & Health Indicator
// ---------------------------------------------------------------------------

@Composable
internal fun TotalItemsCard(
    total: Int,
    valid: Int = 0,
    expiringSoon: Int = 0,
    expired: Int = 0,
    activeRemindersCount: Int = 0,
    onClick: () -> Unit = {},
    onStatusClick: (String?) -> Unit = {},
    onRemindersClick: () -> Unit = {}
) {
    val palette = MaterialTheme.palette
    val shownTotal = rememberCountUp(total)
    val formattedTotal = String.format(java.util.Locale.getDefault(), "%02d", shownTotal)
    val formattedReminders = String.format(java.util.Locale.getDefault(), "%02d", activeRemindersCount)
    val decoration = palette.onSummaryCard
    val interactionSource = remember { MutableInteractionSource() }

    val safeRatio = if (total > 0) valid.toFloat() / total.toFloat() else 1f
    val soonRatio = if (total > 0) expiringSoon.toFloat() / total.toFloat() else 0f
    val expiredRatio = if (total > 0) expired.toFloat() / total.toFloat() else 0f

    val infiniteTransition = rememberInfiniteTransition(label = "ambient_mesh")
    val shift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambient_mesh_shift"
    )

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderColor.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(palette.summaryCardStart, palette.summaryCardEnd),
                    start = Offset(0f, 0f),
                    end = Offset(0f, Float.POSITIVE_INFINITY)
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .drawBehind {
                drawCircle(
                    color = decoration.copy(alpha = 0.08f + (0.04f * shift)),
                    radius = size.minDimension * (0.80f + (0.15f * shift)),
                    center = Offset(size.width * (0.85f + (0.1f * shift)), size.height * (0.1f - (0.05f * shift)))
                )
                drawCircle(
                    color = decoration.copy(alpha = 0.05f + (0.03f * (1f - shift))),
                    radius = size.minDimension * (0.50f + (0.15f * (1f - shift))),
                    center = Offset(size.width * (0.1f + (0.05f * (1f - shift))), size.height * (0.9f + (0.1f * shift)))
                )
            }
    ) {
        Column(
            modifier = Modifier.padding(19.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            // Top Section: Title + Total Count + Animated Ringing Bell
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .pressScale(interactionSource)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = LocalIndication.current,
                        onClick = onClick
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "TOTAL SAVED ITEMS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = palette.onSummaryCard.copy(alpha = 0.85f),
                        modifier = Modifier.padding(bottom = 3.dp)
                    )

                    Text(
                        text = formattedTotal,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 46.sp,
                            lineHeight = 50.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = palette.onSummaryCard
                    )
                }

                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(palette.onSummaryCard.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = palette.onSummaryCard,
                        modifier = Modifier
                            .size(35.dp)
                            .bellSwing()
                    )
                }
            }

            // Health Segmented Progress Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(palette.onSummaryCard.copy(alpha = 0.20f))
            ) {
                if (total > 0) {
                    if (safeRatio > 0f) {
                        Box(
                            modifier = Modifier
                                .weight(safeRatio)
                                .height(6.dp)
                                .background(palette.successColor)
                        )
                    }
                    if (soonRatio > 0f) {
                        Box(
                            modifier = Modifier
                                .weight(soonRatio)
                                .height(6.dp)
                                .background(palette.warningColor)
                        )
                    }
                    if (expiredRatio > 0f) {
                        Box(
                            modifier = Modifier
                                .weight(expiredRatio)
                                .height(6.dp)
                                .background(palette.errorColor)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(palette.successColor)
                    )
                }
            }

            // Embedded Status Boxes inside the Card (Valid, Expiring, Expired)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EmbeddedStatusTile(
                    title = "Valid",
                    count = valid,
                    icon = Icons.Default.CheckCircle,
                    textColor = palette.onSuccessContainer,
                    bgColor = palette.successBackground.copy(alpha = 0.95f),
                    onClick = { onStatusClick("VALID") },
                    modifier = Modifier.weight(1f)
                )
                EmbeddedStatusTile(
                    title = "Expiring",
                    count = expiringSoon,
                    icon = Icons.Default.AccessTime,
                    textColor = palette.onWarningContainer,
                    bgColor = palette.warningBackground.copy(alpha = 0.95f),
                    isAnimatedBell = true,
                    onClick = { onStatusClick("EXPIRING") },
                    modifier = Modifier.weight(1f)
                )
                EmbeddedStatusTile(
                    title = "Expired",
                    count = expired,
                    icon = Icons.Default.ErrorOutline,
                    textColor = palette.onErrorContainer,
                    bgColor = palette.errorBackground.copy(alpha = 0.95f),
                    onClick = { onStatusClick("EXPIRED") },
                    modifier = Modifier.weight(1f)
                )
            }

            // Daily Reminder Count Strip below Status Boxes
            Surface(
                onClick = onRemindersClick,
                shape = RoundedCornerShape(14.dp),
                color = palette.onSummaryCard.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, palette.onSummaryCard.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = palette.onSummaryCard,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Daily Reminders Active: $formattedReminders",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = palette.onSummaryCard
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open Daily Reminders",
                        tint = palette.onSummaryCard,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmbeddedStatusTile(
    title: String,
    count: Int,
    icon: ImageVector,
    textColor: Color,
    bgColor: Color,
    isAnimatedBell: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val formattedCount = String.format(java.util.Locale.getDefault(), "%02d", count)

    Surface(
        onClick = onClick,
        shape = shape,
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.25f)),
        interactionSource = interactionSource,
        modifier = modifier.pressScale(interactionSource)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(textColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier
                        .size(18.dp)
                        .then(if (isAnimatedBell) Modifier.bellSwing() else Modifier)
                )
            }

            Text(
                text = formattedCount,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp),
                color = textColor
            )

            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Section Header Component
// ---------------------------------------------------------------------------

@Composable
internal fun SectionHeader(
    title: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val palette = MaterialTheme.palette

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 18.dp)
                .clip(RoundedCornerShape(50))
                .background(palette.primaryAccent)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
            color = palette.headingText
        )
        if (!actionText.isNullOrBlank() && onActionClick != null) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onActionClick)
                    .padding(start = 10.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = palette.primaryAccent
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = palette.primaryAccent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Feature Quick Access Hub (Vault + Daily Reminders Flagship Cards)
// ---------------------------------------------------------------------------

// ---------------------------------------------------------------------------
// Feature Quick Access Hub (Vault + Daily Reminders Grouped Options Card)
// ---------------------------------------------------------------------------

@Composable
internal fun FeatureQuickAccessCards(
    onVaultClick: () -> Unit,
    onRemindersClick: () -> Unit
) {
    val palette = MaterialTheme.palette
    val vaultInteraction = remember { MutableInteractionSource() }
    val reminderInteraction = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Vault App Grid Card
        Card(
            onClick = onVaultClick,
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
            border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .weight(1f)
                .pressScale(vaultInteraction)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(palette.oppositeAccentContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Vault",
                            tint = palette.onOppositeAccentContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = palette.oppositeAccentContainer
                    ) {
                        Text(
                            text = "VAULT",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.ExtraBold),
                            color = palette.onOppositeAccentContainer,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Item Vault",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.ExtraBold),
                        color = palette.headingText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "PAN, Aadhaar & PDFs",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                        color = palette.secondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Daily Reminders App Grid Card
        Card(
            onClick = onRemindersClick,
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
            border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .weight(1f)
                .pressScale(reminderInteraction)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(palette.accentContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = "Reminders",
                            tint = palette.primaryAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = palette.accentContainer
                    ) {
                        Text(
                            text = "ALARMS",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.ExtraBold),
                            color = palette.onAccentContainer,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Daily Reminders",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.ExtraBold),
                        color = palette.headingText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "Custom task alarms",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                        color = palette.secondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Upcoming Expirations Card
// ---------------------------------------------------------------------------

@Composable
internal fun UpcomingExpirations(
    items: List<Item>,
    onSeeAllClick: () -> Unit,
    onItemClick: (String) -> Unit,
    maskNumbers: Boolean = false
) {
    val palette = MaterialTheme.palette
    val shape = RoundedCornerShape(26.dp)
    val today = LocalDate.now()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(palette.cardBackground)
            .border(1.dp, palette.borderColor, shape)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        SectionHeader(
            title = "Upcoming Expirations",
            actionText = "See all",
            onActionClick = onSeeAllClick
        )

        if (items.isEmpty()) {
            Spacer(Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = palette.secondaryCardBackground,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "All Items Up to Date 🎉",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = palette.headingText
                    )
                    Text(
                        text = "No items expiring in the next 30 days.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.secondaryText
                    )
                }
            }
        } else {
            items.forEachIndexed { index, item ->
                ItemRow(
                    item = item,
                    showDivider = index != items.lastIndex,
                    today = today,
                    onClick = { onItemClick(item.id) },
                    maskNumbers = maskNumbers
                )
            }
        }
    }
}

@Composable
private fun ItemRow(
    item: Item,
    showDivider: Boolean,
    today: LocalDate,
    onClick: () -> Unit,
    maskNumbers: Boolean = false
) {
    val palette = MaterialTheme.palette
    val categoryColors = item.category.colors()
    val daysLeft = item.expiryDate?.let { ChronoUnit.DAYS.between(today, it) } ?: Long.MAX_VALUE
    val level = expiryLevelFor(daysLeft)
    val levelColors = level.colors()
    val formattedNum = item.formattedNumber(maskNumbers)
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .pressScale(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(categoryColors.container),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.category.icon,
                    contentDescription = null,
                    tint = categoryColors.content,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = palette.headingText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.category.title} ${if (formattedNum != null) "• $formattedNum" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 12.sp,
                    color = palette.secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.expiryDate?.format(DateTimeFormatter.ofPattern("d MMM yyyy")) ?: "No Expiry",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = palette.bodyText
                )
            }

            // Status pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(levelColors.container)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (level == ExpiryLevel.URGENT) {
                    PulsingDot(color = levelColors.content)
                    Spacer(Modifier.width(6.dp))
                }
                if (daysLeft != Long.MAX_VALUE) {
                    Text(
                        text = remainingText(daysLeft),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = levelColors.content,
                        maxLines = 1
                    )
                }
            }
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(top = 10.dp),
                color = palette.dividerColor.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun PulsingDot(color: Color) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = Modifier
            .size(7.dp)
            .graphicsLayer { this.alpha = pulse }
            .background(color, CircleShape)
    )
}

// ---------------------------------------------------------------------------
// Add Item Button
// ---------------------------------------------------------------------------

@Composable
internal fun AddItemButton(onClick: () -> Unit) {
    val palette = MaterialTheme.palette
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .pressScale(interactionSource)
            .clip(RoundedCornerShape(50))
            .background(palette.oppositeAccent)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            )
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(palette.onOppositeAccent.copy(alpha = 0.20f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = palette.onOppositeAccent
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = "Add New Item",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = palette.onOppositeAccent
        )
    }
}

// ---------------------------------------------------------------------------
// Ad Banner Container
// ---------------------------------------------------------------------------

@Composable
internal fun AdBannerPlaceholder() {
    com.docsalert.reminder.ui.components.BannerAdView(modifier = Modifier.padding(vertical = 4.dp))
}
