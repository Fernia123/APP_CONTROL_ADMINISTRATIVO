package com.example.ctpa.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ctpa.ui.theme.Emerald500
import com.example.ctpa.ui.theme.Gray400
import com.example.ctpa.ui.theme.White

private data class BlipData(
    val initials: String,
    val name: String,
    val zone: String,
    val fx: Float,
    val fy: Float,
    val color: Color
)

/**
 * Radar GPS decorado y "vivo": barrido animado, anillos concéntricos,
 * retícula, blips de trabajadores con halo pulsante y leyenda de señal.
 */
@Composable
fun GpsRadar(modifier: Modifier = Modifier) {
    val blips = listOf(
        BlipData("MS", "Mario Silva", "Sector 7B", 0.24f, 0.28f, Emerald500),
        BlipData("ER", "Elvira Rios", "Warehouse B", 0.70f, 0.24f, Color(0xFFF59E0B)),
        BlipData("CR", "Carlos Rod.", "Plant 7", 0.58f, 0.66f, Color(0xFF3B82F6)),
        BlipData("JJ", "Joshue Jesus", "Oficina", 0.30f, 0.72f, Emerald500)
    )

    val transition = rememberInfiniteTransition(label = "radar")
    val sweep by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )
    val ping by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ping"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2937)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // ===== Header con badge de icono =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconBadge(
                    icon = Icons.Filled.Radar,
                    tint = Emerald500,
                    containerColor = Emerald500.copy(alpha = 0.18f),
                    borderColor = Emerald500.copy(alpha = 0.45f),
                    size = 34.dp,
                    iconSize = 18.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Industrial GPS Radar",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                    Text(
                        text = "Rastreo en tiempo real • ${blips.size} unidades",
                        style = MaterialTheme.typography.labelSmall,
                        color = Gray400,
                        fontSize = 10.sp
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Emerald500.copy(alpha = 0.18f))
                        .border(
                            width = 1.dp,
                            color = Emerald500.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PulsingDot(color = Emerald500, dotSize = 6.dp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Live",
                        style = MaterialTheme.typography.labelSmall,
                        color = Emerald500,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ===== Mapa / radar =====
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(10.dp), clip = true)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF111827))
            ) {
                val areaWidth = maxWidth
                val areaHeight = maxHeight

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = minOf(size.width, size.height) / 2f - 8.dp.toPx()

                    // Retícula
                    val gridColor = Color.White.copy(alpha = 0.06f)
                    val gridStep = 34.dp.toPx()
                    var gx = gridStep
                    while (gx < size.width) {
                        drawLine(gridColor, Offset(gx, 0f), Offset(gx, size.height), 1.dp.toPx())
                        gx += gridStep
                    }
                    var gy = gridStep
                    while (gy < size.height) {
                        drawLine(gridColor, Offset(0f, gy), Offset(size.width, gy), 1.dp.toPx())
                        gy += gridStep
                    }

                    // Anillos concéntricos
                    val ringColor = Emerald500.copy(alpha = 0.28f)
                    listOf(1f, 0.72f, 0.44f, 0.2f).forEach { factor ->
                        drawCircle(
                            color = ringColor,
                            radius = radius * factor,
                            center = center,
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }

                    // Ejes
                    val axisColor = Emerald500.copy(alpha = 0.22f)
                    drawLine(
                        axisColor,
                        Offset(center.x - radius, center.y),
                        Offset(center.x + radius, center.y),
                        1.dp.toPx()
                    )
                    drawLine(
                        axisColor,
                        Offset(center.x, center.y - radius),
                        Offset(center.x, center.y + radius),
                        1.dp.toPx()
                    )

                    // Barrido rotatorio con estela
                    rotate(degrees = sweep) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Emerald500.copy(alpha = 0f),
                                    Emerald500.copy(alpha = 0.10f),
                                    Emerald500.copy(alpha = 0.34f)
                                )
                            ),
                            startAngle = -90f,
                            sweepAngle = 68f,
                            useCenter = true,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2)
                        )
                        drawLine(
                            color = Emerald500.copy(alpha = 0.85f),
                            start = center,
                            end = Offset(center.x, center.y - radius),
                            strokeWidth = 1.6.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }

                    // Halo central pulsante
                    drawCircle(
                        color = Emerald500.copy(alpha = (0.35f * (1f - ping)).coerceAtLeast(0f)),
                        radius = 4.dp.toPx() + ping * 14.dp.toPx(),
                        center = center
                    )
                    drawCircle(color = Emerald500, radius = 3.5.dp.toPx(), center = center)
                    drawCircle(
                        color = White.copy(alpha = 0.85f),
                        radius = 3.5.dp.toPx(),
                        center = center,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                // Blips de trabajadores (halo + avatar + etiqueta)
                blips.forEachIndexed { index, blip ->
                    val phase = (ping + index * 0.25f) % 1f
                    Box(
                        modifier = Modifier
                            .offset(
                                x = areaWidth * blip.fx - 11.dp,
                                y = areaHeight * blip.fy - 11.dp
                            )
                            .size(22.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(
                                    blip.color.copy(alpha = (0.30f * (1f - phase)).coerceAtLeast(0f))
                                )
                        )
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .shadow(elevation = 3.dp, shape = CircleShape, clip = false)
                                .clip(CircleShape)
                                .background(blip.color)
                                .border(1.5.dp, White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = blip.initials.first().toString(),
                                color = White,
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = blip.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = White.copy(alpha = 0.85f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .offset(
                                x = areaWidth * blip.fx - 8.dp,
                                y = areaHeight * blip.fy + 13.dp
                            )
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.45f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }

                // Coordenadas del terminal
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.45f))
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.NearMe,
                        contentDescription = null,
                        tint = Emerald500,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "19.4326° N, 99.1332° W",
                        style = MaterialTheme.typography.labelSmall,
                        color = White.copy(alpha = 0.8f),
                        fontSize = 8.sp
                    )
                }

                // Escala gráfica
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height(3.dp)
                            .background(White.copy(alpha = 0.6f))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "100 m",
                        style = MaterialTheme.typography.labelSmall,
                        color = White.copy(alpha = 0.7f),
                        fontSize = 8.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ===== Leyenda de señal =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LegendChip(
                    icon = Icons.Filled.MyLocation,
                    text = "Precisión ±3 m",
                    color = Emerald500,
                    modifier = Modifier.weight(1f)
                )
                LegendChip(
                    icon = Icons.Filled.Refresh,
                    text = "Sync hace 2 s",
                    color = Color(0xFF3B82F6),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${blips.size} trabajadores dentro del rango verificado",
                style = MaterialTheme.typography.labelSmall,
                color = Gray400
            )
        }
    }
}

@Composable
private fun LegendChip(
    icon: ImageVector,
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF374151))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = White.copy(alpha = 0.85f),
            fontSize = 10.sp,
            maxLines = 1
        )
    }
}
