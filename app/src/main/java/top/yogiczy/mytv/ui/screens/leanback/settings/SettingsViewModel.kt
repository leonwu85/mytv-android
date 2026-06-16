package top.yogiczy.mytv.ui.screens.leanback.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import top.yogiczy.mytv.ui.utils.SP

class LeanbackSettingsViewModel : ViewModel() {
    fun syncFromStorage() {
        _iptvLastIptvIdx = SP.iptvLastIptvIdx
        _iptvSourceUrl = SP.iptvSourceUrl
        _iptvSourceUrlHistoryList = SP.iptvSourceUrlHistoryList
        _iptvSourceEmbeddedEpgUrl = SP.iptvSourceEmbeddedEpgUrl
        _epgXmlUrl = SP.epgXmlUrl
        _epgXmlUrlHistoryList = SP.epgXmlUrlHistoryList
        _videoPlayerUserAgent = SP.videoPlayerUserAgent
    }

    private var _appBootLaunch by mutableStateOf(SP.appBootLaunch)
    var appBootLaunch: Boolean
        get() = _appBootLaunch
        set(value) {
            _appBootLaunch = value
            SP.appBootLaunch = value
        }

    private var _appLastLatestVersion by mutableStateOf(SP.appLastLatestVersion)
    var appLastLatestVersion: String
        get() = _appLastLatestVersion
        set(value) {
            _appLastLatestVersion = value
            SP.appLastLatestVersion = value
        }

    private var _appDeviceDisplayType by mutableStateOf(SP.appDeviceDisplayType)
    var appDeviceDisplayType: SP.AppDeviceDisplayType
        get() = _appDeviceDisplayType
        set(value) {
            _appDeviceDisplayType = value
            SP.appDeviceDisplayType = value
        }

    private var _debugShowFps by mutableStateOf(SP.debugShowFps)
    var debugShowFps: Boolean
        get() = _debugShowFps
        set(value) {
            _debugShowFps = value
            SP.debugShowFps = value
        }

    private var _debugShowVideoPlayerMetadata by mutableStateOf(SP.debugShowVideoPlayerMetadata)
    var debugShowVideoPlayerMetadata: Boolean
        get() = _debugShowVideoPlayerMetadata
        set(value) {
            _debugShowVideoPlayerMetadata = value
            SP.debugShowVideoPlayerMetadata = value
        }

    private var _debugAppLog by mutableStateOf(SP.debugAppLog)
    var debugAppLog: Boolean
        get() = _debugAppLog
        set(value) {
            _debugAppLog = value
            SP.debugAppLog = value
        }

    private var _playbackTraceLogcatEnabled by mutableStateOf(SP.playbackTraceLogcatEnabled)
    var playbackTraceLogcatEnabled: Boolean
        get() = _playbackTraceLogcatEnabled
        set(value) {
            _playbackTraceLogcatEnabled = value
            SP.playbackTraceLogcatEnabled = value
        }

    private var _httpServerAdvertiseIp by mutableStateOf(SP.httpServerAdvertiseIp)
    var httpServerAdvertiseIp: String
        get() = _httpServerAdvertiseIp
        set(value) {
            _httpServerAdvertiseIp = value
            SP.httpServerAdvertiseIp = value
        }

    private var _iptvLastIptvIdx by mutableIntStateOf(SP.iptvLastIptvIdx)
    var iptvLastIptvIdx: Int
        get() = _iptvLastIptvIdx
        set(value) {
            _iptvLastIptvIdx = value
            SP.iptvLastIptvIdx = value
        }

    private var _iptvChannelChangeFlip by mutableStateOf(SP.iptvChannelChangeFlip)
    var iptvChannelChangeFlip: Boolean
        get() = _iptvChannelChangeFlip
        set(value) {
            _iptvChannelChangeFlip = value
            SP.iptvChannelChangeFlip = value
        }

    private var _iptvSourceSimplify by mutableStateOf(SP.iptvSourceSimplify)
    var iptvSourceSimplify: Boolean
        get() = _iptvSourceSimplify
        set(value) {
            _iptvSourceSimplify = value
            SP.iptvSourceSimplify = value
        }

    private var _iptvSourceCacheTime by mutableLongStateOf(SP.iptvSourceCacheTime)
    var iptvSourceCacheTime: Long
        get() = _iptvSourceCacheTime
        set(value) {
            _iptvSourceCacheTime = value
            SP.iptvSourceCacheTime = value
        }

    private var _iptvSourceUrl by mutableStateOf(SP.iptvSourceUrl)
    var iptvSourceUrl: String
        get() = _iptvSourceUrl
        set(value) {
            _iptvSourceUrl = value
            SP.iptvSourceUrl = value
        }

    private var _iptvPlayableHostList by mutableStateOf(SP.iptvPlayableHostList)
    var iptvPlayableHostList: Set<String>
        get() = _iptvPlayableHostList
        set(value) {
            _iptvPlayableHostList = value
            SP.iptvPlayableHostList = value
        }

    private var _iptvChannelNoSelectEnable by mutableStateOf(SP.iptvChannelNoSelectEnable)
    var iptvChannelNoSelectEnable: Boolean
        get() = _iptvChannelNoSelectEnable
        set(value) {
            _iptvChannelNoSelectEnable = value
            SP.iptvChannelNoSelectEnable = value
        }

    private var _iptvSourceUrlHistoryList by mutableStateOf(SP.iptvSourceUrlHistoryList)
    var iptvSourceUrlHistoryList: Set<String>
        get() = _iptvSourceUrlHistoryList
        set(value) {
            _iptvSourceUrlHistoryList = value
            SP.iptvSourceUrlHistoryList = value
        }

    private var _iptvSourceRequestHeaders by mutableStateOf(SP.iptvSourceRequestHeaders)
    var iptvSourceRequestHeaders: String
        get() = _iptvSourceRequestHeaders
        set(value) {
            _iptvSourceRequestHeaders = value
            SP.iptvSourceRequestHeaders = value
        }

    private var _iptvChannelRequestHeaders by mutableStateOf(SP.iptvChannelRequestHeaders)
    var iptvChannelRequestHeaders: String
        get() = _iptvChannelRequestHeaders
        set(value) {
            _iptvChannelRequestHeaders = value
            SP.iptvChannelRequestHeaders = value
        }

    private var _iptvSourceEmbeddedEpgUrl by mutableStateOf(SP.iptvSourceEmbeddedEpgUrl)
    var iptvSourceEmbeddedEpgUrl: String
        get() = _iptvSourceEmbeddedEpgUrl
        set(value) {
            _iptvSourceEmbeddedEpgUrl = value
            SP.iptvSourceEmbeddedEpgUrl = value
        }

    private var _iptvSourceEmbeddedEpgPriority by mutableStateOf(SP.iptvSourceEmbeddedEpgPriority)
    var iptvSourceEmbeddedEpgPriority: Boolean
        get() = _iptvSourceEmbeddedEpgPriority
        set(value) {
            _iptvSourceEmbeddedEpgPriority = value
            SP.iptvSourceEmbeddedEpgPriority = value
        }

    private var _iptvHiddenGroupNames by mutableStateOf(SP.iptvHiddenGroupNames)
    var iptvHiddenGroupNames: Set<String>
        get() = _iptvHiddenGroupNames
        set(value) {
            _iptvHiddenGroupNames = value
            SP.iptvHiddenGroupNames = value
        }

    private var _iptvChannelFavoriteEnable by mutableStateOf(SP.iptvChannelFavoriteEnable)
    var iptvChannelFavoriteEnable: Boolean
        get() = _iptvChannelFavoriteEnable
        set(value) {
            _iptvChannelFavoriteEnable = value
            SP.iptvChannelFavoriteEnable = value
        }

    private var _iptvChannelFavoritesOnlyMode by mutableStateOf(SP.iptvChannelFavoritesOnlyMode)
    var iptvChannelFavoritesOnlyMode: Boolean
        get() = _iptvChannelFavoritesOnlyMode
        set(value) {
            _iptvChannelFavoritesOnlyMode = value
            SP.iptvChannelFavoritesOnlyMode = value
        }

    private var _iptvExpandedChannelEnable by mutableStateOf(SP.iptvExpandedChannelEnable)
    var iptvExpandedChannelEnable: Boolean
        get() = _iptvExpandedChannelEnable
        set(value) {
            _iptvExpandedChannelEnable = value
            SP.iptvExpandedChannelEnable = value
        }

    private var _iptvExpandedChannelBucketsJson by mutableStateOf(SP.iptvExpandedChannelBucketsJson)
    var iptvExpandedChannelBucketsJson: String
        get() = _iptvExpandedChannelBucketsJson
        set(value) {
            _iptvExpandedChannelBucketsJson = value
            SP.iptvExpandedChannelBucketsJson = value
        }

    private var _iptvChannelFavoritesJson by mutableStateOf(SP.iptvChannelFavoritesJson)
    var iptvChannelFavoritesJson: String
        get() = _iptvChannelFavoritesJson
        set(value) {
            _iptvChannelFavoritesJson = value
            SP.iptvChannelFavoritesJson = value
        }

    private var _iptvChannelFavoriteListVisible by mutableStateOf(SP.iptvChannelFavoriteListVisible)
    var iptvChannelFavoriteListVisible: Boolean
        get() = _iptvChannelFavoriteListVisible
        set(value) {
            _iptvChannelFavoriteListVisible = value
            SP.iptvChannelFavoriteListVisible = value
        }

    private var _iptvChannelFavoriteList by mutableStateOf(SP.iptvChannelFavoriteList)
    var iptvChannelFavoriteList: Set<String>
        get() = _iptvChannelFavoriteList
        set(value) {
            _iptvChannelFavoriteList = value
            SP.iptvChannelFavoriteList = value
        }

    private var _epgEnable by mutableStateOf(SP.epgEnable)
    var epgEnable: Boolean
        get() = _epgEnable
        set(value) {
            _epgEnable = value
            SP.epgEnable = value
        }

    private var _epgXmlUrl by mutableStateOf(SP.epgXmlUrl)
    var epgXmlUrl: String
        get() = _epgXmlUrl
        set(value) {
            _epgXmlUrl = value
            SP.epgXmlUrl = value
        }

    private var _epgXmlRequestHeaders by mutableStateOf(SP.epgXmlRequestHeaders)
    var epgXmlRequestHeaders: String
        get() = _epgXmlRequestHeaders
        set(value) {
            _epgXmlRequestHeaders = value
            SP.epgXmlRequestHeaders = value
        }

    private var _epgRefreshTimeThreshold by mutableIntStateOf(SP.epgRefreshTimeThreshold)
    var epgRefreshTimeThreshold: Int
        get() = _epgRefreshTimeThreshold
        set(value) {
            _epgRefreshTimeThreshold = value
            SP.epgRefreshTimeThreshold = value
        }

    private var _epgXmlUrlHistoryList by mutableStateOf(SP.epgXmlUrlHistoryList)
    var epgXmlUrlHistoryList: Set<String>
        get() = _epgXmlUrlHistoryList
        set(value) {
            _epgXmlUrlHistoryList = value
            SP.epgXmlUrlHistoryList = value
        }

    private var _uiShowEpgProgrammeProgress by mutableStateOf(SP.uiShowEpgProgrammeProgress)
    var uiShowEpgProgrammeProgress: Boolean
        get() = _uiShowEpgProgrammeProgress
        set(value) {
            _uiShowEpgProgrammeProgress = value
            SP.uiShowEpgProgrammeProgress = value
        }

    private var _uiUseClassicPanelScreen by mutableStateOf(SP.uiUseClassicPanelScreen)
    var uiUseClassicPanelScreen: Boolean
        get() = _uiUseClassicPanelScreen
        set(value) {
            _uiUseClassicPanelScreen = value
            SP.uiUseClassicPanelScreen = value
        }

    private var _uiDensityScaleRatio by mutableFloatStateOf(SP.uiDensityScaleRatio)
    var uiDensityScaleRatio: Float
        get() = _uiDensityScaleRatio
        set(value) {
            _uiDensityScaleRatio = value
            SP.uiDensityScaleRatio = value
        }

    private var _uiFontScaleRatio by mutableFloatStateOf(SP.uiFontScaleRatio)
    var uiFontScaleRatio: Float
        get() = _uiFontScaleRatio
        set(value) {
            _uiFontScaleRatio = value
            SP.uiFontScaleRatio = value
        }

    private var _uiTimeShowMode by mutableStateOf(SP.uiTimeShowMode)
    var uiTimeShowMode: SP.UiTimeShowMode
        get() = _uiTimeShowMode
        set(value) {
            _uiTimeShowMode = value
            SP.uiTimeShowMode = value
        }

    private var _uiPipMode by mutableStateOf(SP.uiPipMode)
    var uiPipMode: Boolean
        get() = _uiPipMode
        set(value) {
            _uiPipMode = value
            SP.uiPipMode = value
        }

    private var _updateForceRemind by mutableStateOf(SP.updateForceRemind)
    var updateForceRemind: Boolean
        get() = _updateForceRemind
        set(value) {
            _updateForceRemind = value
            SP.updateForceRemind = value
        }

    private var _videoPlayerUserAgent by mutableStateOf(SP.videoPlayerUserAgent)
    var videoPlayerUserAgent: String
        get() = _videoPlayerUserAgent
        set(value) {
            _videoPlayerUserAgent = value
            SP.videoPlayerUserAgent = value
        }

    private var _videoPlayerLoadTimeout by mutableLongStateOf(SP.videoPlayerLoadTimeout)
    var videoPlayerLoadTimeout: Long
        get() = _videoPlayerLoadTimeout
        set(value) {
            _videoPlayerLoadTimeout = value
            SP.videoPlayerLoadTimeout = value
        }

    private var _videoPlayerAspectRatio by mutableStateOf(SP.videoPlayerAspectRatio)
    var videoPlayerAspectRatio: SP.VideoPlayerAspectRatio
        get() = _videoPlayerAspectRatio
        set(value) {
            _videoPlayerAspectRatio = value
            SP.videoPlayerAspectRatio = value
        }

    private var _videoPlayerBufferDuration by mutableLongStateOf(SP.videoPlayerBufferDuration)
    var videoPlayerBufferDuration: Long
        get() = _videoPlayerBufferDuration
        set(value) {
            _videoPlayerBufferDuration = value
            SP.videoPlayerBufferDuration = value
        }

    private var _videoPlayerSegmentDiskCacheEnable by mutableStateOf(SP.videoPlayerSegmentDiskCacheEnable)
    var videoPlayerSegmentDiskCacheEnable: Boolean
        get() = _videoPlayerSegmentDiskCacheEnable
        set(value) {
            _videoPlayerSegmentDiskCacheEnable = value
            SP.videoPlayerSegmentDiskCacheEnable = value
        }

    private var _videoRtspForceTcp by mutableStateOf(SP.videoRtspForceTcp)
    var videoRtspForceTcp: Boolean
        get() = _videoRtspForceTcp
        set(value) {
            _videoRtspForceTcp = value
            SP.videoRtspForceTcp = value
        }

    private var _videoRtspRtpSilenceTimeoutMs by mutableLongStateOf(SP.videoRtspRtpSilenceTimeoutMs)
    var videoRtspRtpSilenceTimeoutMs: Long
        get() = _videoRtspRtpSilenceTimeoutMs
        set(value) {
            _videoRtspRtpSilenceTimeoutMs = value
            SP.videoRtspRtpSilenceTimeoutMs = value
        }

    private var _videoRtspTcpPrepareRetryCount by mutableIntStateOf(SP.videoRtspTcpPrepareRetryCount)
    var videoRtspTcpPrepareRetryCount: Int
        get() = _videoRtspTcpPrepareRetryCount
        set(value) {
            _videoRtspTcpPrepareRetryCount = value
            SP.videoRtspTcpPrepareRetryCount = value
        }

    private var _videoRtspPrepareRetryDelayMs by mutableLongStateOf(SP.videoRtspPrepareRetryDelayMs)
    var videoRtspPrepareRetryDelayMs: Long
        get() = _videoRtspPrepareRetryDelayMs
        set(value) {
            _videoRtspPrepareRetryDelayMs = value
            SP.videoRtspPrepareRetryDelayMs = value
        }
}
