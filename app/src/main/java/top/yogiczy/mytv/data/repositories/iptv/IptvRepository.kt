package top.yogiczy.mytv.data.repositories.iptv

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import top.yogiczy.mytv.data.entities.Iptv
import top.yogiczy.mytv.data.entities.IptvGroup
import top.yogiczy.mytv.data.entities.IptvGroupList
import top.yogiczy.mytv.data.entities.IptvList
import top.yogiczy.mytv.data.repositories.FileCacheRepository
import top.yogiczy.mytv.data.repositories.iptv.parser.IptvParser
import top.yogiczy.mytv.ui.utils.SP
import top.yogiczy.mytv.utils.Logger

/**
 * 直播源获取
 */
class IptvRepository : FileCacheRepository("iptv.txt") {
    private val log = Logger.create(javaClass.simpleName)

    /**
     * 获取远程直播源数据
     */
    private suspend fun fetchSource(sourceUrl: String) = withContext(Dispatchers.IO) {
        log.d("获取远程直播源: $sourceUrl")

        val client = OkHttpClient()
        // 拉取订阅 User-Agent：仅当用户配置非空时附加
        val request = Request.Builder().url(sourceUrl).apply {
            SP.iptvSourceRequestHeaders.trim().takeIf { it.isNotBlank() }?.let { ua ->
                header("User-Agent", ua)
            }
        }.build()

        try {
            with(client.newCall(request).execute()) {
                if (!isSuccessful) {
                    throw Exception("获取远程直播源失败: $code")
                }

                val contentLength = body?.contentLength() ?: -1
                if (SP.debugAppLog) {
                    log.i("HTTP GET $sourceUrl → $code; len=$contentLength")
                }
                return@with body!!.string()
            }
        } catch (ex: Exception) {
            log.e("获取远程直播源失败", ex)
            throw Exception("获取远程直播源失败，请检查网络连接", ex)
        }
    }

    /**
     * 简化规则
     */
    private fun simplifyTest(group: IptvGroup, iptv: Iptv): Boolean {
        return iptv.name.lowercase().startsWith("cctv") || iptv.name.endsWith("卫视")
    }

    /**
     * 获取直播源分组列表
     */
    suspend fun getIptvGroupList(
        sourceUrl: String,
        cacheTime: Long,
        simplify: Boolean = false,
    ): IptvGroupList {
        if (sourceUrl.isBlank()) {
            throw Exception("IPTV source URL is empty")
        }

        try {
            val sourceData = getOrRefresh(cacheTime) {
                fetchSource(sourceUrl)
            }

            val parser = IptvParser.instances.first { it.isSupport(sourceUrl, sourceData) }

            // 提取并记录内嵌 EPG 地址（供“直播源内置优先”使用）
            SP.iptvSourceEmbeddedEpgUrl = parser.extractEmbeddedEpgUrl(sourceData) ?: ""

            val groupList = parser.parse(sourceData)
            log.i("解析直播源完成：${groupList.size}个分组，${groupList.flatMap { it.iptvList }.size}个频道")

            // 过滤已隐藏分组
            val hiddenGroups = SP.iptvHiddenGroupNames
            val groupListAfterHidden = if (hiddenGroups.isEmpty()) groupList
            else IptvGroupList(groupList.filter { it.name !in hiddenGroups })

            if (simplify) {
                return IptvGroupList(groupListAfterHidden.map { group ->
                    IptvGroup(
                        name = group.name, iptvList = IptvList(group.iptvList.filter { iptv ->
                            simplifyTest(group, iptv)
                        })
                    )
                }.filter { it.iptvList.isNotEmpty() })
            }

            return groupListAfterHidden
        } catch (ex: Exception) {
            log.e("获取直播源失败", ex)
            throw Exception(ex)
        }
    }
}
