package com.docsalert.reminder.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.ui.graphics.vector.ImageVector
import java.time.LocalDate

enum class VaultItemType(val title: String) {
    PAN_CARD("PAN Card"),
    AADHAAR_CARD("Aadhaar Card"),
    DRIVING_LICENSE("Driving License"),
    PASSPORT("Passport"),
    VOTER_ID("Voter ID"),
    OTHER("Other");

    val icon: ImageVector
        get() = when (this) {
            PAN_CARD -> Icons.Default.CreditCard
            AADHAAR_CARD -> Icons.Default.Badge
            DRIVING_LICENSE -> Icons.Default.DirectionsCar
            PASSPORT -> Icons.Default.Flight
            VOTER_ID -> Icons.Default.HowToVote
            OTHER -> Icons.Default.Description
        }
}

@Entity(tableName = "vault_items")
data class VaultItem(
    @PrimaryKey
    val id: String,
    val name: String,
    val type: VaultItemType,
    val customType: String? = null,
    val itemNumber: String? = null,
    val expiryDate: LocalDate? = null,
    val notes: String = "",
    val attachmentPaths: List<String> = emptyList(),
    val createdDate: LocalDate = LocalDate.now()
) {
    val displayType: String
        get() = if (type == VaultItemType.OTHER && !customType.isNullOrBlank()) customType else type.title
}
