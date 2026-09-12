package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.VisualizerStyle
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun AudioVisualizer(
    isPlaying: Boolean,
    style: VisualizerStyle,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    barCount: Int = 24,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    if (style == VisualizerStyle.OFF) return

    val infiniteTransition = rememberInfiniteTransition(label = "visualizer_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isPlaying) 1400 else 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isPlaying) 380 else 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val effectiveStyle = if (style == VisualizerStyle.AUTO) VisualizerStyle.BARS else style

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerY = canvasHeight / 2f

        when (effectiveStyle) {
            VisualizerStyle.BARS -> {
                val totalBars = barCount.coerceIn(12, 48)
                val barSpacing = 4.dp.toPx()
                val totalSpacing = barSpacing * (totalBars - 1)
                val barWidth = ((canvasWidth - totalSpacing) / totalBars).coerceAtLeast(2.dp.toPx())

                for (i in 0 until totalBars) {
                    val progress = i.toFloat() / totalBars.toFloat()
                    val wave1 = sin(phase + progress * 6f)
                    val wave2 = sin(phase * 1.5f + progress * 12f)
                    val combinedWave = ((wave1 + wave2) / 2f + 1f) / 2f // normalized 0..1

                    val dynamicHeight = if (isPlaying) {
                        val baseH = canvasHeight * 0.15f
                        val variableH = canvasHeight * 0.85f * combinedWave * pulseScale
                        (baseH + variableH).coerceIn(4.dp.toPx(), canvasHeight)
                    } else {
                        canvasHeight * 0.12f
                    }

                    val x = i * (barWidth + barSpacing)
                    val y = centerY - (dynamicHeight / 2f)

                    val barColor = Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.95f),
                            accentColor.copy(alpha = 0.35f)
                        ),
                        startY = y,
                        endY = y + dynamicHeight
                    )

                    drawRoundRect(
                        brush = barColor,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, dynamicHeight),
                        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                    )
                }
            }
            VisualizerStyle.WAVE -> {
                val path = Path()
                val steps = 60
                val stepWidth = canvasWidth / steps

                path.moveTo(0f, centerY)
                for (i in 0..steps) {
                    val x = i * stepWidth
                    val progress = i.toFloat() / steps.toFloat()
                    val amplitude = if (isPlaying) (canvasHeight * 0.42f) * pulseScale else canvasHeight * 0.08f
                    val y = centerY + sin(phase + progress * 8f) * amplitude
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                drawPath(
                    path = path,
                    color = accentColor.copy(alpha = 0.85f),
                    style = Stroke(width = 3.dp.toPx())
                )

                // Secondary soft glow waveform
                val glowPath = Path()
                for (i in 0..steps) {
                    val x = i * stepWidth
                    val progress = i.toFloat() / steps.toFloat()
                    val amplitude = if (isPlaying) (canvasHeight * 0.28f) * pulseScale else canvasHeight * 0.05f
                    val y = centerY + sin(phase * 1.2f + progress * 6f + 1f) * amplitude
                    if (i == 0) glowPath.moveTo(x, y) else glowPath.lineTo(x, y)
                }
                drawPath(
                    path = glowPath,
                    color = accentColor.copy(alpha = 0.4f),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
            VisualizerStyle.PARTICLES -> {
                val particleCount = 20
                for (i in 0 until particleCount) {
                    val norm = i.toFloat() / particleCount.toFloat()
                    val x = canvasWidth * norm
                    val yOffset = sin(phase * (1f + norm) + norm * 10f) * (canvasHeight * 0.35f) * (if (isPlaying) pulseScale else 0.2f)
                    val y = centerY + yOffset
                    val radius = if (isPlaying) (3.5.dp.toPx() * (0.6f + pulseScale * 0.5f)) else 2.dp.toPx()

                    drawCircle(
                        color = accentColor.copy(alpha = if (isPlaying) 0.85f else 0.4f),
                        radius = radius,
                        center = Offset(x, y)
                    )
                }
            }
            else -> {}
        }
    }
}
