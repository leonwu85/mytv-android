package top.yogiczy.mytv.ui.screens.leanback.video

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import top.yogiczy.mytv.ui.theme.LeanbackGlass
import top.yogiczy.mytv.ui.theme.LeanbackGlassSurface
import top.yogiczy.mytv.ui.theme.LeanbackTheme

@Composable
fun LeanbackVideoPlayerErrorScreen(
    modifier: Modifier = Modifier,
    errorProvider: () -> String? = { null },
) {
    Box(modifier = modifier.fillMaxSize()) {
        val error = errorProvider()
        if (error != null) {
            LeanbackGlassSurface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .sizeIn(maxWidth = 560.dp),
                containerColor = LeanbackGlass.OverlayStrong,
                borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.42f),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "播放失败",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.error,
                    )

                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f),
                    )
                }
            }
        }
    }
}

@Preview(device = "id:Android TV (720p)")
@Composable
private fun LeanbackVideoPlayerErrorScreenPreview() {
    LeanbackTheme {
        LeanbackVideoPlayerErrorScreen(
            errorProvider = { "ERROR_CODE_BEHIND_LIVE_WINDOW" }
        )
    }
}
