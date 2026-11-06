package com.docsalert.reminder.items

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.docsalert.reminder.data.model.FamilyMember
import com.docsalert.reminder.data.model.Item
import com.docsalert.reminder.data.model.ItemCategory
import com.docsalert.reminder.data.model.formattedNumber
import com.docsalert.reminder.ui.components.MemberProfileAvatar
import com.docsalert.reminder.ui.components.ScreenHeader
import com.docsalert.reminder.ui.components.StaggeredEntrance
import com.docsalert.reminder.ui.components.pressScale
import com.docsalert.reminder.ui.theme.palette
import com.docsalert.reminder.ui.viewmodels.FamilyViewModel
import com.docsalert.reminder.ui.viewmodels.ItemViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private const val EXPIRING_SOON_DAYS = 30L

internal enum class ItemStatus(val label: String) {
    VALID("Valid"),
    EXPIRING_SOON("Expiring soon"),
    EXPIRED("Expired")
}

private enum class StatusFilter(val label: String, val status: ItemStatus?) {
    ALL("All", null),
    VALID("Valid", ItemStatus.VALID),
    EXPIRING("Expiring soon", ItemStatus.EXPIRING_SOON),
    EXPIRED("Expired", ItemStatus.EXPIRED)
}

private enum class ItemsSortOption(val label: String) {
    EXPIRY_DATE("Expiry Date"),
    NAME_AZ("Name A–Z"),
    RECENTLY_ADDED("Recently Added"),
    OLDEST_FIRST("Oldest First")
}

private enum class LayoutStyle { LIST, GRID }

enum class ItemsViewMode { LIST, CATEGORY }

private enum class ListState { NO_ITEMS, NO_MATCHES, RESULTS }

internal fun Item.toStatus(today: LocalDate): ItemStatus {
    val daysLeft = expiryDate?.let { ChronoUnit.DAYS.between(today, it) }
    return when {
        daysLeft == null -> ItemStatus.VALID
        daysLeft < 0 -> ItemStatus.EXPIRED
        daysLeft <= EXPIRING_SOON_DAYS -> ItemStatus.EXPIRING_SOON
        else -> ItemStatus.VALID
    }
}

private fun Item.daysLeft(today: LocalDate): Long? {
    return expiryDate?.let { ChronoUnit.DAYS.between(today, it) }
}

private val expiryFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")

internal fun Item.daysLeftText(today: LocalDate): String? {
    val days = daysLeft(today) ?: return null
    return when {
        days < -1 -> "${-days} days ago"
        days == -1L -> "1 day ago"
        days == 0L -> "Today"
        days == 1L -> "1 day left"
        else -> "$days days left"
    }
}

@Immutable
internal data class ColorPair(val content: Color, val container: Color)

@Composable
internal fun ItemStatus.colors(): ColorPair {
    val p = MaterialTheme.palette
    return when (this) {
        ItemStatus.VALID -> ColorPair(p.onSuccessContainer, p.successBackground)
        ItemStatus.EXPIRING_SOON -> ColorPair(p.onWarningContainer, p.warningBackground)
        ItemStatus.EXPIRED -> ColorPair(p.onErrorContainer, p.errorBackground)
    }
}

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

@Composable
fun ItemsScreen(
    itemViewModel: ItemViewModel,
    familyViewModel: FamilyViewModel? = null,
    initialStatusFilter: String? = null,
    initialCategoryFilter: ItemCategory? = null,
    maskNumbers: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    onAddItemClick: () -> Unit = {},
    onItemClick: (String) -> Unit = {}
) {
    val palette = MaterialTheme.palette
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable {
        mutableStateOf(
            when (initialStatusFilter) {
                "VALID" -> StatusFilter.VALID
                "EXPIRING" -> StatusFilter.EXPIRING
                "EXPIRED" -> StatusFilter.EXPIRED
                else -> StatusFilter.ALL
            }
        )
    }
    var selectedCategoryFilter by remember { mutableStateOf<ItemCategory?>(initialCategoryFilter) }
    var selectedFamilyMemberId by remember { mutableStateOf<String?>(null) }
    var selectedSort by remember { mutableStateOf(ItemsSortOption.EXPIRY_DATE) }
    var viewMode by remember { mutableStateOf(ItemsViewMode.LIST) }
    var layoutStyle by rememberSaveable { mutableStateOf(LayoutStyle.LIST) }

    val itemsList by itemViewModel.items.collectAsState()
    val familyMembers by familyViewModel?.members?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) }
    val today = remember { LocalDate.now() }

    LaunchedEffect(initialStatusFilter, initialCategoryFilter) {
        if (initialStatusFilter == null && initialCategoryFilter == null) {
            searchQuery = ""
            selectedFilter = StatusFilter.ALL
            selectedCategoryFilter = null
            selectedFamilyMemberId = null
        }
    }

    val familyFiltered = remember(itemsList, selectedFamilyMemberId) {
        if (selectedFamilyMemberId == null) itemsList
        else itemsList.filter { it.familyMemberId == selectedFamilyMemberId }
    }

    val categoryFiltered = remember(familyFiltered, selectedCategoryFilter) {
        if (selectedCategoryFilter == null) familyFiltered
        else familyFiltered.filter { it.category == selectedCategoryFilter }
    }

    val searched = remember(categoryFiltered, searchQuery) {
        val query = searchQuery.trim()
        if (query.isEmpty()) {
            categoryFiltered
        } else {
            categoryFiltered.filter { item ->
                item.name.contains(query, ignoreCase = true) ||
                        item.category.title.contains(query, ignoreCase = true) ||
                        (item.itemNumber?.contains(query, ignoreCase = true) == true) ||
                        item.fileType.contains(query, ignoreCase = true)
            }
        }
    }

    val visible = remember(searched, selectedFilter, today) {
        val wanted = selectedFilter.status
        if (wanted == null) searched else searched.filter { it.toStatus(today) == wanted }
    }

    val sortedItems = remember(visible, selectedSort, today) {
        when (selectedSort) {
            ItemsSortOption.EXPIRY_DATE -> visible.sortedBy { it.daysLeft(today) ?: Long.MAX_VALUE }
            ItemsSortOption.NAME_AZ -> visible.sortedBy { it.name.lowercase() }
            ItemsSortOption.RECENTLY_ADDED -> visible.reversed()
            ItemsSortOption.OLDEST_FIRST -> visible
        }
    }

    val listState = when {
        itemsList.isEmpty() -> ListState.NO_ITEMS
        sortedItems.isEmpty() -> ListState.NO_MATCHES
        else -> ListState.RESULTS
    }

    val isFiltering = searchQuery.isNotBlank() || selectedFilter != StatusFilter.ALL || selectedCategoryFilter != null || selectedFamilyMemberId != null

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ScreenHeader(
                    title = "My Items",
                    subtitle = "Saved Items",
                    onBackClick = onBackClick,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .clip(RoundedCornerShape(50))
                        .background(palette.secondaryCardBackground)
                        .padding(4.dp)
                ) {
                    ViewModeButton(
                        icon = Icons.AutoMirrored.Filled.List,
                        label = "All Items",
                        isSelected = viewMode == ItemsViewMode.LIST,
                        onClick = { viewMode = ItemsViewMode.LIST }
                    )
                    ViewModeButton(
                        icon = Icons.Default.Category,
                        label = "Grouped",
                        isSelected = viewMode == ItemsViewMode.CATEGORY,
                        onClick = { viewMode = ItemsViewMode.CATEGORY }
                    )
                }
            }

            StaggeredEntrance(0) {
                SearchField(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                )
            }

            val scrollableFilterSection = @Composable {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                ) {
                    StaggeredEntrance(1) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(items = StatusFilter.entries, key = { it.name }) { filter ->
                                val count = if (filter.status == null) {
                                    searched.size
                                } else {
                                    searched.count { it.toStatus(today) == filter.status }
                                }
                                FilterPill(
                                    filter = filter,
                                    count = count,
                                    selected = filter == selectedFilter,
                                    onClick = { selectedFilter = filter }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    StaggeredEntrance(2) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            item {
                                var catMenuExpanded by remember { mutableStateOf(false) }
                                Box {
                                    Surface(
                                        onClick = { catMenuExpanded = true },
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (selectedCategoryFilter != null) palette.oppositeAccent else palette.inputBackground,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedCategoryFilter != null) palette.oppositeAccent else palette.borderColor)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = selectedCategoryFilter?.icon ?: Icons.Default.Category,
                                                contentDescription = null,
                                                tint = if (selectedCategoryFilter != null) palette.onOppositeAccent else palette.secondaryText,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = selectedCategoryFilter?.title ?: "Category",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = if (selectedCategoryFilter != null) palette.onOppositeAccent else palette.bodyText
                                            )
                                        }
                                    }
                                    DropdownMenu(
                                        expanded = catMenuExpanded,
                                        onDismissRequest = { catMenuExpanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("All Categories") },
                                            onClick = {
                                                selectedCategoryFilter = null
                                                catMenuExpanded = false
                                            }
                                        )
                                        ItemCategory.entries.forEach { cat ->
                                            DropdownMenuItem(
                                                text = { Text(cat.title) },
                                                onClick = {
                                                    selectedCategoryFilter = cat
                                                    catMenuExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            if (familyMembers.isNotEmpty()) {
                                item {
                                    var familyMenuExpanded by remember { mutableStateOf(false) }
                                    val selectedMember = familyMembers.find { it.id == selectedFamilyMemberId }
                                    val selectedMemberName = remember(selectedFamilyMemberId, familyMembers) {
                                        if (selectedFamilyMemberId == null) "All Members"
                                        else selectedMember?.fullName ?: "All Members"
                                    }
                                    Box {
                                        Surface(
                                            onClick = { familyMenuExpanded = true },
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (selectedFamilyMemberId != null) palette.oppositeAccent else palette.oppositeAccentContainer,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedFamilyMemberId != null) palette.oppositeAccent else palette.oppositeAccent.copy(alpha = 0.40f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                MemberProfileAvatar(
                                                    photoUrl = selectedMember?.profilePhotoUrl,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = selectedMemberName,
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                                    color = if (selectedFamilyMemberId != null) palette.onOppositeAccent else palette.onOppositeAccentContainer
                                                )
                                            }
                                        }
                                        DropdownMenu(
                                            expanded = familyMenuExpanded,
                                            onDismissRequest = { familyMenuExpanded = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("All Members") },
                                                leadingIcon = { MemberProfileAvatar(photoUrl = null, modifier = Modifier.size(20.dp)) },
                                                onClick = {
                                                    selectedFamilyMemberId = null
                                                    familyMenuExpanded = false
                                                }
                                            )
                                            familyMembers.forEach { member ->
                                                DropdownMenuItem(
                                                    text = { Text(member.fullName) },
                                                    leadingIcon = { MemberProfileAvatar(photoUrl = member.profilePhotoUrl, modifier = Modifier.size(20.dp)) },
                                                    onClick = {
                                                        selectedFamilyMemberId = member.id
                                                        familyMenuExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                var sortMenuExpanded by remember { mutableStateOf(false) }
                                Box {
                                    Surface(
                                        onClick = { sortMenuExpanded = true },
                                        shape = RoundedCornerShape(4.dp),
                                        color = palette.inputBackground,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderColor)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                                contentDescription = "Sort",
                                                tint = palette.secondaryText,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = selectedSort.label,
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = palette.bodyText
                                            )
                                        }
                                    }
                                    DropdownMenu(
                                        expanded = sortMenuExpanded,
                                        onDismissRequest = { sortMenuExpanded = false }
                                    ) {
                                        ItemsSortOption.entries.forEach { option ->
                                            DropdownMenuItem(
                                                text = { Text(option.label) },
                                                onClick = {
                                                    selectedSort = option
                                                    sortMenuExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                var layoutMenuExpanded by remember { mutableStateOf(false) }
                                Box {
                                    Surface(
                                        onClick = { layoutMenuExpanded = true },
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (layoutStyle == LayoutStyle.GRID) palette.oppositeAccent else palette.inputBackground,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (layoutStyle == LayoutStyle.GRID) palette.oppositeAccent else palette.borderColor)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (layoutStyle == LayoutStyle.GRID) Icons.Default.GridView else Icons.AutoMirrored.Filled.List,
                                                contentDescription = "Layout",
                                                tint = if (layoutStyle == LayoutStyle.GRID) palette.onOppositeAccent else palette.secondaryText,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = if (layoutStyle == LayoutStyle.GRID) "Grid" else "List View",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = if (layoutStyle == LayoutStyle.GRID) palette.onOppositeAccent else palette.bodyText
                                            )
                                        }
                                    }
                                    DropdownMenu(
                                        expanded = layoutMenuExpanded,
                                        onDismissRequest = { layoutMenuExpanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("List View") },
                                            onClick = {
                                                layoutStyle = LayoutStyle.LIST
                                                layoutMenuExpanded = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Grid View") },
                                            onClick = {
                                                layoutStyle = LayoutStyle.GRID
                                                layoutMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            Crossfade(
                targetState = listState,
                modifier = Modifier.weight(1f),
                animationSpec = tween(durationMillis = 250),
                label = "itemsContent"
            ) { state ->
                when (state) {
                    ListState.NO_ITEMS -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item { scrollableFilterSection() }
                        item {
                            EmptyItemsState(
                                icon = Icons.Default.FolderOpen,
                                title = "No items yet",
                                message = "Tap + to add items or bills"
                            )
                        }
                    }

                    ListState.NO_MATCHES -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item { scrollableFilterSection() }
                        item {
                            EmptyItemsState(
                                icon = Icons.Default.Search,
                                title = "No matching items",
                                message = "Try another title or category",
                                actionLabel = "Clear search and filters",
                                onAction = {
                                    searchQuery = ""
                                    selectedFilter = StatusFilter.ALL
                                    selectedCategoryFilter = null
                                    selectedFamilyMemberId = null
                                }
                            )
                        }
                    }

                    ListState.RESULTS -> {
                        if (viewMode == ItemsViewMode.CATEGORY) {
                            val categoryGroups = remember(sortedItems) {
                                ItemCategory.entries.mapNotNull { cat ->
                                    val docsInCat = sortedItems.filter { it.category == cat }
                                    if (docsInCat.isNotEmpty()) cat to docsInCat else null
                                }
                            }

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                item(key = "filters_header") {
                                    scrollableFilterSection()
                                }
                                item(key = "ad-banner-category") {
                                    com.docsalert.reminder.ui.components.BannerAdView(modifier = Modifier.padding(vertical = 4.dp))
                                }
                                items(items = categoryGroups, key = { it.first.name }) { (category, catDocs) ->
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = category.icon,
                                                    contentDescription = null,
                                                    tint = palette.primaryAccent,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Text(
                                                    text = category.title,
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = palette.headingText
                                                )
                                            }
                                            Text(
                                                text = "${catDocs.size} items",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = palette.secondaryText
                                            )
                                        }

                                        if (layoutStyle == LayoutStyle.GRID) {
                                            catDocs.chunked(2).forEach { rowDocs ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    rowDocs.forEach { doc ->
                                                        Box(modifier = Modifier.weight(1f)) {
                                                            GridItemCard(
                                                                item = doc,
                                                                today = today,
                                                                familyMembers = familyMembers,
                                                                maskNumbers = maskNumbers,
                                                                onClick = { onItemClick(doc.id) }
                                                            )
                                                        }
                                                    }
                                                    repeat(2 - rowDocs.size) {
                                                        Spacer(modifier = Modifier.weight(1f))
                                                    }
                                                }
                                            }
                                        } else {
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
                                                shape = RoundedCornerShape(22.dp),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderColor),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column {
                                                    catDocs.forEachIndexed { index, doc ->
                                                        ItemRow(
                                                            item = doc,
                                                            today = today,
                                                            familyMembers = familyMembers,
                                                            maskNumbers = maskNumbers,
                                                            showDivider = index < catDocs.lastIndex,
                                                            onClick = { onItemClick(doc.id) }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (layoutStyle == LayoutStyle.GRID) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 24.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                item(key = "filters_header", span = { GridItemSpan(maxLineSpan) }) {
                                    scrollableFilterSection()
                                }
                                items(items = sortedItems, key = { it.id }) { item ->
                                    GridItemCard(
                                        item = item,
                                        today = today,
                                        familyMembers = familyMembers,
                                        maskNumbers = maskNumbers,
                                        onClick = { onItemClick(item.id) }
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                item(key = "filters_header") {
                                    scrollableFilterSection()
                                }
                                item(key = "results-header") {
                                    ResultsHeader(
                                        title = if (isFiltering) "Results" else "All Items",
                                        count = sortedItems.size
                                    )
                                }
                                item(key = "ad-banner") {
                                    com.docsalert.reminder.ui.components.BannerAdView(modifier = Modifier.padding(vertical = 4.dp))
                                }
                                item(key = "items_grouped_list") {
                                    StaggeredEntrance(2) {
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
                                            shape = RoundedCornerShape(22.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderColor),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column {
                                                sortedItems.forEachIndexed { index, item ->
                                                    ItemRow(
                                                        item = item,
                                                        today = today,
                                                        familyMembers = familyMembers,
                                                        maskNumbers = maskNumbers,
                                                        showDivider = index < sortedItems.lastIndex,
                                                        onClick = { onItemClick(item.id) }
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
            }
        }

        ExtendedFloatingActionButton(
            onClick = onAddItemClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.palette.oppositeAccent,
            contentColor = MaterialTheme.palette.onOppositeAccent,
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Add Item", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) }
        )
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = MaterialTheme.palette
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = "Search items, policy numbers...",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.secondaryText
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = palette.primaryAccent
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search",
                        tint = palette.secondaryText
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = palette.inputBackground,
            unfocusedContainerColor = palette.inputBackground,
            focusedBorderColor = palette.focusedBorderColor,
            unfocusedBorderColor = palette.inputBorderColor,
            focusedTextColor = palette.bodyText,
            unfocusedTextColor = palette.bodyText
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
    )
}

@Composable
private fun FilterPill(
    filter: StatusFilter,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    val palette = MaterialTheme.palette
    val containerColor = if (selected) palette.oppositeAccent else palette.inputBackground
    val textColor = if (selected) palette.onOppositeAccent else palette.bodyText

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = containerColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) palette.oppositeAccent else palette.borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = filter.label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = textColor
            )
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (selected) palette.onOppositeAccent.copy(alpha = 0.25f) else palette.secondaryCardBackground)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = textColor
                )
            }
        }
    }
}

@Composable
private fun ResultsHeader(title: String, count: Int) {
    val palette = MaterialTheme.palette

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = palette.headingText
        )
        Text(
            text = "$count item(s)",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = palette.secondaryText
        )
    }
}

@Composable
private fun ItemRow(
    item: Item,
    today: LocalDate,
    familyMembers: List<FamilyMember> = emptyList(),
    maskNumbers: Boolean = false,
    showDivider: Boolean = false,
    onClick: () -> Unit
) {
    val palette = MaterialTheme.palette
    val status = item.toStatus(today)
    val statusColors = status.colors()
    val categoryColors = item.category.colors()
    val formattedNum = item.formattedNumber(maskNumbers)
    val ownerName = remember(item.familyMemberId, familyMembers) {
        familyMembers.find { it.id == item.familyMemberId }?.fullName ?: "Self"
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
                    .background(categoryColors.container),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.category.icon,
                    contentDescription = null,
                    tint = categoryColors.content,
                    modifier = Modifier.size(22.dp)
                )
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
                    text = "${item.category.title}${if (formattedNum != null) " • $formattedNum" else ""}${if (ownerName.isNotBlank() && ownerName != "Self") " • 👤 $ownerName" else ""}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = palette.secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(10.dp))

            Surface(
                shape = RoundedCornerShape(50),
                color = statusColors.container
            ) {
                Text(
                    text = item.daysLeftText(today) ?: "Safe",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = statusColors.content,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(Modifier.width(6.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = palette.secondaryText,
                modifier = Modifier.size(18.dp)
            )
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 72.dp),
                color = palette.dividerColor.copy(alpha = 0.65f)
            )
        }
    }
}

@Composable
private fun GridItemCard(
    item: Item,
    today: LocalDate,
    familyMembers: List<FamilyMember> = emptyList(),
    maskNumbers: Boolean = false,
    onClick: () -> Unit
) {
    val palette = MaterialTheme.palette
    val status = item.toStatus(today)
    val statusColors = status.colors()
    val categoryColors = item.category.colors()
    val formattedNum = item.formattedNumber(maskNumbers)
    val ownerName = remember(item.familyMemberId, familyMembers) {
        familyMembers.find { it.id == item.familyMemberId }?.fullName ?: "Self"
    }
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
                onClick = onClick
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Row: Category Icon + Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(categoryColors.container),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.category.icon,
                        contentDescription = null,
                        tint = categoryColors.content,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = statusColors.container
                ) {
                    Text(
                        text = item.daysLeftText(today) ?: "Safe",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = statusColors.content,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Title
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 20.sp
                ),
                color = palette.headingText,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Category & Owner Tags
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = palette.accentContainer
                ) {
                    Text(
                        text = item.category.title,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = palette.primaryAccent,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (ownerName.isNotBlank() && ownerName != "Self") {
                    Text(
                        text = "👤 $ownerName",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium),
                        color = palette.secondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Credential Number (if present)
            if (formattedNum != null) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = palette.secondaryCardBackground,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = formattedNum,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                        color = palette.bodyText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Expiry Date Footer
            if (item.expiryDate != null) {
                Text(
                    text = "Expiry: ${expiryFormatter.format(item.expiryDate)}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                    color = palette.secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun EmptyItemsState(
    icon: ImageVector,
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: () -> Unit = {}
) {
    val palette = MaterialTheme.palette

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(palette.accentContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = palette.primaryAccent,
                modifier = Modifier.size(34.dp)
            )
        }

        Spacer(Modifier.height(18.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
            color = palette.headingText,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = palette.secondaryText,
            textAlign = TextAlign.Center
        )

        if (actionLabel != null) {
            Spacer(Modifier.height(20.dp))
            Surface(
                onClick = onAction,
                shape = RoundedCornerShape(50),
                color = palette.primaryAccent
            ) {
                Text(
                    text = actionLabel,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 11.dp),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = palette.onAccentText
                )
            }
        }
    }
}

@Composable
private fun ViewModeButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val palette = MaterialTheme.palette
    val backgroundColor = if (isSelected) palette.primaryAccent else Color.Transparent
    val contentColor = if (isSelected) palette.onAccentText else palette.secondaryText

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(16.dp), tint = contentColor)
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = contentColor)
    }
}
