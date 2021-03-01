package com.tck.av.music.audio.record

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.tck.av.common.PermissionsUtils
import com.tck.av.common.TLog
import com.tck.av.music.audio.record.databinding.ActivityAudioRecordHomeBinding
import com.tck.av.pcm.player.DefaultTaskExecutor
import com.tck.av.pcm.player.PcmPlayerController
import com.tck.av.pcm.player.PlayHandler
import java.io.File
import java.lang.ref.WeakReference

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
            binding.tvPcmFile.text = audioRecordThread?.pcmFile?.absolutePath
            audioRecordThread = null

        }
        binding.btnPlayPcm.setOnClickListener {
            startPlay()
        }
    }


    private fun startPlay() {
        val pcmPath = binding.tvPcmFile.text.toString().trim()
        val file = File(pcmPath)
        if (!file.exists()) {
            return
        }
        PcmPlayerController.instances.play(file, object : PlayHandler {
            override fun onStart() {
                binding.btnPlayPcm.text = "播放中..."
            }

            override fun onEnd() {
                binding.btnPlayPcm.text = "播放"
            }
        })

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

