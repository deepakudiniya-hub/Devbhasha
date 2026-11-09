package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Adds a smooth scale and opacity effect on press to any Composable.
 */
fun Modifier.pressFeedback(
    onClick: () -> Unit,
    interactionSource: MutableInteractionSource? = null,
    enabled: Boolean = true
): Modifier = composed {
    val localInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by localInteractionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        label = "scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        label = "alpha"
    )

    this
        .graphicsLayer(
            scaleX = scale,
            scaleY = scale,
            alpha = alpha
        )
        .clickable(
            enabled = enabled,
            interactionSource = localInteractionSource,
            indication = ripple(),
            onClick = onClick
        )
}
