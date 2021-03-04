package com.tck.av.extractor

import android.media.MediaExtractor
import android.media.MediaFormat
import com.tck.av.extractor.databinding.ActivityAudioExtractorHomeBinding
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.tck.av.common.TLog
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import kotlin.math.min

class AudioExtractorHomeActivity : AppCompatActivity() {
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

    private fun extractorAudio() {
        val mediaExtractor = MediaExtractor()
        mediaExtractor.setDataSource(assets.openFd("WeChat_20210304214737.mp4"))

        val audioTrackIndex = findAudioFormat(mediaExtractor)
        if (audioTrackIndex == -1) {
            Toast.makeText(this, "找不到音频", Toast.LENGTH_SHORT).show()
            return
        }
        val audioFormat = mediaExtractor.getTrackFormat(audioTrackIndex)

        var maxAudioBufferCount = audioFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
        maxAudioBufferCount = if (maxAudioBufferCount == 0) {
            4 * 1024
        } else {
            maxAudioBufferCount.coerceAtMost(4 * 1024)
        }
        mediaExtractor.selectTrack(audioTrackIndex)
        val audioByteBuffer = ByteBuffer.allocate(maxAudioBufferCount)
        var readCount = 0

        var fileName = "${System.currentTimeMillis()}"

        FileOutputStream(createAudioFile(fileName)).use { audioOutputStream ->
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
        TLog.i("音频抽取完毕")
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
}