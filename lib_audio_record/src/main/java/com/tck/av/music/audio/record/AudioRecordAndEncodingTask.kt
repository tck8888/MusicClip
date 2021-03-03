package com.tck.av.music.audio.record

import android.media.*
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
    private val channelConfig = AudioFormat.CHANNEL_IN_STEREO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private var mediaCodec: MediaCodec? = null
    private var audioRecord: AudioRecord? = null
    private var minBufferSize = 0
    var isRecording: Boolean = false

    companion object{
        const val MAX_INPUT_SIZE=4096
    }

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
                44100
            )
            setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, MAX_INPUT_SIZE)
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
            minBufferSize = MAX_INPUT_SIZE
        } else {
            minBufferSize = (MAX_INPUT_SIZE).coerceAtMost(minBufferSize)
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
                        addADTStoPacket(outByteBuffer, perpcmsize)
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

    private fun addADTStoPacket(packet: ByteArray, frame_length: Int) {
        //MediaCodecInfo.CodecProfileLevel.AACObjectLC
        val profile = 2
        //44100
        val sampling_frequency_index = 4
        //2个声道
        val channel_configuration = 2

        packet[0] = 0xFF.toByte()
        packet[1] = 0xF1.toByte()
        packet[2] =
            ((profile - 1).shl(6) + sampling_frequency_index.shl(2) + channel_configuration.shr(2)).toByte()
        packet[3] = ((channel_configuration - 1).shl(7) + (frame_length).shr(11)).toByte()
        packet[4] = frame_length.and(0x7FF).shr(3).toByte()
        packet[5] = (frame_length.and(7).shl(5) + 0x1F).toByte()
        packet[6] = 0xFC.toByte()

    }
}