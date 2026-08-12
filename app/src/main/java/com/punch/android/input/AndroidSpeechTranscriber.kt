package com.punch.android.input

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import java.util.Locale

class AndroidSpeechTranscriber(
    context: Context,
    private val mainHandler: Handler = Handler(Looper.getMainLooper()),
) : SpeechTranscriber {
    private val appContext = context.applicationContext
    private var recognizer: SpeechRecognizer? = null
    private var callback: SpeechTranscriber.Callback? = null

    override fun start(callback: SpeechTranscriber.Callback) {
        mainHandler.post {
            this.callback = callback
            if (!SpeechRecognizer.isRecognitionAvailable(appContext)) {
                callback.onError("unavailable")
                return@post
            }
            destroyRecognizer()
            val speech = SpeechRecognizer.createSpeechRecognizer(appContext)
            recognizer = speech
            speech.setRecognitionListener(Listener())
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                )
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            Log.d(TAG, "startListening")
            speech.startListening(intent)
        }
    }

    override fun stopListening() {
        mainHandler.post {
            Log.d(TAG, "stopListening")
            recognizer?.stopListening()
        }
    }

    override fun cancel() {
        mainHandler.post {
            Log.d(TAG, "cancel")
            recognizer?.cancel()
            destroyRecognizer()
            callback = null
        }
    }

    private fun destroyRecognizer() {
        recognizer?.destroy()
        recognizer = null
    }

    private inner class Listener : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Log.d(TAG, "onReadyForSpeech")
        }

        override fun onBeginningOfSpeech() {
            Log.d(TAG, "onBeginningOfSpeech")
        }

        override fun onRmsChanged(rmsdB: Float) = Unit

        override fun onBufferReceived(buffer: ByteArray?) = Unit

        override fun onEndOfSpeech() {
            Log.d(TAG, "onEndOfSpeech")
        }

        override fun onError(error: Int) {
            Log.d(TAG, "onError code=$error")
            val cb = callback
            destroyRecognizer()
            callback = null
            cb?.onError(error.toString())
        }

        override fun onResults(results: Bundle?) {
            val text = results
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                .orEmpty()
            Log.d(TAG, "onResults len=${text.length}")
            val cb = callback
            destroyRecognizer()
            callback = null
            cb?.onFinal(text)
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val text = partialResults
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                .orEmpty()
            if (text.isNotEmpty()) {
                callback?.onPartial(text)
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) = Unit
    }

    companion object {
        private const val TAG = "HermesSpeech"
    }
}
