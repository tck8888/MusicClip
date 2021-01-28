package com.tck.av.music.clip

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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


    private fun audioTrack() {
        val sampleRateInHz=44100
        val audioFormat=AudioFormat.ENCODING_PCM_16BIT
        val channelMask=AudioFormat.CHANNEL_OUT_STEREO
        val minBufferSize =
            AudioTrack.getMinBufferSize(sampleRateInHz, channelMask, audioFormat)

        val player = AudioTrack.Builder().setAudioAttributes(
            AudioAttributes
                .Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        ).setAudioFormat(
            AudioFormat.Builder()
                .setEncoding(audioFormat)
                .setSampleRate(sampleRateInHz)
                .setChannelMask(channelMask)
                .build()
        )
            .setBufferSizeInBytes(minBufferSize)
            .build()

    }
}