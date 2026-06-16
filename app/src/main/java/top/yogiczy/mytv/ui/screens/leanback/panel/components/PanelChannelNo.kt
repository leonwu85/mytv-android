package top.yogiczy.mytv.ui.screens.leanback.panel.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import top.yogiczy.mytv.ui.theme.LeanbackTheme

@Composable
fun LeanbackPanelChannelNo(
    modifier: Modifier = Modifier,
    channelNoProvider: () -> String = { "" },
) {
    Text(
        modifier = modifier,
        text = channelNoProvider(),
        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Light),
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.96f),
    )
}

@Preview
@Composable
private fun LeanbackPanelChannelNoPreview() {
    LeanbackTheme {
        LeanbackPanelChannelNo(
            channelNoProvider = { "01" }
        )
    }
}
