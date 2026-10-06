package com.example.ctpa.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ctpa.domain.model.Task
import com.example.ctpa.domain.model.TaskStatus
import com.example.ctpa.ui.theme.Emerald50
import com.example.ctpa.ui.theme.Emerald500
import com.example.ctpa.ui.theme.Emerald600
import com.example.ctpa.ui.theme.Gray100
import com.example.ctpa.ui.theme.Gray400
import com.example.ctpa.ui.theme.Gray500
import com.example.ctpa.ui.theme.Gray900
import com.example.ctpa.ui.theme.White

/**
 * Fila de tarea con icono de estado en badge tintado, barra de avance real
 * (transcurrido / estimado) y chips de estado con icono propio.
 */
@Composable
fun TaskItem(task: Task, modifier: Modifier = Modifier) {
    val accent = statusColor(task.status)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icono de estado en badge tintado
            CircleIconBadge(
                icon = statusIcon(task.status),
                tint = accent,
                containerColor = accent.copy(alpha = 0.12f),
                size = 36.dp,
                iconSize = 18.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Gray900,
                        maxLines = 2,
                        modifier = Modifier.weight(1f)
                    )
                    StatusChip(status = task.status)
                }

                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500,
                        modifier = Modifier.padding(top = 2.dp),
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ===== Detalle inferior según estado =====
                when (task.status) {
                    TaskStatus.ACTIVE -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Schedule,
                                contentDescription = null,
                                tint = Gray400,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${task.elapsedMinutes}m / ${task.estimatedMinutes}m estimados",
                                style = MaterialTheme.typography.labelSmall,
                                color = Gray500
                            )
                        }
                    }

                    TaskStatus.IN_PROGRESS -> {
                        val progress = if (task.estimatedMinutes > 0) {
                            task.elapsedMinutes.toFloat() / task.estimatedMinutes
                        } else 0f
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${task.elapsedMinutes}m de ${task.estimatedMinutes}m",
                                style = MaterialTheme.typography.labelSmall,
                                color = Gray500
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "${(progress.coerceIn(0f, 1f) * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFD97706),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        ProgressBar(
                            progress = progress,
                            barColor = Color(0xFFF59E0B),
                            trackColor = Gray100,
                            height = 5.dp
                        )
                    }

                    TaskStatus.COMPLETED -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Emerald500,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Firmada ${task.signedTime ?: "--:--"} • ${task.signedBy ?: "Supervisor"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Gray500
                            )
                        }
                    }

                    TaskStatus.DONE -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.CameraAlt,
                                contentDescription = null,
                                tint = Gray400,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (task.photoCount > 0) {
                                    "${task.photoCount} evidencia(s) adjunta(s)"
                                } else "Archivada sin evidencia",
                                style = MaterialTheme.typography.labelSmall,
                                color = Gray500
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Icono derecho: verificado o chevron
            if (task.status == TaskStatus.COMPLETED || task.status == TaskStatus.DONE) {
                Icon(
                    Icons.Filled.DoneAll,
                    contentDescription = "Completada",
                    tint = Emerald500,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterVertically)
                )
            } else {
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = Gray400,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}

@Composable
private fun StatusChip(status: TaskStatus) {
    val (label, color) = when (status) {
        TaskStatus.ACTIVE -> "Activa" to Emerald600
        TaskStatus.IN_PROGRESS -> "En proceso" to Color(0xFFD97706)
        TaskStatus.COMPLETED -> "Firmada" to Color(0xFF2563EB)
        TaskStatus.DONE -> "Archivada" to Gray500
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.10f))
            .padding(horizontal = 7.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box8(status = status, color = color)
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun Box8(status: TaskStatus, color: Color) {
    Icon(
        imageVector = statusIcon(status),
        contentDescription = null,
        tint = color,
        modifier = Modifier.size(10.dp)
    )
}

private fun statusIcon(status: TaskStatus): ImageVector = when (status) {
    TaskStatus.ACTIVE -> Icons.Filled.DirectionsRun
    TaskStatus.IN_PROGRESS -> Icons.Filled.Autorenew
    TaskStatus.COMPLETED -> Icons.Filled.CheckCircle
    TaskStatus.DONE -> Icons.Filled.DoneAll
}

private fun statusColor(status: TaskStatus): Color = when (status) {
    TaskStatus.ACTIVE -> Emerald500
    TaskStatus.IN_PROGRESS -> Color(0xFFF59E0B)
    TaskStatus.COMPLETED -> Color(0xFF3B82F6)
    TaskStatus.DONE -> Gray400
}
