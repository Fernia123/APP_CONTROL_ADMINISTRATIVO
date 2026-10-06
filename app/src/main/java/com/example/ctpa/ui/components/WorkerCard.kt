package com.example.ctpa.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ctpa.domain.model.Worker
import com.example.ctpa.ui.theme.*

@Composable
fun WorkerCard(
    worker: Worker,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Foto con borde verde si está seleccionado
            Box {
                AsyncImage(
                    model = worker.photoUrl,
                    contentDescription = worker.name,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .then(
                            if (isSelected) Modifier.border(2.dp, Emerald500, CircleShape)
                            else Modifier
                        ),
                    contentScale = ContentScale.Crop
                )
                // Badge "Active"
                if (worker.isActive) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Emerald500)
                            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info del trabajador
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = worker.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Gray900
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "#${worker.id.takeLast(3)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500
                    )
                }

                Text(
                    text = "Shift A • 07:00 - 15:30",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500,
                    fontSize = 12.sp
                )

                // Tags
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TagChip(text = "11/AC")
                    TagChip(text = "Tech")
                }
            }

            // Flecha dropdown
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = Gray400,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun TagChip(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = Gray500,
        fontSize = 10.sp,
        modifier = Modifier
            .background(Gray100, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}