package top.yogiczy.mytv.ui.screens.leanback.panel.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

@Composable
fun LeanbackChannelLogo(
    modifier: Modifier = Modifier,
    logoUrlProvider: () -> String = { "" },
    contentScale: ContentScale = ContentScale.Fit,
    cornerRadius: Dp = 8.dp,
    innerPadding: Dp = 4.dp,
) {
    val logoUrl = logoUrlProvider().trim()
    var imageBitmap by remember(logoUrl) { mutableStateOf<ImageBitmap?>(null) }

    if (logoUrl.isBlank()) return

    LaunchedEffect(logoUrl) {
        imageBitmap = LeanbackChannelLogoLoader.load(logoUrl)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(innerPadding),
        contentAlignment = Alignment.Center,
    ) {
        imageBitmap?.let {
            Image(
                bitmap = it,
                contentDescription = null,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

private object LeanbackChannelLogoLoader {
    private val client = OkHttpClient()

    suspend fun load(url: String): ImageBitmap? = withContext(Dispatchers.IO) {
        runCatching {
            client.newCall(Request.Builder().url(url).build()).execute().use { response ->
                if (!response.isSuccessful) return@runCatching null
                val bytes = response.body?.bytes() ?: return@runCatching null
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            }
        }.getOrNull()
    }
}
