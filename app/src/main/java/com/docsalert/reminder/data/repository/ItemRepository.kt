package com.docsalert.reminder.data.repository

import com.docsalert.reminder.data.local.ItemDao
import com.docsalert.reminder.data.model.Item
import kotlinx.coroutines.flow.Flow

class ItemRepository(private val itemDao: ItemDao) {

    val items: Flow<List<Item>> = itemDao.getAllItems()

    suspend fun addItem(item: Item) {
        val itemToInsert = if (item.id.isEmpty()) item.copy(id = java.util.UUID.randomUUID().toString()) else item
        itemDao.insertItem(itemToInsert)
    }

    suspend fun updateItem(item: Item) {
        itemDao.updateItem(item)
    }

    suspend fun removeItem(id: String) {
        itemDao.deleteItem(id)
    }

    suspend fun removeItemsByFamilyMember(familyMemberId: String) {
        itemDao.deleteItemsByFamilyMember(familyMemberId)
    }

    suspend fun clearAll() {
        itemDao.deleteAllItems()
    }
}
