package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FastingStage
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun FastingRing(
    elapsedMs: Long,
    targetDurationHours: Float,
    isFastingActive: Boolean,
    showRemainingTime: Boolean,
    currentStage: FastingStage,
    onToggleDisplayMode: () -> Unit,
    modifier: Modifier = Modifier,
    ringSize: Dp = 260.dp,
    strokeWidth: Dp = 18.dp
) {
    val totalTargetMs = (targetDurationHours * 60 * 60 * 1000L).toLong()
    val progressRaw = if (totalTargetMs > 0) elapsedMs.toFloat() / totalTargetMs.toFloat() else 0f
    val progressClamped = progressRaw.coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = if (isFastingActive) progressClamped else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "fasting_ring_progress"
    )

    // Calculate time formatted
    val displayMs = if (showRemainingTime && isFastingActive) {
        maxOf(0L, totalTargetMs - elapsedMs)
    } else {
        elapsedMs
    }

    val hours = (displayMs / (1000 * 60 * 60)).toInt()
    val minutes = ((displayMs / (1000 * 60)) % 60).toInt()
    val seconds = ((displayMs / 1000) % 60).toInt()
    val formattedTime = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)

    val ringColor = if (isFastingActive) currentStage.color else MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(ringSize)
            .testTag("fasting_ring_container")
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onToggleDisplayMode
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidthPx = strokeWidth.toPx()
            val arcSize = size.width - strokeWidthPx

            // Draw Background Track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )

            if (isFastingActive) {
                // Draw Active Progress Arc
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            ringColor.copy(alpha = 0.7f),
                            ringColor,
                            ringColor
                        )
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
            }
        }

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            if (isFastingActive) {
                Text(
                    text = if (showRemainingTime) "REMAINING" else "ELAPSED",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = (-1).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.testTag("timer_text")
                )

                Spacer(modifier = Modifier.height(4.dp))

                val percentInt = (progressRaw * 100).toInt()
                Text(
                    text = "$percentInt% of ${targetDurationHours.toInt()}h goal",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Text(
                    text = "Tap to toggle",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            } else {
                Text(
                    text = "READY",
                    style = MaterialTheme.typography.labelLarge.copy(
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = EmeraldPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${targetDurationHours.toInt()}:00:00",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Target Duration",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}
