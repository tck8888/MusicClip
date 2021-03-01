package com.tck.av.pcm.player


import java.io.File
import java.lang.ref.WeakReference


/**
 *<p>description:</p>
 *<p>created on: 2021/3/1 13:52</p>
 * @author tck
 * @version v1.0
 *
 */
class PcmPlayerController private constructor() {
    companion object {
        val instances: PcmPlayerController by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { PcmPlayerController() }
    }

    private val TAG = "PcmPlayerController >>> "


    private var pcmPlayerTask: PcmPlayerTask? = null


    fun play(pcmFile: File, callback: PlayHandler?) {
        val isPlaying = pcmPlayerTask?.isPlaying ?: false
        if (isPlaying) {
            return
        }

        release()

        val pcmPlayerTaskTemp = PcmPlayerTask(pcmFile, WeakReference(callback))
        pcmPlayerTask = pcmPlayerTaskTemp
        DefaultTaskExecutor.instances.executeOnDiskIO(pcmPlayerTaskTemp)
    }


    fun release() {
        pcmPlayerTask?.release()
        pcmPlayerTask = null
    }

}
