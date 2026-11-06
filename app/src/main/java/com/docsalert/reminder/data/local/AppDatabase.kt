package com.docsalert.reminder.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.docsalert.reminder.data.model.CustomReminder
import com.docsalert.reminder.data.model.Item
import com.docsalert.reminder.data.model.FamilyMember
import com.docsalert.reminder.data.model.VaultItem

@Database(
    entities = [Item::class, FamilyMember::class, VaultItem::class, CustomReminder::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun itemDao(): ItemDao
    abstract fun familyMemberDao(): FamilyMemberDao
    abstract fun vaultItemDao(): VaultItemDao
    abstract fun customReminderDao(): CustomReminderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "docsalert.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
