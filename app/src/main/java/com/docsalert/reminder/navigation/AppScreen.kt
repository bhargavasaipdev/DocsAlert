package com.docsalert.reminder.navigation

/**
 * All screens in the app.
 *
 * tabIndex  - position in the bottom bar (used to pick the slide direction).
 * THEMES, NOTIFICATION_SOUNDS and ABOUT are children of SETTINGS, so they share the Settings tab.
 */
enum class AppScreen(val tabIndex: Int) {
    ONBOARDING(0),
    HOME(0),
    ITEMS(1),
    CALENDAR(2),
    FAMILY(3),
    MORE(4),
    SETTINGS(5),
    THEMES(5),
    NOTIFICATION_SOUNDS(5),
    ABOUT(5),
    ADD_ITEM(1),
    ADD_FAMILY_MEMBER(3),
    VAULT(4),
    ADD_VAULT_ITEM(4),
    REMINDERS(4),
    ADD_REMINDER(4)
}
