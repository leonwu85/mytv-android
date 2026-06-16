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
import top.yogiczy.mytv.data.utils.Constants
import top.yogiczy.mytv.ui.screens.leanback.settings.LeanbackSettingsViewModel
import top.yogiczy.mytv.ui.theme.LeanbackTheme
import top.yogiczy.mytv.ui.utils.SP
import top.yogiczy.mytv.utils.humanizeMs

@Composable
fun LeanbackSettingsCategoryNetwork(
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
                headlineContent = "应用调试日志",
                supportingContent = "记录 HTTP 请求地址、请求头、响应状态及体长（不含正文）到本机日志页",
                trailingContent = {
                    Switch(checked = settingsViewModel.debugAppLog, onCheckedChange = null)
                },
                onSelected = {
                    settingsViewModel.debugAppLog = !settingsViewModel.debugAppLog
                },
            )
        }

        item {
            LeanbackSettingsCategoryListItem(
                headlineContent = "配置服务器广播 IP",
                supportingContent = if (settingsViewModel.httpServerAdvertiseIp.isBlank())
                    "空则自动获取本机 IP；配置后在 Web 配置页扫码入口使用该 IP"
                else settingsViewModel.httpServerAdvertiseIp,
                trailingContent = if (settingsViewModel.httpServerAdvertiseIp.isBlank()) "自动" else "已配置",
                remoteConfig = true,
            )
        }

        item {
            LeanbackSettingsCategoryListItem(
                headlineContent = "HTTP请求重试次数",
                supportingContent = "影响直播源、节目单数据获取",
                trailingContent = Constants.HTTP_RETRY_COUNT.toString(),
                locK = true,
            )
        }

        item {
            LeanbackSettingsCategoryListItem(
                headlineContent = "HTTP请求重试间隔时间",
                supportingContent = "影响直播源、节目单数据获取",
                trailingContent = Constants.HTTP_RETRY_INTERVAL.humanizeMs(),
                locK = true,
            )
        }
    }
}

@Preview
@Composable
private fun LeanbackSettingsCategoryNetworkPreview() {
    SP.init(LocalContext.current)
    LeanbackTheme {
        LeanbackSettingsCategoryNetwork(
            modifier = Modifier.padding(20.dp),
        )
    }
}
