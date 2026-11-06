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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.docsalert.reminder.data.model.CustomReminder
import com.docsalert.reminder.ui.components.StaggeredEntrance
import com.docsalert.reminder.ui.components.pressScale
import com.docsalert.reminder.ui.theme.palette
import com.docsalert.reminder.ui.viewmodels.CustomReminderViewModel
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderScreen(
    reminderViewModel: CustomReminderViewModel,
    reminderId: Int? = null,
    onBackClick: () -> Unit
) {
    val palette = MaterialTheme.palette
    val context = LocalContext.current

    val reminders by reminderViewModel.reminders.collectAsState()
    val existingReminder = remember(reminderId, reminders) {
        if (reminderId != null && reminderId != 0) reminders.find { it.id == reminderId } else null
    }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var hour by remember { mutableStateOf(9) }
    var minute by remember { mutableStateOf(0) }
    val selectedDays = remember { mutableStateListOf<Int>() } // 1=Mon..7=Sun

    var showTimePickerDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(existingReminder) {
        if (existingReminder != null) {
            title = existingReminder.title
            description = existingReminder.description ?: ""
            hour = existingReminder.hour
            minute = existingReminder.minute
            selectedDays.clear()
            selectedDays.addAll(existingReminder.repeatDays)
        } else {
            selectedDays.clear()
            selectedDays.addAll(listOf(1, 2, 3, 4, 5, 6, 7))
        }
    }

    val formattedTimeText = remember(hour, minute) {
        val h = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        val amPm = if (hour >= 12) "PM" else "AM"
        String.format(Locale.getDefault(), "%d:%02d %s", h, minute, amPm)
    }

    val inputShape = RoundedCornerShape(16.dp)
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = palette.bodyText,
        unfocusedTextColor = palette.bodyText,
        focusedContainerColor = palette.inputBackground,
        unfocusedContainerColor = palette.inputBackground,
        focusedBorderColor = palette.focusedBorderColor,
        unfocusedBorderColor = palette.inputBorderColor,
        cursorColor = palette.primaryAccent,
        focusedLabelColor = palette.primaryAccent,
        unfocusedLabelColor = palette.secondaryText
    )

    if (showTimePickerDialog) {
        val timePickerState = rememberTimePickerState(initialHour = hour, initialMinute = minute, is24Hour = false)
        AlertDialog(
            onDismissRequest = { showTimePickerDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    hour = timePickerState.hour
                    minute = timePickerState.minute
                    showTimePickerDialog = false
                }) {
                    Text("OK", color = palette.oppositeAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePickerDialog = false }) {
                    Text("Cancel", color = palette.secondaryText)
                }
            },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            },
            containerColor = palette.dialogBackground
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Reminder") },
            text = { Text("Are you sure you want to delete this reminder?") },
            confirmButton = {
                TextButton(onClick = {
                    if (existingReminder != null) {
                        reminderViewModel.deleteReminder(context, existingReminder.id)
                    }
                    showDeleteConfirmDialog = false
                    onBackClick()
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        containerColor = palette.screenBackground,
        topBar = {
            com.docsalert.reminder.ui.components.ScreenHeader(
                title = if (existingReminder != null) "Edit Reminder" else "Add Daily Reminder",
                subtitle = "Manage alarm schedule & days",
                onBackClick = onBackClick,
                actionIcon = if (existingReminder != null) Icons.Default.Delete else null,
                actionIconTint = palette.errorColor,
                onActionClick = { showDeleteConfirmDialog = true }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
        ) {
            item(key = "top_ad_banner") {
                com.docsalert.reminder.ui.components.BannerAdView(modifier = Modifier.padding(bottom = 4.dp))
            }

            // Title (Max 30 Chars)
            item {
                StaggeredEntrance(0) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { if (it.length <= 30) title = it },
                        label = { Text("Reminder Title *") },
                        supportingText = {
                            Text(
                                text = "${title.length}/30",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.secondaryText,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )
                        },
                        leadingIcon = { Icon(Icons.Default.Alarm, contentDescription = null, tint = palette.secondaryAccent) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = inputShape,
                        colors = textFieldColors
                    )
                }
            }

            // Time Selector Card
            item {
                StaggeredEntrance(1) {
                    Card(
                        onClick = { showTimePickerDialog = true },
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
                        border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(palette.accentContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = palette.primaryAccent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Reminder Time *",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = palette.secondaryText
                                    )
                                    Text(
                                        text = formattedTimeText,
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                        color = palette.headingText
                                    )
                                }
                            }

                            TextButton(onClick = { showTimePickerDialog = true }) {
                                Text("Change", color = palette.oppositeAccent, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Repeat Days Selector
            item {
                StaggeredEntrance(2) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
                        shape = RoundedCornerShape(22.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.EventRepeat, contentDescription = null, tint = palette.oppositeAccent, modifier = Modifier.size(20.dp))
                                    Text("Repeat Days *", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = palette.headingText)
                                }

                                FilterChip(
                                    selected = selectedDays.size == 7,
                                    onClick = {
                                        if (selectedDays.size == 7) {
                                            selectedDays.clear()
                                        } else {
                                            selectedDays.clear()
                                            selectedDays.addAll(listOf(1, 2, 3, 4, 5, 6, 7))
                                        }
                                    },
                                    label = { Text("Everyday", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = palette.tertiaryAccent,
                                        selectedLabelColor = palette.onTertiaryAccent
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Day Chips Row (Mon .. Sun)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                (1..7).forEach { dayInt ->
                                    val isSelected = selectedDays.contains(dayInt)
                                    val dayName = DayOfWeek.of(dayInt).getDisplayName(TextStyle.NARROW, Locale.getDefault())

                                    Card(
                                        onClick = {
                                            if (isSelected) selectedDays.remove(dayInt)
                                            else selectedDays.add(dayInt)
                                        },
                                        shape = CircleShape,
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) palette.tertiaryAccent else palette.inputBackground
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) palette.tertiaryAccent else palette.borderColor),
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Text(
                                                text = dayName,
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (isSelected) palette.onAccentText else palette.bodyText
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Description (Max 200 Chars)
            item {
                StaggeredEntrance(3) {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { if (it.length <= 200) description = it },
                        label = { Text("Description (Optional)") },
                        supportingText = {
                            Text(
                                text = "${description.length}/200",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.secondaryText,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )
                        },
                        leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = palette.secondaryAccent) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        maxLines = 4,
                        shape = inputShape,
                        colors = textFieldColors
                    )
                }
            }

            // Bottom Save / Update Button
            item {
                StaggeredEntrance(4) {
                    val saveInteraction = remember { MutableInteractionSource() }

                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                Toast.makeText(context, "Reminder Title is required!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            if (selectedDays.isEmpty()) {
                                Toast.makeText(context, "Please select at least one repeat day!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val reminderToSave = CustomReminder(
                                id = existingReminder?.id ?: 0,
                                title = title.trim(),
                                description = description.ifBlank { null },
                                hour = hour,
                                minute = minute,
                                repeatDays = selectedDays.toList(),
                                enabled = existingReminder?.enabled ?: true
                            )

                            reminderViewModel.saveReminder(context, reminderToSave)
                            Toast.makeText(context, if (existingReminder != null) "Daily reminder updated!" else "Daily reminder saved!", Toast.LENGTH_SHORT).show()
                            onBackClick()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .pressScale(saveInteraction),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.oppositeAccent,
                            contentColor = palette.onOppositeAccent
                        )
                    ) {
                        Icon(
                            imageVector = if (existingReminder != null) Icons.Default.Check else Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (existingReminder != null) "Update Daily Reminder" else "Save Daily Reminder",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
