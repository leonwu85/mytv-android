package top.yogiczy.mytv.ui.screens.leanback.video.player

import android.net.Uri
import android.util.SparseArray
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.util.ParsableByteArray
import androidx.media3.common.util.TimestampAdjuster
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.analytics.PlayerId
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.extractor.Extractor
import androidx.media3.extractor.ExtractorInput
import androidx.media3.extractor.ExtractorOutput
import androidx.media3.extractor.ExtractorsFactory
import androidx.media3.extractor.TrackOutput
import androidx.media3.extractor.text.SubtitleParser
import androidx.media3.extractor.ts.DefaultTsPayloadReaderFactory
import androidx.media3.extractor.ts.ElementaryStreamReader
import androidx.media3.extractor.ts.PesReader
import androidx.media3.extractor.ts.TsExtractor
import androidx.media3.extractor.ts.TsPayloadReader
import androidx.media3.exoplayer.hls.BundledHlsMediaChunkExtractor
import androidx.media3.exoplayer.hls.DefaultHlsExtractorFactory
import androidx.media3.exoplayer.hls.HlsExtractorFactory
import androidx.media3.exoplayer.hls.HlsMediaChunkExtractor
import java.io.EOFException

internal const val MIME_TYPE_AUDIO_AV3A = "audio/av3a"

private const val TS_STREAM_TYPE_PRIVATE_DATA = 0x06
private const val TS_STREAM_TYPE_PRIVATE_SECTION = 0x05
private const val TS_STREAM_TYPE_AV3A = 0xD5
private const val TS_PMT_DESC_REGISTRATION = 0x05
private val AV3A_REGISTRATION = byteArrayOf('a'.code.toByte(), 'v'.code.toByte(), '3'.code.toByte(), 'a'.code.toByte())

@OptIn(UnstableApi::class)
internal class Av3aExtractorsFactory : ExtractorsFactory {
    private val defaultFactory = DefaultExtractorsFactory()

    override fun createExtractors(): Array<Extractor> = createExtractors(Uri.EMPTY, emptyMap())

    override fun createExtractors(
        uri: Uri,
        responseHeaders: Map<String, List<String>>,
    ): Array<Extractor> {
        return arrayOf<Extractor>(newAv3aTsExtractor(TsExtractor.MODE_SINGLE_PMT, TimestampAdjuster(0))) +
                defaultFactory.createExtractors(uri, responseHeaders)
    }
}

@OptIn(UnstableApi::class)
internal class Av3aHlsExtractorFactory : HlsExtractorFactory {
    private val defaultFactory = DefaultHlsExtractorFactory()

    override fun createExtractor(
        uri: Uri,
        format: Format,
        muxedCaptionFormats: List<Format>?,
        timestampAdjuster: TimestampAdjuster,
        responseHeaders: Map<String, List<String>>,
        sniffingExtractorInput: ExtractorInput,
        playerId: PlayerId,
    ): HlsMediaChunkExtractor {
        val tsExtractor = newAv3aTsExtractor(
            mode = TsExtractor.MODE_HLS,
            timestampAdjuster = timestampAdjuster,
            payloadReaderFactoryFlags = DefaultTsPayloadReaderFactory.FLAG_IGNORE_SPLICE_INFO_STREAM,
        )

        sniffingExtractorInput.resetPeekPosition()
        if (sniffQuietly(tsExtractor, sniffingExtractorInput) ||
            shouldUseTransportStream(uri, format, responseHeaders)
        ) {
            return BundledHlsMediaChunkExtractor(tsExtractor, format, timestampAdjuster)
        }

        sniffingExtractorInput.resetPeekPosition()
        return defaultFactory.createExtractor(
            uri,
            format,
            muxedCaptionFormats,
            timestampAdjuster,
            responseHeaders,
            sniffingExtractorInput,
            playerId,
        )
    }

    override fun setSubtitleParserFactory(
        subtitleParserFactory: SubtitleParser.Factory,
    ): HlsExtractorFactory {
        defaultFactory.setSubtitleParserFactory(subtitleParserFactory)
        return this
    }

    override fun experimentalParseSubtitlesDuringExtraction(
        parseSubtitlesDuringExtraction: Boolean,
    ): HlsExtractorFactory {
        defaultFactory.experimentalParseSubtitlesDuringExtraction(parseSubtitlesDuringExtraction)
        return this
    }

    override fun getOutputTextFormat(sourceFormat: Format): Format {
        return defaultFactory.getOutputTextFormat(sourceFormat)
    }
}

@OptIn(UnstableApi::class)
private fun newAv3aTsExtractor(
    mode: Int,
    timestampAdjuster: TimestampAdjuster,
    payloadReaderFactoryFlags: Int = 0,
): TsExtractor {
    return TsExtractor(
        mode,
        /* extractorFlags= */ 0,
        SubtitleParser.Factory.UNSUPPORTED,
        timestampAdjuster,
        Av3aTsPayloadReaderFactory(
            DefaultTsPayloadReaderFactory(payloadReaderFactoryFlags),
        ),
        TsExtractor.DEFAULT_TIMESTAMP_SEARCH_BYTES,
    )
}

@OptIn(UnstableApi::class)
private class Av3aTsPayloadReaderFactory(
    private val defaultFactory: DefaultTsPayloadReaderFactory,
) : TsPayloadReader.Factory {
    override fun createInitialPayloadReaders(): SparseArray<TsPayloadReader> {
        return defaultFactory.createInitialPayloadReaders()
    }

    override fun createPayloadReader(
        streamType: Int,
        esInfo: TsPayloadReader.EsInfo,
    ): TsPayloadReader? {
        if (isAv3aStream(streamType, esInfo)) {
            return PesReader(Av3aReader(esInfo.language, esInfo.getRoleFlags()))
        }
        return defaultFactory.createPayloadReader(streamType, esInfo)
    }
}

@OptIn(UnstableApi::class)
private class Av3aReader(
    private val language: String?,
    private val roleFlags: Int,
) : ElementaryStreamReader {
    private lateinit var output: TrackOutput
    private var sampleTimeUs = C.TIME_UNSET
    private var sampleSize = 0

    override fun seek() {
        sampleTimeUs = C.TIME_UNSET
        sampleSize = 0
    }

    override fun createTracks(
        extractorOutput: ExtractorOutput,
        idGenerator: TsPayloadReader.TrackIdGenerator,
    ) {
        idGenerator.generateNewId()
        output = extractorOutput.track(idGenerator.getTrackId(), C.TRACK_TYPE_AUDIO)
        output.format(
            Format.Builder()
                .setId(idGenerator.getFormatId())
                .setSampleMimeType(MIME_TYPE_AUDIO_AV3A)
                .setLanguage(language)
                .setRoleFlags(roleFlags)
                .build(),
        )
    }

    override fun packetStarted(pesTimeUs: Long, flags: Int) {
        sampleTimeUs = pesTimeUs
        sampleSize = 0
    }

    override fun consume(data: ParsableByteArray) {
        val bytesLeft = data.bytesLeft()
        output.sampleData(data, bytesLeft)
        sampleSize += bytesLeft
    }

    override fun packetFinished(isEndOfInput: Boolean) {
        if (sampleSize > 0 && sampleTimeUs != C.TIME_UNSET) {
            output.sampleMetadata(
                sampleTimeUs,
                C.BUFFER_FLAG_KEY_FRAME,
                sampleSize,
                /* offset= */ 0,
                /* cryptoData= */ null,
            )
        }
        sampleSize = 0
    }
}

private fun isAv3aStream(streamType: Int, esInfo: TsPayloadReader.EsInfo): Boolean {
    return streamType == TS_STREAM_TYPE_AV3A ||
            ((streamType == TS_STREAM_TYPE_PRIVATE_DATA ||
                    streamType == TS_STREAM_TYPE_PRIVATE_SECTION) &&
                    hasAv3aRegistrationDescriptor(esInfo.descriptorBytes))
}

private fun hasAv3aRegistrationDescriptor(descriptorBytes: ByteArray): Boolean {
    var offset = 0
    while (offset + 2 <= descriptorBytes.size) {
        val tag = descriptorBytes[offset].toInt() and 0xFF
        val length = descriptorBytes[offset + 1].toInt() and 0xFF
        val payloadOffset = offset + 2
        val nextOffset = payloadOffset + length
        if (nextOffset > descriptorBytes.size) {
            return false
        }
        if (tag == TS_PMT_DESC_REGISTRATION &&
            length >= AV3A_REGISTRATION.size &&
            descriptorBytes.matchesAt(payloadOffset, AV3A_REGISTRATION)
        ) {
            return true
        }
        offset = nextOffset
    }
    return false
}

private fun ByteArray.matchesAt(offset: Int, expected: ByteArray): Boolean {
    if (offset + expected.size > size) {
        return false
    }
    for (index in expected.indices) {
        if (this[offset + index] != expected[index]) {
            return false
        }
    }
    return true
}

private fun shouldUseTransportStream(
    uri: Uri,
    format: Format,
    responseHeaders: Map<String, List<String>>,
): Boolean {
    val path = uri.path.orEmpty()
    return path.endsWith(".ts", ignoreCase = true) ||
            path.endsWith(".m2ts", ignoreCase = true) ||
            format.sampleMimeType == "video/mp2t" ||
            format.containerMimeType == "video/mp2t" ||
            responseHeaders.values.flatten().any { value ->
                value.contains("video/mp2t", ignoreCase = true)
            }
}

private fun sniffQuietly(extractor: Extractor, input: ExtractorInput): Boolean {
    return try {
        extractor.sniff(input)
    } catch (_: EOFException) {
        false
    } finally {
        input.resetPeekPosition()
    }
}
