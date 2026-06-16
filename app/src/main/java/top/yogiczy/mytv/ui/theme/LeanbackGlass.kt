package top.yogiczy.mytv.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object LeanbackGlass {
    val Overlay = Color(0xCC07111A)
    val OverlaySoft = Color(0x8C07111A)
    val OverlayStrong = Color(0xE6081119)
    val Panel = Color(0xB30B2230)
    val Stroke = Color.White.copy(alpha = 0.20f)
    val StrokeSoft = Color.White.copy(alpha = 0.10f)
    val Focus = Color(0xFF22D8F5)
    val FocusContainer = Color(0xB0129EB3)
    val Success = Color(0xFF72F28A)
    val Warning = Color(0xFFFFE500)
    val Muted = Color.White.copy(alpha = 0.66f)
}

@Composable
fun LeanbackGlassSurface(
    modifier: Modifier = Modifier,
    focused: Boolean = false,
    selected: Boolean = false,
    shape: Shape = MaterialTheme.shapes.medium,
    containerColor: Color = when {
        focused -> LeanbackGlass.FocusContainer
        selected -> LeanbackGlass.Focus.copy(alpha = 0.24f)
        else -> LeanbackGlass.Overlay
    },
    contentColor: Color = when {
        focused || selected -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurface
    },
    borderColor: Color = when {
        focused -> LeanbackGlass.Focus
        selected -> LeanbackGlass.Focus.copy(alpha = 0.78f)
        else -> LeanbackGlass.Stroke
    },
    borderWidth: Dp = if (focused || selected) 1.5.dp else 1.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(containerColor, shape)
            .border(BorderStroke(borderWidth, borderColor), shape),
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            content()
        }
    }
}

@Composable
fun LeanbackStatusChip(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    active: Boolean = false,
    accentColor: Color = if (active) LeanbackGlass.Success else MaterialTheme.colorScheme.primary,
) {
    val containerColor = if (active) accentColor.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.07f)
    val contentColor = if (active) accentColor else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier
            .height(30.dp)
            .clip(MaterialTheme.shapes.small)
            .background(containerColor, MaterialTheme.shapes.small)
            .border(
                BorderStroke(1.dp, contentColor.copy(alpha = if (active) 0.44f else 0.28f)),
                MaterialTheme.shapes.small,
            )
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides contentColor,
            LocalTextStyle provides MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp),
                )
            }
            Text(text = text, maxLines = 1)
        }
    }
}

fun Modifier.leanbackBottomScrim(): Modifier = background(
    Brush.verticalGradient(
        0f to Color.Transparent,
        0.32f to Color.Black.copy(alpha = 0.12f),
        1f to Color.Black.copy(alpha = 0.82f),
    )
)

fun Modifier.leanbackSideScrim(): Modifier = background(
    Brush.horizontalGradient(
        0f to Color.Transparent,
        0.52f to Color.Black.copy(alpha = 0.18f),
        1f to Color.Black.copy(alpha = 0.74f),
    )
)
