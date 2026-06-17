package top.yogiczy.mytv.ui.screens.leanback.video.player

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.HttpDataSource
import java.io.IOException
import kotlin.math.min
import kotlin.random.Random
import top.yogiczy.mytv.data.utils.Constants

/**
 * 上游 [DataSource] 重试包装：在 [HttpDataSource.open] 失败时按
 * [Constants.PLAYER_DS_OPEN_RETRY_COUNT] 做快速连接级重试，在 Media3 的 Loader/LoadErrorHandlingPolicy
 * 察觉到错误前吸收一部分瞬时连接失败（连接重置 / 早期 EOF / DNS 抖动等），进一步降低
 * [ERROR_CODE_IO_UNSPECIFIED] (2000) 的冒泡频率。
 *
 * 仅覆写 [open]；读/关/URI/响应头等行为全部委托给上游 [delegate]。
 * 对永久性 HTTP 状态码（403/404/410/451）直接抛出，不浪费重试次数。
 *
 * 注意：此层与 [IptvLoadErrorHandlingPolicy] 是互补关系——前者做连接级快速重试（毫秒级），
 * 后者做加载级指数退避重试（数百毫秒~秒级）。
 */
@OptIn(UnstableApi::class)
internal class RetryingDataSource(
    private val delegate: HttpDataSource,
    private val maxRetries: Int,
    private val backoffBaseMs: Long,
    private val backoffMaxMs: Long,
) : DataSource by delegate {

    override fun open(dataSpec: DataSpec): Long {
        var lastError: IOException? = null
        // 总尝试次数 = 首次 + maxRetries
        repeat(maxRetries + 1) { attempt ->
            try {
                return delegate.open(dataSpec)
            } catch (ex: IOException) {
                lastError = ex
                // 永久性 HTTP 状态码：直接抛出，交给上层换源
                if (findInvalidResponseCode(ex)?.responseCode in permanentHttpCodes) throw ex
                if (attempt < maxRetries) {
                    closeAfterFailedOpen()
                    // 指数退避 + ±20% 抖动，阻塞等待；连接级重试不致过长
                    val exp = 1L shl attempt.coerceAtMost(10)
                    val backoff = min(backoffBaseMs * exp, backoffMaxMs)
                    val jitter = (backoff * 0.2 * (Random.nextDouble() * 2 - 1)).toLong()
                    try {
                        Thread.sleep((backoff + jitter).coerceAtLeast(0))
                    } catch (_: InterruptedException) {
                        Thread.currentThread().interrupt()
                        throw ex
                    }
                }
            }
        }
        throw lastError ?: IOException("RetryingDataSource open failed")
    }

    private fun closeAfterFailedOpen() {
        try {
            delegate.close()
        } catch (_: IOException) {
            // 重试前尽力清理半开连接；保留原始 open() 失败作为最终异常。
        }
    }

    private fun findInvalidResponseCode(throwable: Throwable?): HttpDataSource.InvalidResponseCodeException? {
        var current: Throwable? = throwable
        while (current != null) {
            if (current is HttpDataSource.InvalidResponseCodeException) return current
            current = current.cause
        }
        return null
    }

    private companion object {
        private val permanentHttpCodes = setOf(403, 404, 410, 451)
    }

    /**
     * 工厂：把上游 [HttpDataSource.Factory] 包装为带重试的 [DataSource.Factory]，
     * 供 [DefaultDataSource.Factory] 或 [CacheDataSource] 作为底层使用。
     */
    class Factory(
        private val delegateFactory: HttpDataSource.Factory,
        private val maxRetries: Int = Constants.PLAYER_DS_OPEN_RETRY_COUNT,
        private val backoffBaseMs: Long = Constants.PLAYER_LOAD_BACKOFF_BASE_MS,
        private val backoffMaxMs: Long = Constants.PLAYER_LOAD_BACKOFF_MAX_MS,
    ) : DataSource.Factory {
        override fun createDataSource(): DataSource =
            RetryingDataSource(
                delegate = delegateFactory.createDataSource(),
                maxRetries = maxRetries,
                backoffBaseMs = backoffBaseMs,
                backoffMaxMs = backoffMaxMs,
            )
    }
}
