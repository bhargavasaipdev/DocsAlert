package com.docsalert.reminder.util

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.docsalert.reminder.data.local.AppDatabase
import com.docsalert.reminder.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class DailyNotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("DocsAlertNotif", "DailyNotificationWorker.doWork() triggered!")
        try {
            val preferencesRepository = UserPreferencesRepository(applicationContext)
            val appSettings = preferencesRepository.appSettings.first()
            val notificationsEnabled = androidx.core.app.NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()
            val fullScreenAlarmEnabled = appSettings.fullScreenAlarmEnabled

            if (!notificationsEnabled && !fullScreenAlarmEnabled) {
                Log.d("DocsAlertNotif", "Both notifications and fullScreenAlarm are disabled. Skipping.")
                return Result.success()
            }

            val database = AppDatabase.getDatabase(applicationContext)
            val items = database.itemDao().getAllItemsList()
            val familyMembers = database.familyMemberDao().getAllFamilyMembersList()
            val today = LocalDate.now()

            val dueItems = items.filter { doc ->
                if (doc.remindersEnabled && doc.expiryDate != null) {
                    val daysUntilExpiry = ChronoUnit.DAYS.between(today, doc.expiryDate).toInt()
                    val activeReminderDays = if (doc.reminderDaysBefore.isNotEmpty()) doc.reminderDaysBefore else listOf(30, 15, 7, 3, 1, 0)
                    activeReminderDays.contains(daysUntilExpiry)
                } else false
            }

            Log.d("DocsAlertNotif", "DailyNotificationWorker found ${dueItems.size} due items for today ($today)")

            if (dueItems.isNotEmpty()) {
                if (notificationsEnabled) {
                    dueItems.forEachIndexed { index, doc ->
                        val daysUntilExpiry = ChronoUnit.DAYS.between(today, doc.expiryDate).toInt()
                        val title = when {
                            daysUntilExpiry < 0 -> "${doc.name} has expired!"
                            daysUntilExpiry == 0 -> "${doc.name} expires today!"
                            else -> "${doc.name} expires in $daysUntilExpiry days"
                        }
                        val body = "Don't forget to renew your ${doc.category.title} item."
                        val memberName = familyMembers.find { it.id == doc.familyMemberId }?.fullName ?: "Self"
                        val notificationId = Math.abs(doc.id.hashCode()) + 1

                        Log.d("DocsAlertNotif", "Posting status bar notification for '${doc.name}' (id: $notificationId, days: $daysUntilExpiry)")

                        NotificationHelper.sendReminderNotification(
                            context = applicationContext,
                            id = notificationId,
                            title = title,
                            body = body,
                            itemName = doc.name,
                            categoryName = doc.category.title,
                            expiryDateStr = doc.expiryDate.toString(),
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

        } catch (e: Exception) {
            Log.e("DocsAlertNotif", "Error in DailyNotificationWorker", e)
            return Result.retry()
        }

        return Result.success()
    }

    companion object {
        const val WORK_TAG = "DailyExactNotificationWork"

        fun scheduleNextExactWorker(context: Context, notificationTime: String) {
            val timeParts = notificationTime.split(":")
            val targetHour = timeParts.getOrNull(0)?.toIntOrNull() ?: 9
            val targetMinute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0

            val now = LocalDateTime.now()
            var targetTime = now.withHour(targetHour).withMinute(targetMinute).withSecond(0).withNano(0)
            if (now.isAfter(targetTime) || now.isEqual(targetTime)) {
                targetTime = targetTime.plusDays(1)
            }

            val delaySeconds = ChronoUnit.SECONDS.between(now, targetTime)
            Log.d("DocsAlertNotif", "Scheduling PeriodicWorkRequestBuilder for $targetTime (delay: $delaySeconds secs)")

            val workRequest = PeriodicWorkRequestBuilder<DailyNotificationWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
                .addTag(WORK_TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_TAG,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                workRequest
            )
        }
    }
}
