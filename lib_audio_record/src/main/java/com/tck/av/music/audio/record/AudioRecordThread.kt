package com.tck.av.music.audio.record

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.tck.av.common.TLog
import java.io.File
import java.io.FileOutputStream

/**
 *<p>description:</p>
 *<p>created on: 2021/3/1 13:55</p>
 * @author tck
 * @version v1.0
 *
 */
class AudioRecordThread(val pcmFile: File) : Thread() {

    private val TAG = "AudioRecordThread >>> "
    private val sampleRateInHz = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_STEREO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private var minBufferSize = 0
    private var audioRecord: AudioRecord? = null

    var isRecording: Boolean = false

    private fun createAudioRecord(): AudioRecord {
        return AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRateInHz,
            channelConfig,
            audioFormat,
            minBufferSize
        )
    }

    fun startRecord() {
        minBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)
        if (minBufferSize == 0) {
            minBufferSize = 8 * 1024
        }
        audioRecord = createAudioRecord()

        start()
    }

    fun stopRecord() {
        isRecording = false
    }

    override fun run() {
        super.run()
        val audioRecordTemp = audioRecord ?: return
        isRecording = true
        audioRecordTemp.startRecording()
        val buffer = ByteArray(minBufferSize)
        FileOutputStream(pcmFile).use { fileOutputStream ->
            while (isRecording) {
                val readStatus = audioRecordTemp.read(buffer, 0, minBufferSize)
                TLog.i("$TAG readStatus: $readStatus")
                if (readStatus >= 0) {
                    fileOutputStream.write(buffer, 0, minBufferSize)
                }
            }
        }
        isRecording = false
        audioRecordTemp.stop()
        audioRecordTemp.release()
        audioRecord = null
        TLog.i("$TAG recording end")

    }
}