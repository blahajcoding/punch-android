package com.hermes.launcher.input

enum class PttPhase {
    Idle,
    Armed,
    Listening,
    Transcribing,
}

data class PttUiState(
    val phase: PttPhase = PttPhase.Idle,
    val listeningVisible: Boolean = false,
    val transcript: String = "",
    val statusMessage: String = "",
    val pendingHermesSend: Boolean = false,
) {
    companion object {
        val Idle = PttUiState()
    }
}

fun interface Cancelable {
    fun cancel()
}

fun interface DelayedExecutor {
    fun postDelayed(delayMs: Long, action: () -> Unit): Cancelable
}

interface SpeechTranscriber {
    fun start(callback: Callback)
    fun stopListening()
    fun cancel()

    interface Callback {
        fun onPartial(text: String) {}
        fun onFinal(text: String)
        fun onError(message: String)
    }
}
