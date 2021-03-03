package com.tck.av.music.clip

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tck.av.extractor.AudioExtractorHomeActivity
import com.tck.av.music.audio.record.AudioRecordAACHomeActivity
import com.tck.av.music.audio.record.AudioRecordPCMHomeActivity
import com.tck.av.music.audio.record.AudioRecordHomeActivity
import com.tck.av.music.clip.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnClipMusic.setOnClickListener {
            startClip()
        }

        binding.btnMediaPlayerPlay.setOnClickListener {
            //val file = File(cacheDir, "tempPcmFile.pcm")
            val file = File(cacheDir, "aacPath/1614609321405.aac")

            if (!file.exists()) {
                return@setOnClickListener
            }

            if (file.length() <= 0) {
                return@setOnClickListener
            }

            MediaPlayerHelper.instances.play(file.absolutePath)
        }

        binding.btnMediaPlayerPause.setOnClickListener {
            MediaPlayerHelper.instances.pause()
        }

        binding.btnMediaPlayerRelease.setOnClickListener {
            MediaPlayerHelper.instances.release()
        }

        binding.btnAudioTrackPlay.setOnClickListener {

            val file = File(cacheDir, "tempPcmFile.pcm")

            if (!file.exists()) {
                return@setOnClickListener
            }

            if (file.length() <= 0) {
                return@setOnClickListener
            }

            AudioTrackManager.instances.play(file.absolutePath)
        }

        binding.btnAudioStart.setOnClickListener {
            startActivity(Intent(this, AudioRecordPCMHomeActivity::class.java))
        }

        binding.btnAudioStartAac.setOnClickListener {
            startActivity(Intent(this, AudioRecordAACHomeActivity::class.java))
        }

        binding.btnAudioRecord.setOnClickListener {
            startActivity(Intent(this, AudioRecordHomeActivity::class.java))
        }

        binding.btnAudioRecord.setOnClickListener {
            startActivity(Intent(this, AudioExtractorHomeActivity::class.java))
        }
    }

    private fun startClip() {
        val sourceFile = File(cacheDir, "music.mp3")
        if (sourceFile.exists()) {
            sourceFile.delete()
        }
        val tempPcmFile = File(cacheDir, "tempPcmFile.pcm")
        if (tempPcmFile.exists()) {
            tempPcmFile.delete()
        }

        copyAssetsToCache("music.mp3", sourceFile.absolutePath)

        MusicProcess().clip(
            sourceFile.absolutePath, tempPcmFile.absolutePath,
            10 * 1000 * 1000,
            15 * 1000 * 1000
        )
    }

    private fun copyAssetsToCache(assetsName: String, outPath: String) {
        val assetFileDescriptor = assets.openFd(assetsName)
        val from = assetFileDescriptor.createInputStream().channel
        val to = FileOutputStream(outPath).channel
        from.transferTo(assetFileDescriptor.startOffset, assetFileDescriptor.length, to)
    }

}

