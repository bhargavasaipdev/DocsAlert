package com.docsalert.reminder.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.docsalert.reminder.data.model.VaultItem
import com.docsalert.reminder.data.repository.VaultRepository
import com.docsalert.reminder.util.VaultFileHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VaultViewModel(private val repository: VaultRepository) : ViewModel() {

    val vaultItems: StateFlow<List<VaultItem>> = repository.vaultItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveVaultItem(item: VaultItem) {
        viewModelScope.launch {
            repository.saveVaultItem(item)
        }
    }

    fun deleteVaultItem(item: VaultItem) {
        viewModelScope.launch {
            item.attachmentPaths.forEach { path ->
                VaultFileHelper.deleteFileAtPath(path)
            }
            repository.deleteVaultItem(item.id)
        }
    }

    class Factory(private val repository: VaultRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(VaultViewModel::class.java)) {
                return VaultViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
