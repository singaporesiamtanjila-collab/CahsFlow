package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.theme.CncBorder
import com.example.ui.theme.CncBorderBright
import com.example.ui.theme.CncCost
import com.example.ui.theme.CncCostLight
import com.example.ui.theme.CncLaserCyan
import com.example.ui.theme.CncPrimary
import com.example.ui.theme.CncProfit
import com.example.ui.theme.CncProfitLight
import com.example.ui.theme.CncSurface
import com.example.ui.theme.CncTextPrimary
import com.example.ui.theme.CncTextSecondary
import java.util.Locale

@Composable
fun CadGridBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val step = 32.dp.toPx()
        val gridColor = Color(0xFFF1F5F9)
        val dotColor = Color(0xFFE2E8F0)

        // Draw light subtle technical CAD grid lines
        var x = 0f
        while (x < size.width) {
            drawLine(
                color = gridColor,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1f
            )
            x += step
        }

        var y = 0f
        while (y < size.height) {
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            y += step
        }

        // Intersections dots
        var dx = 0f
        while (dx < size.width) {
            var dy = 0f
            while (dy < size.height) {
                drawCircle(
                    color = dotColor,
                    radius = 1.5f,
                    center = Offset(dx, dy)
                )
                dy += step
            }
            dx += step
        }
    }
}

@Composable
fun AnimatedCncCutter(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cnc_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier
            .size(size)
            .rotate(rotation),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.toPx() / 2, size.toPx() / 2)
            val radius = size.toPx() / 2 - 2f
            drawCircle(
                brush = Brush.sweepGradient(listOf(CncPrimary, CncLaserCyan, Color(0xFF38BDF8), CncPrimary)),
                radius = radius,
                center = center,
                style = Stroke(width = 3f)
            )
            // 4 cutting flutes
            for (i in 0..3) {
                val angle = Math.toRadians((i * 90.0))
                val endX = center.x + (radius * 0.7f * Math.cos(angle)).toFloat()
                val endY = center.y + (radius * 0.7f * Math.sin(angle)).toFloat()
                drawLine(
                    color = CncPrimary,
                    start = center,
                    end = Offset(endX, endY),
                    strokeWidth = 2f
                )
            }
        }
    }
}

@Composable
fun FuturisticMetricCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    accentColor: Color = CncPrimary,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Card(
        modifier = modifier
            .testTag(testTag)
            .border(1.dp, CncBorder, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CncSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Top accent indicator glow line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(accentColor, accentColor.copy(alpha = 0.2f))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title.uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = CncTextSecondary
                    )

                    Surface(
                        shape = CircleShape,
                        color = accentColor.copy(alpha = 0.12f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = (-0.5).sp
                    ),
                    color = CncTextPrimary
                )

                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = if (subtitle.startsWith("+")) CncProfit else CncTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun BiometricRadarScanner(
    modifier: Modifier = Modifier,
    isScanning: Boolean = true,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "biometric_radar")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep_angle"
    )

    Box(
        modifier = modifier
            .size(130.dp)
            .clickable(onClick = onClick)
            .testTag("biometric_scanner_target"),
        contentAlignment = Alignment.Center
    ) {
        // Radar waves Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val baseRadius = size.width / 2 - 16.dp.toPx()

            // Outer pulse circle
            if (isScanning) {
                drawCircle(
                    color = CncLaserCyan.copy(alpha = pulseAlpha),
                    radius = baseRadius * pulseScale,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // High-tech circular grid rings
            drawCircle(
                color = CncBorderBright,
                radius = baseRadius,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )
            drawCircle(
                color = Color(0xFFE2E8F0),
                radius = baseRadius * 0.7f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // Scanning radar sweep line
            if (isScanning) {
                val rad = Math.toRadians(sweepAngle.toDouble())
                val target = Offset(
                    (center.x + baseRadius * Math.cos(rad)).toFloat(),
                    (center.y + baseRadius * Math.sin(rad)).toFloat()
                )
                drawLine(
                    brush = Brush.linearGradient(listOf(CncLaserCyan, Color.Transparent), center, target),
                    start = center,
                    end = target,
                    strokeWidth = 2.5f
                )
            }
        }

        // Inner glowing button
        Surface(
            shape = CircleShape,
            color = CncSurface,
            shadowElevation = 6.dp,
            border = androidx.compose.foundation.BorderStroke(2.dp, CncBorderBright),
            modifier = Modifier.size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Scan Biometrics",
                    tint = CncPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

@Composable
fun CncImageThumbnail(
    imageUri: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF1F5F9))
            .border(1.dp, CncBorder, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        when {
            imageUri == "preset:door" -> {
                Image(
                    painter = painterResource(id = R.drawable.cnc_door_sample),
                    contentDescription = "Door Design",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            imageUri == "preset:panel" -> {
                Image(
                    painter = painterResource(id = R.drawable.cnc_panel_sample),
                    contentDescription = "Panel Design",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            imageUri == "preset:banner" -> {
                Image(
                    painter = painterResource(id = R.drawable.cnc_hero_banner),
                    contentDescription = "CAD Blueprint",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            !imageUri.isNullOrBlank() -> {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "CNC Design Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = CncTextSecondary.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "CNC",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = CncTextSecondary.copy(alpha = 0.7f),
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}
