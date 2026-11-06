package com.docsalert.reminder.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

data class AppLanguage(
    val code: String,
    val name: String,
    val nativeName: String
)

object LanguageHelper {

    val supportedLanguages = listOf(
        AppLanguage("en", "English", "English"),
        AppLanguage("es", "Spanish", "Español"),
        AppLanguage("hi", "Hindi", "हिन्दी"),
        AppLanguage("fr", "French", "Français"),
        AppLanguage("de", "German", "Deutsch")
    )

    fun applyLanguage(context: Context, languageCode: String) {
        val locale = java.util.Locale(languageCode)
        java.util.Locale.setDefault(locale)

        val resources = context.resources
        val config = resources.configuration
        config.setLocale(locale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)

        val appLocales = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocales)
    }
}
