package com.docsalert.reminder.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.docsalert.reminder.ui.theme.palette

@Composable
fun MemberProfileAvatar(
    photoUrl: String?,
    modifier: Modifier = Modifier.size(40.dp),
    fallbackIcon: ImageVector = Icons.Default.AccountCircle
) {
    val context = LocalContext.current
    val palette = MaterialTheme.palette

    val bitmap: ImageBitmap? = remember(photoUrl) {
        if (!photoUrl.isNullOrBlank()) {
            try {
                val uri = android.net.Uri.parse(photoUrl)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    android.graphics.BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            } catch (e: Exception) {
                null
            }
        } else null
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(palette.accentContainer),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Profile Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = fallbackIcon,
                contentDescription = "Profile Photo",
                tint = palette.primaryAccent,
                modifier = Modifier.fillMaxSize(0.7f)
            )
        }
    }
}
