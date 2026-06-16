package top.yogiczy.mytv.ui.screens.leanback.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import top.yogiczy.mytv.ui.screens.leanback.settings.LeanbackSettingsCategories
import top.yogiczy.mytv.ui.screens.leanback.settings.LeanbackSettingsViewModel
import top.yogiczy.mytv.utils.Logger

@Composable
fun LeanbackSettingsCategoryContent(
    modifier: Modifier = Modifier,
    focusedCategoryProvider: () -> LeanbackSettingsCategories = { LeanbackSettingsCategories.entries.first() },
    settingsViewModel: LeanbackSettingsViewModel = viewModel(),
) {
    val focusedCategory = focusedCategoryProvider()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = focusedCategory.title,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
        )

        when (focusedCategory) {
            LeanbackSettingsCategories.APP -> LeanbackSettingsCategoryApp(settingsViewModel = settingsViewModel)
            LeanbackSettingsCategories.IPTV -> LeanbackSettingsCategoryIptv(settingsViewModel = settingsViewModel)
            LeanbackSettingsCategories.EPG -> LeanbackSettingsCategoryEpg(settingsViewModel = settingsViewModel)
            LeanbackSettingsCategories.UI -> LeanbackSettingsCategoryUI(settingsViewModel = settingsViewModel)
            LeanbackSettingsCategories.FAVORITE -> LeanbackSettingsCategoryFavorite(settingsViewModel = settingsViewModel)
            LeanbackSettingsCategories.MERGE -> LeanbackSettingsCategoryMerge(settingsViewModel = settingsViewModel)
            LeanbackSettingsCategories.VIDEO_PLAYER -> LeanbackSettingsCategoryVideoPlayer(settingsViewModel = settingsViewModel)
            LeanbackSettingsCategories.NETWORK -> LeanbackSettingsCategoryNetwork(settingsViewModel = settingsViewModel)
            LeanbackSettingsCategories.LOG -> LeanbackSettingsCategoryLog(
                settingsViewModel = settingsViewModel,
                history = Logger.history,
            )
            LeanbackSettingsCategories.ABOUT -> LeanbackSettingsCategoryAbout(settingsViewModel = settingsViewModel)
        }
    }
}
