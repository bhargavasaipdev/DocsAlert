package com.docsalert.reminder.family

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.docsalert.reminder.data.model.FamilyMember
import com.docsalert.reminder.ui.components.PlaceholderState
import com.docsalert.reminder.ui.components.ScreenHeader
import com.docsalert.reminder.ui.components.StaggeredEntrance
import com.docsalert.reminder.ui.components.pressScale
import com.docsalert.reminder.ui.theme.palette
import com.docsalert.reminder.ui.viewmodels.FamilyViewModel
import com.docsalert.reminder.ui.viewmodels.ItemViewModel
import java.time.LocalDate

@Composable
fun FamilyScreen(
    familyViewModel: FamilyViewModel? = null,
    itemViewModel: ItemViewModel? = null,
    onBackClick: (() -> Unit)? = null,
    onAddFamilyMemberClick: (String?) -> Unit = {},
    onMemberClick: (String?) -> Unit = {}
) {
    val palette = MaterialTheme.palette
    val membersState = familyViewModel?.members?.collectAsState()
    val members = membersState?.value ?: emptyList()

    val itemsState = itemViewModel?.items?.collectAsState()
    val items = itemsState?.value ?: emptyList()

    val today = remember { LocalDate.now() }

    val totalMembers = members.size
    val totalItems = items.size
    val validItems = items.count { it.expiryDate == null || java.time.temporal.ChronoUnit.DAYS.between(today, it.expiryDate) > 30 }
    val expiringSoonItems = items.count { it.expiryDate != null && java.time.temporal.ChronoUnit.DAYS.between(today, it.expiryDate) in 0..30 }
    val expiredItems = items.count { it.expiryDate != null && java.time.temporal.ChronoUnit.DAYS.between(today, it.expiryDate) < 0 }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            StaggeredEntrance(0) {
                ScreenHeader(
                    title = "Family",
                    subtitle = "Organize items for family members",
                    onBackClick = onBackClick
                )
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    StaggeredEntrance(1) {
                        FamilyStatsCard(
                            totalMembers = totalMembers,
                            totalItems = totalItems,
                            validItems = validItems,
                            expiringSoonItems = expiringSoonItems,
                            expiredItems = expiredItems
                        )
                    }
                }

                item {
                    com.docsalert.reminder.ui.components.BannerAdView(modifier = Modifier.padding(vertical = 4.dp))
                }

                if (members.isEmpty()) {
                    item {
                        StaggeredEntrance(2) {
                            PlaceholderState(
                                icon = Icons.Default.Groups,
                                title = "No family members added yet",
                                message = "Tap the + button to add family members and keep their items organized.",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    item {
                        StaggeredEntrance(2) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
                                shape = RoundedCornerShape(24.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderColor),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    members.forEachIndexed { index, member ->
                                        val memberItemCount = items.count { it.familyMemberId == member.id }
                                        FamilyMemberRow(
                                            member = member,
                                            itemCount = memberItemCount,
                                            showDivider = index < members.lastIndex,
                                            onClick = { onMemberClick(member.id) },
                                            onEditClick = { onAddFamilyMemberClick(member.id) },
                                            onDeleteClick = {
                                                itemViewModel?.removeItemsByFamilyMember(member.id)
                                                familyViewModel?.removeFamilyMember(member.id)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { onAddFamilyMemberClick(null) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.palette.oppositeAccent,
            contentColor = MaterialTheme.palette.onOppositeAccent,
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Add Family Member", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) }
        )
    }
}

@Composable
private fun FamilyStatsCard(
    totalMembers: Int,
    totalItems: Int,
    validItems: Int,
    expiringSoonItems: Int,
    expiredItems: Int
) {
    val palette = MaterialTheme.palette
    val shape = RoundedCornerShape(26.dp)

    Card(
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
        border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Family Overview",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    color = palette.headingText
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(palette.oppositeAccentContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$totalMembers Member(s)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = palette.onOppositeAccentContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatPill(label = "Total Items", count = totalItems, color = palette.onAccentContainer, bg = palette.accentContainer)
                StatPill(label = "Valid", count = validItems, color = palette.onSuccessContainer, bg = palette.successBackground)
                StatPill(label = "Expiring", count = expiringSoonItems, color = palette.onWarningContainer, bg = palette.warningBackground)
                StatPill(label = "Expired", count = expiredItems, color = palette.onErrorContainer, bg = palette.errorBackground)
            }
        }
    }
}

@Composable
private fun StatPill(label: String, count: Int, color: androidx.compose.ui.graphics.Color, bg: androidx.compose.ui.graphics.Color) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bg,
        modifier = Modifier.width(74.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                color = color,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun FamilyMemberRow(
    member: FamilyMember,
    itemCount: Int = 0,
    showDivider: Boolean,
    onClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Family Member", color = MaterialTheme.palette.headingText, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to remove ${member.fullName}? This will also delete ${if (itemCount > 0) "$itemCount item(s)" else "0 items"} associated with this member.", color = MaterialTheme.palette.secondaryText) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteClick()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.palette.errorColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = MaterialTheme.palette.secondaryText)
                }
            },
            containerColor = MaterialTheme.palette.dialogBackground
        )
    }

    val palette = MaterialTheme.palette
    val interactionSource = remember { MutableInteractionSource() }

    Column {
        Row(
            modifier = Modifier
                .clickable(
                    interactionSource = interactionSource,
                    indication = androidx.compose.foundation.LocalIndication.current,
                    onClick = onClick
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            com.docsalert.reminder.ui.components.MemberProfileAvatar(
                photoUrl = member.profilePhotoUrl,
                modifier = Modifier.size(44.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.fullName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = palette.headingText,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${member.relationship} • $itemCount item(s)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = palette.secondaryText,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEditClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = palette.oppositeAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
                if (member.id != "default_user_profile") {
                    IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = palette.errorColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
        
        if (showDivider) {
            androidx.compose.material3.HorizontalDivider(
                modifier = Modifier.padding(start = 72.dp),
                color = palette.dividerColor.copy(alpha = 0.65f)
            )
        }
    }
}
