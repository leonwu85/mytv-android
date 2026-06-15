package top.yogiczy.mytv.ui.screens.leanback.video.player

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import top.yogiczy.mytv.AppGlobal
import top.yogiczy.mytv.data.utils.Constants
import java.io.File

/**
 * 视频分片磁盘缓存单例。
 *
 * 播放器实例会随换台/页面进出频繁创建与释放，但 SimpleCache 必须在整个应用生命周期内常驻，
 * 并且仅能初始化一次（内部维护索引库），因此抽取为全局单例。
 */
@OptIn(UnstableApi::class)
object VideoCache {
    private const val TAG = "VideoCache"

    @Volatile
    private var cache: SimpleCache? = null

    /**
     * 获取常驻的 SimpleCache，懒加载且仅初始化一次。
     * 若先前已通过 [release] 释放，会自动重建。
     */
    fun get(context: Context): SimpleCache {
        cache?.let { return it }
        synchronized(this) {
            cache?.let { return it }
            val cacheDir = File(AppGlobal.cacheDir, "media").apply {
                if (!exists()) mkdirs()
            }
            cache = SimpleCache(
                cacheDir,
                LeastRecentlyUsedCacheEvictor(Constants.VIDEO_PLAYER_SEGMENT_DISK_CACHE_MAX_SIZE),
                StandaloneDatabaseProvider(context.applicationContext),
            )
            Log.d(TAG, "Video cache initialized at ${cacheDir.absolutePath}")
            return cache!!
        }
    }

    /**
     * 释放缓存对象，不删除已缓存文件。
     * 供「清除缓存」调用前先释放，避免索引库与磁盘文件不一致。
     */
    fun release() {
        synchronized(this) {
            cache?.let {
                try {
                    it.release()
                } catch (ex: Exception) {
                    Log.w(TAG, "Release video cache failed", ex)
                }
            }
            cache = null
        }
    }
}

/**
 * 缓存标记位：缓存层出现任何异常时自动回退到上游直连，保证直播不中断。
 */
@OptIn(UnstableApi::class)
internal const val CACHE_FLAGS = CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR
