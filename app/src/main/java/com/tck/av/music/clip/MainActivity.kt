package com.tck.av.music.clip

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
    }

    private fun startClip() {
        val sourceFile = File(cacheDir, "music.mp3")
        val resultFile = File(cacheDir, "out.mp3")
        if (sourceFile.exists()) {
            sourceFile.delete()
        }

        copyAssetsToCache("music.mp3",sourceFile.absolutePath)

        MusicProcess().clip(sourceFile.absolutePath)
    }

    private fun copyAssetsToCache(assetsName: String, outPath: String) {
        val assetFileDescriptor = assets.openFd(assetsName)
        val from = assetFileDescriptor.createInputStream().channel
        val to = FileOutputStream(outPath).channel
        from.transferTo(assetFileDescriptor.startOffset, assetFileDescriptor.length, to)
    }
}