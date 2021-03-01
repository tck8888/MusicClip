package com.tck.av.music.audio.record

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tck.av.common.PermissionsUtils
import com.tck.av.music.audio.record.databinding.ActivityAudioRecordAacHomeBinding
import com.tck.av.pcm.player.DefaultTaskExecutor
import java.io.File

class AudioRecordAACHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAudioRecordAacHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioRecordAacHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnStartRecord.setOnClickListener {
            if (PermissionsUtils.checkAudioPermissions(this)) {
                startRecord()
            } else {
                PermissionsUtils.requestAudioPermissions(this)
            }
        }

        binding.btnStopRecord.setOnClickListener {
            stopRecord()
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val onRequestPermissionsResult =
            PermissionsUtils.onRequestPermissionsResult(this, requestCode, grantResults)
        if (onRequestPermissionsResult) {
            startRecord()
        }
    }

    private var audioRecordAndEncodingTask: AudioRecordAndEncodingTask? = null

    private fun startRecord() {
        val aacFile = createAACFile("${System.currentTimeMillis()}.aac")
        if (audioRecordAndEncodingTask == null) {
            audioRecordAndEncodingTask =
                AudioRecordAndEncodingTask(aacFile, object : AudioRecordCallback {
                    override fun onStart() {
                        binding.btnStartRecord.text = "录制中...."
                        binding.btnStartRecord.isEnabled = false
                    }

                    override fun onEnd() {
                        binding.btnStartRecord.text = "开始录制"
                        binding.btnStartRecord.isEnabled = true
                    }

                })
            audioRecordAndEncodingTask?.startRecord()
            DefaultTaskExecutor.instances.executeOnDiskIO(audioRecordAndEncodingTask!!)
        }
    }

    private fun stopRecord() {
        audioRecordAndEncodingTask?.let {
            if (it.isRecording) {
                it.isRecording = false
                binding.tvAacFile.text =
                    audioRecordAndEncodingTask?.aacFile?.absolutePath ?: ""
            }
        }
        audioRecordAndEncodingTask = null

    }

    private fun createAACFile(fileName: String): File {
        val file = File(cacheDir, "aacPath")
        if (!file.exists()) {
            file.mkdirs()
        }
        return File(file, fileName)
    }
}