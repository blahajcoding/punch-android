package com.hermes.launcher.input

import android.util.Log

/**
 * Push-to-talk state machine.
 *
 * Volume Down (or in-app hold) must be held for [holdThresholdMs] before listening starts.
 * Key-repeat downs do not restart recording. Short presses never enter Listening.
 */
class PttController(
    private val speech: SpeechTranscriber,
    private val executor: DelayedExecutor,
    private val holdThresholdMs: Long = HOLD_THRESHOLD_MS,
    private val adjustVolumeDown: () -> Unit = {},
    private val onStateChanged: (PttUiState) -> Unit = {},
) {
    private var phase: PttPhase = PttPhase.Idle
    private var holdTask: Cancelable? = null
    private var lastTranscript: String = ""
    private var statusMessage: String = ""

    val currentPhase: PttPhase
        get() = phase

    fun onPressDown(isRepeat: Boolean = false) {
        if (isRepeat) {
            Log.d(TAG, "ignore key repeat while phase=$phase")
            return
        }
        when (phase) {
            PttPhase.Idle -> armHold()
            PttPhase.Armed, PttPhase.Listening, PttPhase.Transcribing -> {
                Log.d(TAG, "ignore extra down while phase=$phase")
            }
        }
    }

    fun onPressUp() {
        when (phase) {
            PttPhase.Armed -> {
                cancelHoldTimer()
                adjustVolumeDown()
                setPhase(PttPhase.Idle, listening = false, status = "")
            }

            PttPhase.Listening -> {
                setPhase(PttPhase.Transcribing, listening = false, status = "transcribing…")
                speech.stopListening()
            }

            PttPhase.Transcribing -> {
                Log.d(TAG, "release during transcribe ignored")
            }

            PttPhase.Idle -> Unit
        }
    }

    fun cancel(reason: String) {
        Log.d(TAG, "cancel reason=$reason phase=$phase")
        cancelHoldTimer()
        speech.cancel()
        setPhase(PttPhase.Idle, listening = false, status = "")
    }

    fun onSpeechFinal(text: String) {
        lastTranscript = text.trim()
        val status = if (lastTranscript.isEmpty()) {
            "No speech detected."
        } else {
            "Transcribed. Sending…"
        }
        setPhase(
            PttPhase.Idle,
            listening = false,
            status = status,
            transcript = lastTranscript,
            pendingHermesSend = lastTranscript.isNotEmpty(),
        )
    }

    fun onSpeechError(message: String) {
        Log.d(TAG, "speech error codeOrMsg=$message")
        cancelHoldTimer()
        speech.cancel()
        setPhase(PttPhase.Idle, listening = false, status = "Speech error. Idle.")
    }

    private fun armHold() {
        cancelHoldTimer()
        setPhase(PttPhase.Armed, listening = false, status = "")
        holdTask = executor.postDelayed(holdThresholdMs) {
            if (phase == PttPhase.Armed) {
                beginListening()
            }
        }
    }

    private fun beginListening() {
        setPhase(PttPhase.Listening, listening = true, status = "listening…")
        speech.start(
            object : SpeechTranscriber.Callback {
                override fun onPartial(text: String) {
                    if (phase == PttPhase.Listening && text.isNotBlank()) {
                        lastTranscript = text
                        publish()
                    }
                }

                override fun onFinal(text: String) {
                    onSpeechFinal(text)
                }

                override fun onError(message: String) {
                    onSpeechError(message)
                }
            },
        )
    }

    private fun cancelHoldTimer() {
        holdTask?.cancel()
        holdTask = null
    }

    private fun setPhase(
        newPhase: PttPhase,
        listening: Boolean,
        status: String,
        transcript: String = lastTranscript,
        pendingHermesSend: Boolean = false,
    ) {
        phase = newPhase
        statusMessage = status
        lastTranscript = transcript
        Log.d(TAG, "phase=$phase listening=$listening")
        publish(listening, pendingHermesSend)
    }

    private fun publish(
        listening: Boolean = phase == PttPhase.Listening,
        pendingHermesSend: Boolean = false,
    ) {
        onStateChanged(
            PttUiState(
                phase = phase,
                listeningVisible = listening,
                transcript = lastTranscript,
                statusMessage = statusMessage,
                pendingHermesSend = pendingHermesSend,
            ),
        )
    }

    companion object {
        const val HOLD_THRESHOLD_MS: Long = 250L
        private const val TAG = "HermesPtt"
    }
}
