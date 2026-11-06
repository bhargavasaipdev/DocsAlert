package com.docsalert.reminder.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.docsalert.reminder.MainActivity
import com.docsalert.reminder.R
import com.docsalert.reminder.data.model.NotificationSoundPreset
import com.docsalert.reminder.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.first

object NotificationHelper {

    private const val CHANNEL_NAME = "Reminder & Expiry Alerts"
    private const val CHANNEL_DESC = "Full-screen alerts and notifications for Item expiration and daily reminders"

    fun getSavedSoundKey(context: Context): String {
        return try {
            kotlinx.coroutines.runBlocking {
                UserPreferencesRepository(context).appSettings.first().notificationSoundKey
            }
        } catch (e: Exception) {
            NotificationSoundPreset.DEFAULT.key
        }
    }

    fun createNotificationChannel(context: Context, soundKey: String = getSavedSoundKey(context)): String {
        val preset = NotificationSoundPreset.fromKey(soundKey)
        val channelId = "reminder_channel_v6_${preset.key}"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val soundUri = Uri.parse("android.resource://" + context.packageName + "/" + preset.rawResId)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(channelId, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                enableLights(true)
                setSound(soundUri, audioAttributes)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        return channelId
    }

    fun sendTestNotification(context: Context) {
        sendReminderNotification(
            context = context,
            id = 1001,
            title = context.getString(R.string.test_notification_title),
            body = context.getString(R.string.test_notification_body),
            itemName = "Sample Passport Expiry",
            categoryName = "Identity",
            expiryDateStr = "Due Today",
            memberName = "Self",
            enableFullScreenIntent = true,
            playSound = true
        )
    }

    fun sendReminderNotification(
        context: Context,
        id: Int,
        title: String,
        body: String,
        itemName: String = "",
        categoryName: String = "",
        expiryDateStr: String = "",
        memberName: String = "",
        enableFullScreenIntent: Boolean = true,
        playSound: Boolean = true
    ) {
        val channelId = createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("OPEN_SCREEN", "ITEMS")
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val largeIconBitmap = try {
            BitmapFactory.decodeResource(context.resources, R.drawable.app_logo)
        } catch (e: Exception) {
            null
        }

        val detailedText = if (itemName.isNotBlank()) {
            "📄 Item: $itemName\n📁 Category: $categoryName\n📅 Expiry Date: $expiryDateStr\n👤 Owner: $memberName"
        } else {
            body
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .apply { largeIconBitmap?.let { setLargeIcon(it) } }
            .setContentTitle(title)
            .setContentText(if (itemName.isNotBlank()) "$itemName ($categoryName) • $expiryDateStr" else body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(detailedText))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSilent(!playSound)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (enableFullScreenIntent) {
            val alarmIntent = Intent(context, ReminderAlarmActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(ReminderAlarmActivity.EXTRA_NOTIF_ID, id)
                putExtra(ReminderAlarmActivity.EXTRA_DOC_ID, id.toString())
                putExtra(ReminderAlarmActivity.EXTRA_DOC_NAME, itemName.ifBlank { title })
                putExtra(ReminderAlarmActivity.EXTRA_DOC_CATEGORY, categoryName)
                putExtra(ReminderAlarmActivity.EXTRA_EXPIRY_DATE, expiryDateStr)
                putExtra(ReminderAlarmActivity.EXTRA_MEMBER_NAME, memberName)
                putExtra(ReminderAlarmActivity.EXTRA_DOC_SUMMARY, body)
            }

            val fullScreenPendingIntent: PendingIntent = PendingIntent.getActivity(
                context,
                id,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.setFullScreenIntent(fullScreenPendingIntent, true)
        }

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(id, builder.build())
        } catch (e: SecurityException) {
            // Permission missing
        }
    }

    fun sendCustomReminderNotification(
        context: Context,
        reminderId: Int,
        title: String,
        description: String,
        notificationsEnabled: Boolean = true,
        fullScreenAlarmEnabled: Boolean = true
    ) {
        if (!notificationsEnabled && !fullScreenAlarmEnabled) {
            return
        }

        val channelId = createNotificationChannel(context)

        val alarmIntent = Intent(context, ReminderAlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("IS_CUSTOM_REMINDER", true)
            putExtra(ReminderAlarmActivity.EXTRA_NOTIF_ID, reminderId)
            putExtra(ReminderAlarmActivity.EXTRA_DOC_ID, reminderId.toString())
            putExtra(ReminderAlarmActivity.EXTRA_DOC_NAME, title)
            putExtra(ReminderAlarmActivity.EXTRA_DOC_CATEGORY, "Custom Reminder")
            putExtra(ReminderAlarmActivity.EXTRA_EXPIRY_DATE, "Daily Reminder")
            putExtra(ReminderAlarmActivity.EXTRA_DOC_SUMMARY, description)
            putExtra(ReminderAlarmActivity.EXTRA_MEMBER_NAME, "Scheduled Alert")
        }

        if (notificationsEnabled) {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("OPEN_SCREEN", "REMINDERS")
            }
            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                context,
                reminderId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val largeIconBitmap = try {
                BitmapFactory.decodeResource(context.resources, R.drawable.app_logo)
            } catch (e: Exception) {
                null
            }

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .apply { largeIconBitmap?.let { setLargeIcon(it) } }
                .setContentTitle(title)
                .setContentText(description.ifBlank { "Scheduled Reminder Alert" })
                .setStyle(NotificationCompat.BigTextStyle().bigText(description.ifBlank { "Scheduled Reminder Alert" }))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            if (fullScreenAlarmEnabled) {
                val fullScreenPendingIntent: PendingIntent = PendingIntent.getActivity(
                    context,
                    reminderId,
                    alarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.setFullScreenIntent(fullScreenPendingIntent, true)
            }

            try {
                val notificationManager = NotificationManagerCompat.from(context)
                notificationManager.notify(reminderId, builder.build())
            } catch (e: SecurityException) {
                // Permission missing
            }
        }
    }
}
