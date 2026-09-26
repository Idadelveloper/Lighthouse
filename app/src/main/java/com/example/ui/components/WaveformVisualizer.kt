package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PrimaryActionBlue
import kotlin.math.sin

@Composable
fun WaveformVisualizer(
    modifier: Modifier = Modifier,
    isThinking: Boolean = false,
    waveColor: Color = PrimaryActionBlue
) {
    val transition = rememberInfiniteTransition(label = "waveform")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(if (isThinking) 1200 else 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
    ) {
        val width = size.width
        val height = size.height
        val midY = height / 2f

        // Draw primary harmonic wave
        val path1 = Path()
        val path2 = Path()

        val amplitude1 = if (isThinking) height * 0.38f else height * 0.28f
        val amplitude2 = amplitude1 * 0.6f

        val points = 80
        for (i in 0..points) {
            val x = (i.toFloat() / points) * width
            // Window envelope to taper edges smoothly
            val envelope = sin((i.toFloat() / points) * Math.PI).toFloat()

            val y1 = midY + sin((i * 0.25f) + phase) * amplitude1 * envelope
            val y2 = midY + sin((i * 0.35f) - phase * 0.8f) * amplitude2 * envelope

            if (i == 0) {
                path1.moveTo(x, y1)
                path2.moveTo(x, y2)
            } else {
                path1.lineTo(x, y1)
                path2.lineTo(x, y2)
            }
        }

        // Secondary subtle harmonic wave
        drawPath(
            path = path2,
            color = waveColor.copy(alpha = 0.35f),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        // Main harmonic wave
        drawPath(
            path = path1,
            color = waveColor.copy(alpha = 0.85f),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
