package com.tck.av.extractor
import com.tck.av.extractor.databinding.ActivityAudioExtractorHomeBinding
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class AudioExtractorHomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAudioExtractorHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioExtractorHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}