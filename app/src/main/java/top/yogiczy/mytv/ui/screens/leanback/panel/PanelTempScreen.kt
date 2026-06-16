package top.yogiczy.mytv.ui.screens.leanback.panel

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import top.yogiczy.mytv.data.entities.EpgProgramme
import top.yogiczy.mytv.data.entities.EpgProgrammeCurrent
import top.yogiczy.mytv.data.entities.Iptv
import top.yogiczy.mytv.ui.rememberLeanbackChildPadding
import top.yogiczy.mytv.ui.screens.leanback.panel.components.LeanbackPanelChannelNo
import top.yogiczy.mytv.ui.screens.leanback.panel.components.LeanbackPanelIptvInfo
import top.yogiczy.mytv.ui.theme.LeanbackTheme

@Composable
fun LeanbackPanelTempScreen(
    modifier: Modifier = Modifier,
    channelNoProvider: () -> Int = { 0 },
    currentIptvProvider: () -> Iptv = { Iptv() },
    currentIptvUrlIdxProvider: () -> Int = { 0 },
    currentProgrammesProvider: () -> EpgProgrammeCurrent? = { null },
    showProgrammeProgressProvider: () -> Boolean = { false },
) {
    val childPadding = rememberLeanbackChildPadding()

    Box(modifier = modifier.fillMaxSize()) {
        LeanbackPanelChannelNo(
            channelNoProvider = { channelNoProvider().toString().padStart(2, '0') },
            modifier = Modifier
                .padding(top = childPadding.top, end = childPadding.end)
                .align(Alignment.TopEnd),
        )

        LeanbackPanelIptvInfo(
            modifier = Modifier
                .padding(start = childPadding.start, bottom = childPadding.bottom)
                .width(400.dp)
                .align(Alignment.BottomStart),
            iptvProvider = currentIptvProvider,
            iptvUrlIdxProvider = currentIptvUrlIdxProvider,
            currentProgrammesProvider = currentProgrammesProvider,
            programmeProgressProvider = {
                if (showProgrammeProgressProvider()) null else Float.NaN
            },
        )
    }
}

@Preview(device = "id:Android TV (720p)")
@Composable
private fun LeanbackPanelTempScreenPreview() {
    LeanbackTheme {
        LeanbackPanelTempScreen(
            channelNoProvider = { 1 },
            currentIptvProvider = { Iptv.EXAMPLE },
            currentProgrammesProvider = {
                EpgProgrammeCurrent(
                    now = EpgProgramme(
                        startAt = System.currentTimeMillis() - 100000,
                        endAt = System.currentTimeMillis() + 200000,
                        title = "实况录像-2023/"
                    ),
                    next = null,
                )
            },
            showProgrammeProgressProvider = { true },
        )
    }
}
