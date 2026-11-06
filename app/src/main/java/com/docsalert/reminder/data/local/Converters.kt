package com.docsalert.reminder.data.local

import androidx.room.TypeConverter
import com.docsalert.reminder.data.model.ItemCategory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun fromLocalDate(value: LocalDate?): Long? {
        return value?.toEpochDay()
    }

    @TypeConverter
    fun toLocalDate(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(it) }
    }

    @TypeConverter
    fun fromItemCategory(category: ItemCategory?): String? {
        return category?.name
    }

    @TypeConverter
    fun toItemCategory(name: String?): ItemCategory? {
        return name?.let { enumValueOf<ItemCategory>(it) }
    }

    @TypeConverter
    fun fromVaultItemType(type: com.docsalert.reminder.data.model.VaultItemType?): String? {
        return type?.name
    }

    @TypeConverter
    fun toVaultItemType(name: String?): com.docsalert.reminder.data.model.VaultItemType? {
        return name?.let { enumValueOf<com.docsalert.reminder.data.model.VaultItemType>(it) }
    }

    @TypeConverter
    fun fromIntList(list: List<Int>?): String? {
        if (list == null) return null
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun toIntList(data: String?): List<Int>? {
        if (data == null) return null
        val listType = object : TypeToken<List<Int>>() {}.type
        val gson = Gson()
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        if (list == null) return null
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun toStringList(data: String?): List<String>? {
        if (data == null) return null
        val listType = object : TypeToken<List<String>>() {}.type
        val gson = Gson()
        return gson.fromJson(data, listType)
    }
}
