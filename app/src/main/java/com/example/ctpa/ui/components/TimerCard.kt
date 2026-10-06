package com.example.ctpa.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ctpa.ui.theme.Emerald50
import com.example.ctpa.ui.theme.Emerald500
import com.example.ctpa.ui.theme.Emerald600
import com.example.ctpa.ui.theme.Gray200
import com.example.ctpa.ui.theme.Gray50
import com.example.ctpa.ui.theme.Gray500
import com.example.ctpa.ui.theme.Gray700
import com.example.ctpa.ui.theme.Gray900
import com.example.ctpa.ui.theme.White

/**
 * Tarjeta de turno con aspecto "instrumento vivo": badge de icono, punto
 * pulsante de señal, cronómetro grande, barra de avance del turno con porcentaje,
 * fichas de inicio/objetivo y bloque GPS verificado.
 */
@Composable
fun TimerCard(
    timeElapsed: String,
    startTime: String,
    targetTime: String,
    isOnBreak: Boolean,
    onTakeBreak: () -> Unit,
    facilityName: String = "Main Facility",
    modifier: Modifier = Modifier
) {
    val progress = shiftProgress(timeElapsed, targetTime)
    val statusColor = when {
        isOnBreak -> Color(0xFFF59E0B)
        timeElapsed == "00:00:00" -> Gray500
        else -> Emerald500
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // ===== Header =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconBadge(
                    icon = Icons.Filled.Timer,
                    tint = Emerald500,
                    containerColor = Emerald50.copy(alpha = 1f),
                    borderColor = Emerald500.copy(alpha = 0.35f),
                    size = 36.dp,
                    iconSize = 18.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ACTIVE SHIFT • LIVE TIMING",
                        style = MaterialTheme.typography.labelSmall,
                        color = Gray700,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PulsingDot(color = statusColor, dotSize = 5.dp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when {
                                isOnBreak -> "En pausa — tiempo detenido"
                                timeElapsed == "00:00:00" -> "Turno sin iniciar"
                                else -> "Registrando en vivo"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                OutlinedButton(
                    onClick = onTakeBreak,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Gray700),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Gray700.copy(alpha = 0.3f)
                    )
                ) {
                    Icon(
                        if (isOnBreak) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isOnBreak) "Resume" else "Take Break", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ===== Cronómetro =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = timeElapsed,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Gray900,
                    fontSize = 46.sp
                )
                Text(
                    text = "hh:mm:ss",
                    style = MaterialTheme.typography.labelSmall,
                    color = Gray500,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ===== Avance del turno =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.WatchLater,
                    contentDescription = null,
                    tint = Gray500,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Avance del turno",
                    style = MaterialTheme.typography.labelSmall,
                    color = Gray500
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = Emerald600,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            ProgressBar(
                progress = progress,
                barColor = if (isOnBreak) Color(0xFFF59E0B) else Emerald500
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ===== Inicio / Objetivo =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TimeChip(
                    icon = Icons.Filled.Flag,
                    label = "Started",
                    value = startTime,
                    tint = Emerald500,
                    modifier = Modifier.weight(1f)
                )
                TimeChip(
                    icon = Icons.Filled.WatchLater,
                    label = "Target",
                    value = targetTime,
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== GPS verificado =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Emerald50)
                    .border(1.dp, Emerald500.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleIconBadge(
                    icon = Icons.Filled.LocationOn,
                    tint = Emerald500,
                    containerColor = MaterialTheme.colorScheme.surface,
                    size = 32.dp,
                    iconSize = 16.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Facility", style = MaterialTheme.typography.labelSmall, color = Gray500)
                    Text(
                        text = facilityName.ifBlank { "Main Facility" },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray900
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Emerald500)
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Shield,
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Verified Range",
                        color = White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Gray50)
            .border(1.dp, Gray200, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(
            icon = icon,
            tint = tint,
            containerColor = tint.copy(alpha = 0.12f),
            size = 28.dp,
            iconSize = 14.dp,
            cornerRadius = 8.dp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Gray500, fontSize = 10.sp)
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Gray900,
                fontSize = 13.sp
            )
        }
    }
}

/** Avance del turno: tiempo transcurrido / duración objetivo. */
private fun shiftProgress(timeElapsed: String, targetTime: String): Float {
    val elapsed = timeElapsed.split(":").mapNotNull { it.toLongOrNull() }
    val elapsedSeconds = when (elapsed.size) {
        3 -> elapsed[0] * 3600 + elapsed[1] * 60 + elapsed[2]
        2 -> elapsed[0] * 60 + elapsed[1]
        1 -> elapsed[0]
        else -> 0L
    }
    val hours = Regex("(\\d+)\\s*h").find(targetTime)?.groupValues?.get(1)?.toLongOrNull() ?: 8L
    val minutes = Regex("(\\d+)\\s*m").find(targetTime)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
    val total = (hours * 3600 + minutes * 60).coerceAtLeast(1L)
    return (elapsedSeconds.toFloat() / total).coerceIn(0f, 1f)
}
