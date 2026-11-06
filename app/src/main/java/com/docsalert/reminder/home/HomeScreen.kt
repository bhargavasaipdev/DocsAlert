package com.docsalert.reminder.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.docsalert.reminder.data.model.ItemCategory
import com.docsalert.reminder.ui.components.StaggeredEntrance
import com.docsalert.reminder.ui.theme.palette
import com.docsalert.reminder.ui.viewmodels.FamilyViewModel
import com.docsalert.reminder.ui.viewmodels.ItemViewModel
import java.time.LocalDate

@Composable
fun HomeScreen(
    itemViewModel: ItemViewModel,
    familyViewModel: FamilyViewModel? = null,
    customReminderViewModel: com.docsalert.reminder.ui.viewmodels.CustomReminderViewModel? = null,
    maskNumbers: Boolean = false,
    onSeeAllClick: () -> Unit = {},
    onAddItemClick: () -> Unit = {},
    onItemClick: (String) -> Unit = {},
    onVaultClick: () -> Unit = {},
    onRemindersClick: () -> Unit = {},
    onFilterClick: (statusFilter: String?, categoryFilter: ItemCategory?) -> Unit = { _, _ -> }
) {
    val palette = MaterialTheme.palette
    val allItems by itemViewModel.items.collectAsState()
    val familyMembers by familyViewModel?.members?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) }
    val reminders by customReminderViewModel?.reminders?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) }
    
    var selectedFamilyMemberId by remember { mutableStateOf<String?>(null) }

    val items = remember(allItems, selectedFamilyMemberId) {
        if (selectedFamilyMemberId == null) {
            allItems
        } else {
            allItems.filter { it.familyMemberId == selectedFamilyMemberId }
        }
    }
    
    val today = LocalDate.now()
    
    val valid = items.count { it.expiryDate == null || java.time.temporal.ChronoUnit.DAYS.between(today, it.expiryDate) > 30 }
    val expiringSoon = items.count { it.expiryDate != null && java.time.temporal.ChronoUnit.DAYS.between(today, it.expiryDate) in 0..30 }
    val expired = items.count { it.expiryDate != null && java.time.temporal.ChronoUnit.DAYS.between(today, it.expiryDate) < 0 }
    val activeRemindersCount = remember(reminders) { reminders.count { it.enabled } }

    val upcomingItems = items.filter { it.expiryDate != null && java.time.temporal.ChronoUnit.DAYS.between(today, it.expiryDate) >= 0 }
        .sortedBy { it.expiryDate }
        .take(4)

    Box(modifier = Modifier.fillMaxSize()) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(palette.accentContainer.copy(alpha = 0.8f), palette.screenBackground)
                    )
                )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                StaggeredEntrance(0) {
                    HomeHeader(
                        familyMembers = familyMembers,
                        selectedMemberId = selectedFamilyMemberId,
                        onMemberSelected = { selectedFamilyMemberId = it }
                    )
                }
            }
            item {
                StaggeredEntrance(1) {
                    TotalItemsCard(
                        total = valid + expiringSoon + expired,
                        valid = valid,
                        expiringSoon = expiringSoon,
                        expired = expired,
                        activeRemindersCount = activeRemindersCount,
                        onClick = { onFilterClick(null, null) },
                        onStatusClick = { status -> onFilterClick(status, null) },
                        onRemindersClick = onRemindersClick
                    )
                }
            }
            item {
                StaggeredEntrance(2) {
                    UpcomingExpirations(
                        items = upcomingItems,
                        onSeeAllClick = onSeeAllClick,
                        onItemClick = onItemClick,
                        maskNumbers = maskNumbers
                    )
                }
            }
            item {
                StaggeredEntrance(3) {
                    AddItemButton(onClick = onAddItemClick)
                }
            }
            item {
                StaggeredEntrance(4) {
                    AdBannerPlaceholder()
                }
            }
            item {
                StaggeredEntrance(5) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader(title = "More Features")
                        FeatureQuickAccessCards(
                            onVaultClick = onVaultClick,
                            onRemindersClick = onRemindersClick
                        )
                    }
                }
            }
        }
    }
}
