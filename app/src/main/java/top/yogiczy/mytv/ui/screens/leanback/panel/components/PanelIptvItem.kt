package top.yogiczy.mytv.ui.screens.leanback.panel.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import top.yogiczy.mytv.data.entities.EpgProgramme
import top.yogiczy.mytv.data.entities.EpgProgramme.Companion.progress
import top.yogiczy.mytv.data.entities.Iptv
import top.yogiczy.mytv.ui.theme.LeanbackTheme
import top.yogiczy.mytv.ui.utils.handleLeanbackKeyEvents

@Composable
fun LeanbackPanelIptvItem(
    modifier: Modifier = Modifier,
    iptvProvider: () -> Iptv = { Iptv() },
    currentProgrammeProvider: () -> EpgProgramme? = { null },
    showProgrammeProgressProvider: () -> Boolean = { false },
    onIptvSelected: () -> Unit = {},
    onIptvFavoriteToggle: () -> Unit = {},
    onShowEpg: () -> Unit = {},
    initialFocusedProvider: () -> Boolean = { false },
    onHasFocused: () -> Unit = {},
    onFocused: () -> Unit = {},
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val iptv = iptvProvider()
    val currentProgramme = currentProgrammeProvider()
    val showProgrammeProgress = showProgrammeProgressProvider()
    val colorScheme = MaterialTheme.colorScheme
    val containerColor = remember(isFocused) {
        if (isFocused) colorScheme.onBackground
        else colorScheme.background.copy(alpha = 0.8f)
    }
    val contentColor = remember(isFocused) {
        if (isFocused) colorScheme.background
        else colorScheme.onBackground
    }
    val borderStroke = remember(isFocused) {
        if (isFocused) BorderStroke(width = 1.dp, color = colorScheme.onBackground)
        else BorderStroke(width = 0.dp, color = Color.Transparent)
    }

    LaunchedEffect(Unit) {
        if (initialFocusedProvider()) {
            onHasFocused()
            focusRequester.requestFocus()
        }
    }

    Box(
        modifier = modifier
            .width(130.dp)
            .height(54.dp)
            .focusRequester(focusRequester)
            .onFocusChanged {
                isFocused = it.isFocused || it.hasFocus
                if (isFocused) onFocused()
            }
            .handleLeanbackKeyEvents(
                onSelect = {
                    if (isFocused) onIptvSelected()
                    else focusRequester.requestFocus()
                },
                onLongSelect = {
                    if (isFocused) onIptvFavoriteToggle()
                    else focusRequester.requestFocus()
                },
                onSettings = {
                    if (isFocused) onShowEpg()
                    else focusRequester.requestFocus()
                }
            )
            .focusable()
            .border(borderStroke, MaterialTheme.shapes.small)
            .clip(MaterialTheme.shapes.small)
            .background(containerColor),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.SpaceAround,
        ) {
            Text(
                text = iptv.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                color = contentColor,
            )

            Text(
                text = currentProgramme?.title ?: "",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = contentColor.copy(alpha = 0.8f),
                ),
                maxLines = 1,
            )
        }

        // 节目进度条
        if (showProgrammeProgress && currentProgramme != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(currentProgramme.progress())
                    .height(3.dp)
                    .background(
                        if (isFocused) colorScheme.surface.copy(alpha = 0.9f)
                        else colorScheme.onSurface.copy(alpha = 0.9f)
                    ),
            )
        }
    }
}

@Preview
@Composable
private fun LeanbackPanelIptvItemPreview() {
    LeanbackTheme {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            LeanbackPanelIptvItem(
                iptvProvider = { Iptv.EXAMPLE },
                currentProgrammeProvider = {
                    EpgProgramme(
                        startAt = System.currentTimeMillis() - 100000,
                        endAt = System.currentTimeMillis() + 200000,
                        title = "新闻联播",
                    )
                },
                showProgrammeProgressProvider = { true },
            )

            LeanbackPanelIptvItem(
                iptvProvider = { Iptv.EXAMPLE },
                currentProgrammeProvider = {
                    EpgProgramme(
                        startAt = System.currentTimeMillis() - 100000,
                        endAt = System.currentTimeMillis() + 200000,
                        title = "新闻联播",
                    )
                },
                showProgrammeProgressProvider = { true },
                initialFocusedProvider = { true },
            )
        }
    }
}
