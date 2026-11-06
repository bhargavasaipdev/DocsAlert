package com.docsalert.reminder.data.repository

import com.docsalert.reminder.data.local.CustomReminderDao
import com.docsalert.reminder.data.model.CustomReminder
import kotlinx.coroutines.flow.Flow

class CustomReminderRepository(private val reminderDao: CustomReminderDao) {

    val reminders: Flow<List<CustomReminder>> = reminderDao.getAllReminders()

    suspend fun getEnabledReminders(): List<CustomReminder> {
        return reminderDao.getEnabledReminders()
    }

    suspend fun getReminderById(id: Int): CustomReminder? {
        return reminderDao.getReminderById(id)
    }

    suspend fun saveReminder(reminder: CustomReminder): Int {
        return if (reminder.id == 0) {
            reminderDao.insertReminder(reminder).toInt()
        } else {
            reminderDao.updateReminder(reminder)
            reminder.id
        }
    }

    suspend fun setReminderEnabled(id: Int, enabled: Boolean) {
        reminderDao.setReminderEnabled(id, enabled)
    }

    suspend fun deleteReminder(id: Int) {
        reminderDao.deleteReminder(id)
    }

    suspend fun clearAll() {
        reminderDao.deleteAllReminders()
    }
}
