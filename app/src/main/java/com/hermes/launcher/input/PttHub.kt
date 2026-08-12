package com.hermes.launcher.input

import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Process-wide bridge between [com.hermes.launcher.accessibility.HermesAccessibilityService]
 * and the launcher UI. Holds diagnostic flags only — never audio.
 */
object PttHub {
    private val controllerRef = AtomicReference<PttController?>(null)
    private val volumeDownObserved = AtomicBoolean(false)
    private val accessibilityConnected = AtomicBoolean(false)

    fun attachController(controller: PttController) {
        controllerRef.set(controller)
        Log.d(TAG, "controller attached")
    }

    fun detachController(controller: PttController) {
        controllerRef.compareAndSet(controller, null)
        Log.d(TAG, "controller detached")
    }

    fun controller(): PttController? = controllerRef.get()

    fun markVolumeDownObserved() {
        volumeDownObserved.set(true)
    }

    fun hasObservedVolumeDown(): Boolean = volumeDownObserved.get()

    fun setAccessibilityConnected(connected: Boolean) {
        accessibilityConnected.set(connected)
        Log.d(TAG, "accessibilityConnected=$connected")
    }

    fun isAccessibilityConnected(): Boolean = accessibilityConnected.get()

    fun mainExecutor(): DelayedExecutor {
        val handler = Handler(Looper.getMainLooper())
        return DelayedExecutor { delayMs, action ->
            val runnable = Runnable { action() }
            handler.postDelayed(runnable, delayMs)
            Cancelable { handler.removeCallbacks(runnable) }
        }
    }

    private const val TAG = "HermesPttHub"
}
