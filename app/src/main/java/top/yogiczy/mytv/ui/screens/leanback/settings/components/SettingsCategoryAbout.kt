package top.yogiczy.mytv.ui.screens.leanback.settings.components

import android.content.Context
import android.content.pm.PackageInfo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.material3.Icon
import kotlinx.coroutines.delay
import top.yogiczy.mytv.data.utils.Constants
import top.yogiczy.mytv.ui.screens.leanback.components.LeanbackQrcode
import top.yogiczy.mytv.ui.screens.leanback.components.LeanbackQrcodeDialog
import top.yogiczy.mytv.ui.screens.leanback.settings.LeanbackSettingsViewModel
import top.yogiczy.mytv.ui.screens.leanback.update.LeanBackUpdateViewModel
import top.yogiczy.mytv.ui.theme.LeanbackTheme
import top.yogiczy.mytv.ui.utils.HttpServer

@Composable
fun LeanbackSettingsCategoryAbout(
    modifier: Modifier = Modifier,
    packageInfo: PackageInfo = rememberPackageInfo(),
    settingsViewModel: LeanbackSettingsViewModel = viewModel(),
    updateViewModel: LeanBackUpdateViewModel = viewModel(),
) {
    val context = LocalContext.current

    TvLazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 10.dp),
    ) {
        item {
            LeanbackSettingsCategoryListItem(
                headlineContent = "应用更新",
                supportingContent = if (updateViewModel.isUpdateAvailable)
                    "可更新到 v${updateViewModel.latestRelease.version}"
                else "当前为最新版本（v${packageInfo.versionName}）",
                trailingContent = if (updateViewModel.isUpdateAvailable) "可更新" else "已最新",
                onSelected = {
                    if (updateViewModel.isUpdateAvailable) {
                        updateViewModel.showDialog = true
                    } else {
                        // 触发一次检查（轻提示在 UpdateScreen 中已处理，这里仅复用现有状态）
                    }
                },
            )
        }

        item {
            LeanbackSettingsCategoryListItem(
                headlineContent = "应用名称",
                trailingContent = Constants.APP_TITLE,
            )
        }

        item {
            LeanbackSettingsCategoryListItem(
                headlineContent = "应用版本",
                trailingContent = packageInfo.versionName,
            )
        }

        item {
            var showQrDialog by remember { mutableStateOf(false) }

            LeanbackSettingsCategoryListItem(
                headlineContent = "项目地址",
                trailingContent = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        androidx.tv.material3.Text(Constants.APP_REPO)

                        Icon(
                            Icons.AutoMirrored.Default.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                },
                onSelected = { showQrDialog = true },
            )

            LeanbackQrcodeDialog(
                text = Constants.APP_REPO,
                description = "扫码前往项目地址",
                showDialogProvider = { showQrDialog },
                onDismissRequest = { showQrDialog = false },
            )
        }

        // 设置页（原“更多设置”二维码入口）
        item {
            val serverUrl = HttpServer.serverUrl
            LeanbackSettingsCategoryListItem(
                headlineContent = "设置页面",
                supportingContent = "局域网浏览器访问可远程配置",
                trailingContent = serverUrl,
            )
        }

        item {
            var show by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                delay(100)
                show = true
            }

            if (show) {
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    LeanbackQrcode(
                        modifier = Modifier
                            .width(200.dp)
                            .height(200.dp),
                        text = HttpServer.serverUrl,
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberPackageInfo(context: Context = LocalContext.current): PackageInfo =
    context.packageManager.getPackageInfo(context.packageName, 0)

@Preview
@Composable
private fun LeanbackSettingsAboutPreview() {
    LeanbackTheme {
        LeanbackSettingsCategoryAbout(
            packageInfo = PackageInfo().apply {
                versionName = "1.0.0"
            }
        )
    }
}
