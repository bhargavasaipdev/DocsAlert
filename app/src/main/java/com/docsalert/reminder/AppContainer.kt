package com.docsalert.reminder

import android.content.Context
import com.docsalert.reminder.data.local.AppDatabase
import com.docsalert.reminder.data.preferences.UserPreferencesRepository
import com.docsalert.reminder.data.repository.CustomReminderRepository
import com.docsalert.reminder.data.repository.ItemRepository
import com.docsalert.reminder.data.repository.FamilyRepository
import com.docsalert.reminder.data.repository.VaultRepository

class AppContainer(private val context: Context) {
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context)
    }
    
    val itemRepository: ItemRepository by lazy {
        ItemRepository(database.itemDao())
    }
    
    val familyRepository: FamilyRepository by lazy {
        FamilyRepository(database.familyMemberDao())
    }

    val vaultRepository: VaultRepository by lazy {
        VaultRepository(database.vaultItemDao())
    }

    val customReminderRepository: CustomReminderRepository by lazy {
        CustomReminderRepository(database.customReminderDao())
    }
}
