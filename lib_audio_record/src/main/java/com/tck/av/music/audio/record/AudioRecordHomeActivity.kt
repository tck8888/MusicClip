package com.tck.av.music.audio.record

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tck.av.common.AACHeaderAttribute
import com.tck.av.common.TLog
import com.tck.av.music.audio.record.databinding.ActivityAudioRecordAacHomeBinding
import com.tck.av.music.audio.record.databinding.ActivityAudioRecordHomeBinding

class AudioRecordHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAudioRecordHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioRecordHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnStartRecord.setOnClickListener {
            testCreateAudioRecord()
        }
    }


    fun tes2(){
        val sampleRate=44100
        val sampling_frequency_index =
            AACHeaderAttribute.find_sampling_frequency_index_by_sampleRate(sampleRate)
        TLog.i("sampleRate:${sampleRate},sampling_frequency_index:${sampling_frequency_index}")
    }


    val channelCount = 2
    val channelConfig = AudioFormat.CHANNEL_IN_STEREO
    val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private fun testCreateAudioRecord() {
        AACHeaderAttribute.sampleRates.forEach { t, sampleRateInHz ->
            val minBufferSize =
                AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)
            val audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRateInHz,
                channelConfig,
                audioFormat,
                minBufferSize
            )
            if (audioRecord.state == AudioRecord.STATE_INITIALIZED) {
                TLog.i("sampleRateInHz:${sampleRateInHz},minBufferSize:${minBufferSize}")
            }
        }
    }
   //sampleRateInHz:96000,minBufferSize:15360
   //sampleRateInHz:88200,minBufferSize:14208
   //sampleRateInHz:64000,minBufferSize:10240
   //sampleRateInHz:48000,minBufferSize:7680
   //sampleRateInHz:44100,minBufferSize:7168
   //sampleRateInHz:32000,minBufferSize:5120
   //sampleRateInHz:24000,minBufferSize:3840
   //sampleRateInHz:22050,minBufferSize:3584
   //sampleRateInHz:16000,minBufferSize:2560
   //sampleRateInHz:12000,minBufferSize:1920
   //sampleRateInHz:11025,minBufferSize:1792
   //sampleRateInHz:8000,minBufferSize:1280
   //sampleRateInHz:7350,minBufferSize:1280

}