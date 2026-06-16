package top.yogiczy.mytv.ui.screens.leanback.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import top.yogiczy.mytv.ui.screens.leanback.settings.LeanbackSettingsCategories
import top.yogiczy.mytv.utils.Logger

@Composable
fun LeanbackSettingsCategoryContent(
    modifier: Modifier = Modifier,
    focusedCategoryProvider: () -> LeanbackSettingsCategories = { LeanbackSettingsCategories.entries.first() },
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
            LeanbackSettingsCategories.APP -> LeanbackSettingsCategoryApp()
            LeanbackSettingsCategories.IPTV -> LeanbackSettingsCategoryIptv()
            LeanbackSettingsCategories.EPG -> LeanbackSettingsCategoryEpg()
            LeanbackSettingsCategories.UI -> LeanbackSettingsCategoryUI()
            LeanbackSettingsCategories.FAVORITE -> LeanbackSettingsCategoryFavorite()
            LeanbackSettingsCategories.MERGE -> LeanbackSettingsCategoryMerge()
            LeanbackSettingsCategories.VIDEO_PLAYER -> LeanbackSettingsCategoryVideoPlayer()
            LeanbackSettingsCategories.NETWORK -> LeanbackSettingsCategoryNetwork()
            LeanbackSettingsCategories.LOG -> LeanbackSettingsCategoryLog(
                history = Logger.history,
            )
            LeanbackSettingsCategories.ABOUT -> LeanbackSettingsCategoryAbout()
        }
    }
}
