package com.docsalert.reminder.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.docsalert.reminder.data.local.AppDatabase
import com.docsalert.reminder.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            try {
                val preferencesRepository = UserPreferencesRepository(context)
                val appSettings = preferencesRepository.appSettings.first()
                val notificationsEnabled = androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
                val fullScreenAlarmEnabled = appSettings.fullScreenAlarmEnabled

                val database = AppDatabase.getDatabase(context)
                val items = database.itemDao().getAllItemsList()
                val familyMembers = database.familyMemberDao().getAllFamilyMembersList()
                val today = LocalDate.now()

                val dueItems = items.filter { item ->
                    if (item.remindersEnabled && item.expiryDate != null) {
                        val daysUntilExpiry = ChronoUnit.DAYS.between(today, item.expiryDate).toInt()
                        val activeReminderDays = if (item.reminderDaysBefore.isNotEmpty()) item.reminderDaysBefore else listOf(30, 15, 7, 3, 1, 0)
                        activeReminderDays.contains(daysUntilExpiry)
                    } else false
                }

                if (dueItems.isNotEmpty()) {
                    if (notificationsEnabled) {
                        dueItems.forEachIndexed { index, item ->
                            val daysUntilExpiry = ChronoUnit.DAYS.between(today, item.expiryDate).toInt()
                            val title = when {
                                daysUntilExpiry < 0 -> "${item.name} has expired!"
                                daysUntilExpiry == 0 -> "${item.name} expires today!"
                                else -> "${item.name} expires in $daysUntilExpiry days"
                            }
                            val body = "Don't forget to renew your ${item.category.title} item."
                            val memberName = familyMembers.find { it.id == item.familyMemberId }?.fullName ?: "Self"
                            val notificationId = Math.abs(item.id.hashCode()) + 1

                            NotificationHelper.sendReminderNotification(
                                context = context,
                                id = notificationId,
                                title = title,
                                body = body,
                                itemName = item.name,
                                categoryName = item.category.title,
                                expiryDateStr = item.expiryDate.toString(),
                                memberName = memberName,
                                enableFullScreenIntent = fullScreenAlarmEnabled && index == 0,
                                playSound = index == 0
                            )
                        }
                    } else if (fullScreenAlarmEnabled) {
                        NotificationHelper.sendReminderNotification(
                            context = context,
                            id = 1001,
                            title = "DocsAlert Expiry",
                            body = "You have ${dueItems.size} item(s) due.",
                            itemName = if (dueItems.size == 1) dueItems.first().name else "${dueItems.size} Items",
                            categoryName = "Alert",
                            expiryDateStr = today.toString(),
                            memberName = "System",
                            enableFullScreenIntent = true
                        )
                    }
                }

                // Automatically reschedule exact daily alarm for TOMORROW at notificationTime
                ReminderAlarmHelper.scheduleDailyAlarm(context, appSettings.notificationTime)

            } catch (e: Exception) {
                Log.e("ReminderReceiver", "Error processing reminders", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
