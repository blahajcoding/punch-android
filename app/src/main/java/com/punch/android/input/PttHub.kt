package com.punch.android.input

import android.os.Handler
import android.os.Looper

object PttHub {
    fun mainExecutor(): DelayedExecutor {
        val handler = Handler(Looper.getMainLooper())
        return DelayedExecutor { delayMs, action ->
            val runnable = Runnable { action() }
            handler.postDelayed(runnable, delayMs)
            Cancelable { handler.removeCallbacks(runnable) }
        }
    }
}
