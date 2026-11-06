package com.docsalert.reminder.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * High-performance, GPU-accelerated staggered entrance motion.
 * Evaluated strictly in the graphics layer draw phase (0 recompositions).
 * Applies a consistent slide-up, scale-up, and fade-in across all elements.
 */
@Composable
fun StaggeredEntrance(
    index: Int = 0,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val animatable = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        val delayMs = (index * 20).coerceAtMost(80)
        animatable.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 180,
                delayMillis = delayMs,
                easing = FastOutSlowInEasing
            )
        )
    }

    Box(
        modifier = modifier.graphicsLayer {
            val p = animatable.value
            alpha = p
            scaleX = 0.96f + (0.04f * p)
            scaleY = 0.96f + (0.04f * p)
            translationY = (1f - p) * 12.dp.toPx()
        }
    ) {
        content()
    }
}

/** Counts up from 0 to [target] efficiently. */
@Composable
fun rememberCountUp(target: Int, durationMillis: Int = 400): Int {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    val value by animateIntAsState(
        targetValue = if (started) target else 0,
        animationSpec = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
        label = "countUp"
    )
    return value
}

/** Playful spring touch press effect for buttons and cards. */
fun Modifier.pressScale(
    interactionSource: InteractionSource,
    pressedScale: Float = 0.96f
): Modifier = composed {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "pressScale"
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/** Gentle bell-ring icon entrance animation (runs once for high-performance 120 FPS rendering). */
fun Modifier.bellSwing(): Modifier = composed {
    val animatable = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animatable.animateTo(
            targetValue = 1f,
            animationSpec = keyframes {
                durationMillis = 700
                0f at 0
                -10f at 120
                8f at 260
                -5f at 400
                3f at 540
                0f at 700
            }
        )
    }

    graphicsLayer {
        rotationZ = animatable.value
        transformOrigin = TransformOrigin(0.5f, 0.15f)
    }
}
