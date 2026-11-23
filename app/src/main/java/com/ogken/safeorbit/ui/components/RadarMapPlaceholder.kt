package com.ogken.safeorbit.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.SensorsOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ogken.safeorbit.theme.SafeOrbitBorder
import com.ogken.safeorbit.theme.SafeOrbitDeepBackground
import com.ogken.safeorbit.theme.SafeOrbitMint
import com.ogken.safeorbit.theme.SafeOrbitNavy
import com.ogken.safeorbit.theme.SafeOrbitSecondaryText
import com.ogken.safeorbit.theme.SafeOrbitTeal
import com.ogken.safeorbit.theme.SafeOrbitWhite

@Composable
fun RadarMapPlaceholder(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(SafeOrbitDeepBackground)
            .border(1.5.dp, SafeOrbitBorder, RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        // High-tech Canvas rendering grid, radar circles, and crosshairs
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerX = canvasWidth / 2f
            val centerY = canvasHeight / 2f

            // Background Grid Lines
            val gridStep = 40.dp.toPx()
            val gridColor = Color(0x1542D9B0)
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

            var x = 0f
            while (x < canvasWidth) {
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, canvasHeight),
                    strokeWidth = 1f,
                    pathEffect = dashEffect
                )
                x += gridStep
            }

            var y = 0f
            while (y < canvasHeight) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(canvasWidth, y),
                    strokeWidth = 1f,
                    pathEffect = dashEffect
                )
                y += gridStep
            }

            // Concentric Radar Rings
            val ringColor = Color(0x3042D9B0)
            val outerRadius = (canvasHeight.coerceAtMost(canvasWidth) / 2f) * 0.85f

            drawCircle(
                color = ringColor,
                radius = outerRadius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 1.5f)
            )

            drawCircle(
                color = ringColor.copy(alpha = 0.2f),
                radius = outerRadius * 0.6f,
                center = Offset(centerX, centerY),
                style = Stroke(width = 1.2f, pathEffect = dashEffect)
            )

            drawCircle(
                color = ringColor.copy(alpha = 0.35f),
                radius = outerRadius * 0.3f,
                center = Offset(centerX, centerY),
                style = Stroke(width = 1.5f)
            )

            // Center crosshairs
            drawLine(
                color = SafeOrbitTeal.copy(alpha = 0.6f),
                start = Offset(centerX - 24f, centerY),
                end = Offset(centerX + 24f, centerY),
                strokeWidth = 2f
            )
            drawLine(
                color = SafeOrbitTeal.copy(alpha = 0.6f),
                start = Offset(centerX, centerY - 24f),
                end = Offset(centerX, centerY + 24f),
                strokeWidth = 2f
            )
        }

        // Informative, honest architectural placeholder overlay
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(SafeOrbitNavy.copy(alpha = 0.9f))
                    .border(1.dp, SafeOrbitTeal.copy(alpha = 0.5f), CircleShape)
                    .padding(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SensorsOff,
                    contentDescription = "Sensors Inactive",
                    tint = SafeOrbitMint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Map Visualization Standby",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SafeOrbitWhite
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Stage 1 Architectural Foundation • No GPS Polling",
                style = MaterialTheme.typography.labelSmall,
                color = SafeOrbitMint
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Google Maps & live GPS tracking will be connected in Stage 2 with strict user consent verification.",
                style = MaterialTheme.typography.bodySmall,
                color = SafeOrbitSecondaryText,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}
