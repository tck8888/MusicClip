package com.tck.av.pcm.player

import android.os.Build
import android.os.Handler
import android.os.Looper
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 *<p>description:</p>
 *<p>created on: 2021/3/1 14:46</p>
 * @author tck
 * @version v1.0
 *
 */
class DefaultTaskExecutor private constructor() : TaskExecutor() {

    private val mLock = Any()

    private val mDiskIO = Executors.newFixedThreadPool(4, object : ThreadFactory {
        private val THREAD_NAME_STEM = "arch_disk_io_%d"
        private val mThreadId = AtomicInteger(0)
        override fun newThread(r: Runnable): Thread {
            val t = Thread(r)
            t.name = String.format(THREAD_NAME_STEM, mThreadId.getAndIncrement())
            return t
        }
    })

    companion object {
        val instances: DefaultTaskExecutor by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { DefaultTaskExecutor() }
    }

    @Volatile
    private var mMainHandler: Handler? = null


    override fun executeOnDiskIO(runnable: Runnable) {
        mDiskIO.execute(runnable)
    }

    override fun postToMainThread(runnable: Runnable) {
        if (mMainHandler == null) {
            synchronized(mLock) {
                if (mMainHandler == null) {
                    mMainHandler = createAsync(Looper.getMainLooper())
                }
            }
        }
        mMainHandler!!.post(runnable)
    }

    override fun isMainThread(): Boolean {
        return Looper.getMainLooper().thread === Thread.currentThread()
    }

    private fun createAsync(looper: Looper): Handler? {
        if (Build.VERSION.SDK_INT >= 28) {
            return Handler.createAsync(looper)
        }
        try {
            return Handler::class.java.getDeclaredConstructor(
                Looper::class.java, Handler.Callback::class.java,
                Boolean::class.javaPrimitiveType
            )
                .newInstance(looper, null, true)
        } catch (ignored: IllegalAccessException) {
        } catch (ignored: InstantiationException) {
        } catch (ignored: NoSuchMethodException) {
        } catch (e: InvocationTargetException) {
            return Handler(looper)
        }
        return Handler(looper)
    }

}