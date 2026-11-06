package com.docsalert.reminder.vault

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.docsalert.reminder.data.model.VaultItem
import com.docsalert.reminder.data.model.VaultItemType
import com.docsalert.reminder.ui.components.PlaceholderState
import com.docsalert.reminder.ui.components.ScreenHeader
import com.docsalert.reminder.ui.components.StaggeredEntrance
import com.docsalert.reminder.ui.components.pressScale
import com.docsalert.reminder.ui.theme.palette
import com.docsalert.reminder.ui.viewmodels.VaultViewModel
import java.io.File

@Composable
fun VaultScreen(
    vaultViewModel: VaultViewModel,
    maskNumbers: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    onAddVaultItemClick: () -> Unit = {},
    onVaultItemClick: (VaultItem) -> Unit = {}
) {
    val palette = MaterialTheme.palette
    val vaultItems by vaultViewModel.vaultItems.collectAsState()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf<VaultItemType?>(null) }

    val filteredItems = remember(vaultItems, searchQuery, selectedTypeFilter) {
        vaultItems.filter { item ->
            val matchesQuery = searchQuery.isBlank() ||
                    item.name.contains(searchQuery, ignoreCase = true) ||
                    item.displayType.contains(searchQuery, ignoreCase = true) ||
                    (item.itemNumber?.contains(searchQuery, ignoreCase = true) == true)
            val matchesType = selectedTypeFilter == null || item.type == selectedTypeFilter
            matchesQuery && matchesType
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader(
                title = "Vault",
                subtitle = "Secure personal credential & file vault",
                onBackClick = onBackClick
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Search Input Field
                if (vaultItems.isNotEmpty()) {
                    item {
                        StaggeredEntrance(0) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Search PAN, Aadhaar, Passport...", color = palette.hintText) },
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = null, tint = palette.primaryAccent)
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotBlank()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = palette.secondaryText)
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = palette.inputBackground,
                                    unfocusedContainerColor = palette.inputBackground,
                                    focusedBorderColor = palette.primaryAccent,
                                    unfocusedBorderColor = palette.borderColor,
                                    focusedTextColor = palette.bodyText,
                                    unfocusedTextColor = palette.bodyText
                                )
                            )
                        }
                    }

                    // Category Filter Row
                    item {
                        StaggeredEntrance(1) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                item {
                                    FilterChip(
                                        selected = selectedTypeFilter == null,
                                        onClick = { selectedTypeFilter = null },
                                        label = { Text("All Vault Items", fontWeight = FontWeight.Bold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = palette.oppositeAccent,
                                            selectedLabelColor = palette.onOppositeAccent
                                        )
                                    )
                                }
                                items(VaultItemType.entries) { type ->
                                    FilterChip(
                                        selected = selectedTypeFilter == type,
                                        onClick = { selectedTypeFilter = if (selectedTypeFilter == type) null else type },
                                        label = { Text(type.title, fontWeight = FontWeight.Bold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = palette.oppositeAccent,
                                            selectedLabelColor = palette.onOppositeAccent
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                if (vaultItems.isEmpty()) {
                    item {
                        StaggeredEntrance(1) {
                            PlaceholderState(
                                icon = Icons.Default.Lock,
                                title = "Vault is empty",
                                message = "Securely store your PAN Card, Aadhaar, License, Passport or PDF files here."
                            )
                        }
                    }
                } else if (filteredItems.isEmpty()) {
                    item {
                        StaggeredEntrance(1) {
                            PlaceholderState(
                                icon = Icons.Default.Search,
                                title = "No matching items",
                                message = "Try searching for another item name or category."
                            )
                        }
                    }
                } else {
                    item {
                        StaggeredEntrance(2) {
                            com.docsalert.reminder.ui.components.BannerAdView(modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }

                    item {
                        StaggeredEntrance(3) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
                                shape = RoundedCornerShape(24.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderColor),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    filteredItems.forEachIndexed { index, item ->
                                        VaultItemRow(
                                            item = item,
                                            maskNumbers = maskNumbers,
                                            showDivider = index < filteredItems.lastIndex,
                                            onClick = { onVaultItemClick(item) }
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
            onClick = onAddVaultItemClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = palette.oppositeAccent,
            contentColor = palette.onOppositeAccent,
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Add Vault Item", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) }
        )
    }
}

@Composable
private fun VaultItemRow(
    item: VaultItem,
    maskNumbers: Boolean = false,
    showDivider: Boolean,
    onClick: () -> Unit
) {
    val palette = MaterialTheme.palette

    val firstImagePath = remember(item.attachmentPaths) {
        item.attachmentPaths.find { path ->
            val p = path.lowercase()
            p.endsWith(".jpg") || p.endsWith(".jpeg") || p.endsWith(".png")
        }
    }

    val thumbnailBitmap = remember(firstImagePath) {
        if (!firstImagePath.isNullOrBlank()) {
            try {
                val file = File(firstImagePath)
                if (file.exists()) android.graphics.BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap() else null
            } catch (e: Exception) {
                null
            }
        } else null
    }

    val formattedNumber = remember(item.itemNumber, maskNumbers) {
        val num = item.itemNumber
        if (num.isNullOrBlank()) null
        else if (maskNumbers) "•••• " + num.takeLast(4)
        else num
    }

    val interactionSource = remember { MutableInteractionSource() }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = interactionSource,
                    indication = androidx.compose.foundation.LocalIndication.current,
                    onClick = onClick
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(palette.accentContainer),
                contentAlignment = Alignment.Center
            ) {
                if (thumbnailBitmap != null) {
                    Image(
                        bitmap = thumbnailBitmap,
                        contentDescription = "Thumbnail",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = item.type.icon,
                        contentDescription = null,
                        tint = palette.primaryAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = palette.headingText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${item.displayType}${if (formattedNumber != null) " • $formattedNumber" else ""}${if (item.attachmentPaths.isNotEmpty()) " • 📎 ${item.attachmentPaths.size} file(s)" else ""}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = palette.secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = palette.secondaryText,
                modifier = Modifier.size(18.dp)
            )
        }
        
        if (showDivider) {
            androidx.compose.material3.HorizontalDivider(
                modifier = Modifier.padding(start = 72.dp),
                color = palette.dividerColor.copy(alpha = 0.65f)
            )
        }
    }
}
