package com.docsalert.reminder.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.docsalert.reminder.R
import com.docsalert.reminder.data.preferences.UserPreferencesRepository
import com.docsalert.reminder.data.repository.FamilyRepository
import com.docsalert.reminder.data.repository.ItemRepository
import com.docsalert.reminder.ui.components.ScreenHeader
import com.docsalert.reminder.ui.components.StaggeredEntrance
import com.docsalert.reminder.ui.theme.palette
import kotlinx.coroutines.launch

@Composable
fun AboutScreen(
    onBackClick: () -> Unit,
    preferencesRepository: UserPreferencesRepository? = null,
    itemRepository: ItemRepository? = null,
    familyRepository: FamilyRepository? = null,
    onRateAppClick: () -> Unit = {},
    onShareAppClick: () -> Unit = {}
) {
    val palette = MaterialTheme.palette
    val scope = rememberCoroutineScope()

    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "About DocsAlert",
            subtitle = "App information and version",
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StaggeredEntrance(0) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(palette.appLogoBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "DocsAlert logo",
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "DocsAlert",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = palette.headingText
                    )

                    Spacer(Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(palette.oppositeAccentContainer)
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Version 1.0.0",
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.onOppositeAccentContainer
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    Text(
                        text = "Keep your important documents organized and never miss an expiry date again.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = palette.secondaryText,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            StaggeredEntrance(1) {
                AboutLinksCard(
                    onRateAppClick = onRateAppClick,
                    onShareAppClick = onShareAppClick,
                    onPrivacyPolicyClick = { showPrivacyDialog = true },
                    onResetDataClick = { showResetDialog = true }
                )
            }

            Spacer(Modifier.height(24.dp))

            StaggeredEntrance(2) {
                Text(
                    text = "Made with care to help you stay on top of what matters.",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.secondaryText,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    if (showPrivacyDialog) {
        PrivacyPolicyDialog(
            onDismiss = { showPrivacyDialog = false }
        )
    }

    if (showResetDialog) {
        ResetDataDialog(
            onConfirm = {
                showResetDialog = false
                scope.launch {
                    preferencesRepository?.resetAll()
                    itemRepository?.clearAll()
                    familyRepository?.clearAll()
                }
            },
            onDismiss = { showResetDialog = false }
        )
    }
}

@Composable
private fun AboutLinksCard(
    onRateAppClick: () -> Unit,
    onShareAppClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onResetDataClick: () -> Unit
) {
    val palette = MaterialTheme.palette
    val shape = RoundedCornerShape(24.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(palette.cardBackground)
            .border(1.dp, palette.borderColor, shape)
    ) {
        AboutLinkRow(Icons.Default.StarRate, "Rate the App", palette.primaryAccent, onRateAppClick)
        HorizontalDivider(
            modifier = Modifier.padding(start = 56.dp, end = 16.dp),
            color = palette.dividerColor.copy(alpha = 0.65f)
        )
        AboutLinkRow(Icons.Default.Share, "Share this App", palette.primaryAccent, onShareAppClick)
        HorizontalDivider(
            modifier = Modifier.padding(start = 56.dp, end = 16.dp),
            color = palette.dividerColor.copy(alpha = 0.65f)
        )
        AboutLinkRow(Icons.Default.Shield, "Privacy Policy & Security", palette.primaryAccent, onPrivacyPolicyClick)
        HorizontalDivider(
            modifier = Modifier.padding(start = 56.dp, end = 16.dp),
            color = palette.dividerColor.copy(alpha = 0.65f)
        )
        AboutLinkRow(Icons.Default.Delete, "Reset All App Data", palette.errorColor, onResetDataClick)
    }
}

@Composable
private fun AboutLinkRow(
    icon: ImageVector,
    title: String,
    tint: Color,
    onClick: () -> Unit
) {
    val palette = MaterialTheme.palette

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            color = if (tint == palette.errorColor) palette.errorColor else palette.headingText
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = palette.secondaryText
        )
    }
}
