package com.hermes.launcher.input

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PttControllerTest {
    private lateinit var speech: FakeSpeech
    private lateinit var executor: FakeExecutor
    private lateinit var controller: PttController
    private var lastState: PttUiState = PttUiState.Idle
    private var volumeAdjustCount = 0

    @Before
    fun setUp() {
        speech = FakeSpeech()
        executor = FakeExecutor()
        volumeAdjustCount = 0
        lastState = PttUiState.Idle
        controller = PttController(
            speech = speech,
            executor = executor,
            holdThresholdMs = 250L,
            adjustVolumeDown = { volumeAdjustCount++ },
            onStateChanged = { lastState = it },
        )
    }

    @Test
    fun startsOnlyAfterHoldThreshold() {
        controller.onPressDown(isRepeat = false)
        assertEquals(PttPhase.Armed, controller.currentPhase)
        assertFalse(lastState.listeningVisible)
        assertEquals(0, speech.startCount)

        executor.advance(249)
        assertEquals(PttPhase.Armed, controller.currentPhase)
        assertEquals(0, speech.startCount)

        executor.advance(1)
        assertEquals(PttPhase.Listening, controller.currentPhase)
        assertTrue(lastState.listeningVisible)
        assertEquals("listening…", lastState.statusMessage)
        assertEquals(1, speech.startCount)
    }

    @Test
    fun repeatedKeyDownDoesNotRestartRecording() {
        controller.onPressDown(isRepeat = false)
        executor.advance(250)
        assertEquals(1, speech.startCount)

        controller.onPressDown(isRepeat = true)
        controller.onPressDown(isRepeat = true)
        assertEquals(1, speech.startCount)
        assertEquals(PttPhase.Listening, controller.currentPhase)
    }

    @Test
    fun releaseStopsRecording() {
        controller.onPressDown(isRepeat = false)
        executor.advance(250)
        controller.onPressUp()

        assertEquals(PttPhase.Transcribing, controller.currentPhase)
        assertEquals(1, speech.stopCount)
        assertFalse(lastState.listeningVisible)

        speech.emitFinal("hello hermes")
        assertEquals(PttPhase.Idle, controller.currentPhase)
        assertEquals("hello hermes", lastState.transcript)
        assertTrue(lastState.pendingHermesSend)
        assertTrue(lastState.statusMessage.contains("Sending"))
    }

    @Test
    fun cancellationAlwaysClearsRecordingState() {
        controller.onPressDown(isRepeat = false)
        executor.advance(250)
        assertEquals(PttPhase.Listening, controller.currentPhase)

        controller.cancel("test")
        assertEquals(PttPhase.Idle, controller.currentPhase)
        assertFalse(lastState.listeningVisible)
        assertEquals(1, speech.cancelCount)
        assertEquals("", lastState.statusMessage)
    }

    @Test
    fun speechErrorsReturnToIdle() {
        controller.onPressDown(isRepeat = false)
        executor.advance(250)
        speech.emitError("7")

        assertEquals(PttPhase.Idle, controller.currentPhase)
        assertFalse(lastState.listeningVisible)
        assertTrue(lastState.statusMessage.contains("Speech error"))
    }

    @Test
    fun shortPressDoesNotListenAndAdjustsVolume() {
        controller.onPressDown(isRepeat = false)
        executor.advance(100)
        controller.onPressUp()

        assertEquals(PttPhase.Idle, controller.currentPhase)
        assertEquals(0, speech.startCount)
        assertEquals(1, volumeAdjustCount)
    }

    private class FakeSpeech : SpeechTranscriber {
        var startCount = 0
        var stopCount = 0
        var cancelCount = 0
        private var callback: SpeechTranscriber.Callback? = null

        override fun start(callback: SpeechTranscriber.Callback) {
            startCount++
            this.callback = callback
        }

        override fun stopListening() {
            stopCount++
        }

        override fun cancel() {
            cancelCount++
            callback = null
        }

        fun emitFinal(text: String) {
            callback?.onFinal(text)
        }

        fun emitError(message: String) {
            callback?.onError(message)
        }
    }

    private class FakeExecutor : DelayedExecutor {
        private data class Task(val dueAt: Long, val action: () -> Unit, var cancelled: Boolean)
        private val tasks = mutableListOf<Task>()
        private var now = 0L

        override fun postDelayed(delayMs: Long, action: () -> Unit): Cancelable {
            val task = Task(dueAt = now + delayMs, action = action, cancelled = false)
            tasks += task
            return Cancelable { task.cancelled = true }
        }

        fun advance(ms: Long) {
            now += ms
            val due = tasks.filter { !it.cancelled && it.dueAt <= now }
            due.forEach { it.action() }
            tasks.removeAll { it.cancelled || it.dueAt <= now }
        }
    }
}
