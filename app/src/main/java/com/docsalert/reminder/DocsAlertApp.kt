package com.docsalert.reminder

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.docsalert.reminder.calendar.CalendarScreen
import com.docsalert.reminder.data.model.VaultItem
import com.docsalert.reminder.family.AddFamilyMemberScreen
import com.docsalert.reminder.family.FamilyScreen
import com.docsalert.reminder.home.HomeScreen
import com.docsalert.reminder.items.AddItemScreen
import com.docsalert.reminder.items.ItemsScreen
import com.docsalert.reminder.navigation.AppBottomBar
import com.docsalert.reminder.navigation.AppScreen
import com.docsalert.reminder.settings.AboutScreen
import com.docsalert.reminder.settings.NotificationSoundScreen
import com.docsalert.reminder.settings.SettingsScreen
import com.docsalert.reminder.settings.ThemeSettingsScreen
import com.docsalert.reminder.ui.components.AppLockOverlay
import com.docsalert.reminder.ui.components.ItemDetailDialog
import com.docsalert.reminder.ui.onboarding.OnboardingScreen
import com.docsalert.reminder.ui.theme.palette
import com.docsalert.reminder.util.AppLockSession
import com.docsalert.reminder.util.BiometricAuthHelper
import com.docsalert.reminder.util.ReminderAlarmHelper
import com.docsalert.reminder.util.ReminderReceiver
import com.docsalert.reminder.util.ReminderWorker
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

private val settingsChildScreens = setOf(AppScreen.THEMES, AppScreen.NOTIFICATION_SOUNDS, AppScreen.ABOUT)

@Composable
fun DocsAlertApp(appContainer: AppContainer) {
    val palette = MaterialTheme.palette
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val appSettingsState = appContainer.userPreferencesRepository.appSettings.collectAsState(initial = null)
    val appSettings = appSettingsState.value

    val needsLock = appSettings?.appLockEnabled == true && !AppLockSession.isUnlockedInSession
    var isLocked by remember { mutableStateOf(needsLock) }

    LaunchedEffect(appSettings?.appLockEnabled) {
        if (appSettings?.appLockEnabled == true && !AppLockSession.isUnlockedInSession) {
            isLocked = true
        }
    }

    LaunchedEffect(appSettings?.notificationTime) {
        appContainer.familyRepository.ensureDefaultProfileExists()

        // Ensure legacy work managers are cancelled
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork("ItemReminderWork")

        appSettings?.notificationTime?.let { timeStr ->
            ReminderAlarmHelper.scheduleDailyAlarm(context, timeStr)
        }
    }

    var currentScreen by rememberSaveable { mutableStateOf(AppScreen.HOME) }
    var previousScreen by rememberSaveable { mutableStateOf<AppScreen?>(null) }
    var selectedFamilyMemberId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedItemId by rememberSaveable { mutableStateOf<String?>(null) }
    var viewingItemId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedVaultItemId by rememberSaveable { mutableStateOf<String?>(null) }
    var viewingVaultDoc by remember { mutableStateOf<VaultItem?>(null) }
    var selectedReminderId by rememberSaveable { mutableStateOf<Int?>(null) }
    var initialStatusFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var initialCategoryName by rememberSaveable { mutableStateOf<String?>(null) }
    var onboardingChecked by remember { mutableStateOf(false) }

    LaunchedEffect(appSettings?.onboardingCompleted) {
        if (!onboardingChecked && appSettings != null) {
            onboardingChecked = true
            if (!appSettings.onboardingCompleted) {
                currentScreen = AppScreen.ONBOARDING
            }
        }
    }

    LaunchedEffect(Unit) {
        val activity = context as? android.app.Activity
        val targetScreen = activity?.intent?.getStringExtra("OPEN_SCREEN")
        if (targetScreen == "REMINDERS") {
            currentScreen = AppScreen.REMINDERS
        } else if (targetScreen == "ITEMS") {
            currentScreen = AppScreen.ITEMS
        }
    }

    if (isLocked) {
        AppLockOverlay(
            onUnlockClick = {
                val activity = context as? FragmentActivity
                if (activity != null && BiometricAuthHelper.isBiometricAvailable(context)) {
                    BiometricAuthHelper.promptBiometricAuth(
                        activity = activity,
                        onSuccess = {
                            AppLockSession.isUnlockedInSession = true
                            isLocked = false
                        },
                        onError = { /* Keep locked */ }
                    )
                } else {
                    AppLockSession.isUnlockedInSession = true
                    isLocked = false
                }
            }
        )
    } else {
        BackHandler(enabled = currentScreen != AppScreen.HOME && currentScreen != AppScreen.ONBOARDING) {
            if (currentScreen == AppScreen.ADD_FAMILY_MEMBER) {
                selectedFamilyMemberId = null
            }
            if (currentScreen == AppScreen.ADD_ITEM) {
                selectedItemId = null
            }
            if (currentScreen == AppScreen.ADD_VAULT_ITEM) {
                selectedVaultItemId = null
            }
            if (currentScreen == AppScreen.ADD_REMINDER) {
                selectedReminderId = null
            }
            currentScreen = when (currentScreen) {
                in settingsChildScreens -> AppScreen.SETTINGS
                AppScreen.VAULT, AppScreen.REMINDERS -> AppScreen.MORE
                AppScreen.ADD_VAULT_ITEM -> previousScreen ?: AppScreen.VAULT
                AppScreen.ADD_REMINDER -> previousScreen ?: AppScreen.REMINDERS
                AppScreen.ADD_ITEM, AppScreen.ADD_FAMILY_MEMBER -> previousScreen ?: AppScreen.HOME
                else -> AppScreen.HOME
            }
        }

        Scaffold(
            containerColor = palette.screenBackground,
            bottomBar = {
                if (currentScreen != AppScreen.ONBOARDING) {
                    AppBottomBar(
                        current = currentScreen,
                        onSelect = { screen ->
                            if (screen == AppScreen.ITEMS) {
                                initialStatusFilter = null
                                initialCategoryName = null
                            }
                            currentScreen = screen
                        }
                    )
                }
            }
        ) { innerPadding ->
            AnimatedContent(
                targetState = currentScreen,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                transitionSpec = { screenTransition() },
                label = "screenTransition"
            ) { screen ->
                when (screen) {
                    AppScreen.ONBOARDING -> OnboardingScreen(
                        onFinishOnboarding = {
                            scope.launch {
                                appContainer.userPreferencesRepository.saveOnboardingCompleted(true)
                            }
                            currentScreen = AppScreen.HOME
                        }
                    )
                    AppScreen.HOME -> HomeScreen(
                        itemViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                            factory = com.docsalert.reminder.ui.viewmodels.ItemViewModel.Factory(appContainer.itemRepository)
                        ),
                        familyViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                            factory = com.docsalert.reminder.ui.viewmodels.FamilyViewModel.Factory(appContainer.familyRepository)
                        ),
                        customReminderViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                            factory = com.docsalert.reminder.ui.viewmodels.CustomReminderViewModel.Factory(appContainer.customReminderRepository)
                        ),
                        maskNumbers = appSettings?.maskItemNumbers ?: false,
                        onSeeAllClick = {
                            initialStatusFilter = null
                            initialCategoryName = null
                            currentScreen = AppScreen.ITEMS
                        },
                        onAddItemClick = { 
                            previousScreen = currentScreen
                            currentScreen = AppScreen.ADD_ITEM 
                        },
                        onItemClick = { itemId ->
                            viewingItemId = itemId
                        },
                        onVaultClick = {
                            previousScreen = currentScreen
                            currentScreen = AppScreen.VAULT
                        },
                        onRemindersClick = {
                            previousScreen = currentScreen
                            currentScreen = AppScreen.REMINDERS
                        },
                        onFilterClick = { status, category ->
                            initialStatusFilter = status
                            initialCategoryName = category?.name
                            currentScreen = AppScreen.ITEMS
                        }
                    )

                    AppScreen.ITEMS -> ItemsScreen(
                        itemViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                            factory = com.docsalert.reminder.ui.viewmodels.ItemViewModel.Factory(appContainer.itemRepository)
                        ),
                        familyViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                            factory = com.docsalert.reminder.ui.viewmodels.FamilyViewModel.Factory(appContainer.familyRepository)
                        ),
                        initialStatusFilter = initialStatusFilter,
                        initialCategoryFilter = initialCategoryName?.let { name ->
                            try { com.docsalert.reminder.data.model.ItemCategory.valueOf(name) } catch (e: Exception) { null }
                        },
                        maskNumbers = appSettings?.maskItemNumbers ?: false,
                        onBackClick = { currentScreen = AppScreen.HOME },
                        onAddItemClick = {
                            previousScreen = currentScreen
                            currentScreen = AppScreen.ADD_ITEM
                        },
                        onItemClick = { itemId ->
                            viewingItemId = itemId
                        }
                    )

                    AppScreen.CALENDAR -> CalendarScreen(
                        itemViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                            factory = com.docsalert.reminder.ui.viewmodels.ItemViewModel.Factory(appContainer.itemRepository)
                        ),
                        maskNumbers = appSettings?.maskItemNumbers ?: false,
                        onBackClick = { currentScreen = AppScreen.HOME },
                        onItemClick = { itemId ->
                            viewingItemId = itemId
                        }
                    )

                    AppScreen.FAMILY -> FamilyScreen(
                        familyViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                            factory = com.docsalert.reminder.ui.viewmodels.FamilyViewModel.Factory(appContainer.familyRepository)
                        ),
                        itemViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                            factory = com.docsalert.reminder.ui.viewmodels.ItemViewModel.Factory(appContainer.itemRepository)
                        ),
                        onBackClick = { currentScreen = AppScreen.HOME },
                        onAddFamilyMemberClick = { memberId ->
                            selectedFamilyMemberId = memberId
                            previousScreen = currentScreen
                            currentScreen = AppScreen.ADD_FAMILY_MEMBER
                        },
                        onMemberClick = { memberId ->
                            currentScreen = AppScreen.ITEMS
                        }
                    )

                    AppScreen.MORE -> com.docsalert.reminder.more.MoreScreen(
                        onBackClick = { currentScreen = AppScreen.HOME },
                        onVaultClick = {
                            previousScreen = currentScreen
                            currentScreen = AppScreen.VAULT
                        },
                        onRemindersClick = {
                            previousScreen = currentScreen
                            currentScreen = AppScreen.REMINDERS
                        }
                    )

                    AppScreen.SETTINGS -> SettingsScreen(
                        preferencesRepository = appContainer.userPreferencesRepository,
                        itemRepository = appContainer.itemRepository,
                        familyRepository = appContainer.familyRepository,
                        vaultRepository = appContainer.vaultRepository,
                        customReminderRepository = appContainer.customReminderRepository,
                        itemCount = appContainer.itemRepository.items.collectAsState(initial = emptyList()).value.size,
                        memberCount = appContainer.familyRepository.members.collectAsState(initial = emptyList()).value.size,
                        onBackClick = { currentScreen = AppScreen.HOME },
                        onThemesClick = { currentScreen = AppScreen.THEMES },
                        onNotificationSoundsClick = { currentScreen = AppScreen.NOTIFICATION_SOUNDS },
                        onAboutClick = { currentScreen = AppScreen.ABOUT }
                    )

                    AppScreen.THEMES -> ThemeSettingsScreen(
                        preferencesRepository = appContainer.userPreferencesRepository,
                        onBackClick = { currentScreen = AppScreen.SETTINGS }
                    )

                    AppScreen.NOTIFICATION_SOUNDS -> NotificationSoundScreen(
                        preferencesRepository = appContainer.userPreferencesRepository,
                        onBackClick = { currentScreen = AppScreen.SETTINGS }
                    )

                    AppScreen.ABOUT -> AboutScreen(
                        onBackClick = { currentScreen = AppScreen.SETTINGS },
                        preferencesRepository = appContainer.userPreferencesRepository,
                        itemRepository = appContainer.itemRepository,
                        familyRepository = appContainer.familyRepository
                    )
                    
                    AppScreen.ADD_ITEM -> AddItemScreen(
                        itemViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                            factory = com.docsalert.reminder.ui.viewmodels.ItemViewModel.Factory(appContainer.itemRepository)
                        ),
                        familyViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                            factory = com.docsalert.reminder.ui.viewmodels.FamilyViewModel.Factory(appContainer.familyRepository)
                        ),
                        itemId = selectedItemId,
                        onBackClick = { 
                            selectedItemId = null
                            currentScreen = previousScreen ?: AppScreen.HOME 
                        }
                    )
                    
                    AppScreen.ADD_FAMILY_MEMBER -> AddFamilyMemberScreen(
                        familyViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                            factory = com.docsalert.reminder.ui.viewmodels.FamilyViewModel.Factory(appContainer.familyRepository)
                        ),
                        memberId = selectedFamilyMemberId,
                        onBackClick = { 
                            selectedFamilyMemberId = null
                            currentScreen = previousScreen ?: AppScreen.FAMILY 
                        }
                    )

                    AppScreen.VAULT -> com.docsalert.reminder.vault.VaultScreen(
                        vaultViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                            factory = com.docsalert.reminder.ui.viewmodels.VaultViewModel.Factory(appContainer.vaultRepository)
                        ),
                        maskNumbers = appSettings?.maskItemNumbers ?: false,
                        onBackClick = { currentScreen = AppScreen.MORE },
                        onAddVaultItemClick = {
                            previousScreen = currentScreen
                            currentScreen = AppScreen.ADD_VAULT_ITEM
                        },
                        onVaultItemClick = { doc ->
                            viewingVaultDoc = doc
                        }
                    )

                    AppScreen.ADD_VAULT_ITEM -> com.docsalert.reminder.vault.AddVaultItemScreen(
                        vaultViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                            factory = com.docsalert.reminder.ui.viewmodels.VaultViewModel.Factory(appContainer.vaultRepository)
                        ),
                        itemId = selectedVaultItemId,
                        onBackClick = {
                            selectedVaultItemId = null
                            currentScreen = previousScreen ?: AppScreen.VAULT
                        }
                    )

                    AppScreen.REMINDERS -> com.docsalert.reminder.reminder.ReminderScreen(
                        reminderViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                            factory = com.docsalert.reminder.ui.viewmodels.CustomReminderViewModel.Factory(appContainer.customReminderRepository)
                        ),
                        onBackClick = { currentScreen = AppScreen.MORE },
                        onAddReminderClick = {
                            selectedReminderId = null
                            previousScreen = currentScreen
                            currentScreen = AppScreen.ADD_REMINDER
                        },
                        onEditReminderClick = { reminderId ->
                            selectedReminderId = reminderId
                            previousScreen = currentScreen
                            currentScreen = AppScreen.ADD_REMINDER
                        }
                    )

                    AppScreen.ADD_REMINDER -> com.docsalert.reminder.reminder.AddReminderScreen(
                        reminderViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                            factory = com.docsalert.reminder.ui.viewmodels.CustomReminderViewModel.Factory(appContainer.customReminderRepository)
                        ),
                        reminderId = selectedReminderId,
                        onBackClick = {
                            selectedReminderId = null
                            currentScreen = previousScreen ?: AppScreen.REMINDERS
                        }
                    )
                }
            }
        }

        viewingVaultDoc?.let { vaultDoc ->
            val vaultViewModel: com.docsalert.reminder.ui.viewmodels.VaultViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = com.docsalert.reminder.ui.viewmodels.VaultViewModel.Factory(appContainer.vaultRepository)
            )
            com.docsalert.reminder.vault.VaultDetailDialog(
                item = vaultDoc,
                maskNumbersDefault = appSettings?.maskItemNumbers ?: false,
                onDismiss = { viewingVaultDoc = null },
                onEditClick = {
                    selectedVaultItemId = vaultDoc.id
                    viewingVaultDoc = null
                    previousScreen = currentScreen
                    currentScreen = AppScreen.ADD_VAULT_ITEM
                },
                onDeleteClick = {
                    vaultViewModel.deleteVaultItem(vaultDoc)
                    viewingVaultDoc = null
                }
            )
        }

        viewingItemId?.let { docId ->
            val allItems by appContainer.itemRepository.items.collectAsState(initial = emptyList())
            val familyMembers by appContainer.familyRepository.members.collectAsState(initial = emptyList())
            val viewingItem = allItems.find { it.id == docId }
            val memberName = familyMembers.find { it.id == viewingItem?.familyMemberId }?.fullName

            if (viewingItem != null) {
                val scope = rememberCoroutineScope()
                com.docsalert.reminder.ui.components.ItemDetailDialog(
                    item = viewingItem,
                    familyMemberName = memberName,
                    maskNumbersDefault = appSettings?.maskItemNumbers ?: false,
                    onDismiss = { viewingItemId = null },
                    onEditClick = {
                        selectedItemId = viewingItem.id
                        viewingItemId = null
                        previousScreen = currentScreen
                        currentScreen = AppScreen.ADD_ITEM
                    },
                    onDeleteClick = {
                        scope.launch {
                            appContainer.itemRepository.removeItem(viewingItem.id)
                        }
                        viewingItemId = null
                    }
                )
            }
        }
    }
}

private fun AnimatedContentTransitionScope<AppScreen>.screenTransition(): ContentTransform {
    val forward = when {
        targetState in settingsChildScreens -> true
        initialState in settingsChildScreens -> false
        else -> targetState.tabIndex > initialState.tabIndex
    }
    val direction = if (forward) 1 else -1

    val enter = fadeIn(tween(durationMillis = 220)) +
            scaleIn(initialScale = 0.96f, animationSpec = tween(durationMillis = 220)) +
            slideInHorizontally(tween(durationMillis = 250, easing = androidx.compose.animation.core.FastOutSlowInEasing)) { fullWidth -> fullWidth / 12 * direction }

    val exit = fadeOut(tween(durationMillis = 180)) +
            scaleOut(targetScale = 0.96f, animationSpec = tween(durationMillis = 220)) +
            slideOutHorizontally(tween(durationMillis = 250, easing = androidx.compose.animation.core.FastOutSlowInEasing)) { fullWidth -> -fullWidth / 12 * direction }

    return enter togetherWith exit
}
