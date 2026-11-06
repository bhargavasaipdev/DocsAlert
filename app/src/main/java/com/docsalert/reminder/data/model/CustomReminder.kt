package com.docsalert.reminder.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@Entity(tableName = "custom_reminders")
data class CustomReminder(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String? = null,
    val hour: Int,
    val minute: Int,
    val repeatDays: List<Int> = emptyList(), // 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat, 7=Sun
    val enabled: Boolean = true
) {
    val formattedTime: String
        get() {
            val h = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
            val amPm = if (hour >= 12) "PM" else "AM"
            return String.format(Locale.getDefault(), "%d:%02d %s", h, minute, amPm)
        }

    val repeatDaysSummary: String
        get() {
            if (repeatDays.isEmpty()) return "Once"
            if (repeatDays.size == 7) return "Everyday"
            val sorted = repeatDays.sorted()
            if (sorted == listOf(6, 7)) return "Weekends"
            if (sorted == listOf(1, 2, 3, 4, 5)) return "Weekdays"
            return sorted.joinToString(", ") { dayInt ->
                DayOfWeek.of(dayInt).getDisplayName(TextStyle.SHORT, Locale.getDefault())
            }
        }
}
