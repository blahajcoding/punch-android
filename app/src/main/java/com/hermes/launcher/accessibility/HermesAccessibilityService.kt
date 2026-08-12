package com.hermes.launcher.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.hermes.launcher.input.PttHub
import com.hermes.launcher.input.PttPhase

/**
 * Filters Volume Down for push-to-talk when enabled by the user.
 *
 * Global key filtering is not universally reliable across OEMs (including Nothing OS).
 * The in-app PTT button remains the supported fallback.
 */
class HermesAccessibilityService : AccessibilityService() {
    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = serviceInfo ?: AccessibilityServiceInfo()
        info.eventTypes = 0
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.flags = info.flags or AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
        serviceInfo = info
        PttHub.setAccessibilityConnected(true)
        Log.d(TAG, "service connected; filterKeyEvents requested")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() {
        Log.d(TAG, "interrupted")
        PttHub.controller()?.cancel("a11y_interrupt")
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode != KeyEvent.KEYCODE_VOLUME_DOWN) {
            return super.onKeyEvent(event)
        }

        PttHub.markVolumeDownObserved()
        val controller = PttHub.controller()
        if (controller == null) {
            Log.d(TAG, "volume_down ignored; no controller")
            return false
        }

        return when (event.action) {
            KeyEvent.ACTION_DOWN -> {
                val isRepeat = event.repeatCount > 0
                if (!isRepeat) {
                    Log.d(TAG, "volume_down down")
                    controller.onPressDown(isRepeat = false)
                } else {
                    controller.onPressDown(isRepeat = true)
                }
                // Consume while armed/listening so volume does not keep stepping during PTT.
                // Short taps still get one programmatic volume step on release (see onPressUp).
                controller.currentPhase == PttPhase.Armed ||
                    controller.currentPhase == PttPhase.Listening ||
                    controller.currentPhase == PttPhase.Transcribing
            }

            KeyEvent.ACTION_UP -> {
                Log.d(TAG, "volume_down up phase=${controller.currentPhase}")
                val consume = controller.currentPhase != PttPhase.Idle
                controller.onPressUp()
                consume
            }

            else -> false
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "service destroy")
        PttHub.controller()?.cancel("a11y_disconnect")
        PttHub.setAccessibilityConnected(false)
        super.onDestroy()
    }

    companion object {
        private const val TAG = "HermesA11y"
    }
}
