package top.yogiczy.mytv.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val darkColorScheme
    @Composable get() = darkColorScheme(
        primary = Color(0xFF27C4FF),
        onPrimary = Color(0xFF001F2B),
        primaryContainer = Color(0xFF08384A),
        onPrimaryContainer = Color(0xFFC6F1FF),
        inversePrimary = Color(0xFF006786),
        secondary = Color(0xFFB8C7D9),
        onSecondary = Color(0xFF203141),
        secondaryContainer = Color(0xFF253648),
        onSecondaryContainer = Color(0xFFD8E8FA),
        tertiary = Color(0xFF75F18B),
        onTertiary = Color(0xFF003913),
        tertiaryContainer = Color(0xFF0E4E24),
        onTertiaryContainer = Color(0xFFB9F8C3),
        background = Color(0xFF05070A),
        onBackground = Color(0xFFF4F8FF),
        surface = Color(0xFF0B0F14),
        onSurface = Color(0xFFF4F8FF),
        surfaceVariant = Color(0xFF1B242E),
        onSurfaceVariant = Color(0xFFC2CCD8),
        surfaceTint = Color(0xFF27C4FF),
        outline = Color(0xFF6E7A89),
        outlineVariant = Color(0xFF27313D),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        scrim = Color(0xFF000000),
        surfaceContainer = Color(0xFF10161D),
        surfaceContainerHigh = Color(0xFF151D26),
        surfaceContainerHighest = Color(0xFF1B2530),
        surfaceContainerLow = Color(0xFF090D12),
        surfaceContainerLowest = Color(0xFF030507),
    )

private val leanbackShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(8.dp),
)

@Composable
fun LeanbackTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = darkColorScheme,
        shapes = leanbackShapes,
    ) {
        androidx.tv.material3.MaterialTheme(
            androidx.tv.material3.darkColorScheme(
                primary = MaterialTheme.colorScheme.primary,
                onPrimary = MaterialTheme.colorScheme.onPrimary,
                primaryContainer = MaterialTheme.colorScheme.primaryContainer,
                onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer,
                secondary = MaterialTheme.colorScheme.secondary,
                onSecondary = MaterialTheme.colorScheme.onSecondary,
                secondaryContainer = MaterialTheme.colorScheme.secondaryContainer,
                onSecondaryContainer = MaterialTheme.colorScheme.onSecondaryContainer,
                tertiary = MaterialTheme.colorScheme.tertiary,
                onTertiary = MaterialTheme.colorScheme.onTertiary,
                tertiaryContainer = MaterialTheme.colorScheme.tertiaryContainer,
                onTertiaryContainer = MaterialTheme.colorScheme.onTertiaryContainer,
                background = MaterialTheme.colorScheme.background,
                onBackground = MaterialTheme.colorScheme.onBackground,
                surface = MaterialTheme.colorScheme.surface,
                onSurface = MaterialTheme.colorScheme.onSurface,
                surfaceVariant = MaterialTheme.colorScheme.surfaceVariant,
                onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant,
                error = MaterialTheme.colorScheme.error,
                onError = MaterialTheme.colorScheme.onError,
                errorContainer = MaterialTheme.colorScheme.errorContainer,
                onErrorContainer = MaterialTheme.colorScheme.onErrorContainer,
            ),
        ) {
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.onBackground,
                androidx.tv.material3.LocalContentColor provides androidx.tv.material3.MaterialTheme.colorScheme.onBackground,
            ) {
                content()
            }
        }
    }
}
