package com.docsalert.reminder.data.model

import androidx.annotation.RawRes
import com.docsalert.reminder.R

enum class NotificationSoundPreset(
    val key: String,
    val title: String,
    @RawRes val rawResId: Int
) {
    AI_SYNTAX_1("ai_syntax_1", "AI Syntax 1 (Default)", R.raw.ai_syntax_1),
    AI_SYNTAX_2("ai_syntax_2", "AI Syntax 2", R.raw.ai_syntax_2),
    KIDDO_1("kiddo_1", "Kiddo 1", R.raw.kiddo_1),
    KIDDO_2("kiddo_2", "Kiddo 2", R.raw.kiddo_2),
    SIGNATURE_1("signature_1", "Signature 1", R.raw.signature_1),
    SIGNATURE_2("signature_2", "Signature 2", R.raw.signature_2),
    ZEN_RETRO("zen_retro", "Zen Retro", R.raw.zen_retro);

    companion object {
        val DEFAULT = AI_SYNTAX_1

        fun fromKey(key: String?): NotificationSoundPreset {
            return entries.find { it.key == key } ?: DEFAULT
        }
    }
}
