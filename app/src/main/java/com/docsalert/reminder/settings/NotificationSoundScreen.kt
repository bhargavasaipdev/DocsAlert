package com.docsalert.reminder.settings

import android.media.MediaPlayer
import android.widget.Toast
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.docsalert.reminder.data.model.NotificationSoundPreset
import com.docsalert.reminder.data.preferences.AppSettings
import com.docsalert.reminder.data.preferences.UserPreferencesRepository
import com.docsalert.reminder.ui.components.ScreenHeader
import com.docsalert.reminder.ui.components.StaggeredEntrance
import com.docsalert.reminder.ui.theme.palette
import com.docsalert.reminder.util.NotificationHelper
import kotlinx.coroutines.launch

@Composable
fun NotificationSoundScreen(
    preferencesRepository: UserPreferencesRepository,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val appSettings by preferencesRepository.appSettings.collectAsState(initial = AppSettings())

    var playingResId by remember { mutableStateOf<Int?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    fun playOrStopPreview(preset: NotificationSoundPreset) {
        if (playingResId == preset.rawResId && mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            playingResId = null
        } else {
            try {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer.create(context, preset.rawResId).apply {
                    start()
                    setOnCompletionListener {
                        it.release()
                        mediaPlayer = null
                        playingResId = null
                    }
                }
                playingResId = preset.rawResId
            } catch (e: Exception) {
                playingResId = null
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Notification Sound",
            subtitle = "Choose alert sound preset",
            onBackClick = {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
                onBackClick()
            }
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                StaggeredEntrance(0) {
                    Column {
                        SectionTitle(
                            title = "Built-in Sound Presets",
                            subtitle = "Select a pleasant reminder sound."
                        )
                    }
                }
            }

            item {
                StaggeredEntrance(1) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.palette.cardBackground),
                        shape = RoundedCornerShape(22.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.palette.borderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            NotificationSoundPreset.entries.forEachIndexed { index, preset ->
                                val isSelected = preset.key == appSettings.notificationSoundKey
                                val isPlayingThis = playingResId == preset.rawResId

                                NotificationSoundRow(
                                    preset = preset,
                                    selected = isSelected,
                                    isPlaying = isPlayingThis,
                                    showDivider = index < NotificationSoundPreset.entries.lastIndex,
                                    onSelect = {
                                        scope.launch {
                                            preferencesRepository.saveNotificationSoundKey(preset.key)
                                            NotificationHelper.createNotificationChannel(context, preset.key)
                                        }
                                        playOrStopPreview(preset)
                                        Toast.makeText(context, "Sound set to ${preset.title}", Toast.LENGTH_SHORT).show()
                                    },
                                    onPreviewClick = {
                                        playOrStopPreview(preset)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                StaggeredEntrance(2) {
                    com.docsalert.reminder.ui.components.BannerAdView(modifier = Modifier.padding(top = 12.dp))
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    val palette = MaterialTheme.palette

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 20.dp)
                .clip(RoundedCornerShape(50))
                .background(palette.primaryAccent)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
            color = palette.headingText
        )
    }
    Spacer(Modifier.height(4.dp))
    Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium,
        color = palette.secondaryText
    )
}

@Composable
private fun NotificationSoundRow(
    preset: NotificationSoundPreset,
    selected: Boolean,
    isPlaying: Boolean,
    showDivider: Boolean,
    onSelect: () -> Unit,
    onPreviewClick: () -> Unit
) {
    val palette = MaterialTheme.palette
    val interactionSource = remember { MutableInteractionSource() }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectable(
                    selected = selected,
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    role = Role.RadioButton,
                    onClick = onSelect
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (selected) palette.accentContainer else palette.inputBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = if (selected) palette.primaryAccent else palette.secondaryText,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = preset.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (selected) palette.headingText else palette.bodyText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (selected) "Active Sound" else "Tap to apply",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = if (selected) palette.primaryAccent else palette.secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    onClick = onPreviewClick,
                    shape = CircleShape,
                    color = if (isPlaying) palette.oppositeAccentContainer else palette.secondaryCardBackground,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = "Preview Sound",
                            tint = if (isPlaying) palette.onOppositeAccentContainer else palette.primaryAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                RadioButton(
                    selected = selected,
                    onClick = onSelect,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = palette.primaryAccent,
                        unselectedColor = palette.secondaryText
                    ),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        if (showDivider) {
            androidx.compose.material3.HorizontalDivider(
                modifier = Modifier.padding(start = 72.dp),
                color = palette.dividerColor.copy(alpha = 0.65f)
            )
        }
    }
}
