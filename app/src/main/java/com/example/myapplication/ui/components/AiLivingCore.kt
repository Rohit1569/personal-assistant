package com.example.myapplication.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.ElectricCyan
import com.example.myapplication.ui.theme.DeepPurple

@Composable
fun AiLivingCore(
    modifier: Modifier = Modifier,
    isListening: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "core")
    
    // Breathing animation
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.5f else 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    // Cinematic rotation
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing)
        ), label = "rotation"
    )

    Canvas(modifier = modifier.size(220.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val baseRadius = size.minDimension / 3.5f
        val currentRadius = baseRadius * pulseScale

        // 1. Exterior Atmospheric Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(ElectricCyan.copy(alpha = 0.2f), Color.Transparent),
                center = center,
                radius = currentRadius * 2.5f
            ),
            radius = currentRadius * 2.5f
        )

        // 2. The Neural Core (Glowing Orb)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(ElectricCyan.copy(alpha = 0.8f), DeepPurple.copy(alpha = 0.3f), Color.Transparent),
                center = center,
                radius = currentRadius
            ),
            radius = currentRadius
        )

        // 3. JARVIS-style HUD Rings
        val ringSize = Size(currentRadius * 2.2f, currentRadius * 2.2f)
        val ringTopLeft = Offset(center.x - currentRadius * 1.1f, center.y - currentRadius * 1.1f)

        drawArc(
            color = ElectricCyan.copy(alpha = 0.6f),
            startAngle = rotation,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = ringTopLeft,
            size = ringSize,
            style = Stroke(width = 3f)
        )

        drawArc(
            color = DeepPurple.copy(alpha = 0.4f),
            startAngle = -rotation * 1.5f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = ringTopLeft,
            size = ringSize,
            style = Stroke(width = 1.5f)
        )
    }
}
