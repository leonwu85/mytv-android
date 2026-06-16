package top.yogiczy.mytv.ui.screens.leanback.quickpanel

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import top.yogiczy.mytv.data.entities.EpgProgrammeCurrent
import top.yogiczy.mytv.data.entities.Iptv
import top.yogiczy.mytv.data.utils.Constants
import top.yogiczy.mytv.ui.rememberLeanbackChildPadding
import top.yogiczy.mytv.ui.screens.leanback.panel.LeanbackPanelScreenTopRight
import top.yogiczy.mytv.ui.screens.leanback.panel.PanelAutoCloseState
import top.yogiczy.mytv.ui.screens.leanback.panel.components.LeanbackPanelIptvInfo
import top.yogiczy.mytv.ui.screens.leanback.panel.components.LeanbackPanelPlayerInfo
import top.yogiczy.mytv.ui.screens.leanback.panel.rememberPanelAutoCloseState
import top.yogiczy.mytv.ui.screens.leanback.quickpanel.components.LeanbackQuickPanelIptvChannelsDialog
import top.yogiczy.mytv.ui.screens.leanback.video.player.LeanbackVideoPlayer
import top.yogiczy.mytv.ui.theme.LeanbackGlass
import top.yogiczy.mytv.ui.theme.LeanbackGlassSurface
import top.yogiczy.mytv.ui.theme.LeanbackStatusChip
import top.yogiczy.mytv.ui.theme.LeanbackTheme
import top.yogiczy.mytv.ui.theme.leanbackBottomScrim
import top.yogiczy.mytv.ui.theme.leanbackSideScrim
import top.yogiczy.mytv.ui.utils.handleLeanbackKeyEvents
import top.yogiczy.mytv.ui.utils.handleLeanbackUserAction

@Composable
fun LeanbackQuickPanelScreen(
    modifier: Modifier = Modifier,
    currentIptvProvider: () -> Iptv = { Iptv() },
    currentIptvUrlIdxProvider: () -> Int = { 0 },
    currentProgrammesProvider: () -> EpgProgrammeCurrent? = { null },
    currentIptvChannelNoProvider: () -> String = { "" },
    currentIptvFavoriteProvider: () -> Boolean = { false },
    iptvFavoriteEnableProvider: () -> Boolean = { false },
    videoPlayerMetadataProvider: () -> LeanbackVideoPlayer.Metadata = { LeanbackVideoPlayer.Metadata() },
    showVideoPlayerMetadataProvider: () -> Boolean = { false },
    videoPlayerAspectRatioProvider: () -> Float = { 16f / 9f },
    onChangeVideoPlayerAspectRatio: (Float) -> Unit = {},
    onIptvUrlIdxChange: (Int) -> Unit = {},
    onRefresh: () -> Unit = {},
    onToggleVideoPlayerMetadata: () -> Unit = {},
    onIptvFavoriteToggle: () -> Unit = {},
    onClearCache: () -> Unit = {},
    onMoreSettings: () -> Unit = {},
    onClose: () -> Unit = {},
    autoCloseState: PanelAutoCloseState = rememberPanelAutoCloseState(
        timeout = Constants.UI_SCREEN_AUTO_CLOSE_DELAY,
        onTimeout = onClose,
    ),
) {
    val childPadding = rememberLeanbackChildPadding()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        autoCloseState.active()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .handleLeanbackUserAction { autoCloseState.active() }
            .pointerInput(Unit) { detectTapGestures(onTap = { onClose() }) },
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxSize()
                .leanbackBottomScrim(),
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxSize()
                .leanbackSideScrim(),
        )

        LeanbackPanelScreenTopRight(
            channelNoProvider = currentIptvChannelNoProvider
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = childPadding.start, bottom = childPadding.bottom + 102.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LeanbackPanelIptvInfo(
                modifier = Modifier.width(540.dp),
                iptvProvider = currentIptvProvider,
                iptvUrlIdxProvider = currentIptvUrlIdxProvider,
                favoriteProvider = currentIptvFavoriteProvider,
                currentProgrammesProvider = currentProgrammesProvider,
            )

            LeanbackPanelPlayerInfo(
                metadataProvider = videoPlayerMetadataProvider,
            )
        }

        LeanbackQuickPanelActionDock(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = childPadding.end, bottom = childPadding.bottom + 102.dp),
            currentIptvProvider = currentIptvProvider,
            currentIptvUrlIdxProvider = currentIptvUrlIdxProvider,
            currentIptvFavoriteProvider = currentIptvFavoriteProvider,
            iptvFavoriteEnableProvider = iptvFavoriteEnableProvider,
            showVideoPlayerMetadataProvider = showVideoPlayerMetadataProvider,
            videoPlayerAspectRatioProvider = videoPlayerAspectRatioProvider,
            onIptvUrlIdxChange = onIptvUrlIdxChange,
            onRefresh = onRefresh,
            onToggleVideoPlayerMetadata = onToggleVideoPlayerMetadata,
            onIptvFavoriteToggle = onIptvFavoriteToggle,
            onClearCache = onClearCache,
            onChangeVideoPlayerAspectRatio = onChangeVideoPlayerAspectRatio,
            onMoreSettings = onMoreSettings,
            onUserAction = { autoCloseState.active() },
        )

        LeanbackQuickPanelMetadataFooter(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    start = childPadding.start,
                    end = childPadding.end,
                    bottom = childPadding.bottom,
                )
                .fillMaxWidth(),
            currentIptvProvider = currentIptvProvider,
            currentIptvUrlIdxProvider = currentIptvUrlIdxProvider,
            showVideoPlayerMetadataProvider = showVideoPlayerMetadataProvider,
        )
    }
}

@Composable
private fun LeanbackQuickPanelActionDock(
    modifier: Modifier = Modifier,
    currentIptvProvider: () -> Iptv,
    currentIptvUrlIdxProvider: () -> Int,
    currentIptvFavoriteProvider: () -> Boolean,
    iptvFavoriteEnableProvider: () -> Boolean,
    showVideoPlayerMetadataProvider: () -> Boolean,
    videoPlayerAspectRatioProvider: () -> Float,
    onIptvUrlIdxChange: (Int) -> Unit,
    onRefresh: () -> Unit,
    onToggleVideoPlayerMetadata: () -> Unit,
    onIptvFavoriteToggle: () -> Unit,
    onClearCache: () -> Unit,
    onChangeVideoPlayerAspectRatio: (Float) -> Unit,
    onMoreSettings: () -> Unit,
    onUserAction: () -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(22.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(100.dp)
                .background(LeanbackGlass.Stroke),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LeanbackQuickPanelButton(
                icon = Icons.Default.Refresh,
                titleProvider = { "刷新" },
                onSelect = onRefresh,
            )

            LeanbackQuickPanelButton(
                icon = Icons.Default.Info,
                titleProvider = {
                    if (showVideoPlayerMetadataProvider()) "隐藏" else "信息"
                },
                onSelect = onToggleVideoPlayerMetadata,
            )

            if (iptvFavoriteEnableProvider()) {
                LeanbackQuickPanelButton(
                    icon = Icons.Default.Star,
                    titleProvider = {
                        if (currentIptvFavoriteProvider()) "已藏" else "收藏"
                    },
                    emphasized = currentIptvFavoriteProvider(),
                    onSelect = onIptvFavoriteToggle,
                )
            }

            LeanbackQuickPanelButton(
                icon = Icons.Default.Cached,
                titleProvider = { "缓存" },
                onSelect = onClearCache,
            )

            LeanbackQuickPanelActionVideoAspectRatio(
                videoPlayerAspectRatioProvider = videoPlayerAspectRatioProvider,
                onChangeVideoPlayerAspectRatio = onChangeVideoPlayerAspectRatio,
            )

            LeanbackQuickPanelActionMultipleChannels(
                currentIptvProvider = currentIptvProvider,
                currentIptvUrlIdxProvider = currentIptvUrlIdxProvider,
                onIptvUrlIdxChange = onIptvUrlIdxChange,
                onUserAction = onUserAction,
            )

            LeanbackQuickPanelButton(
                icon = Icons.Default.Settings,
                titleProvider = { "设置" },
                emphasized = true,
                onSelect = onMoreSettings,
            )
        }
    }
}

@Composable
private fun LeanbackQuickPanelButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    titleProvider: () -> String,
    emphasized: Boolean = false,
    onSelect: () -> Unit = {},
) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    val active = isFocused || emphasized
    val contentColor = MaterialTheme.colorScheme.onSurface

    LeanbackGlassSurface(
        focused = isFocused,
        selected = emphasized,
        containerColor = when {
            isFocused -> LeanbackGlass.FocusContainer
            emphasized -> MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
            else -> LeanbackGlass.OverlaySoft
        },
        contentColor = contentColor,
        borderColor = when {
            isFocused -> LeanbackGlass.Focus
            emphasized -> LeanbackGlass.Focus.copy(alpha = 0.58f)
            else -> LeanbackGlass.StrokeSoft
        },
        modifier = modifier
            .width(92.dp)
            .height(88.dp)
            .focusRequester(focusRequester)
            .onFocusChanged {
                isFocused = it.isFocused || it.hasFocus
            }
            .handleLeanbackKeyEvents(
                onSelect = {
                    if (isFocused) onSelect()
                    else focusRequester.requestFocus()
                },
            )
            .focusable(),
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    tint = if (active) LeanbackGlass.Focus else MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = titleProvider(),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun LeanbackQuickPanelActionMultipleChannels(
    currentIptvProvider: () -> Iptv = { Iptv() },
    currentIptvUrlIdxProvider: () -> Int = { 0 },
    onIptvUrlIdxChange: (Int) -> Unit = {},
    onUserAction: () -> Unit = {},
) {
    if (currentIptvProvider().urlList.size > 1) {
        var showChannelsDialog by remember { mutableStateOf(false) }
        LeanbackQuickPanelButton(
            icon = Icons.Default.Route,
            titleProvider = { "线路" },
            onSelect = { showChannelsDialog = true },
        )
        LeanbackQuickPanelIptvChannelsDialog(
            showDialogProvider = { showChannelsDialog },
            onDismissRequest = { showChannelsDialog = false },
            iptvProvider = currentIptvProvider,
            iptvUrlIdxProvider = currentIptvUrlIdxProvider,
            onIptvUrlIdxChange = onIptvUrlIdxChange,
            onUserAction = onUserAction,
        )
    }
}

@Composable
private fun LeanbackQuickPanelActionVideoAspectRatio(
    videoPlayerAspectRatioProvider: () -> Float = { 16f / 9f },
    onChangeVideoPlayerAspectRatio: (Float) -> Unit = {},
) {
    val configuration = LocalConfiguration.current
    val screenAspectRatio =
        configuration.screenWidthDp.toFloat() / configuration.screenHeightDp.toFloat()
    LeanbackQuickPanelButton(
        icon = Icons.Default.AspectRatio,
        titleProvider = { "比例" },
        onSelect = {
            onChangeVideoPlayerAspectRatio(
                when (videoPlayerAspectRatioProvider()) {
                    16f / 9f -> 4f / 3f
                    4f / 3f -> screenAspectRatio
                    screenAspectRatio -> 16f / 9f
                    else -> 16f / 9f
                }
            )
        },
    )
}

@Composable
private fun LeanbackQuickPanelMetadataFooter(
    modifier: Modifier = Modifier,
    currentIptvProvider: () -> Iptv = { Iptv() },
    currentIptvUrlIdxProvider: () -> Int = { 0 },
    showVideoPlayerMetadataProvider: () -> Boolean = { false },
) {
    LeanbackGlassSurface(
        modifier = modifier.height(58.dp),
        containerColor = LeanbackGlass.OverlaySoft,
        borderColor = LeanbackGlass.StrokeSoft,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LeanbackStatusChip(text = "直播")
            LeanbackStatusChip(
                text = "线路 ${currentIptvUrlIdxProvider() + 1}/${currentIptvProvider().urlList.size.coerceAtLeast(1)}",
                active = currentIptvProvider().urlList.size > 1,
                accentColor = MaterialTheme.colorScheme.primary,
            )
            LeanbackStatusChip(text = if (showVideoPlayerMetadataProvider()) "播放信息 开" else "播放信息 关")
        }
    }
}

@Preview(device = "id:Android TV (720p)")
@Composable
private fun LeanbackQuickPanelScreenPreview() {
    LeanbackTheme {
        LeanbackQuickPanelScreen(
            currentIptvProvider = { Iptv.EXAMPLE },
            currentProgrammesProvider = { EpgProgrammeCurrent.EXAMPLE },
            currentIptvFavoriteProvider = { true },
            iptvFavoriteEnableProvider = { true },
            videoPlayerMetadataProvider = {
                LeanbackVideoPlayer.Metadata(
                    videoWidth = 1920,
                    videoHeight = 1080,
                )
            },
        )
    }
}
