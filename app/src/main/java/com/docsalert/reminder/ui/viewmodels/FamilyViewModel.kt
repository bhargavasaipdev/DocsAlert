package com.docsalert.reminder.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.docsalert.reminder.data.model.FamilyMember
import com.docsalert.reminder.data.repository.FamilyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FamilyViewModel(private val repository: FamilyRepository) : ViewModel() {
    
    val members: StateFlow<List<FamilyMember>> = repository.members
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    fun addFamilyMember(member: FamilyMember) {
        viewModelScope.launch { repository.addFamilyMember(member) }
    }
    
    fun removeFamilyMember(id: String) {
        viewModelScope.launch { repository.removeFamilyMember(id) }
    }
    
    class Factory(private val repository: FamilyRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FamilyViewModel::class.java)) {
                return FamilyViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
