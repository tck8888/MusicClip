package com.tck.av.music.audio.record

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tck.av.music.audio.record.databinding.ActivityAudioRecordHomeBinding
import java.io.File
import java.io.FileOutputStream

class AudioRecordHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAudioRecordHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioRecordHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnStartRecord.setOnClickListener {
            if (PermissionsUtils.checkAudioPermissions(this)) {
                startRecord()
            } else {
                PermissionsUtils.requestAudioPermissions(this)
            }
        }

        binding.btnStopRecord.setOnClickListener {
            audioRecordThread?.stopRecord()
            audioRecordThread = null
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val result = PermissionsUtils.onRequestPermissionsResult(this, requestCode, grantResults)
        if (result) {
            startRecord()
        }
    }

    private var audioRecordThread: AudioRecordThread? = null

    private fun startRecord() {
        val fileName = "${System.currentTimeMillis()}"
        if (audioRecordThread == null) {
            audioRecordThread = AudioRecordThread(createPcmFile("${fileName}.pcm"))
            audioRecordThread?.startRecord()
        }
    }

    private fun createPcmFile(fileName: String): File {
        val file = File(cacheDir, "pcmPath")
        if (!file.exists()) {
            file.mkdirs()
        }
        return File(file, fileName)
    }
}

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