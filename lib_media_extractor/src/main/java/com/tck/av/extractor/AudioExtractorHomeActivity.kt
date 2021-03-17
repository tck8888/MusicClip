package com.tck.av.extractor

import android.content.res.Resources
import android.graphics.Typeface
import android.media.AudioFormat
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

//https://mlog.club/article/5729222
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

    private fun bitsPerSample(format:Int):Byte{
        return when (format) {
            AudioFormat.ENCODING_PCM_16BIT -> {
                16
            }
            AudioFormat.ENCODING_PCM_8BIT -> {
                8
            }
            else -> {
                16
            }
        }
    }

    /**
     * https://www.shuzhiduo.com/A/1O5EOq4rz7/
     */
    /**
     *
     * @param totalDataLen 文件的长度
     */
    private fun addWaveFileHeader(
        totalAudioLen: Long,
        totalDataLen: Long,
        channelCount: Int,
        sampleRate: Long,
        byteRate: Int,
        bitsPerSample:Byte
    ) {
        val header = ByteArray(44)
        // RIFF 头表示
        header[0] = 'R'.toByte()
        header[1] = 'I'.toByte()
        header[2] = 'F'.toByte()
        header[3] = 'F'.toByte()
        //数据大小
        header[4] = totalDataLen.and(0xff).toByte()
        header[5] = totalDataLen.shr(8).and(0xff).toByte()
        header[6] = totalDataLen.shr(16).and(0xff).toByte()
        header[7] = totalDataLen.shr(24).and(0xff).toByte()
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

        header[16] = 16 // 4 bytes: size of 'fmt ' chunk
        header[17] = 0
        header[18] = 0
        header[19] = 0
        //编码方式 10H为PCM编码格式
        header[20] = 1
        header[21] = 0
        //通道数 channelCount
        header[22] = channelCount.toByte()
        header[23] = 0

        //采样频率  sampleRate 2字节
        header[24] = sampleRate.and(0xff).toByte()
        header[25] = sampleRate.shr(8).and(0xff).toByte()
        header[26] = sampleRate.shr(16).and(0xff).toByte()
        header[27] = sampleRate.shr(24).and(0xff).toByte()
        //bitrate  2字节
        // 波形音频数据传送速率，其值为通道数×每秒数据位数×每
        // 样本的数据位数／8。播放软件利用此值可以估计缓冲区的大小
        header[28] = byteRate.and(0xff).toByte()
        header[29] = byteRate.shr(8).and(0xff).toByte()
        header[30] = byteRate.shr(16).and(0xff).toByte()
        header[31] = byteRate.shr(24).and(0xff).toByte()

        //2字节
        //数据块的调整数（按字节算的），
        // 其值为通道数×每样本的数据位值／8。播
        // 放软件需要一次处理多个该值大小的字节数据，以便将其值用于缓冲区的调整
        header[32] = (channelCount*(bitsPerSample/8)).toByte()
        header[33] = 0

        //2
        //每样本的数据位数，表示每个声道中各个样本的数据位数。如果有多个声道，对每个声道而言，样本大小都一样
        header[34] = bitsPerSample
        header[35] = 0

        header[36] = 'd'.toByte()
        header[37] = 'a'.toByte()
        header[38] = 't'.toByte()
        header[39] = 'a'.toByte()

        header[40] = totalAudioLen.and(0xff).toByte()
        header[41] = totalAudioLen.shr(8).and(0xff).toByte()
        header[42] = totalAudioLen.shr(16).and(0xff).toByte()
        header[43] = totalAudioLen.shr(24).and(0xff).toByte()
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