package com.docsalert.reminder.data.repository

import com.docsalert.reminder.data.local.FamilyMemberDao
import com.docsalert.reminder.data.model.FamilyMember
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

class FamilyRepository(private val familyMemberDao: FamilyMemberDao) {

    val members: Flow<List<FamilyMember>> = familyMemberDao.getAllFamilyMembers()

    suspend fun ensureDefaultProfileExists() {
        val all = members.first()
        if (all.none { it.id == "default_user_profile" }) {
            familyMemberDao.insertFamilyMember(
                FamilyMember(
                    id = "default_user_profile",
                    fullName = "My Profile",
                    relationship = "Self",
                    notes = "Automatically created"
                )
            )
        }
    }

    suspend fun addFamilyMember(member: FamilyMember) {
        val memberToInsert = if (member.id.isEmpty()) member.copy(id = java.util.UUID.randomUUID().toString()) else member
        familyMemberDao.insertFamilyMember(memberToInsert)
    }

    suspend fun updateFamilyMember(member: FamilyMember) {
        familyMemberDao.updateFamilyMember(member)
    }

    suspend fun removeFamilyMember(id: String) {
        familyMemberDao.deleteFamilyMember(id)
    }

    suspend fun clearAll() {
        familyMemberDao.deleteAllFamilyMembers()
    }
}
