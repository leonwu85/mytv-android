package top.yogiczy.mytv.ui.screens.leanback.settings.components

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import top.yogiczy.mytv.ui.screens.leanback.components.LeanbackQrcodeDialog
import top.yogiczy.mytv.ui.theme.LeanbackGlass
import top.yogiczy.mytv.ui.theme.LeanbackGlassSurface
import top.yogiczy.mytv.ui.utils.HttpServer
import top.yogiczy.mytv.ui.utils.handleLeanbackKeyEvents

@Composable
fun LeanbackSettingsCategoryListItem(
    modifier: Modifier = Modifier,
    headlineContent: String,
    supportingContent: String? = null,
    trailingContent: @Composable () -> Unit = {},
    onSelected: (() -> Unit)? = null,
    onLongSelected: () -> Unit = {},
    locK: Boolean = false,
    remoteConfig: Boolean = false,
) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    var showServerUrlDialog by remember { mutableStateOf(false) }
    val contentColor = MaterialTheme.colorScheme.onSurface

    LeanbackGlassSurface(
        focused = isFocused,
        containerColor = if (isFocused) LeanbackGlass.FocusContainer else LeanbackGlass.OverlaySoft,
        contentColor = contentColor,
        borderColor = if (isFocused) LeanbackGlass.Focus else LeanbackGlass.StrokeSoft,
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged { isFocused = it.isFocused || it.hasFocus }
            .handleLeanbackKeyEvents(
                onSelect = {
                    if (isFocused) {
                        if (onSelected != null) onSelected()
                        else if (remoteConfig) showServerUrlDialog = true
                    } else focusRequester.requestFocus()
                },
                onLongSelect = {
                    if (isFocused) onLongSelected()
                    else focusRequester.requestFocus()
                },
            )
            .focusable(),
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 15.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = headlineContent,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                    )
                    supportingContent?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor.copy(alpha = 0.68f),
                            maxLines = 1,
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    trailingContent()
                    if (locK) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = contentColor.copy(alpha = 0.74f),
                            modifier = Modifier.size(16.dp),
                        )
                    }

                    if (remoteConfig) {
                        Icon(
                            Icons.AutoMirrored.Default.OpenInNew,
                            contentDescription = null,
                            tint = contentColor.copy(alpha = 0.74f),
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }

    LeanbackQrcodeDialog(
        text = HttpServer.serverUrl,
        description = "扫码前往设置页面",
        showDialogProvider = { showServerUrlDialog },
        onDismissRequest = { showServerUrlDialog = false },
    )
}

@Composable
fun LeanbackSettingsCategoryListItem(
    modifier: Modifier = Modifier,
    headlineContent: String,
    supportingContent: String? = null,
    trailingContent: String,
    onSelected: () -> Unit = {},
    onLongSelected: () -> Unit = {},
    locK: Boolean = false,
    remoteConfig: Boolean = false,
) {
    LeanbackSettingsCategoryListItem(
        modifier = modifier,
        headlineContent = headlineContent,
        supportingContent = supportingContent,
        trailingContent = { Text(trailingContent) },
        onSelected = onSelected,
        onLongSelected = onLongSelected,
        locK = locK,
        remoteConfig = remoteConfig,
    )
}
