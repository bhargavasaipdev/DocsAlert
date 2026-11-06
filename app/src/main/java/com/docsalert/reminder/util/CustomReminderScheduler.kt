package com.docsalert.reminder.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.docsalert.reminder.data.model.CustomReminder
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

object CustomReminderScheduler {
    private const val TAG = "CustomReminderScheduler"
    const val EXTRA_REMINDER_ID = "extra_custom_reminder_id"

    fun calculateNextTriggerMillis(reminder: CustomReminder, now: LocalDateTime = LocalDateTime.now()): Long {
        val targetTimeToday = now.withHour(reminder.hour).withMinute(reminder.minute).withSecond(0).withNano(0)

        if (reminder.repeatDays.isEmpty() || reminder.repeatDays.size == 7) {
            // Everyday or One-time next occurrence
            val nextDateTime = if (now.isBefore(targetTimeToday)) targetTimeToday else targetTimeToday.plusDays(1)
            return nextDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }

        // Find nearest future day from selected repeatDays (1=Mon..7=Sun)
        val candidateTimes = reminder.repeatDays.map { dayInt ->
            val targetDay = DayOfWeek.of(dayInt)
            var candidate = now.with(TemporalAdjusters.nextOrSame(targetDay))
                .withHour(reminder.hour).withMinute(reminder.minute).withSecond(0).withNano(0)
            if (candidate.isBefore(now) || candidate.isEqual(now)) {
                candidate = candidate.plusWeeks(1)
            }
            candidate
        }

        val nearest = candidateTimes.minOrNull() ?: targetTimeToday.plusDays(1)
        return nearest.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun scheduleReminder(context: Context, reminder: CustomReminder) {
        if (!reminder.enabled) {
            cancelReminder(context, reminder.id)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, CustomReminderReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_ID, reminder.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTimeMillis = calculateNextTriggerMillis(reminder)
        Log.d(TAG, "Scheduling reminder ID ${reminder.id} ('${reminder.title}') for trigger millis: $triggerTimeMillis")

        try {
            val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTimeMillis, pendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException setting alarm clock, using setAndAllowWhileIdle", e)
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling reminder ID ${reminder.id}", e)
        }
    }

    fun cancelReminder(context: Context, reminderId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, CustomReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        Log.d(TAG, "Cancelled reminder ID $reminderId")
    }
}
