package com.docsalert.reminder.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.docsalert.reminder.data.local.AppDatabase
import com.docsalert.reminder.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val preferencesRepository = UserPreferencesRepository(applicationContext)
        val appSettings = preferencesRepository.appSettings.first()
        val notificationsEnabled = androidx.core.app.NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()
        val fullScreenAlarmEnabled = appSettings.fullScreenAlarmEnabled

        if (!notificationsEnabled && !fullScreenAlarmEnabled) {
            return Result.success()
        }

        val database = AppDatabase.getDatabase(applicationContext)
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
                    val title = if (daysUntilExpiry < 0) {
                        "${item.name} has expired!"
                    } else if (daysUntilExpiry == 0) {
                        "${item.name} expires today!"
                    } else {
                        "${item.name} expires in $daysUntilExpiry days"
                    }

                    val body = "Don't forget to renew your ${item.category.title} item."
                    val memberName = familyMembers.find { it.id == item.familyMemberId }?.fullName ?: "Self"

                    val notificationId = Math.abs(item.id.hashCode()) + 1

                    NotificationHelper.sendReminderNotification(
                        context = applicationContext,
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
                    context = applicationContext,
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

        return Result.success()
    }
}
