package com.docsalert.reminder.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.docsalert.reminder.data.model.CustomReminder
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomReminderDao {
    @Query("SELECT * FROM custom_reminders ORDER BY hour ASC, minute ASC")
    fun getAllReminders(): Flow<List<CustomReminder>>

    @Query("SELECT * FROM custom_reminders ORDER BY hour ASC, minute ASC")
    suspend fun getAllRemindersList(): List<CustomReminder>

    @Query("SELECT * FROM custom_reminders WHERE enabled = 1")
    suspend fun getEnabledReminders(): List<CustomReminder>

    @Query("SELECT * FROM custom_reminders WHERE id = :id")
    suspend fun getReminderById(id: Int): CustomReminder?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: CustomReminder): Long

    @Update
    suspend fun updateReminder(reminder: CustomReminder)

    @Query("UPDATE custom_reminders SET enabled = :enabled WHERE id = :id")
    suspend fun setReminderEnabled(id: Int, enabled: Boolean)

    @Query("DELETE FROM custom_reminders WHERE id = :id")
    suspend fun deleteReminder(id: Int)

    @Query("DELETE FROM custom_reminders")
    suspend fun deleteAllReminders()
}
