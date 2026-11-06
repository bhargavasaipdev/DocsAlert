package com.docsalert.reminder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.docsalert.reminder.ui.theme.palette

/**
 * Modern shared header bar for all screens.
 */
@Composable
fun ScreenHeader(
    title: String,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    actionIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    actionIconTint: androidx.compose.ui.graphics.Color? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val palette = MaterialTheme.palette
    val interactionSource = remember { MutableInteractionSource() }
    val actionInteraction = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(palette.topBarBackground)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBackClick != null) {
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .pressScale(interactionSource),
                shape = RoundedCornerShape(12.dp),
                color = palette.accentContainer
            ) {
                IconButton(
                    onClick = onBackClick,
                    interactionSource = interactionSource,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = palette.primaryAccent
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = palette.primaryAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = palette.headingText
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.secondaryText
                )
            }
        }
        
        if (actionIcon != null && onActionClick != null) {
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .pressScale(actionInteraction),
                shape = RoundedCornerShape(12.dp),
                color = palette.secondaryCardBackground
            ) {
                IconButton(
                    onClick = onActionClick,
                    interactionSource = actionInteraction,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = actionIconTint ?: palette.primaryAccent
                    )
                ) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = "Action",
                        tint = actionIconTint ?: palette.primaryAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
