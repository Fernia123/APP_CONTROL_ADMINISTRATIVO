package com.example.ctpa.ui.components


import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar con degradado, anillo de estado e indicador pulsante
            val statusColor = when (worker.status) {
                WorkerStatus.ACTIVE -> Emerald500
                WorkerStatus.ON_BREAK -> Color(0xFFF59E0B)
                WorkerStatus.CLOCKED_OUT -> Gray400
            }
            Box(modifier = Modifier.size(48.dp)) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(3.dp, CircleShape, clip = false)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    statusColor.copy(alpha = 0.95f),
                                    statusColor.copy(alpha = 0.60f)
                                )
                            )
                        )
                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = worker.workerName.split(" ")
                            .mapNotNull { it.firstOrNull() }
                            .take(2)
                            .joinToString("")
                            .uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = White,
                        letterSpacing = 1.sp
                    )
                }
                // Anillo exterior del estado
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(statusColor)
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
    // Triple: texto, color de texto y color de fondo
    val (text, color, bgColor, icon) = when (status) {
        WorkerStatus.ACTIVE ->
            StatusChipData("Active", Emerald600, Emerald50, Icons.Filled.PlayCircle)
        WorkerStatus.ON_BREAK ->
            StatusChipData("On Break", Color(0xFFD97706), Amber50, Icons.Filled.Coffee)
        WorkerStatus.CLOCKED_OUT ->
            StatusChipData("Clocked Out", Gray500, Gray100, Icons.Filled.PowerSettingsNew)
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(11.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp
        )
    }
}

private data class StatusChipData(
    val text: String,
    val color: Color,
    val background: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private fun formatMinutesToHours(minutes: Int): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return "${hours}h ${String.format("%02d", mins)}m"
}