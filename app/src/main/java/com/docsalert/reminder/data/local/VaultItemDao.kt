package com.docsalert.reminder.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.docsalert.reminder.data.model.VaultItem
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultItemDao {
    @Query("SELECT * FROM vault_items ORDER BY createdDate DESC")
    fun getAllVaultItems(): Flow<List<VaultItem>>

    @Query("SELECT * FROM vault_items ORDER BY createdDate DESC")
    suspend fun getAllVaultItemsList(): List<VaultItem>

    @Query("SELECT * FROM vault_items WHERE id = :id")
    suspend fun getVaultItemById(id: String): VaultItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaultItem(item: VaultItem)

    @Update
    suspend fun updateVaultItem(item: VaultItem)

    @Query("DELETE FROM vault_items WHERE id = :id")
    suspend fun deleteVaultItem(id: String)

    @Query("DELETE FROM vault_items")
    suspend fun deleteAllVaultItems()
}
