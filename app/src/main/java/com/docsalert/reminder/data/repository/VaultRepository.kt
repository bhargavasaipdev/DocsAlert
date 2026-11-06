package com.docsalert.reminder.data.repository

import com.docsalert.reminder.data.local.VaultItemDao
import com.docsalert.reminder.data.model.VaultItem
import kotlinx.coroutines.flow.Flow

class VaultRepository(private val vaultItemDao: VaultItemDao) {

    val vaultItems: Flow<List<VaultItem>> = vaultItemDao.getAllVaultItems()

    suspend fun getVaultItemById(id: String): VaultItem? {
        return vaultItemDao.getVaultItemById(id)
    }

    suspend fun saveVaultItem(item: VaultItem) {
        val itemToSave = if (item.id.isEmpty()) {
            item.copy(id = java.util.UUID.randomUUID().toString())
        } else {
            item
        }
        vaultItemDao.insertVaultItem(itemToSave)
    }

    suspend fun deleteVaultItem(id: String) {
        vaultItemDao.deleteVaultItem(id)
    }

    suspend fun clearAll() {
        vaultItemDao.deleteAllVaultItems()
    }
}
