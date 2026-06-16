package top.yogiczy.mytv.ui.screens.leanback.panel.components

import android.net.TrafficStats
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import top.yogiczy.mytv.ui.screens.leanback.video.player.LeanbackVideoPlayer
import top.yogiczy.mytv.ui.theme.LeanbackGlass
import top.yogiczy.mytv.ui.theme.LeanbackStatusChip
import top.yogiczy.mytv.ui.theme.LeanbackTheme
import java.text.DecimalFormat

@Composable
fun LeanbackPanelPlayerInfo(
    modifier: Modifier = Modifier,
    metadataProvider: () -> LeanbackVideoPlayer.Metadata = { LeanbackVideoPlayer.Metadata() },
) {
    val metadata = metadataProvider()
    val netSpeed = rememberNetSpeed()

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LeanbackStatusChip(
            text = metadata.formatResolution(),
            icon = Icons.Default.AspectRatio,
            active = metadata.videoWidth > 0 && metadata.videoHeight > 0,
            accentColor = LeanbackGlass.Warning,
        )

        LeanbackStatusChip(
            text = netSpeed.formatNetSpeed(),
            icon = Icons.Default.Wifi,
            active = netSpeed > 0,
            accentColor = if (netSpeed > 0) LeanbackGlass.Warning else MaterialTheme.colorScheme.primary,
        )
    }
}

private fun LeanbackVideoPlayer.Metadata.formatResolution(): String =
    if (videoWidth <= 0 || videoHeight <= 0) {
        "清晰度 --"
    } else {
        buildString {
            append(videoHeight)
            append("P")
            if (videoWidth >= 3840 || videoHeight >= 2160) append(" 4K")
        }
    }

private fun Long.formatNetSpeed(): String =
    if (this < 1024 * 999) "${this / 1024}KB/s"
    else "${DecimalFormat("#.#").format(this / 1024 / 1024f)}MB/s"

@Composable
private fun rememberNetSpeed(): Long {
    var netSpeed by remember { mutableLongStateOf(0) }

    LaunchedEffect(Unit) {
        var lastTotalRxBytes = TrafficStats.getTotalRxBytes()
        var lastTimeStamp = System.currentTimeMillis()

        while (true) {
            delay(1000)
            val nowTotalRxBytes = TrafficStats.getTotalRxBytes()
            val nowTimeStamp = System.currentTimeMillis()
            val speed = (nowTotalRxBytes - lastTotalRxBytes) / (nowTimeStamp - lastTimeStamp) * 1000
            lastTimeStamp = nowTimeStamp
            lastTotalRxBytes = nowTotalRxBytes

            netSpeed = speed
        }
    }

    return netSpeed
}

@Preview
@Composable
private fun LeanbackPanelPlayerInfoPreview() {
    LeanbackTheme {
        LeanbackPanelPlayerInfo(
            metadataProvider = {
                LeanbackVideoPlayer.Metadata(
                    videoWidth = 3840,
                    videoHeight = 2160,
                )
            },
        )
    }
}

@Preview
@Composable
private fun LeanbackPanelPlayerInfoNetSpeedPreview() {
    LeanbackTheme {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LeanbackStatusChip(text = 0L.formatNetSpeed())
            LeanbackStatusChip(text = 54321L.formatNetSpeed(), active = true)
            LeanbackStatusChip(text = (1222L * 1222L).formatNetSpeed(), active = true)
        }
    }
}
