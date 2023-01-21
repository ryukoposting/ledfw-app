package com.evanperrygrove.fwcom.util

import android.os.Handler
import android.os.Looper

object Defer {
    fun post(looper: Looper, delayMillis: Long, runnable: Runnable) {
        Handler(looper).postDelayed(runnable, delayMillis)
    }

    fun cancel(looper: Looper, runnable: Runnable) {
        Handler(looper).removeCallbacks(runnable)
    }

    fun postSequential(looper: Looper, delayMillis: Long, predicate: () -> Boolean, runnables: List<Runnable>) {
        if (runnables.isEmpty()) return
        post(looper, delayMillis) {
            if (predicate()) {
                runnables[0].run()
                postSequential(looper, delayMillis, predicate, runnables.drop(1))
            }
        }
    }

    fun postSequential(looper: Looper, delayMillis: Long, runnables: List<Runnable>) =
        postSequential(looper, delayMillis, { -> true }, runnables)

    fun retryUntilSucceed(looper: Looper, delayMillis: Long, maxTries: Int, predicate: () -> Boolean, action: () -> Boolean, completion: (Boolean) -> Unit) {
        val runnable = object: Runnable {
            var nTriesLeft = maxTries
            override fun run() {
                if (!predicate()) return

                val didSucceed = action()

                if (nTriesLeft >= 0 && !didSucceed) {
                    nTriesLeft -= 1
                    post(looper, delayMillis, this)
                } else {
                    post(looper, delayMillis, Runnable { completion(didSucceed) })
                }
            }
        }

        runnable.run()
    }
}