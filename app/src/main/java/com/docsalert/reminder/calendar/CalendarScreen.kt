package com.docsalert.reminder.calendar

import androidx.compose.animation.Crossfade
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.docsalert.reminder.data.model.Item
import com.docsalert.reminder.data.model.formattedNumber
import com.docsalert.reminder.items.ItemStatus
import com.docsalert.reminder.items.colors
import com.docsalert.reminder.items.daysLeftText
import com.docsalert.reminder.items.toStatus
import com.docsalert.reminder.ui.components.ScreenHeader
import com.docsalert.reminder.ui.components.StaggeredEntrance
import com.docsalert.reminder.ui.theme.palette
import com.docsalert.reminder.ui.viewmodels.ItemViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

enum class CalendarViewMode {
    MONTH, LIST
}

@Composable
fun CalendarScreen(
    itemViewModel: ItemViewModel,
    maskNumbers: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    onItemClick: (String) -> Unit = {}
) {
    var viewMode by remember { mutableStateOf(CalendarViewMode.MONTH) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val palette = MaterialTheme.palette
    val items by itemViewModel.items.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            ScreenHeader(
                title = "Calendar",
                subtitle = "Schedule & Expiries",
                onBackClick = onBackClick,
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .clip(RoundedCornerShape(50))
                    .background(palette.secondaryCardBackground)
                    .padding(4.dp)
            ) {
                ViewModeButton(
                    icon = Icons.Default.CalendarMonth,
                    label = "Month",
                    isSelected = viewMode == CalendarViewMode.MONTH,
                    onClick = { viewMode = CalendarViewMode.MONTH }
                )
                ViewModeButton(
                    icon = Icons.AutoMirrored.Filled.List,
                    label = "List",
                    isSelected = viewMode == CalendarViewMode.LIST,
                    onClick = { viewMode = CalendarViewMode.LIST }
                )
            }
        }

        Crossfade(targetState = viewMode, label = "calendarView") { mode ->
            when (mode) {
                CalendarViewMode.MONTH -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        StaggeredEntrance(0) {
                            MonthView(
                                currentMonth = currentMonth,
                                onMonthChange = { currentMonth = it },
                                selectedDate = selectedDate,
                                onDateSelect = { selectedDate = it },
                                items = items
                            )
                        }
                        
                        com.docsalert.reminder.ui.components.BannerAdView(modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp))
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = palette.dividerColor)
                        
                        val selectedDateItems = items.filter { it.expiryDate == selectedDate }
                        
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                StaggeredEntrance(1) {
                                    Text(
                                        text = "Expiring on ${selectedDate.format(DateTimeFormatter.ofPattern("d MMM yyyy"))}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = palette.headingText,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }
                            }
                            
                            if (selectedDateItems.isEmpty()) {
                                item {
                                    StaggeredEntrance(2) {
                                        Text("No items expiring on this date.", color = palette.secondaryText)
                                    }
                                }
                            } else {
                                item {
                                    StaggeredEntrance(2) {
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
                                            shape = RoundedCornerShape(22.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderColor),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column {
                                                selectedDateItems.forEachIndexed { index, doc ->
                                                    CalendarItemRow(
                                                        item = doc,
                                                        maskNumbers = maskNumbers,
                                                        showDivider = index < selectedDateItems.lastIndex,
                                                        onClick = { onItemClick(doc.id) }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            
                            val otherMonthItems = items.filter { 
                                it.expiryDate != null && YearMonth.from(it.expiryDate) == currentMonth && it.expiryDate != selectedDate 
                            }.sortedBy { it.expiryDate }
                            
                            if (otherMonthItems.isNotEmpty()) {
                                item {
                                    StaggeredEntrance(3) {
                                        Text(
                                            text = "Other expiries in ${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())}",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = palette.headingText,
                                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                                        )
                                    }
                                }
                                
                                item {
                                    StaggeredEntrance(4) {
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
                                            shape = RoundedCornerShape(22.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderColor),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column {
                                                otherMonthItems.forEachIndexed { index, doc ->
                                                    CalendarItemRow(
                                                        item = doc,
                                                        maskNumbers = maskNumbers,
                                                        showDivider = index < otherMonthItems.lastIndex,
                                                        onClick = { onItemClick(doc.id) }
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
                CalendarViewMode.LIST -> {
                    ListView(items = items, maskNumbers = maskNumbers, onItemClick = onItemClick)
                }
            }
        }
    }
}

@Composable
private fun MonthView(
    currentMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    selectedDate: LocalDate,
    onDateSelect: (LocalDate) -> Unit,
    items: List<Item>
) {
    val palette = MaterialTheme.palette

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onMonthChange(currentMonth.minusMonths(1)) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Month", tint = palette.headingText)
            }

            Text(
                text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = palette.headingText
            )

            IconButton(onClick = { onMonthChange(currentMonth.plusMonths(1)) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Month", tint = palette.headingText)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            val daysOfWeek = listOf("M", "T", "W", "T", "F", "S", "S")
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = palette.secondaryText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        val firstDayOfMonth = currentMonth.atDay(1)
        val dayOfWeekOffset = firstDayOfMonth.dayOfWeek.value - 1
        val daysInMonth = currentMonth.lengthOfMonth()

        val totalGridCells = (dayOfWeekOffset + daysInMonth + 6) / 7 * 7

        val rows = (0 until totalGridCells).chunked(7)

        rows.forEach { rowDays ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                rowDays.forEach { cellIndex ->
                    val dayNumber = cellIndex - dayOfWeekOffset + 1
                    if (dayNumber in 1..daysInMonth) {
                        val date = currentMonth.atDay(dayNumber)
                        val isSelected = date == selectedDate
                        val isToday = date == LocalDate.now()
                        val itemsOnDate = items.filter { it.expiryDate == date }

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected -> palette.oppositeAccent
                                        isToday -> palette.oppositeAccentContainer
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable { onDateSelect(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = dayNumber.toString(),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = when {
                                        isSelected -> palette.onOppositeAccent
                                        isToday -> palette.oppositeAccent
                                        else -> palette.bodyText
                                    }
                                )

                                if (itemsOnDate.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(1.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        itemsOnDate.take(3).forEach { doc ->
                                            val dotColor = when (doc.toStatus(LocalDate.now())) {
                                                ItemStatus.VALID -> palette.successColor
                                                ItemStatus.EXPIRING_SOON -> palette.warningColor
                                                ItemStatus.EXPIRED -> palette.errorColor
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) palette.onOppositeAccent else dotColor)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.width(32.dp))
                    }
                }
            }
        }

        // Calendar Dot Legend Bar
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CalendarLegendDot(color = palette.successColor, label = "Valid")
            CalendarLegendDot(color = palette.warningColor, label = "Expiring Soon")
            CalendarLegendDot(color = palette.errorColor, label = "Expired")
        }
    }
}

@Composable
private fun CalendarLegendDot(color: Color, label: String) {
    val palette = MaterialTheme.palette
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
            color = palette.secondaryText
        )
    }
}

@Composable
private fun ListView(items: List<Item>, maskNumbers: Boolean = false, onItemClick: (String) -> Unit) {
    val palette = MaterialTheme.palette
    val sortedItems = remember(items) { items.filter { it.expiryDate != null }.sortedBy { it.expiryDate } }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            com.docsalert.reminder.ui.components.BannerAdView(modifier = Modifier.padding(bottom = 8.dp))
        }

        if (sortedItems.isEmpty()) {
            item {
                Text("No items found.", modifier = Modifier.padding(16.dp), color = palette.secondaryText)
            }
        } else {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
                    shape = RoundedCornerShape(22.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        sortedItems.forEachIndexed { index, doc ->
                            CalendarItemRow(
                                item = doc,
                                maskNumbers = maskNumbers,
                                showDivider = index < sortedItems.lastIndex,
                                onClick = { onItemClick(doc.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarItemRow(
    item: Item,
    maskNumbers: Boolean = false,
    showDivider: Boolean = false,
    onClick: () -> Unit
) {
    val palette = MaterialTheme.palette
    val today = remember { LocalDate.now() }
    val status = item.toStatus(today)
    val statusColors = status.colors()
    val categoryColors = item.category.colors()
    val formattedNum = item.formattedNumber(maskNumbers)
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
                    text = "${item.category.title}${if (formattedNum != null) " • $formattedNum" else ""} • ${item.expiryDate?.format(DateTimeFormatter.ofPattern("d MMM yyyy")) ?: "No Expiry"}",
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
