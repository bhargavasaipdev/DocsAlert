package com.docsalert.reminder.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "family_members")
data class FamilyMember(
    @PrimaryKey
    val id: String,
    val fullName: String,
    val relationship: String,
    val profilePhotoUrl: String? = null,
    val notes: String = ""
)
