package top.yogiczy.mytv.ui.screens.leanback.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.launch
import top.yogiczy.mytv.data.entities.EpgList
import top.yogiczy.mytv.data.entities.ExpandedChannelBuckets
import top.yogiczy.mytv.data.entities.Iptv
import top.yogiczy.mytv.data.entities.IptvGroup
import top.yogiczy.mytv.data.entities.IptvGroupList
import top.yogiczy.mytv.data.entities.IptvGroupList.Companion.iptvList
import top.yogiczy.mytv.data.entities.IptvList
import top.yogiczy.mytv.data.repositories.epg.EpgRepository
import top.yogiczy.mytv.data.repositories.iptv.IptvRepository
import top.yogiczy.mytv.data.utils.Constants
import top.yogiczy.mytv.ui.utils.SP

class LeanbackMainViewModel : ViewModel() {
    private val iptvRepository = IptvRepository()
    private val epgRepository = EpgRepository()

    private val _uiState = MutableStateFlow<LeanbackMainUiState>(LeanbackMainUiState.Loading())
    val uiState: StateFlow<LeanbackMainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            refreshIptv()
            refreshEpg()
        }
    }

    /**
     * 重新加载直播源与节目单。用于在设置页配置/修改直播源后触发刷新。
     */
    fun reload() {
        viewModelScope.launch {
            _uiState.value = LeanbackMainUiState.Loading()
            refreshIptv()
            refreshEpg()
        }
    }

    private suspend fun refreshIptv() {
        val sourceUrl = SP.iptvSourceUrl
        if (sourceUrl.isBlank()) {
            _uiState.value = LeanbackMainUiState.Error(
                message = "未配置直播源，请在设置中配置自定义直播源",
                isSourceNotConfigured = true,
            )
            return
        }

        flow {
            emit(
                iptvRepository.getIptvGroupList(
                    sourceUrl = sourceUrl,
                    cacheTime = SP.iptvSourceCacheTime,
                    simplify = SP.iptvSourceSimplify,
                )
            )
        }
            .retryWhen { _, attempt ->
                if (attempt >= Constants.HTTP_RETRY_COUNT) return@retryWhen false

                _uiState.value =
                    LeanbackMainUiState.Loading("获取远程直播源(${attempt + 1}/${Constants.HTTP_RETRY_COUNT})...")
                delay(Constants.HTTP_RETRY_INTERVAL)
                true
            }
            .catch {
                _uiState.value = LeanbackMainUiState.Error(it.message)
                SP.iptvSourceUrlHistoryList -= sourceUrl
            }
            .map {
                // 扩展频道：开启时，将各直播源的精选频道固化到一个“扩展频道”分组
                val finalGroupList = if (SP.iptvExpandedChannelEnable) {
                    val buckets = ExpandedChannelBuckets.fromJson(SP.iptvExpandedChannelBucketsJson)
                    val expandedIptvList = buckets.flatMap { bucket ->
                        bucket.channels.map { ch ->
                            Iptv(
                                name = ch.name,
                                channelName = ch.channelName,
                                urlList = ch.urlList,
                            )
                        }
                    }
                    if (expandedIptvList.isEmpty()) it
                    else IptvGroupList(it + IptvGroup(name = "扩展频道", iptvList = IptvList(expandedIptvList)))
                } else it

                _uiState.value = LeanbackMainUiState.Ready(iptvGroupList = finalGroupList)
                SP.iptvSourceUrlHistoryList += sourceUrl
                finalGroupList
            }
            .collect()
    }

    private suspend fun refreshEpg() {
        if (_uiState.value is LeanbackMainUiState.Ready) {
            val iptvGroupList = (_uiState.value as LeanbackMainUiState.Ready).iptvGroupList

            // 直播源内置 EPG 优先：开启且解析到内嵌 EPG 时，优先使用内嵌地址
            val effectiveXmlUrl = if (SP.iptvSourceEmbeddedEpgPriority &&
                SP.iptvSourceEmbeddedEpgUrl.isNotBlank()
            ) SP.iptvSourceEmbeddedEpgUrl else SP.epgXmlUrl

            flow {
                emit(
                    epgRepository.getEpgList(
                        xmlUrl = effectiveXmlUrl,
                        filteredChannels = iptvGroupList.iptvList.map { it.channelName },
                        refreshTimeThreshold = SP.epgRefreshTimeThreshold,
                    )
                )
            }
                .retry(Constants.HTTP_RETRY_COUNT) { delay(Constants.HTTP_RETRY_INTERVAL); true }
                .catch {
                    emit(EpgList())
                    SP.epgXmlUrlHistoryList -= effectiveXmlUrl
                }
                .map { epgList ->
                    _uiState.value =
                        (_uiState.value as LeanbackMainUiState.Ready).copy(epgList = epgList)
                    SP.epgXmlUrlHistoryList += effectiveXmlUrl
                }
                .collect()
        }
    }
}

sealed interface LeanbackMainUiState {
    data class Loading(val message: String? = null) : LeanbackMainUiState
    data class Error(
        val message: String? = null,
        /** 直播源未配置：用于触发自动跳转设置页 */
        val isSourceNotConfigured: Boolean = false,
    ) : LeanbackMainUiState
    data class Ready(
        val iptvGroupList: IptvGroupList = IptvGroupList(),
        val epgList: EpgList = EpgList(),
    ) : LeanbackMainUiState
}
