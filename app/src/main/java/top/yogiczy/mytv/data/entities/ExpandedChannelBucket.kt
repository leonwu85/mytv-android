package top.yogiczy.mytv.data.entities

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 扩展频道：将不同直播源的精选频道固化到一个“扩展频道”分组。
 *
 * 按直播源分桶存储，每桶记录来源 URL、拉取订阅请求头，以及其中的精选频道（含地址）。
 * 这样即使原直播源被删除/切换，扩展频道仍可播放。
 */
@Immutable
@Serializable
data class ExpandedChannelBucket(
    /** 来源直播源 URL */
    val sourceUrl: String = "",
    /** 拉取订阅请求头（User-Agent 等） */
    val sourceHeaders: String = "",
    /** 播放频道请求头（User-Agent 等） */
    val channelHeaders: String = "",
    /** 精选频道（含地址与请求头） */
    val channels: List<ExpandedChannel> = emptyList(),
)

/**
 * 扩展频道条目：独立保存播放地址与请求头，删除订阅后仍可播放。
 */
@Immutable
@Serializable
data class ExpandedChannel(
    val name: String = "",
    val channelName: String = "",
    val urlList: List<String> = emptyList(),
    /** 播放该频道用的请求头（User-Agent 等） */
    val headers: String = "",
)

/**
 * 精选频道（含地址与请求头），用于扩展频道的数据来源。
 * 用于扩展频道的数据来源，也用于断网/删源后仍可播放精选。
 */
@Immutable
@Serializable
data class FavoriteChannel(
    val name: String = "",
    val channelName: String = "",
    val urlList: List<String> = emptyList(),
    val headers: String = "",
)

object ExpandedChannelBuckets {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun toJson(buckets: List<ExpandedChannelBucket>): String =
        if (buckets.isEmpty()) "" else json.encodeToString(buckets)

    fun fromJson(jsonStr: String): List<ExpandedChannelBucket> =
        if (jsonStr.isBlank()) emptyList()
        else runCatching { json.decodeFromString<List<ExpandedChannelBucket>>(jsonStr) }
            .getOrDefault(emptyList())
}

object FavoriteChannels {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun toJson(channels: List<FavoriteChannel>): String =
        if (channels.isEmpty()) "" else json.encodeToString(channels)

    fun fromJson(jsonStr: String): List<FavoriteChannel> =
        if (jsonStr.isBlank()) emptyList()
        else runCatching { json.decodeFromString<List<FavoriteChannel>>(jsonStr) }
            .getOrDefault(emptyList())
}
