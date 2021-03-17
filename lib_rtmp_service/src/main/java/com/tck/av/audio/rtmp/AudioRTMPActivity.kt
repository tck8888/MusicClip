package com.tck.av.audio.rtmp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tck.av.audio.rtmp.databinding.ActivityAudioRtmpBinding

class AudioRTMPActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAudioRtmpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityAudioRtmpBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}