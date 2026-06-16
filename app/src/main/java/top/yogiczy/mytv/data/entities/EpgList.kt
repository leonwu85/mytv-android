package top.yogiczy.mytv.data.entities

import androidx.compose.runtime.Immutable
import top.yogiczy.mytv.data.entities.Epg.Companion.currentProgrammes
import java.util.Locale

@Immutable
data class EpgList(
    val value: List<Epg> = emptyList(),
) : List<Epg> by value {
    companion object {
        /**
         * 当前节目/下一个节目
         */
        fun EpgList.currentProgrammes(iptv: Iptv): EpgProgrammeCurrent? {
            val channelName = iptv.channelName
            if (channelName.isBlank()) return null

            return firstOrNull { it.channel == channelName }?.currentProgrammes()
                ?: firstOrNull {
                    it.channel.normalizedChannelKey() == channelName.normalizedChannelKey()
                }?.currentProgrammes()
        }

        fun String.normalizedChannelKey(): String {
            return trim()
                .lowercase(Locale.ROOT)
                .replace(Regex("""[\s_\-·.]+"""), "")
        }
    }
}
