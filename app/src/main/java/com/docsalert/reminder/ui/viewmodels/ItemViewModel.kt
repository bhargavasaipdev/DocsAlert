package com.docsalert.reminder.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.docsalert.reminder.data.model.Item
import com.docsalert.reminder.data.repository.ItemRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ItemViewModel(private val repository: ItemRepository) : ViewModel() {
    
    val items: StateFlow<List<Item>> = repository.items
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    fun addItem(item: Item) {
        viewModelScope.launch { repository.addItem(item) }
    }

    suspend fun addItemSync(item: Item) {
        repository.addItem(item)
    }
    
    fun updateItem(item: Item) {
        viewModelScope.launch { repository.updateItem(item) }
    }

    suspend fun updateItemSync(item: Item) {
        repository.updateItem(item)
    }
    
    fun removeItem(id: String) {
        viewModelScope.launch { repository.removeItem(id) }
    }

    fun removeItemsByFamilyMember(familyMemberId: String) {
        viewModelScope.launch { repository.removeItemsByFamilyMember(familyMemberId) }
    }
    
    class Factory(private val repository: ItemRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ItemViewModel::class.java)) {
                return ItemViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
