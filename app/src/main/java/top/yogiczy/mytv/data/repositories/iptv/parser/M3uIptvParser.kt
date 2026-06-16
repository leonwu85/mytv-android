package top.yogiczy.mytv.data.repositories.iptv.parser

import top.yogiczy.mytv.data.entities.Iptv
import top.yogiczy.mytv.data.entities.IptvGroup
import top.yogiczy.mytv.data.entities.IptvGroupList
import top.yogiczy.mytv.data.entities.IptvList

class M3uIptvParser : IptvParser {

    override fun isSupport(url: String, data: String): Boolean {
        return data.startsWith("#EXTM3U")
    }

    /**
     * 从 #EXTM3U 头中提取内嵌 EPG 地址（x-tvg-url / url-tvg）。
     */
    override fun extractEmbeddedEpgUrl(data: String): String? {
        // 只看首行（#EXTM3U 头）
        val header = data.lineSequence().firstOrNull { it.startsWith("#EXTM3U") } ?: return null
        val regex = Regex("""(?:x-tvg-url|url-tvg)\s*=\s*"?([^"\s,]+)"?""", RegexOption.IGNORE_CASE)
        return regex.find(header)?.groupValues?.getOrNull(1)?.takeIf { it.isNotBlank() }
    }

    override suspend fun parse(data: String): IptvGroupList {
        val lines = data.split("\r\n", "\n")
        val iptvList = mutableListOf<IptvResponseItem>()

        lines.forEachIndexed { index, line ->
            if (!line.startsWith("#EXTINF")) return@forEachIndexed

            val name = line.split(",").last()
            val channelName = Regex("tvg-name=\"(.+?)\"").find(line)?.groupValues?.get(1) ?: name
            val groupName = Regex("group-title=\"(.+?)\"").find(line)?.groupValues?.get(1) ?: "其他"

            iptvList.add(
                IptvResponseItem(
                    name = name.trim(),
                    channelName = channelName.trim(),
                    groupName = groupName.trim(),
                    url = lines[index + 1].trim(),
                )
            )
        }

        return IptvGroupList(iptvList.groupBy { it.groupName }.map { groupEntry ->
            IptvGroup(
                name = groupEntry.key,
                iptvList = IptvList(groupEntry.value.groupBy { it.name }.map { nameEntry ->
                    Iptv(
                        name = nameEntry.key,
                        channelName = nameEntry.value.first().channelName,
                        urlList = nameEntry.value.map { it.url },
                    )
                })
            )
        })
    }

    private data class IptvResponseItem(
        val name: String,
        val channelName: String,
        val groupName: String,
        val url: String,
    )
}