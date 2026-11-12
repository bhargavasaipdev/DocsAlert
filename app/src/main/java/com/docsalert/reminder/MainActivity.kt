package com.docsalert.reminder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import com.docsalert.reminder.data.preferences.AppSettings
import com.docsalert.reminder.data.preferences.ThemeSettings
import com.docsalert.reminder.ui.theme.DocsAlertTheme
import com.docsalert.reminder.util.LanguageHelper
import com.google.android.gms.ads.MobileAds

class MainActivity : FragmentActivity() {

    // Splash screen stays visible until the saved theme is loaded (no wrong-theme flash).
    private var themeReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        try {
            MobileAds.initialize(this) {
                try {
                    com.docsalert.reminder.util.RewardedAdHelper.loadAd(this)
                    com.docsalert.reminder.util.InterstitialAdHelper.loadAd(this)
                } catch (e: Exception) {
                    // Ignore ad loading exceptions
                }
            }
        } catch (e: Exception) {
            // Ignore MobileAds initialization exceptions
        }
        
        enableEdgeToEdge()

        // Auto-request POST_NOTIFICATIONS permission on Android 13+ if missing
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }

        splashScreen.setKeepOnScreenCondition { !themeReady }

        val appContainer = AppContainer(applicationContext)

        setContent {
            val settings: ThemeSettings? by appContainer.userPreferencesRepository.themeSettings
                .collectAsState(initial = null)
            val appSettings: AppSettings? by appContainer.userPreferencesRepository.appSettings
                .collectAsState(initial = null)

            val loadedSettings = settings
            LaunchedEffect(loadedSettings) {
                if (loadedSettings != null) themeReady = true
            }

            LaunchedEffect(appSettings?.languageCode) {
                appSettings?.languageCode?.let { langCode ->
                    LanguageHelper.applyLanguage(this@MainActivity, langCode)
                }
            }

            if (loadedSettings != null) {
                DocsAlertTheme(
                    preset = loadedSettings.preset,
                    appearance = loadedSettings.appearance
                ) {
                    DocsAlertApp(appContainer = appContainer)
                }
            }
        }
    }
}
