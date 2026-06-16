package top.yogiczy.mytv.ui.screens.leanback.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMerge
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 设置页一级分类，共 10 个分类（按 UI 顺序）。
 */
enum class LeanbackSettingsCategories(
    val icon: ImageVector,
    val title: String
) {
    APP(Icons.Default.Settings, "应用"),
    IPTV(Icons.Default.LiveTv, "直播源"),
    EPG(Icons.Default.Menu, "节目单"),
    UI(Icons.Default.DisplaySettings, "界面"),
    FAVORITE(Icons.Default.Star, "精选"),
    MERGE(Icons.AutoMirrored.Filled.CallMerge, "多源合并"),
    VIDEO_PLAYER(Icons.Default.SmartDisplay, "播放器"),
    NETWORK(Icons.Default.Wifi, "网络"),
    LOG(Icons.Default.FormatListNumbered, "日志"),
    ABOUT(Icons.Default.Info, "关于"),
}
