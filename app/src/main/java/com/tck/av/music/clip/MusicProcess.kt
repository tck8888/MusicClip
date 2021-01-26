package com.tck.av.music.clip

import android.content.res.AssetFileDescriptor
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
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

    fun clip(path: String, startTime: Long = 0) {
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