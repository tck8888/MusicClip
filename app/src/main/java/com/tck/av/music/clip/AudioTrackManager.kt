package com.tck.av.music.clip

import android.media.AudioFormat
import android.media.AudioTrack

/**https://www.jianshu.com/p/6ce1fade1a17
 *<p>description:</p>
 *<p>created on: 2021/1/28 12:57</p>
 * @author tck
 * @version v1.0
 *
 */
class AudioTrackManager private constructor() {

    companion object {
        val instances: AudioTrackManager by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            AudioTrackManager()
        }
    }

    /**
     * 采用率
     */
    val sampleRateInHz = 44100

    /**
     * 声道数目
     */
    val channelConfig = AudioFormat.CHANNEL_OUT_STEREO

    val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    /**
     * 换从取大小
     */
    private var minBufferSize = 0

    init {
        initAudioTrack()
    }

    fun initAudioTrack() {
        minBufferSize = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)
    }
}