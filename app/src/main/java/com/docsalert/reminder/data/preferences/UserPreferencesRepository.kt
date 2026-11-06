package com.docsalert.reminder.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.docsalert.reminder.ui.theme.AppearanceMode
import com.docsalert.reminder.ui.theme.ThemePreset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preferences"
)

/** Theme choices saved by the user. */
data class ThemeSettings(
    val preset: ThemePreset = ThemePreset.DEFAULT_BLUE,
    val appearance: AppearanceMode = AppearanceMode.SYSTEM
)

/** Preference toggles saved by the user, shown on the Settings screen. */
data class AppSettings(
    val notificationsEnabled: Boolean = true,
    val notificationTime: String = "09:00",
    val fullScreenAlarmEnabled: Boolean = true,
    val appLockEnabled: Boolean = false,
    val maskItemNumbers: Boolean = false,
    val languageCode: String = "en",
    val notificationSoundKey: String = "ai_syntax_1",
    val onboardingCompleted: Boolean = false
)

class UserPreferencesRepository(
    context: Context
) {
    private val dataStore = context.applicationContext.dataStore

    private object Keys {
        val THEME_PRESET = stringPreferencesKey("theme_preset")
        val APPEARANCE_MODE = stringPreferencesKey("appearance_mode")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val NOTIFICATION_TIME = stringPreferencesKey("notification_time")
        val FULL_SCREEN_ALARM_ENABLED = booleanPreferencesKey("full_screen_alarm_enabled")
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val MASK_ITEM_NUMBERS = booleanPreferencesKey("mask_item_numbers")
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
        val NOTIFICATION_SOUND_KEY = stringPreferencesKey("notification_sound_key")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    private val safeData: Flow<Preferences> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }

    val themeSettings: Flow<ThemeSettings> = safeData
        .map { prefs ->
            ThemeSettings(
                preset = ThemePreset.fromId(prefs[Keys.THEME_PRESET]),
                appearance = AppearanceMode.fromName(prefs[Keys.APPEARANCE_MODE])
            )
        }
        .distinctUntilChanged()

    val appSettings: Flow<AppSettings> = safeData
        .map { prefs ->
            AppSettings(
                notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
                notificationTime = prefs[Keys.NOTIFICATION_TIME] ?: "09:00",
                fullScreenAlarmEnabled = prefs[Keys.FULL_SCREEN_ALARM_ENABLED] ?: true,
                appLockEnabled = prefs[Keys.APP_LOCK_ENABLED] ?: false,
                maskItemNumbers = prefs[Keys.MASK_ITEM_NUMBERS] ?: false,
                languageCode = prefs[Keys.LANGUAGE_CODE] ?: "en",
                notificationSoundKey = prefs[Keys.NOTIFICATION_SOUND_KEY] ?: "ai_syntax_1",
                onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false
            )
        }
        .distinctUntilChanged()

    suspend fun saveLanguageCode(languageCode: String) {
        dataStore.edit { it[Keys.LANGUAGE_CODE] = languageCode }
    }

    suspend fun saveThemePreset(preset: ThemePreset) {
        dataStore.edit { it[Keys.THEME_PRESET] = preset.id }
    }

    suspend fun saveAppearanceMode(mode: AppearanceMode) {
        dataStore.edit { it[Keys.APPEARANCE_MODE] = mode.name }
    }

    suspend fun saveNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun saveNotificationTime(time: String) {
        dataStore.edit { it[Keys.NOTIFICATION_TIME] = time }
    }

    suspend fun saveFullScreenAlarmEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.FULL_SCREEN_ALARM_ENABLED] = enabled }
    }

    suspend fun saveAppLockEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.APP_LOCK_ENABLED] = enabled }
    }

    suspend fun saveMaskItemNumbers(enabled: Boolean) {
        dataStore.edit { it[Keys.MASK_ITEM_NUMBERS] = enabled }
    }

    suspend fun saveNotificationSoundKey(soundKey: String) {
        dataStore.edit { it[Keys.NOTIFICATION_SOUND_KEY] = soundKey }
    }

    suspend fun saveOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun resetAll() {
        dataStore.edit { it.clear() }
    }
}
