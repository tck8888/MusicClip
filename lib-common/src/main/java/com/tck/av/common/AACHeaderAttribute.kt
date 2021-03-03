package com.tck.av.common

import android.media.MediaCodecInfo

/**
 *
 * https://wiki.multimedia.cx/index.php?title=ADTS
 * description:

 * @date 2021/3/3 21:43

 * @author tck88
 *
 * @version v1.0.0
 *
 */
object AACHeaderAttribute {

    val sampleRates = mapOf(
        0x0 to 96000,
        0x1 to 88200,
        0x2 to 64000,
        0x3 to 48000,
        0x4 to 44100,
        0x5 to 32000,
        0x6 to 24000,
        0x7 to 22050,
        0x8 to 16000,
        0x9 to 12000,
        0xa to 11025,
        0xb to 8000,
        0xc to 7350
    )

    /**
     * 根据采样率，返回索引 占4位置
     */
    fun find_sampling_frequency_index_by_sampleRate(sampleRate: Int): Int {
        var sampling_frequency_index = -1
        sampleRates.forEach { t, u ->
            if (u == sampleRate) {
                sampling_frequency_index = t
                return@forEach
            }
        }
        return sampling_frequency_index
    }

    /**
     * {@link MediaCodecInfo.CodecProfileLevel }
     * @param    profile the MPEG-4 Audio Object Type minus 1
     * @see MediaCodecInfo.CodecProfileLevel.AACObjectLC
     * @param sampleRate 采样率
     * @param channel_configuration 通道数量
     */
     fun addADTStoPacket(
        packet: ByteArray,
        profile: Int,
        sampleRate: Int,
        channelCount: Int,
        frame_length: Int,
        MPEG4: Boolean = true
    ) {

        val sampling_frequency_index = find_sampling_frequency_index_by_sampleRate(sampleRate)

        packet[0] = 0xFF.toByte()
        packet[1] = 0xF1.toByte()
        packet[2] = if (MPEG4) {
            ((profile - 1).shl(6) + sampling_frequency_index.shl(2) + channelCount.shr(2)).toByte()
        } else {
            (profile.shl(7) + sampling_frequency_index.shl(2) + channelCount.shr(2)).toByte()
        }

        packet[3] = ((channelCount - 1).shl(7) + (frame_length).shr(11)).toByte()
        packet[4] = frame_length.and(0x7FF).shr(3).toByte()
        packet[5] = (frame_length.and(7).shl(5) + 0x1F).toByte()
        packet[6] = 0xFC.toByte()
    }
}