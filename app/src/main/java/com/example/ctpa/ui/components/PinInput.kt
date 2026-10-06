package com.example.ctpa.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ctpa.ui.theme.Emerald500
import com.example.ctpa.ui.theme.Gray200
import com.example.ctpa.ui.theme.Gray300
import com.example.ctpa.ui.theme.White

/**
 * Entrada de PIN de 4 dígitos: ranuras con anillo, casilla llenada con check
 * y animación de escala en el último dígito para confirmar la pulsación.
 */
@Composable
fun PinInput(
    pin: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        repeat(4) { index ->
            val isFilled = index < pin.length
            val isLatest = index == pin.length - 1
            val scale by animateFloatAsState(
                targetValue = if (isLatest) 1.08f else 1f,
                animationSpec = tween(durationMillis = 180),
                label = "pinScale"
            )

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .shadow(
                        elevation = if (isFilled) 4.dp else 0.dp,
                        shape = CircleShape,
                        clip = false
                    )
                    .clip(CircleShape)
                    .background(if (isFilled) Emerald500 else MaterialTheme.colorScheme.surface)
                    .border(
                        width = 2.dp,
                        color = if (isFilled) Emerald500.copy(alpha = 0.35f) else Gray200,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isFilled) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Dígito ${index + 1}",
                        tint = White,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Gray300)
                    )
                }
            }
        }
    }
}
