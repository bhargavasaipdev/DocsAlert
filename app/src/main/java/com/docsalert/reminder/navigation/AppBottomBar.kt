package com.docsalert.reminder.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.docsalert.reminder.R
import com.docsalert.reminder.ui.theme.palette

private data class BottomTab(
    val screen: AppScreen,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val bottomTabs = listOf(
    BottomTab(AppScreen.HOME, R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home),
    BottomTab(AppScreen.ITEMS, R.string.nav_documents, Icons.Filled.Description, Icons.Outlined.Description),
    BottomTab(AppScreen.CALENDAR, R.string.nav_calendar, Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    BottomTab(AppScreen.FAMILY, R.string.nav_family, Icons.Filled.Groups, Icons.Outlined.Groups),
    BottomTab(AppScreen.MORE, R.string.nav_more, Icons.Filled.Apps, Icons.Outlined.Apps),
    BottomTab(AppScreen.SETTINGS, R.string.nav_settings, Icons.Filled.Settings, Icons.Outlined.Settings)
)

@Composable
fun AppBottomBar(
    current: AppScreen,
    onSelect: (AppScreen) -> Unit
) {
    val palette = MaterialTheme.palette

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = palette.bottomBarBackground,
        tonalElevation = 3.dp
    ) {
        NavigationBar(
            containerColor = palette.bottomBarBackground,
            contentColor = palette.headingText,
            tonalElevation = 0.dp
        ) {
            bottomTabs.forEach { tab ->
                val selected = current.tabIndex == tab.screen.tabIndex
                val labelText = stringResource(id = tab.labelRes)

                val iconScale by animateFloatAsState(
                    targetValue = if (selected) 1.12f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "nativeTabScale"
                )

                NavigationBarItem(
                    selected = selected,
                    onClick = { onSelect(tab.screen) },
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (selected) palette.oppositeAccent else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = labelText,
                                tint = if (selected) palette.onOppositeAccent else palette.secondaryText,
                                modifier = Modifier
                                    .size(22.dp)
                                    .graphicsLayer {
                                        scaleX = iconScale
                                        scaleY = iconScale
                                    }
                            )
                        }
                    },
                    label = {
                        Text(
                            text = labelText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = palette.onOppositeAccent,
                        selectedTextColor = palette.oppositeAccent,
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = palette.secondaryText,
                        unselectedTextColor = palette.secondaryText
                    )
                )
            }
        }
    }
}
