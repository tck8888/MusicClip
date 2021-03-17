package com.tck.av.audio.rtmp

/**
 *
 * description:

 * @date 2021/3/7 14:05

 * @author tck88
 *
 * @version v1.0.0
 *
 */
class AudioLive {

    init {
        System.loadLibrary("native-lib")
    }

    external fun connect(url: String): Boolean
    external fun sendData(data: ByteArray, len: Int, tms: Long, type: Int): Boolean
}