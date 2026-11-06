package com.docsalert.reminder.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.docsalert.reminder.data.model.CustomReminder
import com.docsalert.reminder.data.repository.CustomReminderRepository
import com.docsalert.reminder.util.CustomReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CustomReminderViewModel(private val repository: CustomReminderRepository) : ViewModel() {

    val reminders: StateFlow<List<CustomReminder>> = repository.reminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveReminder(context: Context, reminder: CustomReminder) {
        viewModelScope.launch {
            val reminderId = repository.saveReminder(reminder)
            val updatedReminder = reminder.copy(id = reminderId)
            CustomReminderScheduler.scheduleReminder(context.applicationContext, updatedReminder)
        }
    }

    fun setReminderEnabled(context: Context, id: Int, enabled: Boolean) {
        viewModelScope.launch {
            repository.setReminderEnabled(id, enabled)
            val reminder = repository.getReminderById(id)
            if (reminder != null) {
                if (enabled) {
                    CustomReminderScheduler.scheduleReminder(context.applicationContext, reminder)
                } else {
                    CustomReminderScheduler.cancelReminder(context.applicationContext, id)
                }
            }
        }
    }

    fun deleteReminder(context: Context, id: Int) {
        viewModelScope.launch {
            CustomReminderScheduler.cancelReminder(context.applicationContext, id)
            repository.deleteReminder(id)
        }
    }

    class Factory(private val repository: CustomReminderRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CustomReminderViewModel::class.java)) {
                return CustomReminderViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
