package com.tck.av.common

import android.media.AudioRecord
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaRecorder

/**
 *
 * description:

 * @date 2021/3/7 14:46

 * @author tck88
 *
 * @version v1.0.0
 *
 */
object AudioFormatUtils {

    const val DEFAULT_MIN_BUFFER_SIZE = 4 * 1024

    fun createMediaFormat(sampleRateInHz: Int, channelCount: Int, bitRate: Int): MediaFormat {
        val mediaFormat =
            MediaFormat.createAudioFormat(
                MediaFormat.MIMETYPE_AUDIO_AAC,
                sampleRateInHz,
                channelCount
            )
        mediaFormat.setInteger(
            MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel
                .AACObjectLC
        )
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
        return mediaFormat
    }

    fun createAudioRecord(
        sampleRateInHz: Int,
        channelConfig: Int,
        audioFormat: Int
    ): Pair<Int, AudioRecord> {
        var minBufferSize = AudioRecord.getMinBufferSize(
            sampleRateInHz,
            channelConfig,
            audioFormat
        )
        if (minBufferSize == 0) {
            minBufferSize = DEFAULT_MIN_BUFFER_SIZE
        } else {
            DEFAULT_MIN_BUFFER_SIZE.coerceAtMost(minBufferSize)
        }
        return Pair(
            minBufferSize, AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRateInHz,
                channelConfig,
                audioFormat,
                minBufferSize
            )
        )
    }

}