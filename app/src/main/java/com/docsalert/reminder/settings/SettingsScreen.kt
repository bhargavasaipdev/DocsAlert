package com.docsalert.reminder.settings

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.docsalert.reminder.R
import com.docsalert.reminder.data.model.NotificationSoundPreset
import com.docsalert.reminder.data.preferences.AppSettings
import com.docsalert.reminder.data.preferences.UserPreferencesRepository
import com.docsalert.reminder.data.repository.CustomReminderRepository
import com.docsalert.reminder.data.repository.FamilyRepository
import com.docsalert.reminder.data.repository.ItemRepository
import com.docsalert.reminder.data.repository.VaultRepository
import com.docsalert.reminder.ui.components.ScreenHeader
import com.docsalert.reminder.ui.components.StaggeredEntrance
import com.docsalert.reminder.ui.theme.palette
import com.docsalert.reminder.util.AppLockSession
import com.docsalert.reminder.util.BackupHelper
import com.docsalert.reminder.util.BiometricAuthHelper
import com.docsalert.reminder.util.InterstitialAdHelper
import com.docsalert.reminder.util.NotificationHelper
import com.docsalert.reminder.util.OverlayPermissionHelper
import com.docsalert.reminder.util.ReminderAlarmActivity
import com.docsalert.reminder.util.ReminderAlarmHelper
import com.docsalert.reminder.util.RewardedAdHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// Settings Screen
// ---------------------------------------------------------------------------

@Composable
fun SettingsScreen(
    preferencesRepository: UserPreferencesRepository,
    itemRepository: ItemRepository? = null,
    familyRepository: FamilyRepository? = null,
    vaultRepository: VaultRepository? = null,
    customReminderRepository: CustomReminderRepository? = null,
    itemCount: Int = 0,
    memberCount: Int = 0,
    onBackClick: (() -> Unit)? = null,
    onThemesClick: () -> Unit = {},
    onNotificationSoundsClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    onRateAppClick: () -> Unit = {},
    onShareAppClick: () -> Unit = {},
    onRemoveAdsClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val palette = MaterialTheme.palette

    val settings by preferencesRepository.appSettings
        .collectAsState(initial = AppSettings())

    val appSettings = settings

    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val success = BackupHelper.exportBackup(
                    context = context,
                    uri = uri,
                    preferencesRepository = preferencesRepository,
                    itemRepository = itemRepository,
                    familyRepository = familyRepository,
                    vaultRepository = vaultRepository,
                    customReminderRepository = customReminderRepository
                )
                if (success) {
                    Toast.makeText(context, "Backup saved successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to create backup", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val restoreBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val success = BackupHelper.importBackup(
                    context = context,
                    uri = uri,
                    preferencesRepository = preferencesRepository,
                    itemRepository = itemRepository,
                    familyRepository = familyRepository,
                    vaultRepository = vaultRepository,
                    customReminderRepository = customReminderRepository
                )
                if (success) {
                    Toast.makeText(context, "Backup restored successfully!", Toast.LENGTH_SHORT).show()
                    (context as? Activity)?.recreate()
                } else {
                    Toast.makeText(context, "Failed to restore backup", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun triggerNotification() {
        NotificationHelper.sendTestNotification(context)
        Toast.makeText(context, "Test Notification Triggered!", Toast.LENGTH_SHORT).show()
    }

    fun formatTimeForDisplay(time24: String): String {
        val parts = time24.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 9
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val period = if (h >= 12) "PM" else "AM"
        val displayHour = when {
            h == 0 -> 12
            h > 12 -> h - 12
            else -> h
        }
        return String.format(java.util.Locale.getDefault(), "%d:%02d %s", displayHour, m, period)
    }

    fun showTimePicker() {
        val parts = appSettings.notificationTime.split(":")
        val initialH = parts.getOrNull(0)?.toIntOrNull() ?: 9
        val initialM = parts.getOrNull(1)?.toIntOrNull() ?: 0

        android.app.TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val time24 = String.format(java.util.Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                scope.launch { preferencesRepository.saveNotificationTime(time24) }
                if (appSettings.notificationsEnabled) {
                    ReminderAlarmHelper.scheduleDailyAlarm(context, time24)
                }
                val formatted = formatTimeForDisplay(time24)
                Toast.makeText(context, "Item reminder time set to $formatted", Toast.LENGTH_SHORT).show()
            },
            initialH,
            initialM,
            false
        ).show()
    }

    val primaryTint = IconTint(palette.oppositeAccent, palette.oppositeAccentContainer)
    val infoTint = IconTint(palette.infoColor, palette.infoBackground)
    val warningTint = IconTint(palette.warningColor, palette.warningBackground)
    val successTint = IconTint(palette.successColor, palette.successBackground)

    // Section 1: Ads (Remove Ads / Premium)
    val adsEntries = listOf(
        SettingsEntry(
            icon = Icons.Default.Block,
            title = "Remove Ads",
            subtitle = "Watch video for Ad-Free Premium Mode",
            tint = warningTint,
            action = EntryAction.Navigate {
                val activity = context as? Activity
                if (activity != null) {
                    RewardedAdHelper.showAd(activity) {
                        Toast.makeText(context, "Ad-Free Premium Mode Activated! Thank you for supporting DocsAlert!", Toast.LENGTH_LONG).show()
                        onRemoveAdsClick()
                    }
                } else {
                    onRemoveAdsClick()
                }
            }
        )
    )

    // Section 2: Preferences (Item Reminder Time, Notification Sound, Theme)
    val preferenceEntries = listOf(
        SettingsEntry(
            icon = Icons.Default.Alarm,
            title = "Item Reminder Time",
            subtitle = "Daily alerts at ${formatTimeForDisplay(appSettings.notificationTime)}",
            tint = primaryTint,
            action = EntryAction.Navigate {
                showTimePicker()
            },
            enabled = true
        ),
        SettingsEntry(
            icon = Icons.Default.MusicNote,
            title = "Notification Sound",
            subtitle = NotificationSoundPreset.fromKey(appSettings.notificationSoundKey).title,
            tint = primaryTint,
            action = EntryAction.Navigate {
                val activity = context as? Activity
                if (activity != null) {
                    RewardedAdHelper.showAd(activity) {
                        onNotificationSoundsClick()
                    }
                } else {
                    onNotificationSoundsClick()
                }
            },
            enabled = true
        ),
        SettingsEntry(
            icon = Icons.Default.Palette,
            title = "Theme",
            subtitle = "App colors & light/dark mode",
            tint = primaryTint,
            action = EntryAction.Navigate {
                val activity = context as? Activity
                if (activity != null) {
                    InterstitialAdHelper.showAd(activity) {
                        onThemesClick()
                    }
                } else {
                    onThemesClick()
                }
            }
        )
    )

    val systemNotifsAllowed = androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()

    // Section 3: Permissions
    val permissionsEntries = listOf(
        SettingsEntry(
            icon = Icons.Default.Notifications,
            title = "Notifications",
            subtitle = if (systemNotifsAllowed) "System notifications allowed" else "Tap to allow notifications in Settings",
            tint = if (systemNotifsAllowed) infoTint else warningTint,
            action = EntryAction.Navigate {
                try {
                    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                    } else {
                        Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = android.net.Uri.fromParts("package", context.packageName, null)
                        }
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Cannot open Settings", Toast.LENGTH_SHORT).show()
                }
            }
        ),
        SettingsEntry(
            icon = Icons.Default.NotificationsActive,
            title = "Full-Screen Expiry Alarm",
            subtitle = "Show overlay when an Item expires",
            tint = warningTint,
            action = EntryAction.Toggle(appSettings.fullScreenAlarmEnabled) { enabled ->
                scope.launch { preferencesRepository.saveFullScreenAlarmEnabled(enabled) }
            }
        ),
        SettingsEntry(
            icon = Icons.Default.NotificationsActive,
            title = "Full-Screen Notifications",
            subtitle = if (OverlayPermissionHelper.canUseFullScreenIntent(context)) "Full-screen overlay active" else "Tap to grant permission",
            tint = primaryTint,
            action = EntryAction.Navigate {
                OverlayPermissionHelper.requestFullScreenIntentPermission(context)
            }
        )
    )

    // Section 4: Testing
    val testingEntries = listOf(
        SettingsEntry(
            icon = Icons.Default.NotificationsActive,
            title = "Send a Test Notification",
            subtitle = "Verify reminder alert notifications",
            tint = infoTint,
            action = EntryAction.Navigate {
                triggerNotification()
            },
            enabled = true
        ),
        SettingsEntry(
            icon = Icons.Default.NotificationsActive,
            title = "Test Expiry Reminders Now",
            subtitle = "Check all Items and trigger due alerts",
            tint = successTint,
            action = EntryAction.Navigate {
                scope.launch(Dispatchers.IO) {
                    ReminderAlarmHelper.checkAndTriggerDueReminders(context)
                }
                Toast.makeText(context, "Checking Item expiries...", Toast.LENGTH_SHORT).show()
            },
            enabled = true
        ),
        SettingsEntry(
            icon = Icons.Default.NotificationsActive,
            title = "Test Full-Screen Expiry Alarm",
            subtitle = if (appSettings.fullScreenAlarmEnabled) "Preview full-screen caller overlay" else "Disabled in Settings (Enable toggle to test)",
            tint = if (appSettings.fullScreenAlarmEnabled) warningTint else IconTint(palette.disabledText, palette.disabledBackground),
            enabled = appSettings.fullScreenAlarmEnabled,
            action = EntryAction.Navigate {
                if (!appSettings.fullScreenAlarmEnabled) {
                    Toast.makeText(context, "Full-Screen Expiry Alarm is disabled in Settings", Toast.LENGTH_SHORT).show()
                    return@Navigate
                }
                val activity = context as? Activity
                val launchAlarm = {
                    val intent = Intent(context, ReminderAlarmActivity::class.java).apply {
                        putExtra("IS_PREVIEW", true)
                        putExtra(ReminderAlarmActivity.EXTRA_DOC_NAME, "Sample Passport")
                        putExtra(ReminderAlarmActivity.EXTRA_DOC_CATEGORY, "Identity")
                        putExtra(ReminderAlarmActivity.EXTRA_EXPIRY_DATE, "Due Today")
                        putExtra(ReminderAlarmActivity.EXTRA_MEMBER_NAME, "Self")
                    }
                    context.startActivity(intent)
                }
                if (activity != null) {
                    InterstitialAdHelper.showAd(activity) {
                        launchAlarm()
                    }
                } else {
                    launchAlarm()
                }
            }
        )
    )

    // Section 5: Security (App Lock & Mask Sensitive Numbers)
    val privacyEntries = listOf(
        SettingsEntry(
            icon = Icons.Default.Lock,
            title = "App Lock (Biometric / PIN)",
            subtitle = if (BiometricAuthHelper.isBiometricAvailable(context)) "Lock DocsAlert when backgrounded" else "Biometric lock unavailable",
            tint = warningTint,
            enabled = BiometricAuthHelper.isBiometricAvailable(context),
            action = EntryAction.Toggle(appSettings.appLockEnabled) { enabled ->
                val activity = context as? androidx.fragment.app.FragmentActivity
                if (enabled) {
                    if (activity != null && BiometricAuthHelper.isBiometricAvailable(context)) {
                        BiometricAuthHelper.promptBiometricAuth(
                            activity = activity,
                            title = "Enable App Lock",
                            subtitle = "Authenticate to enable security lock",
                            onSuccess = {
                                scope.launch { preferencesRepository.saveAppLockEnabled(true) }
                                AppLockSession.isUnlockedInSession = true
                                Toast.makeText(context, "App Lock Enabled", Toast.LENGTH_SHORT).show()
                            },
                            onError = { err ->
                                Toast.makeText(context, "Authentication Failed: $err", Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else {
                        scope.launch { preferencesRepository.saveAppLockEnabled(true) }
                    }
                } else {
                    scope.launch { preferencesRepository.saveAppLockEnabled(false) }
                    AppLockSession.isUnlockedInSession = false
                    Toast.makeText(context, "App Lock Disabled", Toast.LENGTH_SHORT).show()
                }
            }
        ),
        SettingsEntry(
            icon = Icons.Default.VisibilityOff,
            title = "Mask Sensitive Numbers",
            subtitle = "Hide Aadhaar & PAN numbers by default",
            tint = primaryTint,
            action = EntryAction.Toggle(appSettings.maskItemNumbers) { enabled ->
                scope.launch { preferencesRepository.saveMaskItemNumbers(enabled) }
            }
        )
    )

    // Section 6: Backup & Restore
    val backupEntries = listOf(
        SettingsEntry(
            icon = Icons.Default.Download,
            title = "Export Backup (JSON)",
            subtitle = "Save all Items & settings to file",
            tint = successTint,
            action = EntryAction.Navigate {
                createBackupLauncher.launch("DocsAlert_Backup_${System.currentTimeMillis()}.json")
            }
        ),
        SettingsEntry(
            icon = Icons.Default.Upload,
            title = "Import Backup (JSON)",
            subtitle = "Restore Items & settings from file",
            tint = primaryTint,
            action = EntryAction.Navigate {
                restoreBackupLauncher.launch(arrayOf("application/json", "*/*"))
            }
        )
    )

    // Section 7: About
    val aboutEntries = listOf(
        SettingsEntry(
            icon = Icons.Default.Info,
            title = "About DocsAlert",
            subtitle = "App version & information",
            tint = primaryTint,
            action = EntryAction.Navigate(onAboutClick)
        )
    )

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Settings",
            subtitle = "Preferences and security",
            onBackClick = onBackClick
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { StaggeredEntrance(0) { AppProfileCard(itemCount, memberCount) } }
            item { StaggeredEntrance(1) { com.docsalert.reminder.ui.components.BannerAdView(modifier = Modifier.padding(vertical = 4.dp)) } }
            item { StaggeredEntrance(2) { SettingsSection("Ads", adsEntries) } }
            item { StaggeredEntrance(3) { SettingsSection("Preferences", preferenceEntries) } }
            item { StaggeredEntrance(4) { SettingsSection("Permissions", permissionsEntries) } }
            item { StaggeredEntrance(5) { SettingsSection("Testing", testingEntries) } }
            item { StaggeredEntrance(6) { SettingsSection("Security", privacyEntries) } }
            item { StaggeredEntrance(7) { SettingsSection("Backup & Restore", backupEntries) } }
            item { StaggeredEntrance(8) { SettingsSection("About", aboutEntries) } }
        }
    }
}

// ---------------------------------------------------------------------------
// Profile card
// ---------------------------------------------------------------------------

@Composable
private fun AppProfileCard(ItemCount: Int, memberCount: Int) {
    val palette = MaterialTheme.palette
    val shape = RoundedCornerShape(24.dp)

    val ItemsText = if (ItemCount == 1) "1 item saved" else "$ItemCount items saved"
    val MembersText = if (memberCount == 1) "1 profile" else "$memberCount profiles"

    Card(
        colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
        shape = shape,
        border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(60.dp),
                shape = CircleShape,
                color = palette.accentContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "DocsAlert Logo",
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "DocsAlert",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = palette.headingText
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "$ItemsText • $MembersText",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.secondaryText
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Settings section container
// ---------------------------------------------------------------------------

@Composable
private fun SettingsSection(
    title: String,
    entries: List<SettingsEntry>
) {
    val palette = MaterialTheme.palette
    val shape = RoundedCornerShape(24.dp)

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 4.dp, height = 16.dp)
                    .clip(RoundedCornerShape(50))
                    .background(palette.primaryAccent)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                ),
                color = palette.headingText
            )
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
            shape = shape,
            border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                entries.forEachIndexed { index, entry ->
                    SettingsRow(entry, showDivider = index < entries.lastIndex)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Individual row
// ---------------------------------------------------------------------------

private sealed interface EntryAction {
    data class Toggle(val checked: Boolean, val onCheckedChange: (Boolean) -> Unit) : EntryAction
    data class Navigate(val onNavigate: () -> Unit) : EntryAction
}

private data class IconTint(val content: Color, val container: Color)

private data class SettingsEntry(
    val icon: ImageVector,
    val title: String,
    val subtitle: String? = null,
    val tint: IconTint,
    val action: EntryAction,
    val enabled: Boolean = true
)

@Composable
private fun SettingsRow(
    entry: SettingsEntry,
    showDivider: Boolean
) {
    val palette = MaterialTheme.palette
    val interactionSource = remember { MutableInteractionSource() }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = entry.enabled && entry.action is EntryAction.Navigate,
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    onClick = {
                        (entry.action as? EntryAction.Navigate)?.onNavigate?.invoke()
                    }
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(entry.tint.container),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = entry.icon,
                    contentDescription = null,
                    tint = entry.tint.content,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (entry.enabled) palette.headingText else palette.disabledText
                )
                if (!entry.subtitle.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = entry.subtitle,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = if (entry.enabled) palette.secondaryText else palette.disabledText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            when (val action = entry.action) {
                is EntryAction.Toggle -> {
                    Switch(
                        checked = action.checked,
                        onCheckedChange = action.onCheckedChange,
                        enabled = entry.enabled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = palette.onOppositeAccent,
                            checkedTrackColor = palette.oppositeAccent,
                            checkedBorderColor = palette.oppositeAccent,
                            uncheckedThumbColor = palette.secondaryText,
                            uncheckedTrackColor = palette.inputBackground,
                            disabledCheckedThumbColor = palette.disabledText,
                            disabledCheckedTrackColor = palette.disabledBackground
                        )
                    )
                }

                is EntryAction.Navigate -> {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = if (entry.enabled) palette.secondaryText else palette.disabledText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 72.dp),
                color = palette.dividerColor.copy(alpha = 0.65f)
            )
        }
    }
}
