package com.docsalert.reminder.items

import android.app.DatePickerDialog
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.docsalert.reminder.data.model.Item
import com.docsalert.reminder.data.model.ItemCategory
import com.docsalert.reminder.data.model.FamilyMember
import com.docsalert.reminder.ui.components.MemberProfileAvatar
import com.docsalert.reminder.ui.components.StaggeredEntrance
import com.docsalert.reminder.ui.components.pressScale
import com.docsalert.reminder.ui.theme.palette
import com.docsalert.reminder.ui.viewmodels.ItemViewModel
import com.docsalert.reminder.ui.viewmodels.FamilyViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
    itemViewModel: ItemViewModel? = null,
    familyViewModel: FamilyViewModel? = null,
    itemId: String? = null,
    initialCategory: ItemCategory? = null,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val palette = MaterialTheme.palette

    val items by (itemViewModel?.items?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) })
    val familyMembers by (familyViewModel?.members?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) })

    val existingItem = remember(itemId, items) {
        if (itemId != null) items.find { it.id == itemId } else null
    }

    var selectedFamilyMemberId by remember(existingItem) {
        mutableStateOf<String?>(existingItem?.familyMemberId)
    }

    var itemName by remember(existingItem) { mutableStateOf(existingItem?.name ?: "") }
    var selectedCategory by remember(existingItem, initialCategory) {
        mutableStateOf(existingItem?.category ?: initialCategory ?: ItemCategory.IDENTITY)
    }
    var itemNumber by remember(existingItem) { mutableStateOf(existingItem?.itemNumber ?: "") }
    var isExpiringItem by remember(existingItem) {
        mutableStateOf(existingItem?.expiryDate != null || existingItem == null)
    }
    var issueDate by remember(existingItem) { mutableStateOf(existingItem?.issueDate?.toString() ?: "") }
    var expiryDate by remember(existingItem) { mutableStateOf(existingItem?.expiryDate?.toString() ?: "") }
    var remindersEnabled by remember(existingItem) { mutableStateOf(existingItem?.remindersEnabled ?: true) }
    var notes by remember(existingItem) { mutableStateOf(existingItem?.notes ?: "") }

    val allReminderOptions = listOf("30 days before", "15 days before", "7 days before", "3 days before", "1 day before", "On day of expiry")

    fun optionToDays(option: String): Int = when (option) {
        "30 days before" -> 30
        "15 days before" -> 15
        "7 days before" -> 7
        "3 days before" -> 3
        "1 day before" -> 1
        "On day of expiry" -> 0
        else -> 0
    }

    fun daysToOption(days: Int): String = when (days) {
        30 -> "30 days before"
        15 -> "15 days before"
        7 -> "7 days before"
        3 -> "3 days before"
        1 -> "1 day before"
        0 -> "On day of expiry"
        else -> "On day of expiry"
    }

    var selectedReminders by remember(existingItem) {
        val initial = if (existingItem != null && existingItem.reminderDaysBefore.isNotEmpty()) {
            existingItem.reminderDaysBefore.map { daysToOption(it) }.toSet()
        } else {
            setOf("30 days before", "15 days before", "7 days before", "1 day before", "On day of expiry")
        }
        mutableStateOf(initial)
    }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val showDatePicker = { initialDateStr: String, onDatePicked: (String) -> Unit ->
        val cal = Calendar.getInstance()
        if (initialDateStr.isNotBlank()) {
            try {
                val parts = initialDateStr.split("-")
                if (parts.size == 3) {
                    cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                }
            } catch (e: Exception) {
                // Use current date
            }
        }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val dateStr = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                onDatePicked(dateStr)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
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

    val onSaveClick = {
        if (itemName.isBlank()) {
            Toast.makeText(context, "Item Title / Name is required!", Toast.LENGTH_SHORT).show()
        } else if (isExpiringItem && expiryDate.isBlank()) {
            Toast.makeText(context, "Expiry Date is required for expiring items!", Toast.LENGTH_SHORT).show()
        } else {
            val expDate = try {
                if (isExpiringItem && expiryDate.isNotBlank()) LocalDate.parse(expiryDate) else null
            } catch (e: Exception) {
                if (isExpiringItem) {
                    Toast.makeText(context, "Invalid Expiry Date format (YYYY-MM-DD)", Toast.LENGTH_SHORT).show()
                }
                null
            }

            if (!isExpiringItem || expDate != null) {
                val issDate = try {
                    if (issueDate.isNotBlank()) LocalDate.parse(issueDate) else null
                } catch (e: Exception) {
                    null
                }

                val finalOwnerId = selectedFamilyMemberId ?: "default_user_profile"
                val reminderDaysList = selectedReminders.map { optionToDays(it) }.distinct()

                val newItem = Item(
                    id = existingItem?.id ?: java.util.UUID.randomUUID().toString(),
                    name = itemName.trim(),
                    category = selectedCategory,
                    itemNumber = itemNumber.ifBlank { null },
                    issueDate = issDate,
                    expiryDate = expDate,
                    remindersEnabled = if (isExpiringItem) remindersEnabled else false,
                    reminderDaysBefore = reminderDaysList,
                    notes = notes.trim(),
                    familyMemberId = finalOwnerId
                )

                scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    if (existingItem != null) {
                        itemViewModel?.updateItemSync(newItem)
                    } else {
                        itemViewModel?.addItemSync(newItem)
                    }

                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            if (existingItem != null) "Item updated successfully!" else "Item saved successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        onBackClick()
                    }
                }
            }
        }
    }

    if (showDeleteConfirmDialog && existingItem != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Item") },
            text = { Text("Are you sure you want to delete '${existingItem.name}'? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        itemViewModel?.removeItem(existingItem.id)
                        Toast.makeText(context, "Item deleted", Toast.LENGTH_SHORT).show()
                        onBackClick()
                    }
                ) {
                    Text("Delete", color = palette.errorColor)
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
                title = if (existingItem != null) "Edit Item" else "Add New Item",
                subtitle = "Manage item details",
                onBackClick = onBackClick,
                actionIcon = if (existingItem != null) Icons.Default.Delete else null,
                onActionClick = { showDeleteConfirmDialog = true },
                actionIconTint = palette.errorColor
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

            item {
                StaggeredEntrance(0) {
                    val selectedMember = familyMembers.find { it.id == selectedFamilyMemberId }
                    var memberDropdownExpanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = memberDropdownExpanded,
                        onExpandedChange = { memberDropdownExpanded = !memberDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedMember?.fullName ?: "Self / Default Profile",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Assign to Member") },
                            leadingIcon = {
                                MemberProfileAvatar(photoUrl = selectedMember?.profilePhotoUrl, modifier = Modifier.size(24.dp))
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = memberDropdownExpanded) },
                            shape = inputShape,
                            colors = textFieldColors,
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = memberDropdownExpanded,
                            onDismissRequest = { memberDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Self / Default Profile", fontWeight = FontWeight.Bold) },
                                leadingIcon = { MemberProfileAvatar(photoUrl = null, modifier = Modifier.size(24.dp)) },
                                onClick = {
                                    selectedFamilyMemberId = null
                                    memberDropdownExpanded = false
                                }
                            )
                            familyMembers.forEach { member ->
                                DropdownMenuItem(
                                    text = { Text(member.fullName) },
                                    leadingIcon = { MemberProfileAvatar(photoUrl = member.profilePhotoUrl, modifier = Modifier.size(24.dp)) },
                                    onClick = {
                                        selectedFamilyMemberId = member.id
                                        memberDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                StaggeredEntrance(1) {
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        label = { Text("Item Title / Name *") },
                        placeholder = { Text("e.g. Passport, Driving License, Car Insurance") },
                        leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = palette.secondaryAccent) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = inputShape,
                        colors = textFieldColors
                    )
                }
            }

            item {
                StaggeredEntrance(2) {
                    var categoryExpanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = !categoryExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedCategory.title,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category *") },
                            leadingIcon = { Icon(selectedCategory.icon, contentDescription = null, tint = palette.secondaryAccent) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            shape = inputShape,
                            colors = textFieldColors,
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            ItemCategory.entries.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.title) },
                                    leadingIcon = { Icon(category.icon, contentDescription = null, tint = palette.secondaryAccent) },
                                    onClick = {
                                        selectedCategory = category
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                StaggeredEntrance(3) {
                    OutlinedTextField(
                        value = itemNumber,
                        onValueChange = { itemNumber = it },
                        label = { Text("Item / Policy Number (Optional)") },
                        placeholder = { Text("e.g. Z1234567 or KA-01-2023") },
                        leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null, tint = palette.secondaryAccent) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = inputShape,
                        colors = textFieldColors
                    )
                }
            }

            item {
                StaggeredEntrance(4) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(text = "Item Type", style = MaterialTheme.typography.labelLarge, color = palette.secondaryText)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(50))
                                .background(palette.secondaryCardBackground)
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Button(
                                onClick = { isExpiringItem = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isExpiringItem) palette.tertiaryAccent else androidx.compose.ui.graphics.Color.Transparent,
                                    contentColor = if (isExpiringItem) palette.onTertiaryAccent else palette.secondaryText
                                )
                            ) {
                                Text("Expiring Item", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { isExpiringItem = false },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!isExpiringItem) palette.tertiaryAccent else androidx.compose.ui.graphics.Color.Transparent,
                                    contentColor = if (!isExpiringItem) palette.onTertiaryAccent else palette.secondaryText
                                )
                            ) {
                                Text("No Expiry Date", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                StaggeredEntrance(5) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = issueDate,
                            onValueChange = { issueDate = it },
                            label = { Text("Issue Date") },
                            placeholder = { Text("YYYY-MM-DD") },
                            leadingIcon = { Icon(Icons.Default.Event, contentDescription = null, tint = palette.secondaryAccent) },
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker(issueDate) { issueDate = it } }) {
                                    Icon(Icons.Default.DateRange, contentDescription = "Pick Date")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = inputShape,
                            colors = textFieldColors
                        )

                        if (isExpiringItem) {
                            OutlinedTextField(
                                value = expiryDate,
                                onValueChange = { expiryDate = it },
                                label = { Text("Expiry Date *") },
                                placeholder = { Text("YYYY-MM-DD") },
                                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = palette.secondaryAccent) },
                                trailingIcon = {
                                    IconButton(onClick = { showDatePicker(expiryDate) { expiryDate = it } }) {
                                        Icon(Icons.Default.DateRange, contentDescription = "Pick Date")
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = inputShape,
                                colors = textFieldColors
                            )
                        }
                    }
                }
            }

            if (isExpiringItem) {
                item {
                    StaggeredEntrance(6) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
                            shape = inputShape,
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, if (remindersEnabled) palette.tertiaryAccent else palette.borderColor)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(if (remindersEnabled) palette.accentContainer else palette.inputBackground),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Event,
                                                contentDescription = null,
                                                tint = if (remindersEnabled) palette.tertiaryAccent else palette.secondaryText
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = "Enable Reminders",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = palette.headingText
                                            )
                                            Text(
                                                text = "Get notified before expiry",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = palette.secondaryText
                                            )
                                        }
                                    }

                                    Switch(
                                        checked = remindersEnabled,
                                        onCheckedChange = { remindersEnabled = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = palette.onAccentText,
                                            checkedTrackColor = palette.tertiaryAccent,
                                            uncheckedThumbColor = palette.secondaryText,
                                            uncheckedTrackColor = palette.inputBackground,
                                            uncheckedBorderColor = palette.borderColor
                                        )
                                    )
                                }

                                if (remindersEnabled) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = palette.borderColor.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "Notify Me",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = palette.tertiaryAccent
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    allReminderOptions.forEach { option ->
                                        val isChecked = selectedReminders.contains(option)
                                        Card(
                                            onClick = {
                                                selectedReminders = if (isChecked) {
                                                    selectedReminders - option
                                                } else {
                                                    selectedReminders + option
                                                }
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isChecked) palette.accentContainer else palette.inputBackground
                                            ),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isChecked) palette.tertiaryAccent else palette.borderColor),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(
                                                    checked = isChecked,
                                                    onCheckedChange = {
                                                        selectedReminders = if (it) {
                                                            selectedReminders + option
                                                        } else {
                                                            selectedReminders - option
                                                        }
                                                    },
                                                    colors = CheckboxDefaults.colors(
                                                        checkedColor = palette.tertiaryAccent,
                                                        checkmarkColor = palette.onAccentText
                                                    )
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = option,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Medium
                                                    ),
                                                    color = if (isChecked) palette.tertiaryAccent else palette.bodyText
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                StaggeredEntrance(7) {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes & Additional Details (Optional)") },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null, tint = palette.secondaryAccent) },
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
                StaggeredEntrance(8) {
                    val saveInteraction = remember { MutableInteractionSource() }

                    Button(
                        onClick = onSaveClick,
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
                            imageVector = if (existingItem != null) Icons.Default.Check else Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (existingItem != null) "Update Item" else "Save Item",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
