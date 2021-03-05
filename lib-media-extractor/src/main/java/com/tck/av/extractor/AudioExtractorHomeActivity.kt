package com.tck.av.extractor

import android.content.res.Resources
import android.graphics.Typeface
import android.media.MediaExtractor
import android.media.MediaFormat
import com.tck.av.extractor.databinding.ActivityAudioExtractorHomeBinding
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.toColorInt
import com.tck.av.common.TLog
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import kotlin.math.min

class AudioExtractorHomeActivity : AppCompatActivity() {

    val testInfo =
        "{max-bitrate=134072, sample-rate=44100, track-id=1, durationUs=23359274, mime=audio/mp4a-latm, profile=2, channel-count=2, bitrate=128001, language=und, aac-profile=2, max-input-size=581, csd-0=java.nio.HeapByteBuffer[pos=0 lim=2 cap=2]}"


    private lateinit var binding: ActivityAudioExtractorHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioExtractorHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnExtractorAudio.setOnClickListener {
            extractorAudio()
        }
    }

    private fun createAudioFile(fileName: String): File {
        val fileDir = File(cacheDir, "extractor")
        if (!fileDir.exists()) {
            fileDir.mkdir()
        }
        return File(fileDir, "audio_${fileName}.pcm")
    }


    private fun setAudioInfoView(info: String) {
        binding.llAudioInfo.removeAllViews()
        val replace = info.replace("{", "")
        val replace1 = replace.replace("}", "")
        replace1.split(",").forEach {

            val key = it.substring(0, it.indexOf("=")).trim()
            val value = it.substring(it.indexOf("=") + 1).trim()

            if (key.isNotEmpty()) {
                binding.llAudioInfo.addView(createKeyValueInfoWidget(key, value))
            }
        }

        binding.llAudioInfo.setBackgroundResource(R.drawable.shape_corners_4dp_stroke_fff0f0f0_solid_1ad8d8d8)

    }

    private fun extractorAudio() {
        val mediaExtractor = MediaExtractor()
        mediaExtractor.setDataSource(assets.openFd("WeChat_20210304214737.mp4"))

        val audioTrackIndex = findAudioFormat(mediaExtractor)
        if (audioTrackIndex == -1) {
            Toast.makeText(this, "找不到音频", Toast.LENGTH_SHORT).show()
            return
        }
        val audioFormat = mediaExtractor.getTrackFormat(audioTrackIndex)

        setAudioInfoView(audioFormat.toString())

        var maxAudioBufferCount = audioFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
        maxAudioBufferCount = if (maxAudioBufferCount == 0) {
            4 * 1024
        } else {
            maxAudioBufferCount.coerceAtMost(4 * 1024)
        }
        mediaExtractor.selectTrack(audioTrackIndex)
        val audioByteBuffer = ByteBuffer.allocate(maxAudioBufferCount)
        var readCount = 0

        val fileName = "${System.currentTimeMillis()}"
        val createAudioFile = createAudioFile(fileName)

        FileOutputStream(createAudioFile).use { audioOutputStream ->
            do {
                readCount = mediaExtractor.readSampleData(audioByteBuffer, 0)
                TLog.i("音频抽取:${readCount}")
                if (readCount != -1) {
                    val byteArray = ByteArray(readCount)
                    audioByteBuffer.get(byteArray)
                    audioOutputStream.write(byteArray)
                    audioByteBuffer.clear()
                    mediaExtractor.advance()
                }
            } while (readCount != -1)
        }
        mediaExtractor.release()
        TLog.i("音频抽取完毕,文件大小：${createAudioFile.length() / 1024}kb")
    }

    private fun findAudioFormat(mediaExtractor: MediaExtractor): Int {
        return findMediaFormatByMime(mediaExtractor, MediaFormat.MIMETYPE_AUDIO_AAC)
    }

    private fun findVideoFormat(mediaExtractor: MediaExtractor): Int {
        return findMediaFormatByMime(mediaExtractor, MediaFormat.MIMETYPE_VIDEO_AVC)
    }

    private fun findMediaFormatByMime(
        mediaExtractor: MediaExtractor,
        mimeType: String
    ): Int {
        val trackCount = mediaExtractor.trackCount
        for (index in 0 until trackCount) {
            val mediaFormat = mediaExtractor.getTrackFormat(index)
            TLog.i("index:${index},mediaFormat:${mediaFormat}")
            val mime = mediaFormat.getString(MediaFormat.KEY_MIME)
            if (mime.isNullOrEmpty()) {
                continue
            }
            if (TextUtils.equals(mime, mimeType)) {
                return index
            }
        }
        return -1
    }

    /**
     * https://www.shuzhiduo.com/A/1O5EOq4rz7/
     */
    private fun addWaveFileHeader(
        dataSize: Int,
        channelCount: Int,
        sampleRate: Int,
    ) {
        val header = ByteArray(44)
        // RIFF 头表示
        header[0] = 'R'.toByte()
        header[1] = 'I'.toByte()
        header[2] = 'F'.toByte()
        header[3] = 'F'.toByte()
        //数据大小
        header[4] = 'R'.toByte()
        header[5] = 'I'.toByte()
        header[6] = 'F'.toByte()
        header[7] = 'F'.toByte()
        //wave格式
        header[8] = 'W'.toByte()
        header[9] = 'A'.toByte()
        header[10] = 'V'.toByte()
        header[11] = 'E'.toByte()
        //fmt Chunk
        header[12] = 'f'.toByte()
        header[13] = 'm'.toByte()
        header[14] = 't'.toByte()
        header[15] = ' '.toByte()

        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        //编码方式 10H为PCM编码格式
        header[20] = 1
        header[21] = 0
        //通道数 channelCount
        header[22] = if (channelCount > 2) {
            2
        } else {
            1
        }
        header[23] = 0

        //采样频率  sampleRate
        header[24] = 0
        header[25] = 1
        header[26] = 1
        header[27] = 1
        //bitrate
        header[28] = 1
        header[29] = 1
        header[30] = 1
        header[31] = 1

        header[32] = 1

        header[33] = 0

        header[34] = 0

        header[35] = 0

        header[36] = 'd'.toByte()
        header[37] = 'a'.toByte()
        header[38] = 't'.toByte()
        header[39] = 'a'.toByte()

        header[40] = 1
        header[41] = 1
        header[42] = 1
        header[43] = 1
    }

    private fun createKeyValueInfoWidget(key: String, value: String): LinearLayout {
        val linearLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(-1, -2)
        }

        val tvKey = TextView(this).apply {
            textSize = 13f
            setTextColor("#FF666666".toColorInt())
            text = key
            layoutParams = LinearLayout.LayoutParams(100f.dp2px(), -2)
        }
        linearLayout.addView(tvKey)

        val tvValue = TextView(this).apply {
            textSize = 13f
            typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            setTextColor("#FF222222".toColorInt())
            text = value
            layoutParams = LinearLayout.LayoutParams(-2, -2)
        }
        linearLayout.addView(tvValue)


        return linearLayout
    }
}

fun Float.dp2px(): Int = (Resources.getSystem().displayMetrics.density * this + 0.5f).toInt()