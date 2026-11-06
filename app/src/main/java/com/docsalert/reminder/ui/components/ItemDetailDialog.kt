package com.docsalert.reminder.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.docsalert.reminder.data.model.Item
import com.docsalert.reminder.data.model.formattedNumber
import com.docsalert.reminder.ui.theme.palette
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun ItemDetailDialog(
    item: Item,
    familyMemberName: String? = null,
    maskNumbersDefault: Boolean = false,
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val palette = MaterialTheme.palette
    var numberMasked by remember { mutableStateOf(maskNumbersDefault) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val today = remember { LocalDate.now() }
    val daysLeft = item.expiryDate?.let { ChronoUnit.DAYS.between(today, it) }

    val statusText = when {
        item.expiryDate == null -> "Never Expires"
        daysLeft != null && daysLeft < 0 -> "Expired"
        daysLeft != null && daysLeft <= 30 -> "Expiring Soon"
        else -> "Valid"
    }

    val statusColor = when {
        item.expiryDate == null -> palette.onInfoContainer
        daysLeft != null && daysLeft < 0 -> palette.onErrorContainer
        daysLeft != null && daysLeft <= 30 -> palette.onWarningContainer
        else -> palette.onSuccessContainer
    }

    val statusBg = when {
        item.expiryDate == null -> palette.infoBackground
        daysLeft != null && daysLeft < 0 -> palette.errorBackground
        daysLeft != null && daysLeft <= 30 -> palette.warningBackground
        else -> palette.successBackground
    }

    val formattedNum = item.formattedNumber(numberMasked)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
            border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderColor)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(palette.accentContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.category.icon,
                            contentDescription = null,
                            tint = palette.primaryAccent,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(statusBg)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = statusColor
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = palette.secondaryText)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = item.name,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = palette.headingText
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = palette.dividerColor)
                Spacer(modifier = Modifier.height(16.dp))

                DetailRow(label = "Category", value = item.category.title)
                
                if (!familyMemberName.isNullOrBlank()) {
                    DetailRow(label = "Family Member", value = familyMemberName)
                }

                if (!item.itemNumber.isNullOrBlank()) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Item Number", style = MaterialTheme.typography.labelMedium, color = palette.secondaryText)
                            Text(
                                text = formattedNum ?: "••••",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = palette.bodyText
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Item Number", item.itemNumber)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Item number copied to clipboard", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Item Number",
                                    tint = palette.primaryAccent
                                )
                            }
                            IconButton(onClick = { numberMasked = !numberMasked }) {
                                Icon(
                                    imageVector = if (numberMasked) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Reveal",
                                    tint = palette.primaryAccent
                                )
                            }
                        }
                    }
                }

                item.issueDate?.let { date ->
                    DetailRow(
                        label = "Issue Date",
                        value = date.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
                    )
                }

                item.expiryDate?.let { date ->
                    DetailRow(
                        label = "Expiry Date",
                        value = date.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
                    )
                } ?: DetailRow(label = "Expiry Status", value = "Non-Expiring Item")

                if (item.remindersEnabled) {
                    DetailRow(label = "Reminders", value = "Enabled")
                }

                if (item.notes.isNotBlank()) {
                    DetailRow(label = "Notes", value = item.notes)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = palette.errorColor)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete")
                    }

                    Button(
                        onClick = onEditClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.primaryAccent,
                            contentColor = palette.onAccentText
                        )
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit")
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Item") },
            text = { Text("Are you sure you want to delete '${item.name}'? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteClick()
                    }
                ) {
                    Text("Delete", color = palette.errorColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    val palette = MaterialTheme.palette
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = palette.secondaryText)
        Text(text = value, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), color = palette.bodyText)
    }
}
