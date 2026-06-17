package top.yogiczy.mytv.data.utils

/**
 * 常量
 */
object Constants {
    /**
     * 应用 标题
     */
    const val APP_TITLE = "我的电视"

    /**
     * 应用 代码仓库
     */
    const val APP_REPO = "https://github.com/leonwu85/mytv-android"

    /**
     * IPTV源地址
     */
    const val IPTV_SOURCE_URL = ""

    /**
     * IPTV源缓存时间（毫秒）
     */
    const val IPTV_SOURCE_CACHE_TIME = 1000 * 60 * 60 * 24L // 24小时

    /**
     * 节目单XML地址
     */
    const val EPG_XML_URL = "http://epg.51zmt.top:8000/e.xml.gz"

    /**
     * 节目单刷新时间阈值（小时）
     */
    const val EPG_REFRESH_TIME_THRESHOLD = 2 // 不到2点不刷新

    /**
     * Git最新版本信息
     */
    const val GIT_RELEASE_LATEST_URL =
        "https://api.github.com/repos/leonwu85/mytv-android/releases/latest"

    /**
     * GitHub加速代理地址
     */
    const val GITHUB_PROXY = "https://mirror.ghproxy.com/"

    /**
     * HTTP请求重试次数
     */
    const val HTTP_RETRY_COUNT = 10L

    /**
     * HTTP请求重试间隔时间（毫秒）
     */
    const val HTTP_RETRY_INTERVAL = 3000L

    /**
     * 播放器 userAgent
     */
    const val VIDEO_PLAYER_USER_AGENT = "ExoPlayer"

    /**
     * 日志历史最大保留条数
     */
    const val LOG_HISTORY_MAX_SIZE = 50

    /**
     * 播放器加载超时
     */
    const val VIDEO_PLAYER_LOAD_TIMEOUT = 1000L * 15 // 15秒

    /**
     * 播放器播放缓冲时长（毫秒），值越大抗抖动能力越强，可减少卡顿
     */
    const val VIDEO_PLAYER_BUFFER_DURATION = 1000L * 30 // 30秒

    /**
     * 播放器是否启用分片磁盘缓存
     */
    const val VIDEO_PLAYER_SEGMENT_DISK_CACHE_ENABLE = true

    /**
     * 播放器分片磁盘缓存上限（字节）
     */
    const val VIDEO_PLAYER_SEGMENT_DISK_CACHE_MAX_SIZE = 512L * 1024 * 1024 // 512MB

    /**
     * 播放器 LoadErrorHandlingPolicy 最小可重试次数：分片/播放列表加载失败时，
     * 在数据源层静默重试这么多次后才允许冒泡为致命 onPlayerError，吸收瞬时 2000。
     */
    const val PLAYER_LOAD_MIN_RETRY_COUNT = 5

    /**
     * 播放器 LoadErrorHandlingPolicy 退避基数（毫秒），指数退避 2^errorCount 倍递增
     */
    const val PLAYER_LOAD_BACKOFF_BASE_MS = 500L

    /**
     * 播放器 LoadErrorHandlingPolicy 退避上限（毫秒）
     */
    const val PLAYER_LOAD_BACKOFF_MAX_MS = 4000L

    /**
     * 播放器 RetryingDataSource open() 失败时的快速重试次数（连接级重试，在 Media3 察觉到错误前）
     */
    const val PLAYER_DS_OPEN_RETRY_COUNT = 1

    /**
     * 播放器级 IO 重试退避基数（毫秒），用于 retryCurrentMediaItem 等指数退避 + 抖动
     */
    const val PLAYER_IO_RETRY_BACKOFF_BASE_MS = 500L

    /**
     * 播放器级 IO 重试退避上限（毫秒）
     */
    const val PLAYER_IO_RETRY_BACKOFF_MAX_MS = 4000L

    /**
     * 界面 超时未操作自动关闭界面
     */
    const val UI_SCREEN_AUTO_CLOSE_DELAY = 1000L * 15 // 15秒

    /**
     * 界面 时间显示前后范围
     */
    const val UI_TIME_SHOW_RANGE = 1000L * 30 // 前后30秒

    /**
     * 界面 临时面板界面显示时间
     */
    const val UI_TEMP_PANEL_SCREEN_SHOW_DURATION = 1500L // 1.5秒
}
