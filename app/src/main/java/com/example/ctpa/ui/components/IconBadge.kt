package com.example.ctpa.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ctpa.ui.theme.Emerald500
import com.example.ctpa.ui.theme.Gray200
import com.example.ctpa.ui.theme.Gray500
import com.example.ctpa.ui.theme.Gray900
import com.example.ctpa.ui.theme.White

/**
 * Contenedor decorativo para iconos: fondo tintado, borde sutil de 1dp,
 * sombra ligera e icono centrado. Da profundidad a cualquier icono plano.
 */
@Composable
fun IconBadge(
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    containerColor: Color = tint.copy(alpha = 0.12f),
    size: Dp = 36.dp,
    iconSize: Dp = 18.dp,
    cornerRadius: Dp = 10.dp,
    borderColor: Color = tint.copy(alpha = 0.25f),
    contentDescription: String? = null
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(cornerRadius), clip = false)
            .clip(RoundedCornerShape(cornerRadius))
            .background(containerColor)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

/** Variante redonda del [IconBadge]. */
@Composable
fun CircleIconBadge(
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    containerColor: Color = tint.copy(alpha = 0.12f),
    size: Dp = 36.dp,
    iconSize: Dp = 18.dp,
    contentDescription: String? = null
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(elevation = 2.dp, shape = CircleShape, clip = false)
            .clip(CircleShape)
            .background(containerColor)
            .border(width = 1.dp, color = tint.copy(alpha = 0.25f), shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * Badge con degradado: ideal para indicadores de estado "activos", donde
 * el icono se ve sólido y con brillo.
 */
@Composable
fun GradientIconBadge(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    gradient: Brush = Brush.linearGradient(
        colors = listOf(Emerald500, Emerald500.copy(alpha = 0.7f))
    ),
    size: Dp = 40.dp,
    iconSize: Dp = 20.dp,
    cornerRadius: Dp = 12.dp,
    contentDescription: String? = null
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(cornerRadius), clip = false)
            .clip(RoundedCornerShape(cornerRadius))
            .background(gradient)
            .border(
                width = 1.dp,
                color = White.copy(alpha = 0.35f),
                shape = RoundedCornerShape(cornerRadius)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = White,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * Punto con halo pulsante: comunica que algo está "en vivo" (GPS, cronómetro).
 */
@Composable
fun PulsingDot(
    color: Color,
    modifier: Modifier = Modifier,
    dotSize: Dp = 8.dp
) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale"
    )

    Box(modifier = modifier.size(dotSize * 2.6f), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(dotSize * scale)
                .clip(CircleShape)
                .background(color.copy(alpha = (0.28f * (1f - pulse)).coerceAtLeast(0f)))
        )
        Box(
            modifier = Modifier
                .size(dotSize)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, White.copy(alpha = 0.6f), CircleShape)
        )
    }
}

/**
 * Barra de progreso con pista tintada y relleno degradado.
 */
@Composable
fun ProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    barColor: Color = Emerald500,
    trackColor: Color = Gray200,
    height: Dp = 6.dp
) {
    val clamped = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(clamped)
                .fillMaxHeight()
                .clip(RoundedCornerShape(height / 2))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(barColor.copy(alpha = 0.7f), barColor)
                    )
                )
        )
    }
}

/**
 * Fila con badge de icono + textos: patrón repetido en tarjetas de estado.
 */
@Composable
fun IconRow(
    icon: ImageVector,
    tint: Color,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    badgeSize: Dp = 34.dp,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        IconBadge(icon = icon, tint = tint, size = badgeSize, iconSize = badgeSize * 0.5f)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Gray900,
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = Gray500,
                fontSize = 11.sp
            )
        }
        trailing()
    }
}

/** Icono de verificación, útil en chips de confirmación. */
@Composable
fun VerifiedCheck(modifier: Modifier = Modifier, color: Color = Emerald500, size: Dp = 14.dp) {
    Icon(
        imageVector = Icons.Filled.CheckCircle,
        contentDescription = null,
        tint = color,
        modifier = modifier.size(size)
    )
}
