package com.tck.av.music.clip

import android.media.MediaPlayer
import android.view.SurfaceHolder

/**
 *<p>description:</p>
 *<p>created on: 2021/2/20 16:09</p>
 * @author tck
 * @version v1.0
 *
 */
class MediaPlayerHelper private constructor() : MediaPlayer.OnErrorListener,
    MediaPlayer.OnCompletionListener,
    MediaPlayer.OnPreparedListener {

    private var mediaPlayer: MediaPlayer? = null
    private var hasPrepared: Boolean = false

    companion object {
        val instances: MediaPlayerHelper by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            MediaPlayerHelper()
        }
    }

    private fun initIfNecessary() {
        if (mediaPlayer == null) {
            val mediaPlayerTemp = MediaPlayer()
            mediaPlayerTemp.setOnErrorListener(this)
            mediaPlayerTemp.setOnCompletionListener(this)
            mediaPlayerTemp.setOnPreparedListener(this)
            mediaPlayer = mediaPlayerTemp
        }
    }

    fun play(pcmPath: String) {
        try {
            hasPrepared = false
            initIfNecessary()
            mediaPlayer?.reset()
            mediaPlayer?.setDataSource(pcmPath)
            mediaPlayer?.prepareAsync()
        } catch (e: Exception) {
            TLog.d("play $pcmPath error:${e.message}")
        }
    }

    fun start() {
        val mediaPlayerTemp = mediaPlayer
        if (mediaPlayerTemp != null && hasPrepared) {
            mediaPlayerTemp.start()
        }
    }

    fun pause() {
        val mediaPlayerTemp = mediaPlayer
        if (mediaPlayerTemp != null && hasPrepared) {
            mediaPlayerTemp.pause()
        }
    }

    fun seekTo(position: Int) {
        val mediaPlayerTemp = mediaPlayer
        if (mediaPlayerTemp != null && hasPrepared) {
            mediaPlayerTemp.seekTo(position)
        }
    }

    fun setDisplay(surfaceHolder: SurfaceHolder) {
        mediaPlayer?.apply {
            setDisplay(surfaceHolder)
        }
    }

    fun release() {
        hasPrepared = false
        mediaPlayer?.apply {
            stop()
            release()
        }
        mediaPlayer = null
    }

    override fun onPrepared(mp: MediaPlayer?) {
        hasPrepared = true
        start()
        TLog.d("media prepared success,start play...")
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        hasPrepared = false
        TLog.d("play error what:${what},extra:${extra}")
        return false
    }

    override fun onCompletion(mp: MediaPlayer?) {
        hasPrepared = false
        TLog.d("play completion")
    }


}