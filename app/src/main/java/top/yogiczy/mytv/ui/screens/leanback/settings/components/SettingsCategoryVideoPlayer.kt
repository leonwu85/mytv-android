package top.yogiczy.mytv.ui.screens.leanback.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.foundation.lazy.list.TvLazyColumn
import top.yogiczy.mytv.ui.screens.leanback.settings.LeanbackSettingsViewModel
import top.yogiczy.mytv.ui.theme.LeanbackTheme
import top.yogiczy.mytv.ui.utils.SP
import top.yogiczy.mytv.utils.humanizeMs
import kotlin.math.max

@Composable
fun LeanbackSettingsCategoryVideoPlayer(
    modifier: Modifier = Modifier,
    settingsViewModel: LeanbackSettingsViewModel = viewModel(),
) {
    TvLazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 10.dp),
    ) {
        item {
            LeanbackSettingsCategoryListItem(
                headlineContent = "全局画面比例",
                trailingContent = when (settingsViewModel.videoPlayerAspectRatio) {
                    SP.VideoPlayerAspectRatio.ORIGINAL -> "原始"
                    SP.VideoPlayerAspectRatio.SIXTEEN_NINE -> "16:9"
                    SP.VideoPlayerAspectRatio.FOUR_THREE -> "4:3"
                    SP.VideoPlayerAspectRatio.AUTO -> "自动拉伸"
                },
                onSelected = {
                    settingsViewModel.videoPlayerAspectRatio =
                        SP.VideoPlayerAspectRatio.entries.let {
                            it[(it.indexOf(settingsViewModel.videoPlayerAspectRatio) + 1) % it.size]
                        }
                },
            )
        }


        item {
            val min = 1000 * 5L
            val max = 1000 * 30L
            val step = 1000 * 5L

            LeanbackSettingsCategoryListItem(
                headlineContent = "播放器加载超时",
                supportingContent = "影响超时换源、断线重连",
                trailingContent = settingsViewModel.videoPlayerLoadTimeout.humanizeMs(),
                onSelected = {
                    settingsViewModel.videoPlayerLoadTimeout =
                        max(min, (settingsViewModel.videoPlayerLoadTimeout + step) % (max + step))
                },
            )
        }

        item {
            LeanbackSettingsCategoryListItem(
                headlineContent = "播放器自定义UA",
                supportingContent = settingsViewModel.videoPlayerUserAgent,
                remoteConfig = true,
            )
        }

        item {
            val min = 1000 * 10L
            val max = 1000 * 60L
            val step = 1000 * 10L

            LeanbackSettingsCategoryListItem(
                headlineContent = "播放缓冲时长",
                supportingContent = "加大可减少卡顿与缓冲圈",
                trailingContent = settingsViewModel.videoPlayerBufferDuration.humanizeMs(),
                onSelected = {
                    settingsViewModel.videoPlayerBufferDuration =
                        max(min, (settingsViewModel.videoPlayerBufferDuration + step) % (max + step))
                },
            )
        }

        item {
            LeanbackSettingsCategoryListItem(
                headlineContent = "启用分片磁盘缓存",
                supportingContent = "断网时优先读取已缓存分片",
                trailingContent = {
                    Switch(
                        checked = settingsViewModel.videoPlayerSegmentDiskCacheEnable,
                        onCheckedChange = null,
                    )
                },
                onSelected = {
                    settingsViewModel.videoPlayerSegmentDiskCacheEnable =
                        !settingsViewModel.videoPlayerSegmentDiskCacheEnable
                },
            )
        }

        item {
            LeanbackSettingsCategoryListItem(
                headlineContent = "RTSP 优先 TCP（Interleaved）",
                supportingContent = "默认开启，穿越 NAT/防火墙更稳，之后再按需回退 RTP/UDP",
                trailingContent = {
                    Switch(
                        checked = settingsViewModel.videoRtspForceTcp,
                        onCheckedChange = null,
                    )
                },
                onSelected = {
                    settingsViewModel.videoRtspForceTcp = !settingsViewModel.videoRtspForceTcp
                },
            )
        }

        item {
            // 10000–60000（步进 5000），对应 Media3 收流静默判定
            val min = 1000 * 10L
            val max = 1000 * 60L
            val step = 1000 * 5L

            LeanbackSettingsCategoryListItem(
                headlineContent = "RTSP 超时容忍",
                supportingContent = "对应 Media3 收流静默判定，过小易误断",
                trailingContent = settingsViewModel.videoRtspRtpSilenceTimeoutMs.humanizeMs(),
                onSelected = {
                    settingsViewModel.videoRtspRtpSilenceTimeoutMs =
                        max(min, (settingsViewModel.videoRtspRtpSilenceTimeoutMs + step) % (max + step))
                },
            )
        }

        item {
            // 0–5（步进 1）
            val min = 0
            val max = 5
            val step = 1

            LeanbackSettingsCategoryListItem(
                headlineContent = "RTSP TCP 起播重试",
                supportingContent = "TCP/interleaved 时同址重试次数，之后才尝试 RTP/UDP",
                trailingContent = "${settingsViewModel.videoRtspTcpPrepareRetryCount}次",
                onSelected = {
                    settingsViewModel.videoRtspTcpPrepareRetryCount =
                        max(min, (settingsViewModel.videoRtspTcpPrepareRetryCount + step) % (max + step))
                },
            )
        }

        item {
            // 400–4000（步进 200）
            val min = 400L
            val max = 4000L
            val step = 200L

            LeanbackSettingsCategoryListItem(
                headlineContent = "RTSP 重试间隔",
                supportingContent = "TCP 起播失败后等待再拉同一地址",
                trailingContent = settingsViewModel.videoRtspPrepareRetryDelayMs.humanizeMs(),
                onSelected = {
                    settingsViewModel.videoRtspPrepareRetryDelayMs =
                        max(min, (settingsViewModel.videoRtspPrepareRetryDelayMs + step) % (max + step))
                },
            )
        }
    }
}

@Preview
@Composable
private fun LeanbackSettingsCategoryHttpPreview() {
    SP.init(LocalContext.current)
    LeanbackTheme {
        LeanbackSettingsCategoryVideoPlayer(
            modifier = Modifier.padding(20.dp),
        )
    }
}
