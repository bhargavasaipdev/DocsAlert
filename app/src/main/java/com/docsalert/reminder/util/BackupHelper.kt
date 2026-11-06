package com.docsalert.reminder.util

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.docsalert.reminder.data.model.CustomReminder
import com.docsalert.reminder.data.model.FamilyMember
import com.docsalert.reminder.data.model.Item
import com.docsalert.reminder.data.model.ItemCategory
import com.docsalert.reminder.data.model.VaultItem
import com.docsalert.reminder.data.model.VaultItemType
import com.docsalert.reminder.data.preferences.AppSettings
import com.docsalert.reminder.data.preferences.ThemeSettings
import com.docsalert.reminder.data.preferences.UserPreferencesRepository
import com.docsalert.reminder.data.repository.CustomReminderRepository
import com.docsalert.reminder.data.repository.FamilyRepository
import com.docsalert.reminder.data.repository.ItemRepository
import com.docsalert.reminder.data.repository.VaultRepository
import com.docsalert.reminder.ui.theme.AppearanceMode
import com.docsalert.reminder.ui.theme.ThemePreset
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDate

data class CompleteBackupData(
    val items: List<Item>,
    val familyMembers: List<FamilyMember>,
    val vaultItems: List<VaultItem>,
    val customReminders: List<CustomReminder>,
    val appSettings: AppSettings?,
    val themeSettings: ThemeSettings?
)

object BackupHelper {
    private const val TAG = "BackupHelper"

    suspend fun exportBackup(
        context: Context,
        uri: Uri,
        preferencesRepository: UserPreferencesRepository,
        itemRepository: ItemRepository?,
        familyRepository: FamilyRepository?,
        vaultRepository: VaultRepository? = null,
        customReminderRepository: CustomReminderRepository? = null
    ): Boolean {
        return try {
            val items = itemRepository?.items?.first() ?: emptyList()
            val family = familyRepository?.members?.first() ?: emptyList()
            val vaultItems = vaultRepository?.vaultItems?.first() ?: emptyList()
            val reminders = customReminderRepository?.reminders?.first() ?: emptyList()
            val appSettings = preferencesRepository.appSettings.first()
            val themeSettings = preferencesRepository.themeSettings.first()

            val jsonString = exportToJson(
                context = context,
                items = items,
                familyMembers = family,
                vaultItems = vaultItems,
                customReminders = reminders,
                appSettings = appSettings,
                themeSettings = themeSettings
            )

            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(jsonString.toByteArray(Charsets.UTF_8))
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting backup", e)
            false
        }
    }

    suspend fun importBackup(
        context: Context,
        uri: Uri,
        preferencesRepository: UserPreferencesRepository,
        itemRepository: ItemRepository?,
        familyRepository: FamilyRepository?,
        vaultRepository: VaultRepository? = null,
        customReminderRepository: CustomReminderRepository? = null
    ): Boolean {
        return try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader(Charsets.UTF_8).readText()
            } ?: return false

            val backup = importFromJson(context, jsonString)

            // 1. Restore Family Members
            if (backup.familyMembers.isNotEmpty()) {
                backup.familyMembers.forEach { familyRepository?.addFamilyMember(it) }
            }

            // 2. Restore Items
            if (backup.items.isNotEmpty()) {
                backup.items.forEach { itemRepository?.addItem(it) }
            }

            // 3. Restore Vault Items & Media Files
            if (backup.vaultItems.isNotEmpty()) {
                backup.vaultItems.forEach { vaultRepository?.saveVaultItem(it) }
            }

            // 4. Restore Custom Daily Reminders & Reschedule Alarms
            if (backup.customReminders.isNotEmpty()) {
                backup.customReminders.forEach { reminder ->
                    customReminderRepository?.saveReminder(reminder)
                    if (reminder.enabled) {
                        CustomReminderScheduler.scheduleReminder(context, reminder)
                    }
                }
            }

            // 5. Restore App Settings & Preferences
            backup.appSettings?.let { settings ->
                preferencesRepository.saveNotificationsEnabled(settings.notificationsEnabled)
                preferencesRepository.saveNotificationTime(settings.notificationTime)
                preferencesRepository.saveFullScreenAlarmEnabled(settings.fullScreenAlarmEnabled)
                preferencesRepository.saveAppLockEnabled(settings.appLockEnabled)
                preferencesRepository.saveMaskItemNumbers(settings.maskItemNumbers)
                preferencesRepository.saveLanguageCode(settings.languageCode)
                preferencesRepository.saveNotificationSoundKey(settings.notificationSoundKey)

                // Update notification channels & reschedule daily alarm
                NotificationHelper.createNotificationChannel(context, settings.notificationSoundKey)
                ReminderAlarmHelper.scheduleDailyAlarm(context, settings.notificationTime)
            }

            // 6. Restore Theme Settings
            backup.themeSettings?.let { theme ->
                preferencesRepository.saveThemePreset(theme.preset)
                preferencesRepository.saveAppearanceMode(theme.appearance)
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error importing backup", e)
            false
        }
    }

    fun exportToJson(
        context: Context,
        items: List<Item>,
        familyMembers: List<FamilyMember>,
        vaultItems: List<VaultItem> = emptyList(),
        customReminders: List<CustomReminder> = emptyList(),
        appSettings: AppSettings? = null,
        themeSettings: ThemeSettings? = null
    ): String {
        val root = JSONObject()

        // 1. Family Members + Profile Photos
        val familyArray = JSONArray()
        familyMembers.forEach { member ->
            val obj = JSONObject().apply {
                put("id", member.id)
                put("fullName", member.fullName)
                put("relationship", member.relationship)
                put("profilePhotoUrl", member.profilePhotoUrl ?: "")
                put("notes", member.notes)

                // Embed profile photo as Base64 if it points to a valid file
                member.profilePhotoUrl?.let { photoPath ->
                    val file = File(photoPath)
                    if (file.exists()) {
                        fileToBase64(file)?.let { b64 ->
                            put("profilePhotoFileName", file.name)
                            put("profilePhotoData", b64)
                        }
                    }
                }
            }
            familyArray.put(obj)
        }
        root.put("familyMembers", familyArray)

        // 2. Items
        val itemsArray = JSONArray()
        items.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("name", item.name)
                put("category", item.category.name)
                put("itemNumber", item.itemNumber ?: "")
                put("issueDate", item.issueDate?.toString() ?: "")
                put("expiryDate", item.expiryDate?.toString() ?: "")
                put("remindersEnabled", item.remindersEnabled)
                put("notes", item.notes)
                put("familyMemberId", item.familyMemberId ?: "default_user_profile")

                val reminderDaysArray = JSONArray()
                item.reminderDaysBefore.forEach { reminderDaysArray.put(it) }
                put("reminderDaysBefore", reminderDaysArray)
            }
            itemsArray.put(obj)
        }
        root.put("items", itemsArray)

        // 3. Vault Items + Media Files (Images / PDFs embedded as Base64)
        val vaultArray = JSONArray()
        vaultItems.forEach { vault ->
            val obj = JSONObject().apply {
                put("id", vault.id)
                put("name", vault.name)
                put("type", vault.type.name)
                put("itemNumber", vault.itemNumber ?: "")
                put("expiryDate", vault.expiryDate?.toString() ?: "")
                put("notes", vault.notes)

                val attachmentsArray = JSONArray()
                vault.attachmentPaths.forEach { filePath ->
                    val file = File(filePath)
                    if (file.exists()) {
                        fileToBase64(file)?.let { b64 ->
                            val fileObj = JSONObject().apply {
                                put("fileName", file.name)
                                put("base64Data", b64)
                            }
                            attachmentsArray.put(fileObj)
                        }
                    }
                }
                put("attachments", attachmentsArray)
            }
            vaultArray.put(obj)
        }
        root.put("vaultItems", vaultArray)

        // 4. Custom Daily Reminders
        val reminderArray = JSONArray()
        customReminders.forEach { reminder ->
            val obj = JSONObject().apply {
                put("id", reminder.id)
                put("title", reminder.title)
                put("description", reminder.description ?: "")
                put("hour", reminder.hour)
                put("minute", reminder.minute)
                put("enabled", reminder.enabled)

                val daysArray = JSONArray()
                reminder.repeatDays.forEach { daysArray.put(it) }
                put("repeatDays", daysArray)
            }
            reminderArray.put(obj)
        }
        root.put("customReminders", reminderArray)

        // 5. App Preferences
        appSettings?.let { settings ->
            val settingsObj = JSONObject().apply {
                put("notificationsEnabled", settings.notificationsEnabled)
                put("notificationTime", settings.notificationTime)
                put("fullScreenAlarmEnabled", settings.fullScreenAlarmEnabled)
                put("appLockEnabled", settings.appLockEnabled)
                put("maskItemNumbers", settings.maskItemNumbers)
                put("languageCode", settings.languageCode)
                put("notificationSoundKey", settings.notificationSoundKey)
            }
            root.put("appSettings", settingsObj)
        }

        // 6. Theme Settings
        themeSettings?.let { theme ->
            val themeObj = JSONObject().apply {
                put("themePreset", theme.preset.id)
                put("appearanceMode", theme.appearance.name)
            }
            root.put("themeSettings", themeObj)
        }

        return root.toString(2)
    }

    fun importFromJson(context: Context, jsonString: String): CompleteBackupData {
        val root = JSONObject(jsonString)

        // 1. Restore Family Members & Profile Photos
        val importedFamily = mutableListOf<FamilyMember>()
        if (root.has("familyMembers")) {
            val familyArray = root.getJSONArray("familyMembers")
            val photosDir = File(context.filesDir, "profile_photos")
            if (!photosDir.exists()) photosDir.mkdirs()

            for (i in 0 until familyArray.length()) {
                val obj = familyArray.getJSONObject(i)
                var photoPath: String? = null

                if (obj.has("profilePhotoData") && obj.has("profilePhotoFileName")) {
                    val fileName = obj.getString("profilePhotoFileName")
                    val b64 = obj.getString("profilePhotoData")
                    val targetFile = File(photosDir, fileName)
                    if (base64ToFile(b64, targetFile)) {
                        photoPath = targetFile.absolutePath
                    }
                }

                if (photoPath == null && obj.has("profilePhotoUrl") && !obj.isNull("profilePhotoUrl")) {
                    photoPath = obj.getString("profilePhotoUrl").ifEmpty { null }
                }

                importedFamily.add(
                    FamilyMember(
                        id = obj.optString("id", System.currentTimeMillis().toString()),
                        fullName = obj.optString("fullName", "Family Member"),
                        relationship = obj.optString("relationship", "Other"),
                        profilePhotoUrl = photoPath,
                        notes = obj.optString("notes", "")
                    )
                )
            }
        }

        // 2. Restore Items
        val importedItems = mutableListOf<Item>()
        if (root.has("items") || root.has("documents")) {
            val itemsArray = if (root.has("items")) root.getJSONArray("items") else root.getJSONArray("documents")
            for (i in 0 until itemsArray.length()) {
                val obj = itemsArray.getJSONObject(i)
                val categoryName = obj.optString("category", "OTHER")
                val category = try { ItemCategory.valueOf(categoryName) } catch (e: Exception) { ItemCategory.OTHER }
                val issueStr = obj.optString("issueDate", "")
                val issueDate = if (issueStr.isNotBlank()) LocalDate.parse(issueStr) else null
                val expiryStr = obj.optString("expiryDate", "")
                val expiryDate = if (expiryStr.isNotBlank()) LocalDate.parse(expiryStr) else null

                val itemNum = if (obj.has("itemNumber") && !obj.isNull("itemNumber")) {
                    obj.getString("itemNumber")
                } else if (obj.has("documentNumber") && !obj.isNull("documentNumber")) {
                    obj.getString("documentNumber")
                } else null

                val familyId = if (obj.has("familyMemberId") && !obj.isNull("familyMemberId")) obj.getString("familyMemberId") else "default_user_profile"

                val reminderDaysList = mutableListOf<Int>()
                if (obj.has("reminderDaysBefore")) {
                    val arr = obj.getJSONArray("reminderDaysBefore")
                    for (j in 0 until arr.length()) {
                        reminderDaysList.add(arr.getInt(j))
                    }
                }

                importedItems.add(
                    Item(
                        id = obj.optString("id", System.currentTimeMillis().toString()),
                        name = obj.optString("name", "Imported Item"),
                        category = category,
                        itemNumber = itemNum?.ifEmpty { null },
                        issueDate = issueDate,
                        expiryDate = expiryDate,
                        remindersEnabled = obj.optBoolean("remindersEnabled", true),
                        reminderDaysBefore = reminderDaysList,
                        notes = obj.optString("notes", ""),
                        familyMemberId = familyId.ifEmpty { "default_user_profile" }
                    )
                )
            }
        }

        // 3. Restore Vault Items & Attachment Files
        val importedVaultItems = mutableListOf<VaultItem>()
        if (root.has("vaultItems")) {
            val vaultArray = root.getJSONArray("vaultItems")
            val vaultDir = File(context.filesDir, "vault_attachments")
            if (!vaultDir.exists()) vaultDir.mkdirs()

            for (i in 0 until vaultArray.length()) {
                val obj = vaultArray.getJSONObject(i)
                val typeName = obj.optString("type", "OTHER")
                val type = try { VaultItemType.valueOf(typeName) } catch (e: Exception) { VaultItemType.OTHER }
                val expiryStr = obj.optString("expiryDate", "")
                val expiryDate = if (expiryStr.isNotBlank()) LocalDate.parse(expiryStr) else null

                val attachmentPaths = mutableListOf<String>()
                if (obj.has("attachments")) {
                    val arr = obj.getJSONArray("attachments")
                    for (j in 0 until arr.length()) {
                        val fileObj = arr.getJSONObject(j)
                        val fileName = fileObj.getString("fileName")
                        val b64 = fileObj.getString("base64Data")
                        val targetFile = File(vaultDir, fileName)
                        if (base64ToFile(b64, targetFile)) {
                            attachmentPaths.add(targetFile.absolutePath)
                        }
                    }
                }

                importedVaultItems.add(
                    VaultItem(
                        id = obj.optString("id", System.currentTimeMillis().toString()),
                        name = obj.optString("name", "Imported Vault Item"),
                        type = type,
                        itemNumber = obj.optString("itemNumber", "").ifEmpty { null },
                        expiryDate = expiryDate,
                        notes = obj.optString("notes", ""),
                        attachmentPaths = attachmentPaths
                    )
                )
            }
        }

        // 4. Restore Custom Daily Reminders
        val importedReminders = mutableListOf<CustomReminder>()
        if (root.has("customReminders")) {
            val remArray = root.getJSONArray("customReminders")
            for (i in 0 until remArray.length()) {
                val obj = remArray.getJSONObject(i)
                val repeatDaysList = mutableListOf<Int>()
                if (obj.has("repeatDays")) {
                    val arr = obj.getJSONArray("repeatDays")
                    for (j in 0 until arr.length()) {
                        repeatDaysList.add(arr.getInt(j))
                    }
                }

                importedReminders.add(
                    CustomReminder(
                        id = obj.optInt("id", 0),
                        title = obj.optString("title", "Daily Reminder"),
                        description = obj.optString("description", ""),
                        hour = obj.optInt("hour", 9),
                        minute = obj.optInt("minute", 0),
                        repeatDays = repeatDaysList,
                        enabled = obj.optBoolean("enabled", true)
                    )
                )
            }
        }

        // 5. Restore App Preferences
        var appSettings: AppSettings? = null
        if (root.has("appSettings")) {
            val sObj = root.getJSONObject("appSettings")
            appSettings = AppSettings(
                notificationsEnabled = sObj.optBoolean("notificationsEnabled", true),
                notificationTime = sObj.optString("notificationTime", "09:00"),
                fullScreenAlarmEnabled = sObj.optBoolean("fullScreenAlarmEnabled", true),
                appLockEnabled = sObj.optBoolean("appLockEnabled", false),
                maskItemNumbers = sObj.optBoolean("maskDocumentNumbers", sObj.optBoolean("maskItemNumbers", false)),
                languageCode = sObj.optString("languageCode", "en"),
                notificationSoundKey = sObj.optString("notificationSoundKey", "ai_syntax_1")
            )
        }

        // 6. Restore Theme Settings
        var themeSettings: ThemeSettings? = null
        if (root.has("themeSettings")) {
            val tObj = root.getJSONObject("themeSettings")
            val presetId = tObj.optString("themePreset", "default_blue")
            val appearanceStr = tObj.optString("appearanceMode", "SYSTEM")
            themeSettings = ThemeSettings(
                preset = ThemePreset.fromId(presetId),
                appearance = AppearanceMode.fromName(appearanceStr)
            )
        }

        return CompleteBackupData(
            items = importedItems,
            familyMembers = importedFamily,
            vaultItems = importedVaultItems,
            customReminders = importedReminders,
            appSettings = appSettings,
            themeSettings = themeSettings
        )
    }

    private fun fileToBase64(file: File): String? {
        return try {
            if (!file.exists()) return null
            val bytes = file.readBytes()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to encode file ${file.name} to Base64", e)
            null
        }
    }

    private fun base64ToFile(base64Str: String, targetFile: File): Boolean {
        return try {
            val bytes = Base64.decode(base64Str, Base64.NO_WRAP)
            targetFile.parentFile?.let { if (!it.exists()) it.mkdirs() }
            targetFile.writeBytes(bytes)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode Base64 into file ${targetFile.name}", e)
            false
        }
    }
}
