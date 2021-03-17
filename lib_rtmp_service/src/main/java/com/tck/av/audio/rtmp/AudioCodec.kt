package com.tck.av.audio.rtmp

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaCodec
import android.media.MediaFormat
import com.tck.av.common.AudioFormatUtils
import com.tck.av.common.TLog

/**
 *
 * description:

 * @date 2021/3/7 14:41

 * @author tck88
 *
 * @version v1.0.0
 *
 */
class AudioCodec : Runnable {
    private val sampleRateInHz = 44100
    private val channelCount = 2
    private val bitRate = 48000
    private val channelConfig = AudioFormat.CHANNEL_IN_STEREO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private var mediaCodec: MediaCodec? = null
    private var audioRecord: AudioRecord? = null
    private var minBufferSize: Int = AudioFormatUtils.DEFAULT_MIN_BUFFER_SIZE

    private var isRecoding = false
    private var startTime = 0L

    fun initCodecAndAudioRecord() {

        val createAudioRecord =
            AudioFormatUtils.createAudioRecord(sampleRateInHz, channelConfig, audioFormat)

        minBufferSize = createAudioRecord.first
        audioRecord = createAudioRecord.second

        try {
            val auAudioFormat =
                AudioFormatUtils.createMediaFormat(sampleRateInHz, channelCount, bitRate)
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
            mediaCodec?.configure(auAudioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        } catch (e: Exception) {
            TLog.e("create mediaCodec error:${e.message}")
        }
    }

    override fun run() {
        val tempMediaCodec = mediaCodec ?: return
        val tempAudioRecord = audioRecord ?: return
        tempMediaCodec.start()
        tempAudioRecord.startRecording()
        isRecoding = true
        val bufferInfo = MediaCodec.BufferInfo()
        val buffer = ByteArray(minBufferSize)
        while (isRecoding) {
            val len = tempAudioRecord.read(buffer, 0, buffer.size)
            if (len <= 0) {
                continue
            }
            var index = tempMediaCodec.dequeueInputBuffer(0)
            if (index >= 0) {
                val inputBuffer = tempMediaCodec.getInputBuffer(index)
                inputBuffer?.let {
                    it.clear()
                    it.put(buffer, 0, len)
                    tempMediaCodec.queueInputBuffer(
                        index,
                        0,
                        len,
                        System.nanoTime() / 1000,
                        0
                    )
                }

                index = tempMediaCodec.dequeueOutputBuffer(bufferInfo, 0)
                while (index >= 0 && isRecoding) {
                    val outputBuffer = tempMediaCodec.getOutputBuffer(index)
                    outputBuffer?.let {
                        val outData = ByteArray(bufferInfo.size)
                        it.get(outData)
                        if (startTime == 0L) {
                            startTime = bufferInfo.presentationTimeUs / 1000
                        }
                    }
                    tempMediaCodec.releaseOutputBuffer(index, false)
                    index = tempMediaCodec.dequeueOutputBuffer(bufferInfo, 0)
                }
            }
        }

        tempAudioRecord.stop()
        tempAudioRecord.release()
        audioRecord = null
        tempMediaCodec.stop()
        tempMediaCodec.release()
        mediaCodec = null
        startTime = 0L
        isRecoding = false
    }
}