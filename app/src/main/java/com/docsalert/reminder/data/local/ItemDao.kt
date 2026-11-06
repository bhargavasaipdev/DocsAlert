package com.docsalert.reminder.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.docsalert.reminder.data.model.Item
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM Items")
    fun getAllItems(): Flow<List<Item>>

    @Query("SELECT * FROM Items")
    suspend fun getAllItemsList(): List<Item>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(Item: Item)

    @Update
    suspend fun updateItem(Item: Item)

    @Query("DELETE FROM Items WHERE id = :id")
    suspend fun deleteItem(id: String)

    @Query("DELETE FROM Items WHERE familyMemberId = :familyMemberId")
    suspend fun deleteItemsByFamilyMember(familyMemberId: String)

    @Query("DELETE FROM Items")
    suspend fun deleteAllItems()
}
