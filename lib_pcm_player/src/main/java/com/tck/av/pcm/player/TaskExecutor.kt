package com.tck.av.pcm.player

/**
 *<p>description:</p>
 *<p>created on: 2021/3/1 14:46</p>
 * @author tck
 * @version v1.0
 *
 */
abstract class TaskExecutor {

    abstract fun executeOnDiskIO(runnable: Runnable)

    abstract fun postToMainThread(runnable: Runnable)


    open fun executeOnMainThread(runnable: Runnable) {
        if (isMainThread()) {
            runnable.run()
        } else {
            postToMainThread(runnable)
        }
    }

    abstract fun isMainThread(): Boolean
}