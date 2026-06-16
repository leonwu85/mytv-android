package top.yogiczy.mytv.ui.screens.leanback.panel.components

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NetworkCell
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import top.yogiczy.mytv.data.entities.EpgProgramme
import top.yogiczy.mytv.data.entities.EpgProgramme.Companion.progress
import top.yogiczy.mytv.data.entities.Iptv
import top.yogiczy.mytv.ui.theme.LeanbackGlass
import top.yogiczy.mytv.ui.theme.LeanbackGlassSurface
import top.yogiczy.mytv.ui.theme.LeanbackTheme
import top.yogiczy.mytv.ui.utils.handleLeanbackKeyEvents

@Composable
fun LeanbackPanelIptvItem(
    modifier: Modifier = Modifier,
    iptvProvider: () -> Iptv = { Iptv() },
    currentProgrammeProvider: () -> EpgProgramme? = { null },
    showProgrammeProgressProvider: () -> Boolean = { false },
    selectedProvider: () -> Boolean = { false },
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
    val isSelected = selectedProvider()
    val contentColor = MaterialTheme.colorScheme.onSurface

    LaunchedEffect(Unit) {
        if (initialFocusedProvider()) {
            onHasFocused()
            focusRequester.requestFocus()
        }
    }

    LeanbackGlassSurface(
        focused = isFocused,
        selected = isSelected,
        containerColor = when {
            isFocused -> LeanbackGlass.FocusContainer
            isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
            else -> LeanbackGlass.OverlaySoft
        },
        contentColor = contentColor,
        borderColor = when {
            isFocused -> LeanbackGlass.Focus
            isSelected -> LeanbackGlass.Focus.copy(alpha = 0.72f)
            else -> LeanbackGlass.StrokeSoft
        },
        modifier = modifier
            .width(196.dp)
            .height(82.dp)
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
            .focusable(),
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (iptv.logoUrl.isNotBlank()) {
                        LeanbackChannelLogo(
                            logoUrlProvider = { iptv.logoUrl },
                            modifier = Modifier.size(34.dp),
                            cornerRadius = 7.dp,
                            innerPadding = 4.dp,
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    Text(
                        text = iptv.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.NetworkCell,
                            contentDescription = null,
                            tint = LeanbackGlass.Focus,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                Text(
                    text = currentProgramme?.title ?: "",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = contentColor.copy(alpha = if (isFocused || isSelected) 0.72f else 0.68f),
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (showProgrammeProgress && currentProgramme != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(currentProgramme.progress())
                    .height(3.dp)
                    .background(MaterialTheme.colorScheme.primary),
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
                selectedProvider = { true },
                initialFocusedProvider = { true },
            )
        }
    }
}
