package com.docsalert.reminder.util

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import com.docsalert.reminder.MainActivity
import com.docsalert.reminder.R
import com.docsalert.reminder.data.local.AppDatabase
import com.docsalert.reminder.data.model.ItemCategory
import com.docsalert.reminder.data.preferences.UserPreferencesRepository
import com.docsalert.reminder.ui.components.bellSwing
import com.docsalert.reminder.ui.components.pressScale
import com.docsalert.reminder.ui.theme.DocsAlertTheme
import com.docsalert.reminder.ui.theme.palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue

data class AlarmCardItem(
    val id: String,
    val name: String,
    val category: ItemCategory,
    val expiryDateStr: String,
    val memberName: String,
    val daysUntilExpiry: Int,
    val notes: String? = null,
    val isCustomReminder: Boolean = false
)

private data class StatusInfo(
    val text: String,
    val bg: Color,
    val color: Color,
    val icon: ImageVector
)

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
class ReminderAlarmActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Safety check: if Full-Screen Expiry Alarm is disabled in settings, finish immediately!
        val isPreview = intent.getBooleanExtra("IS_PREVIEW", false)
        val preferencesRepository = UserPreferencesRepository(applicationContext)
        val fullScreenEnabled = try {
            runBlocking { preferencesRepository.appSettings.first().fullScreenAlarmEnabled }
        } catch (e: Exception) {
            true
        }

        if (!fullScreenEnabled && !isPreview) {
            Log.d("ReminderAlarmActivity", "FullScreenAlarm is disabled in settings. Finishing activity immediately.")
            finish()
            return
        }

        enableEdgeToEdge()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(true)
                setTurnScreenOn(true)
                val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                keyguardManager.requestDismissKeyguard(this, null)
            }
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            )
        } catch (e: Exception) {
            // Ignore keyguard configuration errors
        }

        val notifId = intent.getIntExtra(EXTRA_NOTIF_ID, 1001)
        val itemName = intent.getStringExtra(EXTRA_DOC_NAME) ?: "Item Expiry"
        val categoryName = intent.getStringExtra(EXTRA_DOC_CATEGORY) ?: "General"
        val expiryDate = intent.getStringExtra(EXTRA_EXPIRY_DATE) ?: "Due Soon"
        val memberName = intent.getStringExtra(EXTRA_MEMBER_NAME) ?: "Self"
        val itemSummary = intent.getStringExtra(EXTRA_DOC_SUMMARY)

        setContent {
            DocsAlertTheme {
                val palette = MaterialTheme.palette
                var alarmItems by remember { mutableStateOf<List<AlarmCardItem>>(emptyList()) }

                // Fetch due items from DB on launch
                LaunchedEffect(Unit) {
                    withContext(Dispatchers.IO) {
                        try {
                            val isCustomReminder = intent.getBooleanExtra("IS_CUSTOM_REMINDER", false) || categoryName == "Custom Reminder"
                            if (isCustomReminder) {
                                alarmItems = listOf(
                                    AlarmCardItem(
                                        id = "custom_reminder",
                                        name = itemName,
                                        category = ItemCategory.OTHER,
                                        expiryDateStr = "Scheduled Alarm",
                                        memberName = memberName,
                                        daysUntilExpiry = 0,
                                        notes = itemSummary,
                                        isCustomReminder = true
                                    )
                                )
                                return@withContext
                            }

                            val database = AppDatabase.getDatabase(applicationContext)
                            val items = database.itemDao().getAllItemsList()
                            val familyMembers = database.familyMemberDao().getAllFamilyMembersList()
                            val today = LocalDate.now()

                            val due = items.filter { item ->
                                if (item.remindersEnabled && item.expiryDate != null) {
                                    val days = ChronoUnit.DAYS.between(today, item.expiryDate).toInt()
                                    val activeDays = if (item.reminderDaysBefore.isNotEmpty()) item.reminderDaysBefore else listOf(30, 15, 7, 3, 1, 0)
                                    activeDays.contains(days)
                                } else false
                            }

                            if (due.isNotEmpty()) {
                                alarmItems = due.map { item ->
                                    val days = ChronoUnit.DAYS.between(today, item.expiryDate).toInt()
                                    val mName = familyMembers.find { it.id == item.familyMemberId }?.fullName ?: "Self"
                                    AlarmCardItem(
                                        id = item.id,
                                        name = item.name,
                                        category = item.category,
                                        expiryDateStr = item.expiryDate.toString(),
                                        memberName = mName,
                                        daysUntilExpiry = days,
                                        notes = item.notes.ifBlank { null }
                                    )
                                }
                            } else {
                                val cat = try { ItemCategory.valueOf(categoryName.uppercase()) } catch (e: Exception) { ItemCategory.OTHER }
                                alarmItems = listOf(
                                    AlarmCardItem(
                                        id = "fallback",
                                        name = itemName,
                                        category = cat,
                                        expiryDateStr = expiryDate,
                                        memberName = memberName,
                                        daysUntilExpiry = 0,
                                        notes = itemSummary
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            val cat = try { ItemCategory.valueOf(categoryName.uppercase()) } catch (e: Exception) { ItemCategory.OTHER }
                            alarmItems = listOf(
                                AlarmCardItem(
                                    id = "fallback",
                                    name = itemName,
                                    category = cat,
                                    expiryDateStr = expiryDate,
                                    memberName = memberName,
                                    daysUntilExpiry = 0,
                                    notes = itemSummary
                                )
                            )
                        }
                    }
                }

                val dismissInteraction = remember { MutableInteractionSource() }
                val openInteraction = remember { MutableInteractionSource() }

                // Ambient Background Shift Animation
                val infiniteTransition = rememberInfiniteTransition(label = "ambient_bg")
                val ambientShift by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(6000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "ambient_shift"
                )

                // Bell Pulsing Scale Animation
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1.0f,
                    targetValue = 1.20f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse_scale"
                )

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = palette.screenBackground
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(palette.accentContainer, palette.screenBackground)
                                )
                            )
                            .drawBehind {
                                val shift = ambientShift
                                drawCircle(
                                    color = palette.oppositeAccent.copy(alpha = 0.08f + (0.04f * shift)),
                                    radius = size.minDimension * (0.85f + (0.15f * shift)),
                                    center = Offset(size.width * (0.90f + (0.05f * shift)), size.height * (0.05f + (0.05f * shift)))
                                )
                                drawCircle(
                                    color = palette.primaryAccent.copy(alpha = 0.06f + (0.04f * (1f - shift))),
                                    radius = size.minDimension * (0.65f + (0.15f * (1f - shift))),
                                    center = Offset(size.width * (0.10f - (0.05f * shift)), size.height * (0.90f - (0.05f * shift)))
                                )
                            }
                            .systemBarsPadding()
                            .padding(top = 24.dp, bottom = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Header Section: Logo + Pulsing Bell + Counter Pill
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier.size(36.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        color = palette.appLogoBackground
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Image(
                                                painter = painterResource(id = R.drawable.app_logo),
                                                contentDescription = "DocsAlert logo",
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Fit
                                            )
                                        }
                                    }
                                    Text(
                                        text = "DocsAlert",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                        color = palette.headingText
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Pulsing Ringing Bell Hero Header
                                Box(
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size((78 * pulseScale).dp)
                                            .clip(CircleShape)
                                            .background(palette.warningColor.copy(alpha = 0.20f))
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clip(CircleShape)
                                            .background(palette.warningBackground)
                                            .border(2.dp, palette.warningColor, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.NotificationsActive,
                                            contentDescription = "Ringing Alarm",
                                            tint = palette.warningColor,
                                            modifier = Modifier
                                                .size(38.dp)
                                                .bellSwing()
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Notification Counter Pill Above Swipable Cards
                                if (alarmItems.isNotEmpty()) {
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = palette.oppositeAccentContainer,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, palette.oppositeAccent.copy(alpha = 0.5f))
                                    ) {
                                        Text(
                                            text = if (alarmItems.size > 1) "🔥 ${alarmItems.size} EXPIRATIONS DUE TODAY" else "🔥 EXPIRY NOTIFICATION",
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                letterSpacing = 0.5.sp
                                            ),
                                            color = palette.headingText
                                        )
                                    }
                                }
                            }

                            // Center Content: Spacious Swipable Cards Carousel
                            if (alarmItems.isNotEmpty()) {
                                val pagerState = rememberPagerState(pageCount = { alarmItems.size })

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Item Page Indicator Text
                                    if (alarmItems.size > 1) {
                                        Text(
                                            text = "Item ${pagerState.currentPage + 1} of ${alarmItems.size}  (Swipe to view)",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = palette.secondaryText,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    }

                                    HorizontalPager(
                                        state = pagerState,
                                        contentPadding = PaddingValues(horizontal = 22.dp),
                                        pageSpacing = 12.dp,
                                        modifier = Modifier.fillMaxWidth()
                                    ) { page ->
                                        val item = alarmItems[page]
                                        val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                                        val cardScale = 1f - (pageOffset * 0.08f).coerceIn(0f, 0.20f)
                                        val cardAlpha = 1f - (pageOffset * 0.35f).coerceIn(0f, 0.60f)
                                        val rotation = (if (page > pagerState.currentPage) 2f else -2f) * pageOffset.coerceIn(0f, 1f)

                                        AlarmItemCard(
                                            item = item,
                                            modifier = Modifier
                                                .graphicsLayer {
                                                    scaleX = cardScale
                                                    scaleY = cardScale
                                                    alpha = cardAlpha
                                                    rotationZ = rotation
                                                }
                                        )
                                    }

                                    // Page Dot Indicators
                                    if (alarmItems.size > 1) {
                                        Row(
                                            modifier = Modifier.padding(top = 10.dp),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            repeat(alarmItems.size) { index ->
                                                val active = pagerState.currentPage == index
                                                val dotWidth by animateDpAsState(
                                                    targetValue = if (active) 24.dp else 8.dp,
                                                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                                    label = "dot_width"
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .size(dotWidth, 8.dp)
                                                        .clip(RoundedCornerShape(50))
                                                        .background(if (active) palette.oppositeAccent else palette.secondaryText.copy(alpha = 0.3f))
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = palette.primaryAccent)
                                }
                            }

                            // Bottom Section: Action Buttons (NO SNOOZE)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 22.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Button 1: Dismiss / Okay Got It
                                Button(
                                    onClick = {
                                        dismissNotification(notifId)
                                        Toast.makeText(this@ReminderAlarmActivity, "Alert Dismissed", Toast.LENGTH_SHORT).show()
                                        finish()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp)
                                        .pressScale(dismissInteraction),
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = palette.oppositeAccent,
                                        contentColor = palette.onOppositeAccent
                                    )
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Okay, Got It", style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp, fontWeight = FontWeight.ExtraBold))
                                }

                                // Button 2: Open App
                                OutlinedButton(
                                    onClick = {
                                        dismissNotification(notifId)
                                        val isCustom = intent.getBooleanExtra("IS_CUSTOM_REMINDER", false) || categoryName == "Custom Reminder"
                                        val mainIntent = Intent(this@ReminderAlarmActivity, MainActivity::class.java).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                            if (isCustom) {
                                                putExtra("OPEN_SCREEN", "REMINDERS")
                                            } else {
                                                putExtra("OPEN_SCREEN", "ITEMS")
                                            }
                                        }
                                        startActivity(mainIntent)
                                        finish()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .pressScale(openInteraction),
                                    shape = RoundedCornerShape(50),
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, palette.primaryAccent)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                        contentDescription = null,
                                        tint = palette.primaryAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Open DocsAlert App",
                                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                                        color = palette.primaryAccent
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun dismissNotification(id: Int) {
        try {
            val notificationManager = NotificationManagerCompat.from(this)
            notificationManager.cancel(id)
        } catch (e: Exception) {
            // Ignore
        }
    }

    companion object {
        const val EXTRA_NOTIF_ID = "extra_notif_id"
        const val EXTRA_DOC_ID = "extra_doc_id"
        const val EXTRA_DOC_NAME = "extra_doc_name"
        const val EXTRA_DOC_CATEGORY = "extra_doc_category"
        const val EXTRA_EXPIRY_DATE = "extra_expiry_date"
        const val EXTRA_MEMBER_NAME = "extra_member_name"
        const val EXTRA_DOC_COUNT = "extra_doc_count"
        const val EXTRA_DOC_SUMMARY = "extra_doc_summary"
    }
}

@Composable
private fun AlarmItemCard(
    item: AlarmCardItem,
    modifier: Modifier = Modifier
) {
    val palette = MaterialTheme.palette

    val status = when {
        item.isCustomReminder -> StatusInfo("SCHEDULED ALARM", palette.oppositeAccentContainer, palette.oppositeAccent, Icons.Default.Alarm)
        item.daysUntilExpiry < 0 -> StatusInfo("EXPIRED", palette.errorBackground, palette.errorColor, Icons.Default.Error)
        item.daysUntilExpiry == 0 -> StatusInfo("EXPIRES TODAY", palette.warningBackground, palette.warningColor, Icons.Default.Warning)
        else -> StatusInfo("EXPIRES IN ${item.daysUntilExpiry} DAYS", palette.infoBackground, palette.infoColor, Icons.Default.Info)
    }

    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
        border = androidx.compose.foundation.BorderStroke(2.dp, status.color.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Full Width Top Banner for Status
            Surface(
                color = status.bg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = status.icon,
                        contentDescription = null,
                        tint = status.color,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = status.text,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        ),
                        color = status.color
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header: Icon + Title + Expiry Outline
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = RoundedCornerShape(18.dp),
                        color = palette.accentContainer,
                        border = androidx.compose.foundation.BorderStroke(1.dp, palette.primaryAccent.copy(alpha = 0.3f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = item.category.icon,
                                contentDescription = item.category.title,
                                tint = palette.primaryAccent,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                lineHeight = 28.sp
                            ),
                            color = palette.headingText,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (item.isCustomReminder) "Scheduled Daily Alarm" else "Expiry: ${item.expiryDateStr}",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = status.color
                        )
                    }
                }

                androidx.compose.material3.HorizontalDivider(color = palette.dividerColor.copy(alpha = 0.5f))

                // Details Grid (Category & Profile)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Category
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CATEGORY",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                            color = palette.secondaryText
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = item.category.icon,
                                contentDescription = null,
                                tint = palette.primaryAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = item.category.title,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = palette.headingText
                            )
                        }
                    }

                    // Owner
                    if (item.memberName.isNotBlank()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "PROFILE OWNER",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                                color = palette.secondaryText
                            )
                            Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = palette.primaryAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = item.memberName,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = palette.headingText
                                )
                            }
                        }
                    }
                }

                // Notes Box
                if (!item.notes.isNullOrBlank()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "NOTES",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                            color = palette.secondaryText
                        )
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = palette.secondaryCardBackground,
                            border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderColor.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "“${item.notes}”",
                                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                                color = palette.bodyText,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

