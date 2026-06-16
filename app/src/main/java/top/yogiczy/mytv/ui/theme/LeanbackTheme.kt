package top.yogiczy.mytv.ui.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val darkColorScheme
    @Composable get() = darkColorScheme(
        // 暗色主题：紫系 Material3 基准调色板
        primary = Color(0xFFD0BCFF),
        onPrimary = Color(0xFF381E72),
        primaryContainer = Color(0xFF4F378B),
        onPrimaryContainer = Color(0xFFEADDFF),
        inversePrimary = Color(0xFF6750A4),
        secondary = Color(0xFFCCC2DC),
        onSecondary = Color(0xFF332D41),
        secondaryContainer = Color(0xFF4A4458),
        onSecondaryContainer = Color(0xFFE8DEF8),
        tertiary = Color(0xFFEFB8C8),
        onTertiary = Color(0xFF492532),
        tertiaryContainer = Color(0xFF633B48),
        onTertiaryContainer = Color(0xFFFFD8E4),
        background = Color(0xFF1C1B1F),
        onBackground = Color(0xFFE6E1E5),
        surface = Color(0xFF1C1B1F),
        onSurface = Color(0xFFE6E1E5),
        surfaceVariant = Color(0xFF49454F),
        onSurfaceVariant = Color(0xFFCAC4D0),
        surfaceTint = Color(0xFFD0BCFF),
        outline = Color(0xFF938F99),
        outlineVariant = Color(0xFF49454F),
        error = Color(0xFFF2B8B5),
        onError = Color(0xFF601410),
        errorContainer = Color(0xFF8C1D18),
        onErrorContainer = Color(0xFFF9DEDC),
        scrim = Color(0xFF000000),
        surfaceContainer = Color(0xFF211F26),
        surfaceContainerHigh = Color(0xFF2B2930),
        surfaceContainerHighest = Color(0xFF36343B),
        surfaceContainerLow = Color(0xFF1D1B22),
        surfaceContainerLowest = Color(0xFF161319),
    )

@Composable
fun LeanbackTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = darkColorScheme,
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