package top.yogiczy.mytv.ui.screens.leanback.video.player

import android.content.Context
import android.net.Uri
import android.view.SurfaceView
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DecoderReuseEvaluation
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.hls.HlsDataSourceFactory
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.rtsp.RtspMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.util.EventLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import top.yogiczy.mytv.data.utils.Constants
import top.yogiczy.mytv.ui.utils.SP
import androidx.media3.common.PlaybackException as Media3PlaybackException

@OptIn(UnstableApi::class)
class LeanbackMedia3VideoPlayer(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
) : LeanbackVideoPlayer(coroutineScope) {
    private val videoPlayer = ExoPlayer.Builder(
        context,
        DefaultRenderersFactory(context).setExtensionRendererMode(EXTENSION_RENDERER_MODE_ON)
    ).setLoadControl(buildLoadControl()).build().apply {
        playWhenReady = true
        // 播放链路诊断日志：用 logcat 标签 PlaybackTrace，不写完整频道 URL
        if (SP.playbackTraceLogcatEnabled) {
            addAnalyticsListener(EventLogger("PlaybackTrace"))
        }
    }

    /**
     * 根据用户配置的播放缓冲时长构建 LoadControl。
     * 直播场景使用 minBufferMs == maxBufferMs 的恒定高缓冲，可减少卡顿。
     * 缓冲值限制在 ExoPlayer 允许的区间 [5s, 50s] 内（DefaultLoadControl 约束）。
     */
    private fun buildLoadControl(): DefaultLoadControl {
        // DefaultLoadControl 允许的最小/最大缓冲区间
        val minAllowed = 5_000
        val maxAllowed = 50_000
        val bufferMs = SP.videoPlayerBufferDuration.toInt().coerceIn(minAllowed, maxAllowed)
        return DefaultLoadControl.Builder()
            .setBufferDurationsMs(bufferMs, bufferMs, 1000, 2000)
            .build()
    }

    private val contentTypeAttempts = mutableMapOf<Int, Boolean>()
    private var updatePositionJob: Job? = null
    private var httpStatusRetryCount = 0
    private var ioRetryCount = 0
    private var hlsDirectRetryUsed = false
    private var currentUseSegmentCache = true
    private var currentUserAgent = ""

    /** 加载级错误处理策略：所有 MediaSource 共用，在数据源层吸收瞬时 IO 错误（第1层）。 */
    private val loadErrorHandlingPolicy = IptvLoadErrorHandlingPolicy()

    @OptIn(UnstableApi::class)
    private fun prepare(
        uri: Uri,
        contentType: Int? = null,
        useSegmentCache: Boolean = true,
    ) {
        // 播放频道 User-Agent：优先用频道请求头，回退到全局播放器 UA
        val effectiveUserAgent = currentUserAgent
            .ifBlank { SP.iptvChannelRequestHeaders.trim() }
            .ifBlank { SP.videoPlayerUserAgent }
            .ifBlank { Util.getUserAgent(context, "MyTV") }
        val httpFactory = DefaultHttpDataSource.Factory().apply {
            setUserAgent(effectiveUserAgent)
            setConnectTimeoutMs(SP.videoPlayerLoadTimeout.toInt())
            setReadTimeoutMs(SP.videoPlayerLoadTimeout.toInt())
            setKeepPostFor302Redirects(true)
            setAllowCrossProtocolRedirects(true)
        }
        // 上游 DataSource 重试包装：在 HttpDataSource.open 失败时做连接级快速重试，
        // 在 Media3 察觉到错误前吸收瞬时连接抖动，进一步降低 2000 冒泡频率（第4层）。
        val retryingHttpFactory = RetryingDataSource.Factory(httpFactory)

        // 分片磁盘缓存：开启时用 CacheDataSource 包一层，出错时自动回退到上游直连
        val cacheDataSourceFactory =
            if (SP.videoPlayerSegmentDiskCacheEnable && useSegmentCache) {
                CacheDataSource.Factory()
                    .setCache(VideoCache.get(context))
                    .setUpstreamDataSourceFactory(retryingHttpFactory)
                    .setFlags(CACHE_FLAGS)
            } else {
                null
            }

        val directDataSourceFactory = DefaultDataSource.Factory(context, retryingHttpFactory)
        val dataSourceFactory = cacheDataSourceFactory
            ?.let { DefaultDataSource.Factory(context, it) }
            ?: directDataSourceFactory

        val mediaItem = MediaItem.fromUri(uri)
        val resolvedContentType = contentType ?: Util.inferContentType(uri)

        val mediaSource = when (resolvedContentType) {
            C.CONTENT_TYPE_HLS -> {
                HlsMediaSource.Factory(
                    cacheDataSourceFactory?.let {
                        buildHlsSegmentCacheDataSourceFactory(
                            directDataSourceFactory = directDataSourceFactory,
                            cachedDataSourceFactory = dataSourceFactory,
                        )
                    } ?: HlsDataSourceFactory { directDataSourceFactory.createDataSource() }
                )
                    .setExtractorFactory(Av3aHlsExtractorFactory())
                    // 无分片起播：准备阶段不再多下一个分片，收窄 IO 错误面（第2层）。
                    // 无 CODECS 的源 Media3 会自动回退到普通起播，安全。
                    .setAllowChunklessPreparation(true)
                    // 加载级重试策略：在数据源层吸收瞬时 2000，避免过早冒泡为致命错误（第1层）。
                    .setLoadErrorHandlingPolicy(loadErrorHandlingPolicy)
                    .createMediaSource(mediaItem)
            }

            C.CONTENT_TYPE_RTSP -> {
                // RTSP 优先 TCP（Interleaved）：穿越 NAT/防火墙更稳。
                // 静默超时/TCP起播重试次数/重试间隔：Media3 1.4.1 的 RtspMediaSource.Factory
                // 暂未公开对应 API，这里仅在起播配置 TCP 模式；后续错误重试由上层换源逻辑处理。
                RtspMediaSource.Factory()
                    .setForceUseRtpTcp(SP.videoRtspForceTcp)
                    .setLoadErrorHandlingPolicy(loadErrorHandlingPolicy)
                    .createMediaSource(mediaItem)
            }

            C.CONTENT_TYPE_OTHER -> {
                ProgressiveMediaSource.Factory(dataSourceFactory, Av3aExtractorsFactory())
                    .setLoadErrorHandlingPolicy(loadErrorHandlingPolicy)
                    .createMediaSource(mediaItem)
            }

            else -> {
                triggerError(
                    PlaybackException.UNSUPPORTED_TYPE.copy(
                        errorCodeName = "${PlaybackException.UNSUPPORTED_TYPE.message}_$resolvedContentType"
                    )
                )
                null
            }
        }

        if (mediaSource != null) {
            currentUseSegmentCache = useSegmentCache
            contentTypeAttempts[resolvedContentType] = true
            videoPlayer.setMediaSource(mediaSource)
            videoPlayer.prepare()
            triggerPrepared()
        }
        updatePositionJob?.cancel()
        updatePositionJob = null
    }

    private fun buildHlsSegmentCacheDataSourceFactory(
        directDataSourceFactory: DefaultDataSource.Factory,
        cachedDataSourceFactory: DefaultDataSource.Factory,
    ): HlsDataSourceFactory = HlsDataSourceFactory { dataType ->
        when (dataType) {
            C.DATA_TYPE_MEDIA,
            C.DATA_TYPE_MEDIA_INITIALIZATION -> cachedDataSourceFactory.createDataSource()

            else -> directDataSourceFactory.createDataSource()
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onVideoSizeChanged(videoSize: VideoSize) {
            triggerResolution(videoSize.width, videoSize.height)
        }

        override fun onPlayerError(ex: Media3PlaybackException) {
            // 如果是直播加载位置错误，尝试重新播放
            if (ex.errorCode == Media3PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW) {
                videoPlayer.seekToDefaultPosition()
                videoPlayer.prepare()
            }
            // 当解析容器不支持时，尝试使用其他解析容器
            else if (ex.errorCode == Media3PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED) {
                val uri = videoPlayer.currentMediaItem?.localConfiguration?.uri
                if (uri != null) {
                    if (contentTypeAttempts[C.CONTENT_TYPE_HLS] != true) {
                        prepare(uri, C.CONTENT_TYPE_HLS)
                    } else if (contentTypeAttempts[C.CONTENT_TYPE_OTHER] != true) {
                        prepare(uri, C.CONTENT_TYPE_OTHER)
                    } else if (contentTypeAttempts[C.CONTENT_TYPE_OTHER] != true) {
                        prepare(uri, C.CONTENT_TYPE_OTHER)
                    } else {
                        triggerError(PlaybackException.UNSUPPORTED_TYPE)
                    }
                }
            } else if (ex.errorCode == Media3PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS &&
                retryCurrentMediaItem(ex, "HTTP状态异常")
            ) {
                return
            } else if (shouldRetryAsHls(ex) && retryCurrentMediaItemAsHls(ex)) {
                return
            } else if (retryHlsWithoutSegmentCache(ex)) {
                return
            } else if (isRecoverableIoErrorCode(ex.errorCode) &&
                retryCurrentMediaItem(ex, "IO异常")
            ) {
                return
            } else {
                log.w("播放失败: ${ex.toPlaybackException().errorCodeName}", ex)
                triggerError(ex.toPlaybackException())
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_BUFFERING) {
                triggerError(null)
                triggerBuffering(true)
            } else if (playbackState == Player.STATE_READY) {
                httpStatusRetryCount = 0
                ioRetryCount = 0
                triggerReady()

                updatePositionJob?.cancel()
                updatePositionJob = coroutineScope.launch {
                    triggerCurrentPosition(-1)
                    while (true) {
                        triggerCurrentPosition(videoPlayer.currentPosition)
                        delay(1000)
                    }
                }
            }

            if (playbackState != Player.STATE_BUFFERING) {
                triggerBuffering(false)
            }
        }
    }

    private val metadataListener = @UnstableApi object : AnalyticsListener {
        override fun onVideoInputFormatChanged(
            eventTime: AnalyticsListener.EventTime,
            format: Format,
            decoderReuseEvaluation: DecoderReuseEvaluation?,
        ) {
            metadata = metadata.copy(
                videoMimeType = format.sampleMimeType ?: "",
                videoWidth = format.width,
                videoHeight = format.height,
                videoColor = format.colorInfo?.toLogString() ?: "",
                // TODO 帧率、比特率目前是从tag中获取，有的返回空，后续需要实时计算
                videoFrameRate = format.frameRate,
                videoBitrate = format.bitrate,
            )
            triggerMetadata(metadata)
        }

        override fun onVideoDecoderInitialized(
            eventTime: AnalyticsListener.EventTime,
            decoderName: String,
            initializedTimestampMs: Long,
            initializationDurationMs: Long,
        ) {
            metadata = metadata.copy(videoDecoder = decoderName)
            triggerMetadata(metadata)
        }

        override fun onAudioInputFormatChanged(
            eventTime: AnalyticsListener.EventTime,
            format: Format,
            decoderReuseEvaluation: DecoderReuseEvaluation?,
        ) {
            metadata = metadata.copy(
                audioMimeType = format.sampleMimeType ?: "",
                audioChannels = format.channelCount,
                audioSampleRate = format.sampleRate,
            )
            triggerMetadata(metadata)
        }

        override fun onAudioDecoderInitialized(
            eventTime: AnalyticsListener.EventTime,
            decoderName: String,
            initializedTimestampMs: Long,
            initializationDurationMs: Long,
        ) {
            metadata = metadata.copy(audioDecoder = decoderName)
            triggerMetadata(metadata)
        }
    }

    private val eventLogger = EventLogger()

    override fun initialize() {
        super.initialize()
        videoPlayer.addListener(playerListener)
        videoPlayer.addAnalyticsListener(metadataListener)
        videoPlayer.addAnalyticsListener(eventLogger)
    }

    override fun release() {
        videoPlayer.removeListener(playerListener)
        videoPlayer.removeAnalyticsListener(metadataListener)
        videoPlayer.removeAnalyticsListener(eventLogger)
        videoPlayer.release()
        super.release()
    }

    @UnstableApi
    override fun prepare(url: String, userAgent: String) {
        contentTypeAttempts.clear()
        httpStatusRetryCount = 0
        ioRetryCount = 0
        hlsDirectRetryUsed = false
        currentUseSegmentCache = true
        currentUserAgent = userAgent.trim()
        prepare(Uri.parse(url))
    }

    override fun play() {
        videoPlayer.play()
    }

    override fun pause() {
        videoPlayer.pause()
    }

    override fun setVideoSurfaceView(surfaceView: SurfaceView) {
        videoPlayer.setVideoSurfaceView(surfaceView)
    }

    private fun retryCurrentMediaItem(
        ex: Media3PlaybackException,
        reason: String,
    ): Boolean {
        val uri = videoPlayer.currentMediaItem?.localConfiguration?.uri ?: return false
        val isHttpStatusError = ex.errorCode == Media3PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS
        val retryCount = if (isHttpStatusError) httpStatusRetryCount else ioRetryCount
        val retryLimit = if (isHttpStatusError) HTTP_STATUS_RETRY_LIMIT else IO_RETRY_LIMIT
        if (retryCount >= retryLimit) return false

        if (isHttpStatusError) httpStatusRetryCount++ else ioRetryCount++
        log.w(
            "$reason，重新拉取播放地址(${retryCount + 1}/$retryLimit): " +
                "${ex.toPlaybackException().errorCodeName} $uri",
            ex,
        )

        updatePositionJob?.cancel()
        updatePositionJob = null
        coroutineScope.launch {
            delay(ioRetryDelayMs(retryCount))
            prepare(
                uri = uri,
                contentType = currentContentTypeForRetry(uri),
                useSegmentCache = currentUseSegmentCache,
            )
        }
        return true
    }

    private fun retryCurrentMediaItemAsHls(ex: Media3PlaybackException): Boolean {
        val uri = videoPlayer.currentMediaItem?.localConfiguration?.uri ?: return false
        if (contentTypeAttempts[C.CONTENT_TYPE_HLS] == true) return false

        ioRetryCount++
        log.w("IO异常，尝试按 HLS 重新打开: ${ex.toPlaybackException().errorCodeName} $uri", ex)
        updatePositionJob?.cancel()
        updatePositionJob = null
        coroutineScope.launch {
            delay(ioRetryDelayMs(ioRetryCount))
            prepare(uri, C.CONTENT_TYPE_HLS)
        }
        return true
    }

    private fun currentContentTypeForRetry(uri: Uri): Int? {
        return when {
            contentTypeAttempts[C.CONTENT_TYPE_HLS] == true -> C.CONTENT_TYPE_HLS
            contentTypeAttempts[C.CONTENT_TYPE_OTHER] == true -> C.CONTENT_TYPE_OTHER
            else -> Util.inferContentType(uri)
        }
    }

    private fun retryHlsWithoutSegmentCache(ex: Media3PlaybackException): Boolean {
        if (!isRecoverableIoErrorCode(ex.errorCode)) return false
        if (!SP.videoPlayerSegmentDiskCacheEnable) return false
        if (!currentUseSegmentCache) return false
        if (hlsDirectRetryUsed) return false
        if (contentTypeAttempts[C.CONTENT_TYPE_HLS] != true) return false

        val uri = videoPlayer.currentMediaItem?.localConfiguration?.uri ?: return false
        hlsDirectRetryUsed = true
        ioRetryCount++
        log.w("HLS 分片缓存 IO 异常，切换为直连重试: ${ex.toPlaybackException().errorCodeName} $uri", ex)
        updatePositionJob?.cancel()
        updatePositionJob = null
        coroutineScope.launch {
            delay(ioRetryDelayMs(ioRetryCount))
            prepare(uri, C.CONTENT_TYPE_HLS, useSegmentCache = false)
        }
        return true
    }

    private fun shouldRetryAsHls(ex: Media3PlaybackException): Boolean {
        return isRecoverableIoErrorCode(ex.errorCode) &&
            contentTypeAttempts[C.CONTENT_TYPE_HLS] != true
    }

    private fun isRecoverableIoErrorCode(errorCode: Int): Boolean {
        return errorCode == Media3PlaybackException.ERROR_CODE_IO_UNSPECIFIED ||
            errorCode == Media3PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ||
            errorCode == Media3PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT
    }

    /**
     * 播放器级 IO 重试退避延迟（第3层）：指数退避 + ±20% 抖动，
     * 避免对拥塞源集中重打。[attempt] 从 0 起。
     */
    private fun ioRetryDelayMs(attempt: Int): Long {
        val exp = 1L shl attempt.coerceAtMost(10)
        val backoff = minOf(
            Constants.PLAYER_IO_RETRY_BACKOFF_BASE_MS * exp,
            Constants.PLAYER_IO_RETRY_BACKOFF_MAX_MS,
        )
        val jitter = (backoff * 0.2 * (kotlin.random.Random.nextDouble() * 2 - 1)).toLong()
        return (backoff + jitter).coerceAtLeast(0)
    }

    private fun Media3PlaybackException.toPlaybackException(): PlaybackException {
        val responseCode = findInvalidResponseCodeException()?.responseCode
        val causeName = findUsefulCause()?.toShortErrorName()
        val name = listOfNotNull(
            errorCodeName,
            responseCode?.toString(),
            causeName,
        ).joinToString("_")
        return PlaybackException(name, errorCode)
    }

    private fun Throwable.findInvalidResponseCodeException(): HttpDataSource.InvalidResponseCodeException? {
        var current: Throwable? = this
        while (current != null) {
            if (current is HttpDataSource.InvalidResponseCodeException) return current
            current = current.cause
        }
        return null
    }

    private fun Throwable.findUsefulCause(): Throwable? {
        var current = cause
        var last: Throwable? = null
        while (current != null) {
            last = current
            current = current.cause
        }
        return last ?: cause
    }

    private fun Throwable.toShortErrorName(): String {
        val message = message
            ?.replace(Regex("""https?://\S+"""), "url")
            ?.replace(Regex("""\s+"""), " ")
            ?.take(80)
            ?.replace(Regex("""[^A-Za-z0-9_ .:-]"""), "")
            ?.trim()
            .orEmpty()

        return if (message.isBlank()) javaClass.simpleName
        else "${javaClass.simpleName}:${message}"
    }

    private companion object {
        const val HTTP_STATUS_RETRY_LIMIT = 3
        const val IO_RETRY_LIMIT = 2
    }
}
