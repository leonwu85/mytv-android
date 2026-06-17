package top.yogiczy.mytv.ui.screens.leanback.video.player

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy
import kotlin.math.min
import kotlin.random.Random
import top.yogiczy.mytv.data.utils.Constants

/**
 * 自定义加载错误处理策略，用于降低直播源瞬时 IO 抖动导致的致命 [ERROR_CODE_IO_UNSPECIFIED] (2000)。
 *
 * 与 Media3 默认策略 [androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy] 的区别：
 * 1. 大幅提高静默重试上限（[Constants.PLAYER_LOAD_MIN_RETRY_COUNT]），让单个抖动分片/播放列表
 *    在数据源层被吸收，极少冒泡为致命 onPlayerError；
 * 2. 对永久性 HTTP 状态码（403/404/410/451）不重试，直接交给上层换源，避免无谓等待；
 * 3. 其余 IO 异常使用指数退避 + ±20% 抖动，避免对拥塞源集中重打。
 *
 * 安全网：即便在此层延长重试，[top.yogiczy.mytv.ui.screens.leanback.video.player.LeanbackVideoPlayer]
 * 的 LOAD_TIMEOUT（默认 15s）仍会在长时间 BUFFERING 时触发，由 MainContentState 切换到下一个 URL，
 * 真死源不会被无限重试卡住。
 */
@OptIn(UnstableApi::class)
class IptvLoadErrorHandlingPolicy : LoadErrorHandlingPolicy {

    /**
     * 永久性失败 HTTP 状态码：重试无意义，直接冒泡由上层换源。
     */
    private val permanentHttpCodes = setOf(403, 404, 410, 451)

    override fun getRetryDelayMsFor(loadErrorInfo: LoadErrorHandlingPolicy.LoadErrorInfo): Long {
        val exception = loadErrorInfo.exception

        // 超过最小可重试次数：放行致命错误，让上层播放器重试链/换源介入
        if (loadErrorInfo.errorCount > Constants.PLAYER_LOAD_MIN_RETRY_COUNT) {
            return RETRY_DELAY_MS_UNSET
        }

        // 永久性 HTTP 状态码：不重试
        val responseCode = findInvalidResponseCode(exception)?.responseCode
        if (responseCode in permanentHttpCodes) {
            return RETRY_DELAY_MS_UNSET
        }

        // 指数退避：base * 2^(errorCount-1)，封顶 max，再叠加 ±20% 抖动
        val exp = 1L shl (loadErrorInfo.errorCount - 1).coerceAtMost(10)
        val backoff = min(
            Constants.PLAYER_LOAD_BACKOFF_BASE_MS * exp,
            Constants.PLAYER_LOAD_BACKOFF_MAX_MS,
        )
        val jitter = (backoff * 0.2 * (Random.nextDouble() * 2 - 1)).toLong()
        return backoff + jitter
    }

    override fun getMinimumLoadableRetryCount(dataType: Int): Int {
        return Constants.PLAYER_LOAD_MIN_RETRY_COUNT
    }

    /**
     * 不启用 Media3 的 track/location 排除回退（直播源通常只有单轨道/单地址，
     * 排除反而会无谓地误伤）。
     */
    override fun getFallbackSelectionFor(
        fallbackOptions: LoadErrorHandlingPolicy.FallbackOptions,
        loadErrorInfo: LoadErrorHandlingPolicy.LoadErrorInfo,
    ): LoadErrorHandlingPolicy.FallbackSelection? = null

    private fun findInvalidResponseCode(throwable: Throwable?): HttpDataSource.InvalidResponseCodeException? {
        var current: Throwable? = throwable
        while (current != null) {
            if (current is HttpDataSource.InvalidResponseCodeException) return current
            current = current.cause
        }
        return null
    }

    private companion object {
        /** 等价于 [LoadErrorHandlingPolicy.RETRY_DELAY_MS_UNSET]，即不重试。 */
        const val RETRY_DELAY_MS_UNSET = C.TIME_UNSET
    }
}
