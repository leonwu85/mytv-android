package top.yogiczy.mytv.ui.screens.leanback.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import top.yogiczy.mytv.ui.rememberLeanbackChildPadding
import top.yogiczy.mytv.ui.screens.leanback.settings.components.LeanbackSettingsCategoryContent
import top.yogiczy.mytv.ui.screens.leanback.settings.components.LeanbackSettingsCategoryList
import top.yogiczy.mytv.ui.theme.LeanbackGlass
import top.yogiczy.mytv.ui.theme.LeanbackGlassSurface
import top.yogiczy.mytv.ui.theme.LeanbackTheme
import top.yogiczy.mytv.ui.theme.leanbackBottomScrim
import top.yogiczy.mytv.ui.theme.leanbackSideScrim
import top.yogiczy.mytv.ui.utils.HttpServer

@Composable
fun LeanbackSettingsScreen(
    modifier: Modifier = Modifier,
    channelNameProvider: () -> String = { "" },
    settingsViewModel: LeanbackSettingsViewModel = viewModel(),
) {
    val childPadding = rememberLeanbackChildPadding()
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(settingsViewModel) {
        HttpServer.settingsUpdates.collect {
            settingsViewModel.syncFromStorage()
        }
    }

    var focusedCategory by remember { mutableStateOf(LeanbackSettingsCategories.UI) }
    val channelName = channelNameProvider()

    Box(
        modifier = modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .background(Color.Black.copy(alpha = 0.32f))
            .leanbackBottomScrim()
            .leanbackSideScrim()
            .pointerInput(Unit) { detectTapGestures(onTap = { }) },
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(
                    start = childPadding.start + 22.dp,
                    top = childPadding.top + 18.dp,
                    end = childPadding.end + 22.dp,
                    bottom = childPadding.bottom + 18.dp,
                )
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Text(
                    text = "设置",
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (channelName.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(34.dp)
                            .background(LeanbackGlass.Stroke),
                    )
                    Text(
                        text = channelName,
                        style = MaterialTheme.typography.titleLarge,
                        color = LeanbackGlass.Muted,
                        maxLines = 1,
                    )
                }
            }

            LeanbackGlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                containerColor = LeanbackGlass.Panel,
                borderColor = LeanbackGlass.Stroke,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 22.dp),
                    horizontalArrangement = Arrangement.spacedBy(22.dp),
                ) {
                    LeanbackSettingsCategoryList(
                        modifier = Modifier.width(230.dp),
                        focusedCategoryProvider = { focusedCategory },
                        onFocused = { focusedCategory = it },
                    )

                    LeanbackSettingsCategoryContent(
                        modifier = Modifier.weight(1f),
                        focusedCategoryProvider = { focusedCategory },
                        settingsViewModel = settingsViewModel,
                    )
                }
            }
        }
    }
}

@Preview(device = "id:Android TV (720p)")
@Composable
private fun LeanbackSettingsScreenPreview() {
    LeanbackTheme {
        LeanbackSettingsScreen(
            channelNameProvider = { "CCTV1 4K超高清" },
        )
    }
}
