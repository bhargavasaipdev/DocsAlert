package com.docsalert.reminder.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.docsalert.reminder.ui.theme.palette

@Composable
fun PrivacyPolicyDialog(
    onDismiss: () -> Unit
) {
    val palette = MaterialTheme.palette

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Privacy Policy & Security", color = palette.headingText) },
        text = {
            Column(
                modifier = Modifier
                    .height(300.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "1. Data Collection & Storage",
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.headingText
                )
                Text(
                    text = "DocsAlert respects your privacy. All your document information, dates, and notes are stored locally on your device. We do not collect or transmit your sensitive documents to external servers.\n",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.secondaryText
                )

                Text(
                    text = "2. App Permissions",
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.headingText
                )
                Text(
                    text = "• Notifications: Required to alert you before your documents expire.\n• Biometric Auth: Used strictly on-device to lock the app and safeguard your data.\n",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.secondaryText
                )

                Text(
                    text = "3. Security Toggles",
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.headingText
                )
                Text(
                    text = "You can enable Native App Lock and Number Masking inside Settings to further secure your document metadata from unauthorized access.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.secondaryText
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it", color = palette.oppositeAccent)
            }
        },
        containerColor = palette.dialogBackground
    )
}

@Composable
fun ResetDataDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val palette = MaterialTheme.palette

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = palette.errorColor,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(text = "Reset all data?", color = palette.headingText)
        },
        text = {
            Text(
                text = "This will permanently delete all saved items, custom reminders, profiles, and reset app settings. This action cannot be undone.",
                color = palette.secondaryText
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Reset Data",
                    color = palette.errorColor,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", color = palette.secondaryText)
            }
        },
        containerColor = palette.dialogBackground,
        titleContentColor = palette.headingText,
        textContentColor = palette.secondaryText
    )
}
