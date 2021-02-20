package com.tck.av.music.clip

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.os.Bundle
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tck.av.music.clip.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream
import java.text.FieldPosition

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
            val file = File(cacheDir, "music.mp3")

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

