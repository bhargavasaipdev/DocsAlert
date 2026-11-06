package com.docsalert.reminder.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.docsalert.reminder.data.model.Item
import com.docsalert.reminder.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.first

object ReminderAlarmHelper {
    private const val TAG = "DocsAlertNotif"

    fun scheduleDailyAlarm(context: Context, notificationTime: String) {
        DailyNotificationWorker.scheduleNextExactWorker(context, notificationTime)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1002,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val timeParts = notificationTime.split(":")
        val targetHour = timeParts.getOrNull(0)?.toIntOrNull() ?: 9
        val targetMinute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0

        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, targetHour)
            set(java.util.Calendar.MINUTE, targetMinute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
        }

        Log.d(TAG, "Scheduling daily alarm for ${calendar.time} (notificationTime: $notificationTime)")

        try {
            val alarmClockInfo = AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            Log.d(TAG, "AlarmClock set successfully")
        } catch (e: SecurityException) {
            Log.w(TAG, "Exact alarm permission missing, falling back to setExactAndAllowWhileIdle", e)
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set alarm", e)
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    suspend fun checkAndTriggerDueReminders(context: Context) {
        try {
            val preferencesRepository = UserPreferencesRepository(context)
            val appSettings = preferencesRepository.appSettings.first()
            val notificationsEnabled = androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
            val fullScreenAlarmEnabled = appSettings.fullScreenAlarmEnabled

            if (!notificationsEnabled && !fullScreenAlarmEnabled) {
                return
            }

            val database = com.docsalert.reminder.data.local.AppDatabase.getDatabase(context)
            val items = database.itemDao().getAllItemsList()
            val familyMembers = database.familyMemberDao().getAllFamilyMembersList()
            val today = java.time.LocalDate.now()

            Log.d(TAG, "checkAndTriggerDueReminders running. Total items in DB: ${items.size}, today: $today")

            val dueItems = items.filter { item ->
                if (item.remindersEnabled && item.expiryDate != null) {
                    val daysUntilExpiry = java.time.temporal.ChronoUnit.DAYS.between(today, item.expiryDate).toInt()
                    val activeReminderDays = if (item.reminderDaysBefore.isNotEmpty()) item.reminderDaysBefore else listOf(30, 15, 7, 3, 1, 0)
                    activeReminderDays.contains(daysUntilExpiry)
                } else false
            }

            Log.d(TAG, "Found ${dueItems.size} due items for notification")

            if (dueItems.isNotEmpty()) {
                if (notificationsEnabled) {
                    dueItems.forEach { item ->
                        val daysUntilExpiry = java.time.temporal.ChronoUnit.DAYS.between(today, item.expiryDate).toInt()
                        val title = when {
                            daysUntilExpiry < 0 -> "${item.name} has expired!"
                            daysUntilExpiry == 0 -> "${item.name} expires today!"
                            else -> "${item.name} expires in $daysUntilExpiry days"
                        }
                        val body = "Don't forget to renew your ${item.category.title} item."
                        val memberName = familyMembers.find { it.id == item.familyMemberId }?.fullName ?: "Self"
                        val notificationId = kotlin.math.abs(item.id.hashCode()) + 1

                        Log.d(TAG, "Posting notification for item: ${item.name} (id: $notificationId, daysUntilExpiry: $daysUntilExpiry)")

                        NotificationHelper.sendReminderNotification(
                            context = context,
                            id = notificationId,
                            title = title,
                            body = body,
                            itemName = item.name,
                            categoryName = item.category.title,
                            expiryDateStr = item.expiryDate.toString(),
                            memberName = memberName,
                            enableFullScreenIntent = fullScreenAlarmEnabled
                        )
                    }
                }

                if (fullScreenAlarmEnabled) {
                    val summaryText = dueItems.joinToString("\n• ") { item ->
                        val days = java.time.temporal.ChronoUnit.DAYS.between(today, item.expiryDate).toInt()
                        val status = if (days < 0) "Expired" else if (days == 0) "Expires Today" else "Due in $days days"
                        "${item.name} (${item.category.title}) - $status"
                    }

                    val alarmIntent = Intent(context, ReminderAlarmActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra(ReminderAlarmActivity.EXTRA_DOC_COUNT, dueItems.size)
                        putExtra(ReminderAlarmActivity.EXTRA_DOC_SUMMARY, "• $summaryText")
                        putExtra(ReminderAlarmActivity.EXTRA_DOC_NAME, if (dueItems.size == 1) dueItems.first().name else "${dueItems.size} Items Expiring")
                        putExtra(ReminderAlarmActivity.EXTRA_DOC_CATEGORY, if (dueItems.size == 1) dueItems.first().category.title else "Multiple Categories")
                        putExtra(ReminderAlarmActivity.EXTRA_EXPIRY_DATE, today.toString())
                    }

                    try {
                        context.startActivity(alarmIntent)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to launch alarm activity", e)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking due reminders", e)
        }
    }
}
