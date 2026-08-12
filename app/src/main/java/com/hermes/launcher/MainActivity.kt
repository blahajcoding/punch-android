package com.hermes.launcher

import android.Manifest
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioManager
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import com.hermes.launcher.accessibility.AccessibilityStatus
import com.hermes.launcher.appearance.AppearancePreferences
import com.hermes.launcher.gateway.GatewayClient
import com.hermes.launcher.gateway.GatewayCredentialsStore
import com.hermes.launcher.gateway.GatewayException
import com.hermes.launcher.gateway.GatewayUrl
import com.hermes.launcher.input.AndroidSpeechTranscriber
import com.hermes.launcher.input.PttController
import com.hermes.launcher.input.PttHub
import com.hermes.launcher.input.PttPhase
import com.hermes.launcher.input.PttUiState
import com.hermes.launcher.launcher.ConnectionStatus
import com.hermes.launcher.launcher.DefaultHomeChecker
import com.hermes.launcher.launcher.InstalledAppsRepository
import com.hermes.launcher.model.LaunchableApp
import com.hermes.launcher.ui.AppDrawerScreen
import com.hermes.launcher.ui.HerbScreen
import com.hermes.launcher.ui.HomeScreen
import com.hermes.launcher.ui.SettingsScreen
import com.hermes.launcher.ui.components.HerbBackdrop
import com.hermes.launcher.ui.theme.HermesTheme
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicReference

class MainActivity : ComponentActivity() {
    private lateinit var pttController: PttController
    private lateinit var credentialsStore: GatewayCredentialsStore
    private lateinit var appearancePreferences: AppearancePreferences
    private val gatewayClient = GatewayClient()
    private val ioExecutor = Executors.newSingleThreadExecutor()
    private val inFlight = AtomicReference<Future<*>?>(null)

    private val pttState = mutableStateOf(PttUiState.Idle)
    private val micGrantedState = mutableStateOf(false)
    private val inputState = mutableStateOf("")
    private val responseState = mutableStateOf("")
    private val connectionStatusState = mutableStateOf(ConnectionStatus.Disconnected)
    private val gatewayMessageState = mutableStateOf("")
    private val gatewayUrlDraft = mutableStateOf("")
    private val gatewayUserDraft = mutableStateOf("")
    private val gatewayKeyDraft = mutableStateOf("")
    private val gatewayPairedState = mutableStateOf(false)
    private val useSystemWallpaperState = mutableStateOf(true)

    private val requestMicPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        micGrantedState.value = granted
        Log.d(TAG, "RECORD_AUDIO granted=$granted")
        if (!granted) {
            pttController.cancel("mic_denied")
        }
    }

    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                Log.d(TAG, "screen off → cancel PTT")
                pttController.cancel("screen_off")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        credentialsStore = GatewayCredentialsStore(this)
        appearancePreferences = AppearancePreferences(this)
        gatewayUrlDraft.value = credentialsStore.getOrigin()
        gatewayUserDraft.value = credentialsStore.getUsername()
        gatewayKeyDraft.value = credentialsStore.getPassword()
        gatewayPairedState.value = credentialsStore.isConfigured()
        gatewayClient.sessionId = credentialsStore.getSessionId().ifBlank { null }
        useSystemWallpaperState.value = appearancePreferences.useSystemWallpaper()
        applyWallpaperWindow(useSystemWallpaperState.value)
        micGrantedState.value = hasMicPermission()

        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        val speech = AndroidSpeechTranscriber(this)

        pttController = PttController(
            speech = speech,
            executor = PttHub.mainExecutor(),
            adjustVolumeDown = {
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_LOWER,
                    AudioManager.FLAG_SHOW_UI,
                )
            },
            onStateChanged = { state ->
                runOnUiThread {
                    pttState.value = state
                    if (state.pendingHermesSend && state.transcript.isNotBlank()) {
                        inputState.value = state.transcript
                        sendToAgent(state.transcript, fromVoice = true)
                    }
                }
            },
        )
        PttHub.attachController(pttController)

        ContextCompat.registerReceiver(
            this,
            screenOffReceiver,
            IntentFilter(Intent.ACTION_SCREEN_OFF),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )

        if (credentialsStore.isConfigured()) {
            testGateway(silent = true)
        }

        val appsRepository = InstalledAppsRepository(packageManager)
        val homeChecker = DefaultHomeChecker(packageManager, packageName)

        setContent {
            HermesTheme {
                val useWallpaper by useSystemWallpaperState
                HerbBackdrop(
                    useSystemWallpaper = useWallpaper,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    var screen by rememberSaveable { mutableStateOf(HerbScreen.Home.name) }
                    var input by inputState
                    var response by responseState
                    val connectionStatus by connectionStatusState
                    val apps = remember { appsRepository.launchableApps() }
                    val ptt by pttState
                    val micGranted by micGrantedState
                    var gatewayUrl by gatewayUrlDraft
                    var gatewayUser by gatewayUserDraft
                    var gatewayKey by gatewayKeyDraft
                    val gatewayPaired by gatewayPairedState
                    val gatewayMessage by gatewayMessageState

                    when (screen) {
                        HerbScreen.Drawer.name -> AppDrawerScreen(
                            apps = apps,
                            onAppClick = { app -> launchApp(app) },
                            onBack = { screen = HerbScreen.Home.name },
                        )

                        HerbScreen.Settings.name -> SettingsScreen(
                            packageName = packageName,
                            isDefaultHome = homeChecker.isDefaultHome(),
                            microphoneGranted = micGranted,
                            accessibilityEnabled = AccessibilityStatus.isHermesServiceEnabled(this@MainActivity),
                            accessibilityConnected = PttHub.isAccessibilityConnected(),
                            volumeDownObserved = PttHub.hasObservedVolumeDown(),
                            useSystemWallpaper = useWallpaper,
                            onUseSystemWallpaperChange = { enabled ->
                                appearancePreferences.setUseSystemWallpaper(enabled)
                                useSystemWallpaperState.value = enabled
                                applyWallpaperWindow(enabled)
                            },
                            gatewayUrl = gatewayUrl,
                            onGatewayUrlChange = { gatewayUrl = it },
                            gatewayUsername = gatewayUser,
                            onGatewayUsernameChange = { gatewayUser = it },
                            gatewayApiKey = gatewayKey,
                            onGatewayApiKeyChange = { gatewayKey = it },
                            gatewayPaired = gatewayPaired,
                            connectionStatus = connectionStatus,
                            gatewayMessage = gatewayMessage,
                            onSaveGateway = { saveGateway(gatewayUrl, gatewayUser, gatewayKey) },
                            onTestGateway = { testGateway(silent = false) },
                            onClearGateway = { clearGateway() },
                            onRequestMicrophone = { ensureMicPermission() },
                            onOpenAccessibilitySettings = {
                                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                            },
                            onBack = { screen = HerbScreen.Home.name },
                        )

                        else -> HomeScreen(
                            input = input,
                            onInputChange = { input = it },
                            response = response,
                            onSend = {
                                val prompt = input.trim()
                                if (prompt.isNotEmpty()) {
                                    sendToAgent(prompt, fromVoice = false)
                                }
                            },
                            onStop = {
                                pttController.cancel("stop_button")
                                cancelInFlight("user_stop")
                            },
                            connectionStatus = connectionStatus,
                            pttState = ptt,
                            onPttPressDown = { beginPttFromUi() },
                            onPttPressUp = { pttController.onPressUp() },
                            onOpenDrawer = { screen = HerbScreen.Drawer.name },
                            onOpenSettings = { screen = HerbScreen.Settings.name },
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        micGrantedState.value = hasMicPermission()
    }

    override fun onPause() {
        Log.d(TAG, "focus loss → cancel PTT")
        if (::pttController.isInitialized) {
            pttController.cancel("focus_loss")
        }
        super.onPause()
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(screenOffReceiver)
        } catch (_: IllegalArgumentException) {
            // Already unregistered.
        }
        cancelInFlight("destroy")
        if (::pttController.isInitialized) {
            pttController.cancel("activity_destroy")
            PttHub.detachController(pttController)
        }
        ioExecutor.shutdownNow()
        super.onDestroy()
    }

    private fun saveGateway(rawUrl: String, username: String, password: String) {
        val normalized = GatewayUrl.normalizeOrigin(rawUrl)
        if (normalized.isFailure) {
            gatewayMessageState.value = normalized.exceptionOrNull()?.message ?: "Invalid URL"
            gatewayPairedState.value = false
            connectionStatusState.value = ConnectionStatus.Disconnected
            return
        }
        val origin = normalized.getOrThrow()
        credentialsStore.save(origin, username.trim(), password)
        gatewayUrlDraft.value = origin
        gatewayUserDraft.value = username.trim()
        gatewayKeyDraft.value = password
        gatewayPairedState.value = true
        gatewayClient.sessionId = null
        gatewayMessageState.value = "Saved. Testing connection…"
        Log.d(TAG, "gateway saved scheme=${origin.substringBefore("://")}")
        testGateway(silent = false)
    }

    private fun clearGateway() {
        cancelInFlight("clear")
        credentialsStore.clear()
        gatewayClient.sessionId = null
        gatewayUrlDraft.value = ""
        gatewayUserDraft.value = ""
        gatewayKeyDraft.value = ""
        gatewayPairedState.value = false
        connectionStatusState.value = ConnectionStatus.Disconnected
        gatewayMessageState.value = "Pi pairing cleared"
    }

    private fun testGateway(silent: Boolean) {
        if (!credentialsStore.isConfigured()) {
            if (!silent) {
                gatewayMessageState.value = "Save a Pi gateway URL first"
            }
            connectionStatusState.value = ConnectionStatus.Disconnected
            return
        }
        connectionStatusState.value = ConnectionStatus.Connecting
        if (!silent) {
            gatewayMessageState.value = "Testing…"
        }
        submitIo {
            val origin = credentialsStore.getOrigin()
            val user = credentialsStore.getUsername()
            val pass = credentialsStore.getPassword()
            val health = gatewayClient.health(origin, user, pass)
            runOnUiThread {
                if (health.ok) {
                    connectionStatusState.value = ConnectionStatus.Connected
                    gatewayPairedState.value = true
                    if (!silent) {
                        gatewayMessageState.value = "Connected (${health.detail})"
                    }
                } else {
                    connectionStatusState.value = ConnectionStatus.Disconnected
                    if (!silent) {
                        gatewayMessageState.value = "Unreachable: ${health.detail}"
                    }
                }
            }
        }
    }

    private fun sendToAgent(prompt: String, fromVoice: Boolean) {
        if (!credentialsStore.isConfigured()) {
            responseState.value =
                "Not paired. Open Settings and save a Pi gateway URL."
            connectionStatusState.value = ConnectionStatus.Disconnected
            return
        }
        connectionStatusState.value = ConnectionStatus.Connecting
        responseState.value = if (fromVoice) "Sending transcript…" else "Waiting for pi…"

        submitIo {
            try {
                val result = gatewayClient.prompt(
                    origin = credentialsStore.getOrigin(),
                    username = credentialsStore.getUsername(),
                    password = credentialsStore.getPassword(),
                    text = prompt,
                )
                runOnUiThread {
                    result.sessionId?.let { credentialsStore.saveSessionId(it) }
                    responseState.value = result.text.ifBlank { "(empty response)" }
                    inputState.value = ""
                    connectionStatusState.value = ConnectionStatus.Connected
                    gatewayMessageState.value = "Connected"
                    Log.d(TAG, "prompt ok chars=${result.text.length}")
                }
            } catch (e: Exception) {
                if (e is java.io.IOException && e.message?.contains("abort", true) == true) {
                    return@submitIo
                }
                Log.d(TAG, "prompt failed: ${e.javaClass.simpleName}")
                runOnUiThread {
                    connectionStatusState.value = ConnectionStatus.Disconnected
                    val detail = when (e) {
                        is GatewayException -> e.message ?: "Pi error"
                        else -> e.message ?: "Request failed"
                    }
                    responseState.value = "Error: $detail"
                    gatewayMessageState.value = detail
                }
            }
        }
    }

    private fun submitIo(block: () -> Unit) {
        cancelInFlight("replace")
        val future = ioExecutor.submit {
            try {
                block()
            } catch (e: Exception) {
                Log.d(TAG, "io task ended: ${e.javaClass.simpleName}")
            }
        }
        inFlight.set(future)
    }

    private fun cancelInFlight(reason: String) {
        Log.d(TAG, "cancelInFlight reason=$reason")
        if (credentialsStore.isConfigured()) {
            gatewayClient.abort(
                credentialsStore.getOrigin(),
                credentialsStore.getUsername(),
                credentialsStore.getPassword(),
            )
        } else {
            gatewayClient.cancel()
        }
        inFlight.getAndSet(null)?.cancel(true)
        if (connectionStatusState.value == ConnectionStatus.Connecting) {
            connectionStatusState.value =
                if (credentialsStore.isConfigured()) {
                    ConnectionStatus.Connected
                } else {
                    ConnectionStatus.Disconnected
                }
            responseState.value = "Stopped."
        }
    }

    private fun beginPttFromUi() {
        if (!ensureMicPermission()) {
            return
        }
        pttController.onPressDown(isRepeat = false)
    }

    private fun ensureMicPermission(): Boolean {
        if (hasMicPermission()) {
            micGrantedState.value = true
            return true
        }
        requestMicPermission.launch(Manifest.permission.RECORD_AUDIO)
        return false
    }

    private fun hasMicPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun applyWallpaperWindow(enabled: Boolean) {
        if (enabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
            window.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
            window.setBackgroundDrawable(Color.parseColor("#030806").toDrawable())
        }
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
    }

    private fun launchApp(app: LaunchableApp) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            component = ComponentName(app.packageName, app.activityName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

    companion object {
        private const val TAG = "PincherMain"
    }
}
