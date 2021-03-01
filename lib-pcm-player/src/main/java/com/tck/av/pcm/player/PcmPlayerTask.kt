package com.tck.av.pcm.player

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.tck.av.common.TLog
import java.io.File
import java.io.FileInputStream
import java.lang.ref.WeakReference

/**
 *<p>description:</p>
 *<p>created on: 2021/3/1 15:06</p>
 * @author tck
 * @version v1.0
 *
 */
class PcmPlayerTask(
    private val pcmFile: File,
    var playHandler: WeakReference<PlayHandler>?
) : Runnable {

    private val TAG = "PcmPlayerTask >>> "
    private val sampleRateInHz = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_STEREO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private var minBufferSize = 0
    private val defaultBufferSizeSize = 8 * 1024

    private var audioTrack: AudioTrack? = null
    var isPlaying = false

    init {
        minBufferSize = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)
        if (minBufferSize == 0) {
            minBufferSize = defaultBufferSizeSize
        }
        audioTrack = createAudioTrack(minBufferSize)
    }

    private fun createAudioTrack(bufferSizeInBytes: Int): AudioTrack {
        return AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
            )
            .setAudioFormat(
                AudioFormat.Builder().setEncoding(audioFormat).setSampleRate(sampleRateInHz)
                    .setChannelMask(channelConfig).build()
            )
            .setBufferSizeInBytes(bufferSizeInBytes)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
    }

    override fun run() {
        val audioTrackTemp = audioTrack ?: return
        isPlaying = true
        audioTrackTemp.play()

        DefaultTaskExecutor.instances.executeOnMainThread {
            playHandler?.get()?.onStart()
        }

        FileInputStream(pcmFile).use { fileInputStream ->
            val buffer = ByteArray(minBufferSize)
            var readCount: Int
            while (isPlaying && fileInputStream.available() > 0) {
                readCount = fileInputStream.read(buffer)
                TLog.i("$TAG readCount:$readCount")
                if (readCount > 0) {
                    audioTrackTemp.write(buffer, 0, readCount)
                }
            }
        }
        DefaultTaskExecutor.instances.executeOnMainThread {
            playHandler?.get()?.onEnd()
        }

        release()

        TLog.i("$TAG play end")


    }

    fun release() {
        isPlaying = false
        audioTrack?.pause()
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
        playHandler?.clear()
        playHandler = null
    }
}