package com.example.ctpa.ui.components


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ctpa.domain.model.AttendanceRecord
import com.example.ctpa.domain.model.WorkerStatus
import com.example.ctpa.ui.theme.*

@Composable
fun ActiveWorkerRow(
    worker: AttendanceRecord,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar con indicador de estado
            Box {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Gray200),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = worker.workerName.split(" ").map { it.first() }.joinToString(""),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Gray700
                    )
                }
                // Indicador de estado
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(
                            when (worker.status) {
                                WorkerStatus.ACTIVE -> Emerald500
                                WorkerStatus.ON_BREAK -> Color(0xFFF59E0B) // Amber
                                WorkerStatus.CLOCKED_OUT -> Gray400
                            }
                        )
                        .background(
                            shape = CircleShape,
                            color = Color.Transparent
                        )
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info del trabajador
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = worker.workerName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray900
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "#${worker.workerId}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Gray500
                    )
                }

                Text(
                    text = worker.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            // Tiempo y pago
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatMinutesToHours(worker.elapsedMinutes),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Gray900
                )
                Text(
                    text = "$${String.format("%.2f", worker.currentPay)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Emerald600,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Badge de estado
            StatusBadge(status = worker.status)

            Spacer(modifier = Modifier.width(4.dp))

            // Indicador de detalles
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = "Ver detalle",
                tint = Gray400,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun StatusBadge(status: WorkerStatus) {
    // ✅ CORRECCIÓN: Usar Triple en lugar de "to" anidado
    val (text, color, bgColor) = when (status) {
        WorkerStatus.ACTIVE -> Triple("Active", Emerald600, Emerald50)
        WorkerStatus.ON_BREAK -> Triple("On Break", Color(0xFFD97706), Color(0xFFFEF3C7))
        WorkerStatus.CLOCKED_OUT -> Triple("Clocked Out", Gray500, Gray100)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp
        )
    }
}

private fun formatMinutesToHours(minutes: Int): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return "${hours}h ${String.format("%02d", mins)}m"
}