package com.tck.av.music.audio.record

import android.media.*
import android.widget.LinearLayout
import com.tck.av.common.AACHeaderAttribute
import com.tck.av.common.TLog
import com.tck.av.pcm.player.DefaultTaskExecutor
import java.io.File
import java.io.FileOutputStream


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
    private val profile = MediaCodecInfo.CodecProfileLevel.AACObjectLC
    private val channelConfig = AudioFormat.CHANNEL_IN_STEREO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private var mediaCodec: MediaCodec? = null
    private var audioRecord: AudioRecord? = null
    private var minBufferSize = 0
    var isRecording: Boolean = false

    companion object{
        const val MAX_INPUT_SIZE=8*1024
    }

    private fun createMediaCodec(sampleRate:Int,bufferSizeInBytes: Int): MediaCodec {
        val mediaFormat = MediaFormat.createAudioFormat(
            MediaFormat.MIMETYPE_AUDIO_AAC,
            sampleRateInHz,
            channelCount
        ).apply {
            setInteger(
                MediaFormat.KEY_PROFILE,
                profile
            )
            setInteger(
                MediaFormat.KEY_BIT_RATE,
                sampleRate
            )
            setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, bufferSizeInBytes)
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
        minBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)
        minBufferSize = if (minBufferSize == 0) {
            MAX_INPUT_SIZE
        } else {
            (MAX_INPUT_SIZE).coerceAtMost(minBufferSize)
        }

        mediaCodec = createMediaCodec(sampleRateInHz,minBufferSize)

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
        val info = MediaCodec.BufferInfo()
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

                index = mediaCodecTemp.dequeueOutputBuffer(info, 10)
                while (index >= 0 && isRecording) {
                    val byteBuffer = mediaCodecTemp.getOutputBuffer(index)
                    byteBuffer?.let {
                        val perpcmsize = info.size + 7
                        val outByteBuffer = ByteArray(perpcmsize)
                        AACHeaderAttribute.addADTStoPacket(
                            outByteBuffer,
                            profile,
                            sampleRateInHz,
                            channelCount,
                            perpcmsize,
                        )
                        it.get(outByteBuffer, 7, info.size)
                        fileOutputStream.write(outByteBuffer)
                    }
                    mediaCodecTemp.releaseOutputBuffer(index, false)
                    index = mediaCodecTemp.dequeueOutputBuffer(info, 10)
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