package com.docsalert.reminder.vault

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.sp
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.docsalert.reminder.data.model.VaultItem
import com.docsalert.reminder.data.model.VaultItemType
import com.docsalert.reminder.ui.components.StaggeredEntrance
import com.docsalert.reminder.ui.components.pressScale
import com.docsalert.reminder.ui.theme.palette
import com.docsalert.reminder.ui.viewmodels.VaultViewModel
import com.docsalert.reminder.util.VaultFileHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVaultItemScreen(
    vaultViewModel: VaultViewModel,
    itemId: String? = null,
    onBackClick: () -> Unit
) {
    val palette = MaterialTheme.palette
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val vaultItems by vaultViewModel.vaultItems.collectAsState()
    val existingItem = remember(itemId, vaultItems) {
        vaultItems.find { it.id == itemId }
    }

    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(VaultItemType.PAN_CARD) }
    var customType by remember { mutableStateOf("") }
    var itemNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val attachedFilePaths = remember { mutableStateListOf<String>() }

    var typeExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(existingItem) {
        existingItem?.let { doc ->
            name = doc.name
            selectedType = doc.type
            customType = doc.customType ?: ""
            itemNumber = doc.itemNumber ?: ""
            expiryDate = doc.expiryDate?.toString() ?: ""
            notes = doc.notes
            attachedFilePaths.clear()
            attachedFilePaths.addAll(doc.attachmentPaths)
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEach { uri ->
            val savedPath = VaultFileHelper.saveUriToPrivateStorage(context, uri)
            if (savedPath != null) {
                attachedFilePaths.add(savedPath)
            }
        }
        if (uris.isNotEmpty()) {
            Toast.makeText(context, "${uris.size} image(s) attached", Toast.LENGTH_SHORT).show()
        }
    }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val savedPath = VaultFileHelper.saveUriToPrivateStorage(context, uri)
            if (savedPath != null) {
                attachedFilePaths.add(savedPath)
                Toast.makeText(context, "PDF attached!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val showDatePicker = { onDatePicked: (String) -> Unit ->
        val cal = Calendar.getInstance()
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

    if (showDeleteConfirmDialog && existingItem != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Vault Item") },
            text = { Text("Are you sure you want to delete '${existingItem.name}'? All attached files will also be permanently removed.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        vaultViewModel.deleteVaultItem(existingItem)
                        Toast.makeText(context, "Vault item deleted", Toast.LENGTH_SHORT).show()
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
                title = if (existingItem != null) "Edit Vault Item" else "Add Vault Item",
                subtitle = "Secure document storage",
                onBackClick = onBackClick,
                actionIcon = if (existingItem != null) Icons.Default.Delete else null,
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

            item {
                StaggeredEntrance(0) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { if (it.length <= 30) name = it },
                        label = { Text("Item Name *") },
                        supportingText = {
                            Text(
                                text = "${name.length}/30",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.secondaryText,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )
                        },
                        leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = palette.primaryAccent) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = inputShape,
                        colors = textFieldColors
                    )
                }
            }

            item {
                StaggeredEntrance(1) {
                    ExposedDropdownMenuBox(
                        expanded = typeExpanded,
                        onExpandedChange = { typeExpanded = !typeExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedType.title,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Item Type *") },
                            leadingIcon = { Icon(selectedType.icon, contentDescription = null, tint = palette.secondaryAccent) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                            shape = inputShape,
                            colors = textFieldColors,
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = typeExpanded,
                            onDismissRequest = { typeExpanded = false }
                        ) {
                            VaultItemType.entries.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.title) },
                                    leadingIcon = { Icon(type.icon, contentDescription = null, tint = palette.secondaryAccent) },
                                    onClick = {
                                        selectedType = type
                                        typeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (selectedType == VaultItemType.OTHER) {
                item {
                    StaggeredEntrance(2) {
                        OutlinedTextField(
                            value = customType,
                            onValueChange = { customType = it },
                            label = { Text("Specify Custom Item Type *") },
                            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = palette.secondaryAccent) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = inputShape,
                            colors = textFieldColors
                        )
                    }
                }
            }

            item {
                StaggeredEntrance(3) {
                    OutlinedTextField(
                        value = itemNumber,
                        onValueChange = { if (it.length <= 25) itemNumber = it },
                        label = { Text("Item Number (Optional)") },
                        supportingText = {
                            Text(
                                text = "${itemNumber.length}/25",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.secondaryText,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )
                        },
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
                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = { expiryDate = it },
                        label = { Text("Expiry Date (Optional)") },
                        leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = palette.secondaryAccent) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker { expiryDate = it } },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker { expiryDate = it } }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                            }
                        },
                        readOnly = true,
                        shape = inputShape,
                        colors = textFieldColors
                    )
                }
            }

            item {
                StaggeredEntrance(5) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
                        shape = RoundedCornerShape(22.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Attached Files & Images",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = palette.headingText
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { imagePickerLauncher.launch("image/*") },
                                    modifier = Modifier.weight(1f),
                                    shape = inputShape,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = palette.oppositeAccent)
                                ) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add Photos")
                                }

                                OutlinedButton(
                                    onClick = { pdfPickerLauncher.launch("application/pdf") },
                                    modifier = Modifier.weight(1f),
                                    shape = inputShape,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = palette.oppositeAccent)
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add PDF")
                                }
                            }

                            if (attachedFilePaths.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(14.dp))

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(attachedFilePaths, key = { it }) { filePath ->
                                        val isPdf = filePath.lowercase().endsWith(".pdf")
                                        val file = remember(filePath) { File(filePath) }
                                        val bitmap = remember(filePath) {
                                            if (!isPdf && file.exists()) {
                                                try {
                                                    android.graphics.BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                                                } catch (e: Exception) {
                                                    null
                                                }
                                            } else null
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(80.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(palette.inputBackground)
                                                .border(1.dp, palette.borderColor, RoundedCornerShape(14.dp))
                                        ) {
                                            if (bitmap != null) {
                                                Image(
                                                    bitmap = bitmap,
                                                    contentDescription = "Attachment",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else {
                                                Column(
                                                    modifier = Modifier.fillMaxSize(),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Icon(
                                                        imageVector = if (isPdf) Icons.Default.PictureAsPdf else Icons.Default.Description,
                                                        contentDescription = null,
                                                        tint = palette.primaryAccent,
                                                        modifier = Modifier.size(32.dp)
                                                    )
                                                    Text(
                                                        text = if (isPdf) "PDF" else "FILE",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                        color = palette.secondaryText
                                                    )
                                                }
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(4.dp)
                                                    .size(22.dp)
                                                    .clip(CircleShape)
                                                    .background(palette.errorColor)
                                                    .clickable { attachedFilePaths.remove(filePath) },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove Attachment",
                                                    tint = palette.onAccentText,
                                                    modifier = Modifier.size(14.dp)
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
                StaggeredEntrance(6) {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { if (it.length <= 200) notes = it },
                        label = { Text("Notes (Optional)") },
                        supportingText = {
                            Text(
                                text = "${notes.length}/200",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.secondaryText,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )
                        },
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

            item {
                StaggeredEntrance(7) {
                    val saveInteraction = remember { MutableInteractionSource() }

                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                Toast.makeText(context, "Item Name is required!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            if (selectedType == VaultItemType.OTHER && customType.isBlank()) {
                                Toast.makeText(context, "Please specify custom Item type!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val expDate = try {
                                if (expiryDate.isNotBlank()) LocalDate.parse(expiryDate) else null
                            } catch (e: Exception) {
                                Toast.makeText(context, "Invalid Expiry Date format (YYYY-MM-DD)", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val vaultItem = VaultItem(
                                id = existingItem?.id ?: java.util.UUID.randomUUID().toString(),
                                name = name.trim(),
                                type = selectedType,
                                customType = if (selectedType == VaultItemType.OTHER) customType.trim() else null,
                                itemNumber = itemNumber.ifBlank { null },
                                expiryDate = expDate,
                                notes = notes.trim(),
                                attachmentPaths = attachedFilePaths.toList()
                            )

                            scope.launch(Dispatchers.IO) {
                                vaultViewModel.saveVaultItem(vaultItem)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        context,
                                        if (existingItem != null) "Vault item updated!" else "Vault item saved!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    onBackClick()
                                }
                            }
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
                            imageVector = if (existingItem != null) Icons.Default.Check else Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (existingItem != null) "Update Vault Item" else "Save Vault Item",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
