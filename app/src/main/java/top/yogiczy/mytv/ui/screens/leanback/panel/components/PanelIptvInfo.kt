package top.yogiczy.mytv.ui.screens.leanback.panel.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import top.yogiczy.mytv.data.entities.EpgProgramme.Companion.progress
import top.yogiczy.mytv.data.entities.EpgProgrammeCurrent
import top.yogiczy.mytv.data.entities.Iptv
import top.yogiczy.mytv.ui.theme.LeanbackGlass
import top.yogiczy.mytv.ui.theme.LeanbackStatusChip
import top.yogiczy.mytv.ui.theme.LeanbackTheme
import top.yogiczy.mytv.utils.isIPv6

@Composable
fun LeanbackPanelIptvInfo(
    modifier: Modifier = Modifier,
    iptvProvider: () -> Iptv = { Iptv() },
    iptvUrlIdxProvider: () -> Int = { 0 },
    favoriteProvider: () -> Boolean = { false },
    currentProgrammesProvider: () -> EpgProgrammeCurrent? = { null },
    programmeProgressProvider: () -> Float? = { null },
) {
    val iptv = iptvProvider()
    val iptvUrlIdx = iptvUrlIdxProvider()
    val currentProgrammes = currentProgrammesProvider()
    val channelUrl = iptv.urlList.getOrNull(iptvUrlIdx)
    val progress = (programmeProgressProvider() ?: currentProgrammes?.now?.progress())
        ?.takeIf { it.isFinite() }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (iptv.logoUrl.isNotBlank()) {
                LeanbackChannelLogo(
                    logoUrlProvider = { iptv.logoUrl },
                    modifier = Modifier.size(58.dp),
                    cornerRadius = 10.dp,
                    innerPadding = 6.dp,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = iptv.name,
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = "正在播放：${currentProgrammes?.now?.title ?: "暂无节目"}",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Normal),
                    color = LocalContentColor.current.copy(alpha = 0.82f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (favoriteProvider()) {
                LeanbackStatusChip(
                    text = "已收藏",
                    icon = Icons.Default.Star,
                    active = true,
                    accentColor = LeanbackGlass.Warning,
                )
            }

            if (iptv.urlList.size > 1) {
                LeanbackStatusChip(
                    text = "${iptvUrlIdx + 1}/${iptv.urlList.size}",
                    icon = Icons.Default.Route,
                    active = true,
                    accentColor = MaterialTheme.colorScheme.primary,
                )
            }

            LeanbackStatusChip(text = if (channelUrl?.isIPv6() == true) "IPV6" else "IPV4")
        }

        if (progress != null) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(Color.White.copy(alpha = 0.16f), MaterialTheme.shapes.extraSmall),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .height(3.dp)
                        .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.extraSmall),
                )
            }
        }

        currentProgrammes?.next?.title?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = "稍后：$it",
                style = MaterialTheme.typography.bodyMedium,
                color = LeanbackGlass.Muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview
@Composable
private fun LeanbackPanelIptvInfoPreview() {
    LeanbackTheme {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
            LeanbackPanelIptvInfo(
                modifier = Modifier.width(520.dp),
                iptvProvider = { Iptv.EXAMPLE },
                iptvUrlIdxProvider = { 1 },
                favoriteProvider = { true },
                currentProgrammesProvider = { EpgProgrammeCurrent.EXAMPLE },
            )
        }
    }
}
