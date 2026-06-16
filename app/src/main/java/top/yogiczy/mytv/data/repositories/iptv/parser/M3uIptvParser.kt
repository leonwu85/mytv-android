package top.yogiczy.mytv.data.repositories.iptv.parser

import top.yogiczy.mytv.data.entities.Iptv
import top.yogiczy.mytv.data.entities.IptvGroup
import top.yogiczy.mytv.data.entities.IptvGroupList
import top.yogiczy.mytv.data.entities.IptvList
import java.util.Locale

class M3uIptvParser : IptvParser {

    override fun isSupport(url: String, data: String): Boolean {
        return data.trimStart().startsWith("#EXTM3U", ignoreCase = true)
    }

    /**
     * 从 #EXTM3U 头中提取内嵌 EPG 地址（x-tvg-url / url-tvg / tvg-url）。
     */
    override fun extractEmbeddedEpgUrl(data: String): String? {
        val header = data.lineSequence()
            .map { it.trim() }
            .firstOrNull { it.startsWith("#EXTM3U", ignoreCase = true) }
            ?: return null

        val attributes = parseAttributes(header)
        return listOf("x-tvg-url", "url-tvg", "tvg-url")
            .firstNotNullOfOrNull { key ->
                attributes[key]
                    ?.split(",", ";", "|")
                    ?.map { it.trim() }
                    ?.firstOrNull { it.isNotBlank() }
            }
    }

    override suspend fun parse(data: String): IptvGroupList {
        val lines = data.lineSequence().map { it.trim() }.toList()
        val iptvList = mutableListOf<IptvResponseItem>()

        var index = 0
        while (index < lines.size) {
            val line = lines[index]
            if (!line.startsWith("#EXTINF", ignoreCase = true)) {
                index++
                continue
            }

            val (metadata, displayName) = splitExtinf(line)
            val attributes = parseAttributes(metadata)
            val urlEntry = findNextUrl(lines, index + 1)
            if (urlEntry == null) {
                index++
                continue
            }

            val name = displayName
                .ifBlank { attributes["tvg-name"].orEmpty() }
                .ifBlank { attributes["tvg-id"].orEmpty() }
                .trim()
            if (name.isBlank()) {
                index = urlEntry.first + 1
                continue
            }

            val channelName = attributes["tvg-id"]
                .orEmpty()
                .ifBlank { attributes["tvg-name"].orEmpty() }
                .ifBlank { name }
                .trim()
            val groupName = attributes["group-title"]
                .orEmpty()
                .ifBlank { "其他" }
                .trim()
            val logoUrl = attributes["tvg-logo"]
                .orEmpty()
                .ifBlank { attributes["logo"].orEmpty() }
                .trim()

            iptvList.add(
                IptvResponseItem(
                    name = name,
                    channelName = channelName,
                    groupName = groupName,
                    logoUrl = logoUrl,
                    url = urlEntry.second,
                )
            )

            index = urlEntry.first + 1
        }

        return IptvGroupList(iptvList.groupBy { it.groupName }.map { groupEntry ->
            IptvGroup(
                name = groupEntry.key,
                iptvList = IptvList(groupEntry.value.groupBy { it.name }.map { nameEntry ->
                    val first = nameEntry.value.first()
                    Iptv(
                        name = nameEntry.key,
                        channelName = first.channelName,
                        logoUrl = nameEntry.value.firstOrNull { it.logoUrl.isNotBlank() }?.logoUrl.orEmpty(),
                        urlList = nameEntry.value.map { it.url },
                    )
                })
            )
        })
    }

    private fun findNextUrl(lines: List<String>, startIndex: Int): Pair<Int, String>? {
        for (index in startIndex until lines.size) {
            val line = lines[index]
            if (line.isBlank()) continue
            if (line.startsWith("#EXTINF", ignoreCase = true)) return null
            if (!line.startsWith("#")) return index to line
        }
        return null
    }

    private fun splitExtinf(line: String): Pair<String, String> {
        var quote: Char? = null
        line.forEachIndexed { index, char ->
            when {
                quote != null && char == quote -> quote = null
                quote == null && (char == '"' || char == '\'') -> quote = char
                quote == null && char == ',' -> {
                    return line.substring(0, index) to line.substring(index + 1).trim()
                }
            }
        }
        return line to ""
    }

    private fun parseAttributes(text: String): Map<String, String> {
        return attributeRegex.findAll(text).associate { match ->
            val value = match.groupValues.drop(2).firstOrNull { it.isNotEmpty() }.orEmpty()
            match.groupValues[1].lowercase(Locale.ROOT) to value.trim()
        }
    }

    private data class IptvResponseItem(
        val name: String,
        val channelName: String,
        val groupName: String,
        val logoUrl: String,
        val url: String,
    )

    private companion object {
        val attributeRegex = Regex("""([A-Za-z0-9_-]+)\s*=\s*(?:"([^"]*)"|'([^']*)'|([^\s,]+))""")
    }
}
