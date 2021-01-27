package com.tck.av.music.clip

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

/**
 *<p>description:</p>
 *<p>created on: 2021/1/26 9:19</p>
 * @author tck
 * @version v1.0
 *
 */
class MusicProcess {

    companion object {
        val AUDIO_PREFIX = "audio/"
        val AUDIO_MPEG = "audio/mpeg"
    }

    fun clip(
        path: String,
        pcmPath:String,
        startTime: Long = 0,
        endTime: Long = 0
    ) {
        val mediaExtractor = MediaExtractor()
        mediaExtractor.setDataSource(path)
        val audioTrack = selectTrack(mediaExtractor, AUDIO_PREFIX)
        if (audioTrack == -1) {
            showLog("找不到:$AUDIO_PREFIX 相关track")
            return
        }
        mediaExtractor.selectTrack(audioTrack)

        mediaExtractor.seekTo(startTime, MediaExtractor.SEEK_TO_CLOSEST_SYNC)

        val audioFormat = mediaExtractor.getTrackFormat(audioTrack)

        val maxBufferSize = if (audioFormat.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
            audioFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
        } else {
            100 * 1000
        }

        showLog("maxBufferSize:$maxBufferSize")
        val buffer = ByteBuffer.allocateDirect(maxBufferSize)

        val mediaCodec = MediaCodec.createDecoderByType(
            audioFormat.getString(MediaFormat.KEY_MIME) ?: AUDIO_MPEG
        )
        mediaCodec.configure(audioFormat, null, null, 0)
        val pcmFile = File(pcmPath)
        val writeChannel = FileOutputStream(pcmFile).channel
        mediaCodec.start()

        val bufferInfo = MediaCodec.BufferInfo()
        var outputBufferIndex = MediaCodec.INFO_TRY_AGAIN_LATER
        while (true) {
            val decodeInputIndex = mediaCodec.dequeueInputBuffer(100000)
            if (decodeInputIndex >= 0) {
                val sampleTimeUs = mediaExtractor.sampleTime
                if (sampleTimeUs == -1L) {
                    break
                } else if (sampleTimeUs < startTime) {
                    //丢掉 不用了
                    mediaExtractor.advance()
                    continue
                } else if (sampleTimeUs > endTime) {
                    break
                }
                bufferInfo.size = mediaExtractor.readSampleData(buffer, 0)
                bufferInfo.presentationTimeUs = sampleTimeUs
                bufferInfo.flags = mediaExtractor.sampleFlags

                val content = ByteArray(buffer.remaining())
                buffer.get(content)
                //解码
                val inputBuffer = mediaCodec.getInputBuffer(decodeInputIndex)
                inputBuffer?.put(content)
                mediaCodec.queueInputBuffer(
                    decodeInputIndex,
                    0,
                    bufferInfo.size,
                    bufferInfo.presentationTimeUs,
                    bufferInfo.flags
                )
                //释放上一帧的压缩数据
                mediaExtractor.advance()
            }
            outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 100000)
            while (outputBufferIndex >= 0) {
                val outputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex)
                mediaCodec.releaseOutputBuffer(outputBufferIndex,false)
                writeChannel.write(outputBuffer)
                outputBufferIndex=mediaCodec.dequeueOutputBuffer(bufferInfo,100000)
            }
        }
        writeChannel.close()
        mediaExtractor.release()
        mediaCodec.stop()
        mediaCodec.release()
    }


    private fun selectTrack(mediaExtractor: MediaExtractor, mimeTypePrefix: String): Int {
        val numTracks = mediaExtractor.trackCount
        for (trackIndex in 0 until numTracks) {
            val trackFormat = mediaExtractor.getTrackFormat(trackIndex)
            showLog(">>>> trackIndex is:${trackIndex}")
            showLog(">>>> trackFormat is:${trackFormat}")
            val mime = trackFormat.getString(MediaFormat.KEY_MIME)
            if (mime.isNullOrEmpty()) {
                continue
            }
            if (mime.startsWith(mimeTypePrefix)) {
                return trackIndex
            }
        }
        return -1
    }


    private fun showLog(msg: String) {
        Log.d("tck6666", msg)
    }
}