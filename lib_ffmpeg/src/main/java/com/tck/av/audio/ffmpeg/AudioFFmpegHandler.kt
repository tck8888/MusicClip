package com.tck.av.audio.ffmpeg

/**
 *<p>description:</p>
 *<p>created on: 2021/3/17 13:17</p>
 * @author tck
 * @version v1.0
 *
 */
class AudioFFmpegHandler {
    companion object {
        init {
            System.loadLibrary("myffmpeg")
        }
    }
}