package com.tck.av.music.clip

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.tck.av.music.audio.record.TLog
import java.io.File
import java.io.FileInputStream

/**
 * https://www.jianshu.com/p/6ce1fade1a17
 * https://www.jianshu.com/p/632dce664c3d
 *
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
    private val sampleRateInHz = 44100

    /**
     * 声道数目
     */
    private val channelConfig = AudioFormat.CHANNEL_OUT_STEREO

    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    /**
     * 换从取大小
     */
    private var minBufferSize = 0


    private var audioTrack: AudioTrack? = null
    private var audioTrackThread: Thread? = null

    fun play(pcmPath: String) {
        val pcmFile = File(pcmPath)
        if (!pcmFile.exists()) {
            return
        }
        if (pcmFile.length() <= 0) {
            return
        }

        initAudioTrack()

        val audioTrackTemp = audioTrack ?: return

        TLog.i("audioTrack init success")
        audioTrackThread = Thread {
            FileInputStream(pcmFile).use { fileInputStream ->
                try {
                    val buffer = ByteArray(minBufferSize / 2)
                    var readCount: Int
                    audioTrackTemp.play()
                    while (fileInputStream.available() > 0) {
                        readCount = fileInputStream.read(buffer)
                        TLog.i("audioTrack read readCount:$readCount")
                        if (readCount == AudioTrack.ERROR_BAD_VALUE || readCount == AudioTrack.ERROR_INVALID_OPERATION) {
                            continue
                        }
                        if (readCount > 0) {
                            audioTrackTemp.write(buffer, 0, readCount)
                        }

                    }
                } catch (e: Exception) {
                    TLog.i("play error:${e.message}")
                }
            }
        }

        audioTrackThread?.start()
    }

    private fun initAudioTrack() {
        if (audioTrack == null) {
            minBufferSize = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)
            try {
                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder().setEncoding(audioFormat).setSampleRate(sampleRateInHz)
                            .setChannelMask(channelConfig).build()
                    )
                    .setBufferSizeInBytes(minBufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            } catch (e: Exception) {
                TLog.i("initAudioTrack error:${e.message}")
            }
        }
    }

    fun pause() {
        try {
            audioTrack?.let {
                if (it.state > AudioTrack.STATE_UNINITIALIZED) {
                    it.pause()
                    TLog.i("audioTrack pause success")
                }
            }
        } catch (e: Exception) {
            TLog.i("pause audioTrack pause error:${e.message}")
        }

        try {
            audioTrackThread?.interrupt()
        } catch (e: Exception) {
            TLog.i("pause audioTrackThread interrupt error:${e.message}")
        }

    }

    fun release() {
        try {
            audioTrack?.let {
                if (it.state == AudioTrack.STATE_INITIALIZED) {
                    it.stop()
                    it.release()
                    TLog.i("audioTrack stop and release success")
                }
            }
        } catch (e: Exception) {
            TLog.i("audioTrack release error:${e.message}")
        }

        try {
            audioTrackThread?.interrupt()
        } catch (e: Exception) {
            TLog.i("release audioTrackThread interrupt error:${e.message}")
        }

        audioTrack = null
        audioTrackThread = null
    }


}