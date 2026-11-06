package com.docsalert.reminder.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.docsalert.reminder.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CustomReminderReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "CustomReminderReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getIntExtra(CustomReminderScheduler.EXTRA_REMINDER_ID, -1)
        if (reminderId == -1) return

        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val reminder = db.customReminderDao().getReminderById(reminderId)

                if (reminder != null && reminder.enabled) {
                    Log.d(TAG, "Triggering reminder notification for '${reminder.title}' (ID: $reminderId)")

                    NotificationHelper.sendCustomReminderNotification(
                        context = context,
                        reminderId = reminder.id,
                        title = reminder.title,
                        description = reminder.description ?: "Scheduled Reminder"
                    )

                    // Reschedule for next weekday occurrence
                    CustomReminderScheduler.scheduleReminder(context, reminder)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in CustomReminderReceiver for ID $reminderId", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
