package com.docsalert.reminder.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Shield
import androidx.compose.ui.graphics.vector.ImageVector
import java.time.LocalDate

enum class ItemCategory(val title: String) {
    IDENTITY("Identity"),
    VEHICLE("Vehicle"),
    INSURANCE("Insurance"),
    TRAVEL("Travel"),
    CERTIFICATES("Certificates"),
    OTHER("Other");

    val icon: ImageVector
        get() = when (this) {
            IDENTITY -> Icons.Default.Badge
            VEHICLE -> Icons.Default.DirectionsCar
            INSURANCE -> Icons.Default.Shield
            TRAVEL -> Icons.Default.Flight
            CERTIFICATES -> Icons.Default.Description
            OTHER -> Icons.Default.MoreHoriz
        }
}

@Entity(tableName = "items")
data class Item(
    @PrimaryKey
    val id: String,
    val name: String,
    val category: ItemCategory,
    val itemNumber: String? = null,
    val issueDate: LocalDate? = null,
    val expiryDate: LocalDate? = null,
    val remindersEnabled: Boolean = false,
    val reminderDaysBefore: List<Int> = emptyList(),
    val notes: String = "",
    val familyMemberId: String? = null,
    val fileType: String = "PDF"
)

fun Item.formattedNumber(mask: Boolean): String? {
    val number = itemNumber ?: return null
    if (number.isBlank()) return null
    return if (mask) {
        val suffix = if (number.length > 4) number.takeLast(4) else number
        "•••• $suffix"
    } else {
        number
    }
}
