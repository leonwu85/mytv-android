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
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.rtsp.RtspMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.util.EventLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    @OptIn(UnstableApi::class)
    private fun prepare(uri: Uri, contentType: Int? = null) {
        // 播放频道 User-Agent：优先用频道请求头，回退到全局播放器 UA
        val effectiveUserAgent = SP.iptvChannelRequestHeaders.trim().ifBlank { SP.videoPlayerUserAgent }
        val httpFactory = DefaultHttpDataSource.Factory().apply {
            setUserAgent(effectiveUserAgent)
            setConnectTimeoutMs(SP.videoPlayerLoadTimeout.toInt())
            setReadTimeoutMs(SP.videoPlayerLoadTimeout.toInt())
            setKeepPostFor302Redirects(true)
            setAllowCrossProtocolRedirects(true)
        }

        // 分片磁盘缓存：开启时用 CacheDataSource 包一层，出错时自动回退到上游直连
        val upstreamFactory =
            if (SP.videoPlayerSegmentDiskCacheEnable) {
                CacheDataSource.Factory()
                    .setCache(VideoCache.get(context))
                    .setUpstreamDataSourceFactory(httpFactory)
                    .setFlags(CACHE_FLAGS)
            } else {
                httpFactory
            }

        val dataSourceFactory = DefaultDataSource.Factory(context, upstreamFactory)

        val mediaItem = MediaItem.fromUri(uri)

        val mediaSource = when (val type = contentType ?: Util.inferContentType(uri)) {
            C.CONTENT_TYPE_HLS -> {
                HlsMediaSource.Factory(dataSourceFactory)
                    .setExtractorFactory(Av3aHlsExtractorFactory())
                    .createMediaSource(mediaItem)
            }

            C.CONTENT_TYPE_RTSP -> {
                // RTSP 优先 TCP（Interleaved）：穿越 NAT/防火墙更稳。
                // 静默超时/TCP起播重试次数/重试间隔：Media3 1.4.1 的 RtspMediaSource.Factory
                // 暂未公开对应 API，这里仅在起播配置 TCP 模式；后续错误重试由上层换源逻辑处理。
                RtspMediaSource.Factory()
                    .setForceUseRtpTcp(SP.videoRtspForceTcp)
                    .createMediaSource(mediaItem)
            }

            C.CONTENT_TYPE_OTHER -> {
                ProgressiveMediaSource.Factory(dataSourceFactory, Av3aExtractorsFactory())
                    .createMediaSource(mediaItem)
            }

            else -> {
                triggerError(
                    PlaybackException.UNSUPPORTED_TYPE.copy(
                        errorCodeName = "${PlaybackException.UNSUPPORTED_TYPE.message}_$type"
                    )
                )
                null
            }
        }

        if (mediaSource != null) {
            contentTypeAttempts[contentType ?: Util.inferContentType(uri)] = true
            videoPlayer.setMediaSource(mediaSource)
            videoPlayer.prepare()
            triggerPrepared()
        }
        updatePositionJob?.cancel()
        updatePositionJob = null
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
                retryCurrentMediaItem(ex)
            ) {
                return
            } else {
                triggerError(ex.toPlaybackException())
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_BUFFERING) {
                triggerError(null)
                triggerBuffering(true)
            } else if (playbackState == Player.STATE_READY) {
                httpStatusRetryCount = 0
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
    override fun prepare(url: String) {
        contentTypeAttempts.clear()
        httpStatusRetryCount = 0
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

    private fun retryCurrentMediaItem(ex: Media3PlaybackException): Boolean {
        val uri = videoPlayer.currentMediaItem?.localConfiguration?.uri ?: return false
        if (httpStatusRetryCount >= HTTP_STATUS_RETRY_LIMIT) return false

        httpStatusRetryCount++
        log.w(
            "HTTP状态异常，重新拉取播放地址($httpStatusRetryCount/$HTTP_STATUS_RETRY_LIMIT): " +
                "${ex.toPlaybackException().errorCodeName} $uri",
            ex,
        )

        updatePositionJob?.cancel()
        updatePositionJob = null
        coroutineScope.launch {
            delay(500)
            prepare(uri)
        }
        return true
    }

    private fun Media3PlaybackException.toPlaybackException(): PlaybackException {
        val responseCode = findInvalidResponseCodeException()?.responseCode
        val name = if (responseCode != null) "${errorCodeName}_$responseCode" else errorCodeName
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

    private companion object {
        const val HTTP_STATUS_RETRY_LIMIT = 3
    }
}
