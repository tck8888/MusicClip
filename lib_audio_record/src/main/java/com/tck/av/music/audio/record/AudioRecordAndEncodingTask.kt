package com.tck.av.music.audio.record

import android.media.*
import com.tck.av.common.TLog
import com.tck.av.pcm.player.DefaultTaskExecutor
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference
import java.nio.channels.FileChannel

/**
 *<p>description:</p>
 *<p>created on: 2021/3/1 19:16</p>
 * @author tck
 * @version v1.0
 *
 */
class AudioRecordAndEncodingTask(
     val aacFile: File,
    var callback: AudioRecordCallback? = null
) : Runnable {

    private val TAG = "AudioRecordAndEncodingTask >>> "
    private val sampleRateInHz = 44100
    private val channelCount = 2
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private var mediaCodec: MediaCodec? = null
    private var audioRecord: AudioRecord? = null
    private var minBufferSize = 0
    var isRecording: Boolean = false

    private fun createMediaCodec(): MediaCodec {
        val mediaFormat = MediaFormat.createAudioFormat(
            MediaFormat.MIMETYPE_AUDIO_AAC,
            sampleRateInHz,
            channelCount
        ).apply {
            setInteger(
                MediaFormat.KEY_PROFILE,
                MediaCodecInfo.CodecProfileLevel.AACObjectLC
            )
            setInteger(
                MediaFormat.KEY_BIT_RATE,
                64000
            )
        }
        val mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        return mediaCodec
    }

    private fun createAudioRecord(bufferSizeInBytes: Int): AudioRecord {
        return AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRateInHz,
            channelConfig,
            audioFormat,
            bufferSizeInBytes
        )
    }

    fun startRecord() {
        mediaCodec = createMediaCodec()
        minBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)
        if (minBufferSize == 0) {
            minBufferSize = 8 * 1024
        }
        audioRecord = createAudioRecord(minBufferSize)
    }

    override fun run() {

        val mediaCodecTemp = mediaCodec ?: return
        val audioRecordTemp = audioRecord ?: return
        callback?.let {
            DefaultTaskExecutor.instances.executeOnMainThread {
                callback?.onStart()
            }
        }

        isRecording = true
        audioRecordTemp.startRecording()
        mediaCodecTemp.start()
        val audioData = ByteArray(minBufferSize)
        val bufferInfo = MediaCodec.BufferInfo()
        FileOutputStream(aacFile).use { fileOutputStream ->
            while (isRecording) {
                val readCount = audioRecordTemp.read(audioData, 0, audioData.size)
                TLog.i("$TAG readCount:${readCount}")
                if (readCount <= 0) {
                    continue
                }
                var index = mediaCodecTemp.dequeueInputBuffer(10)
                if (index >= 0) {
                    val inputBuffer = mediaCodecTemp.getInputBuffer(index)
                    inputBuffer?.apply {
                        clear()
                        put(audioData, 0, readCount)
                    }
                    mediaCodecTemp.queueInputBuffer(
                        index,
                        0,
                        readCount,
                        System.nanoTime() / 1000,
                        0
                    )
                }

                index = mediaCodecTemp.dequeueOutputBuffer(bufferInfo, 10)
                while (index >= 0 && isRecording) {
                    val outputBuffer = mediaCodecTemp.getOutputBuffer(index)
                      val data = ByteArray(bufferInfo.size)
                     outputBuffer?.get(data)
                    fileOutputStream.write(data)
                    mediaCodecTemp.releaseOutputBuffer(index, false)
                    index = mediaCodecTemp.dequeueOutputBuffer(bufferInfo, 10)
                }
            }
        }
        isRecording = false

        audioRecordTemp.stop()
        audioRecordTemp.release()
        audioRecord = null

        mediaCodecTemp.stop()
        mediaCodecTemp.release()
        mediaCodec = null

        TLog.i("$TAG recording end ")

        callback?.let {
            DefaultTaskExecutor.instances.executeOnMainThread {
                callback?.onEnd()
            }
        }
    }
}