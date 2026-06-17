package top.yogiczy.mytv.ui.screens.leanback.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.foundation.lazy.list.TvLazyColumn
import top.yogiczy.mytv.data.entities.ExpandedChannel
import top.yogiczy.mytv.data.entities.ExpandedChannelBucket
import top.yogiczy.mytv.data.entities.ExpandedChannelBuckets
import top.yogiczy.mytv.data.entities.FavoriteChannels
import top.yogiczy.mytv.ui.screens.leanback.settings.LeanbackSettingsViewModel
import top.yogiczy.mytv.ui.screens.leanback.toast.LeanbackToastState
import top.yogiczy.mytv.ui.theme.LeanbackTheme
import top.yogiczy.mytv.ui.utils.HttpServer
import top.yogiczy.mytv.ui.utils.SP
import top.yogiczy.mytv.ui.utils.SettingsUpdate

@Composable
fun LeanbackSettingsCategoryMerge(
    modifier: Modifier = Modifier,
    settingsViewModel: LeanbackSettingsViewModel = viewModel(),
) {
    val buckets = remember(settingsViewModel.iptvExpandedChannelBucketsJson) {
        ExpandedChannelBuckets.fromJson(settingsViewModel.iptvExpandedChannelBucketsJson)
    }

    TvLazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 10.dp),
    ) {
        item {
            LeanbackSettingsCategoryListItem(
                headlineContent = "启用扩展频道",
                supportingContent = "开启后，将不同直播源的精选频道固化到“扩展频道”分组，删除订阅后仍可播放",
                trailingContent = {
                    Switch(
                        checked = settingsViewModel.iptvExpandedChannelEnable,
                        onCheckedChange = null
                    )
                },
                onSelected = {
                    settingsViewModel.iptvExpandedChannelEnable =
                        !settingsViewModel.iptvExpandedChannelEnable
                    HttpServer.notifySettingsUpdate(SettingsUpdate(iptvSourceChanged = true))
                },
            )
        }

        item {
            LeanbackSettingsCategoryListItem(
                headlineContent = "更新扩展频道",
                supportingContent = "用当前精选覆盖更新当前直播源对应的扩展频道条目",
                trailingContent = "${buckets.size}个源 / ${buckets.sumOf { it.channels.size }}个频道",
                onSelected = {
                    updateExpandedChannelBuckets(settingsViewModel)
                    HttpServer.notifySettingsUpdate(SettingsUpdate(iptvSourceChanged = true))
                    LeanbackToastState.I.showToast("更新扩展频道成功")
                },
            )
        }

        item {
            LeanbackSettingsCategoryListItem(
                headlineContent = "清空扩展频道",
                supportingContent = "删除扩展频道内所有直播源条目",
                onSelected = {
                    settingsViewModel.iptvExpandedChannelBucketsJson = ""
                    HttpServer.notifySettingsUpdate(SettingsUpdate(iptvSourceChanged = true))
                    LeanbackToastState.I.showToast("已清空扩展频道")
                },
            )
        }
    }
}

/**
 * 用当前精选覆盖更新当前直播源（iptvSourceUrl）对应的扩展频道条目。
 * 精选数据来源：IPTV_CHANNEL_FAVORITES_JSON（含地址与请求头）。
 */
private fun updateExpandedChannelBuckets(settingsViewModel: LeanbackSettingsViewModel) {
    val sourceUrl = SP.iptvSourceUrl
    val sourceHeaders = SP.iptvSourceRequestHeaders
    val channelHeaders = SP.iptvChannelRequestHeaders
    val favorites = FavoriteChannels.fromJson(SP.iptvChannelFavoritesJson)
    val channels = favorites.map {
        ExpandedChannel(
            name = it.name,
            channelName = it.channelName,
            logoUrl = it.logoUrl,
            urlList = it.urlList,
            headers = it.headers.ifBlank { channelHeaders },
        )
    }.filter { it.urlList.isNotEmpty() }
        .distinctBy {
            Triple(
                it.channelName.ifBlank { it.name }.trim().lowercase(),
                it.urlList,
                it.headers,
            )
        }

    val newBucket = ExpandedChannelBucket(
        sourceUrl = sourceUrl,
        sourceHeaders = sourceHeaders,
        channelHeaders = channelHeaders,
        channels = channels,
    )

    val existing = ExpandedChannelBuckets.fromJson(settingsViewModel.iptvExpandedChannelBucketsJson)
    val existingOtherSources = existing.filter { it.sourceUrl != sourceUrl }
    val merged = if (channels.isEmpty()) existingOtherSources else existingOtherSources + newBucket
    settingsViewModel.iptvExpandedChannelBucketsJson =
        ExpandedChannelBuckets.toJson(merged)
}

@Preview
@Composable
private fun LeanbackSettingsCategoryMergePreview() {
    SP.init(LocalContext.current)
    LeanbackTheme {
        LeanbackSettingsCategoryMerge(
            modifier = Modifier.padding(20.dp),
            settingsViewModel = LeanbackSettingsViewModel().apply {
                iptvExpandedChannelBucketsJson = ExpandedChannelBuckets.toJson(
                    listOf(
                        ExpandedChannelBucket(
                            sourceUrl = "https://example.com/a.m3u",
                            channels = listOf(
                                ExpandedChannel(
                                    name = "CCTV-1",
                                )
                            ),
                        )
                    )
                )
            },
        )
    }
}
