package top.yogiczy.mytv.ui.screens.leanback.settings.components

import android.widget.Toast
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

@Composable
fun LeanbackSettingsCategoryApp(
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
                headlineContent = "开机自启",
                supportingContent = "请确保当前设备支持该功能",
                trailingContent = {
                    Switch(checked = settingsViewModel.appBootLaunch, onCheckedChange = null)
                },
                onSelected = {
                    settingsViewModel.appBootLaunch = !settingsViewModel.appBootLaunch
                },
            )
        }

        item {
            val context = LocalContext.current

            LeanbackSettingsCategoryListItem(
                headlineContent = "设备显示类型",
                supportingContent = "短按切换设备显示类型",
                trailingContent = when (settingsViewModel.appDeviceDisplayType) {
                    SP.AppDeviceDisplayType.LEANBACK -> "电视"
                    SP.AppDeviceDisplayType.PAD -> "平板"
                    SP.AppDeviceDisplayType.MOBILE -> "手机"
                },
                onSelected = {
                    // 短按切换设备显示类型；项目暂未完全开放，先提示
                    Toast.makeText(context, "暂未开放", Toast.LENGTH_SHORT).show()
                },
            )
        }

        item {
            LeanbackSettingsCategoryListItem(
                headlineContent = "最新版本号",
                supportingContent = "用于更新提醒去重",
                trailingContent = settingsViewModel.appLastLatestVersion.ifBlank { "未知" },
            )
        }
    }
}

@Preview
@Composable
private fun LeanbackSettingsCategoryAppPreview() {
    SP.init(LocalContext.current)
    LeanbackTheme {
        LeanbackSettingsCategoryApp(
            modifier = Modifier.padding(20.dp),
            settingsViewModel = LeanbackSettingsViewModel(),
        )
    }
}
