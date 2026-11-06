package com.docsalert.reminder.reminder

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.docsalert.reminder.data.model.CustomReminder
import com.docsalert.reminder.ui.components.PlaceholderState
import com.docsalert.reminder.ui.components.ScreenHeader
import com.docsalert.reminder.ui.components.StaggeredEntrance
import com.docsalert.reminder.ui.components.pressScale
import com.docsalert.reminder.ui.theme.palette
import com.docsalert.reminder.ui.viewmodels.CustomReminderViewModel

@Composable
fun ReminderScreen(
    reminderViewModel: CustomReminderViewModel,
    onBackClick: (() -> Unit)? = null,
    onAddReminderClick: () -> Unit = {},
    onEditReminderClick: (Int) -> Unit = {}
) {
    val palette = MaterialTheme.palette
    val context = LocalContext.current
    val reminders by reminderViewModel.reminders.collectAsState()

    val activeCount = remember(reminders) { reminders.count { it.enabled } }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader(
                title = "Daily Reminders",
                subtitle = "Custom scheduled alarms & recurring reminders",
                onBackClick = onBackClick
            )

            if (reminders.isEmpty()) {
                PlaceholderState(
                    icon = Icons.Default.Alarm,
                    title = "No reminders scheduled",
                    message = "Tap + to create custom repeating reminders for tasks, medication, or events."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Summary Pill Banner
                    item {
                        StaggeredEntrance(0) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = palette.accentContainer,
                                border = androidx.compose.foundation.BorderStroke(1.dp, palette.primaryAccent.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(palette.primaryAccent),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Alarm,
                                                contentDescription = null,
                                                tint = palette.onAccentText,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = "Scheduled Alarms",
                                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.ExtraBold),
                                                color = palette.headingText
                                            )
                                            Text(
                                                text = "$activeCount active out of ${reminders.size} total",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = palette.secondaryText
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = palette.oppositeAccent
                                    ) {
                                        Text(
                                            text = "$activeCount ACTIVE",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.ExtraBold),
                                            color = palette.onOppositeAccent,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        StaggeredEntrance(1) {
                            com.docsalert.reminder.ui.components.BannerAdView(modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }

                    items(reminders, key = { it.id }) { reminder ->
                        StaggeredEntrance(2) {
                            ReminderAlarmCard(
                                reminder = reminder,
                                onToggleEnabled = { enabled ->
                                    reminderViewModel.setReminderEnabled(context, reminder.id, enabled)
                                    Toast.makeText(context, if (enabled) "Reminder enabled" else "Reminder disabled", Toast.LENGTH_SHORT).show()
                                },
                                onEditClick = { onEditReminderClick(reminder.id) },
                                onDeleteClick = {
                                    reminderViewModel.deleteReminder(context, reminder.id)
                                    Toast.makeText(context, "Reminder deleted", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = onAddReminderClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = palette.oppositeAccent,
            contentColor = palette.onOppositeAccent,
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Add Daily Reminder", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) }
        )
    }
}

@Composable
private fun ReminderAlarmCard(
    reminder: CustomReminder,
    onToggleEnabled: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val palette = MaterialTheme.palette
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Reminder", color = palette.headingText, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${reminder.title}'?", color = palette.secondaryText) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDeleteClick()
                }) {
                    Text("Delete", color = palette.errorColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = palette.secondaryText)
                }
            },
            containerColor = palette.dialogBackground
        )
    }

    val interactionSource = remember { MutableInteractionSource() }

    Card(
        colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (reminder.enabled) palette.oppositeAccent.copy(alpha = 0.5f) else palette.borderColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interactionSource)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (reminder.enabled) palette.oppositeAccentContainer else palette.inputBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = if (reminder.enabled) palette.onOppositeAccentContainer else palette.secondaryText,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column {
                        Text(
                            text = reminder.formattedTime,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                lineHeight = 30.sp
                            ),
                            color = if (reminder.enabled) palette.headingText else palette.secondaryText
                        )
                        Text(
                            text = reminder.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                            color = if (reminder.enabled) palette.headingText else palette.secondaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Switch(
                    checked = reminder.enabled,
                    onCheckedChange = onToggleEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = palette.onOppositeAccent,
                        checkedTrackColor = palette.oppositeAccent,
                        checkedBorderColor = palette.oppositeAccent,
                        uncheckedThumbColor = palette.cardBackground,
                        uncheckedTrackColor = palette.secondaryCardBackground,
                        uncheckedBorderColor = palette.borderColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (reminder.enabled) palette.accentContainer else palette.secondaryCardBackground
                ) {
                    Text(
                        text = "🗓️ " + reminder.repeatDaysSummary,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                        color = if (reminder.enabled) palette.primaryAccent else palette.secondaryText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        onClick = onEditClick,
                        shape = CircleShape,
                        color = palette.secondaryCardBackground,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = palette.primaryAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Surface(
                        onClick = { showDeleteConfirm = true },
                        shape = CircleShape,
                        color = palette.secondaryCardBackground,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
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

            if (!reminder.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = palette.inputBackground,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = reminder.description,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = palette.bodyText,
                        modifier = Modifier.padding(10.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
