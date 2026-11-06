package com.docsalert.reminder.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.docsalert.reminder.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON" ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val pendingResult = goAsync()
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                try {
                    val appContainer = AppContainer(context.applicationContext)
                    val settings = appContainer.userPreferencesRepository.appSettings.first()
                    if (settings.notificationsEnabled) {
                        ReminderAlarmHelper.scheduleDailyAlarm(context, settings.notificationTime)
                    }

                    val enabledReminders = appContainer.customReminderRepository.getEnabledReminders()
                    enabledReminders.forEach { reminder ->
                        CustomReminderScheduler.scheduleReminder(context, reminder)
                    }
                } catch (e: Exception) {
                    // Ignore
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
