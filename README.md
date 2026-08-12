# pincher

Agent-first Android home launcher for Nothing Phone (3a) / Android 16. Talks to a **Pi** coding-agent HTTP gateway.

## Requirements

- JDK 17 (`JAVA_HOME` pointing at a JDK 17 install)
- Android SDK under `$HOME/Android/Sdk` with:
  - command-line tools
  - `platforms;android-36`
  - `build-tools;36.x`
  - `platform-tools` (`adb`)
- No Android Studio required

## Environment

```bash
export JAVA_HOME="$(/usr/libexec/java_home -v 17 2>/dev/null || echo /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home)"
export ANDROID_HOME="$HOME/Android/Sdk"
export ANDROID_SDK_ROOT="$HOME/Android/Sdk"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"
```

Create a machine-local (gitignored) `local.properties`:

```bash
printf 'sdk.dir=%s\n' "$HOME/Android/Sdk" > local.properties
```

## Toolchain bootstrap (once per machine)

Prefer Homebrew OpenJDK 17 (user-local, no sudo) and official Android command-line tools:

```bash
brew install openjdk@17

mkdir -p "$HOME/Android/Sdk/cmdline-tools"
# Download commandlinetools-mac from https://developer.android.com/studio#command-line-tools-only
# Unpack so sdkmanager lives at:
#   $HOME/Android/Sdk/cmdline-tools/latest/bin/sdkmanager

yes | sdkmanager --licenses
sdkmanager "platforms;android-36" "build-tools;36.0.0" "platform-tools"
```

Verify:

```bash
java -version
adb version
sdkmanager --version
ls "$HOME/Android/Sdk/platforms/android-36"
ls "$HOME/Android/Sdk/build-tools"
```

## Build

```bash
./gradlew assembleDebug
```

Debug APK:

`app/build/outputs/apk/debug/app-debug.apk`

## Test

```bash
./gradlew test
```

## Install on device (adb)

1. Enable Developer options + USB debugging on the phone.
2. Connect via USB (or wireless debugging) and confirm `adb devices`.
3. Install:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

4. Set pincher as the default Home app:
   - Settings → Apps → Default apps → Home app → pincher
   - Or press Home and choose pincher when prompted.

5. Grant microphone permission when prompted (or via Settings → Push-to-talk).

6. Optionally enable **pincher PTT** under system Accessibility settings for Volume Down push-to-talk.

Note: this app registers for the **HOME** role only (`MAIN` + `HOME` + `DEFAULT`). It does not register as a normal `LAUNCHER` app icon.

## Pi gateway connection

The Pi TypeScript SDK (`createAgentSession`) cannot run on Android. pincher uses the same HTTP shape Punch uses in front of `pi --mode rpc`:

- `GET /health`
- `POST /session`
- `POST /session/{id}/message` with `{ "parts": [{ "type": "text", "text": "..." }] }`
- `POST /session/{id}/abort`

Auth is HTTP Basic when a username is set; password-only uses Bearer. Both **HTTP** (LAN) and **HTTPS** (Tailscale / TLS) are supported.

### Pair from the phone

1. Run a Pi HTTP gateway on the host (Punch `pi-gateway` on port **4096**, or equivalent).
2. In pincher Settings, enter:
   - LAN: `http://<host-lan-ip>:4096`
   - Tailscale: `https://<tailscale-name-or-ip>:4096` (or `http://` if that is what you expose)
   - Username / password if the gateway requires Basic auth
3. Tap **Save**, then **Test**. Home connection status should become `Connected`.
4. Type a prompt and **Send**, or use PTT — transcripts are sent when paired.

Never commit passwords. The app stores them in private app storage and does not log them.

## Push-to-talk

- Hold **Volume Down** for **250ms** (when the accessibility service is enabled and receives the key) to start listening.
- Key-repeat events do not restart recording.
- Release stops listening, runs `SpeechRecognizer`, shows the transcript, and **sends it to Pi** when a gateway is paired.
- Short Volume Down presses do not start PTT; pincher synthesizes a single volume step so normal volume behavior is preserved as far as Android permits.
- On-screen **Hold to talk** is the supported fallback when Volume Down is not delivered.
- Cleanup runs on release, cancel/Stop, focus loss, screen lock, speech errors, and accessibility disconnect.
- Logs cover phase / permission / key diagnostics only — **never audio** and never API keys.

### Nothing OS / global key filtering limits

`FLAG_REQUEST_FILTER_KEY_EVENTS` is **not** a guarantee of global Volume Down interception. Nothing OS (and other OEM skins) may:

- swallow volume keys in the system UI / media session path
- refuse or limit accessibility key filtering
- behave differently when another app holds audio focus

If Settings shows “Volume Down events observed: no” after pressing Volume Down, use the on-screen PTT button. Do not assume Volume Down PTT works on every Nothing Phone (3a) build.

## Architecture

```
app/src/main/java/com/hermes/launcher/
  MainActivity.kt
  model/LaunchableApp.kt
  launcher/
    InstalledAppsRepository.kt
    DefaultHomeChecker.kt
    ConnectionStatus.kt
  input/
    PttController.kt
    PttHub.kt
    AndroidSpeechTranscriber.kt
    PttTypes.kt
  gateway/
    GatewayClient.kt           # Pi session + message + health
    GatewayCredentialsStore.kt # local URL + Basic auth (not logged)
    GatewayUrl.kt
    GatewayModels.kt
  accessibility/
    HermesAccessibilityService.kt
    AccessibilityStatus.kt
  ui/
    HomeScreen.kt
    AppDrawerScreen.kt
    SettingsScreen.kt
    theme/Theme.kt
```

| Concern | Location | Status |
| --- | --- | --- |
| AccessibilityService (PTT keys) | `accessibility/HermesAccessibilityService.kt` | Implemented (best-effort) |
| Volume-down push-to-talk | `input/PttController.kt` | Implemented |
| Speech transcription | `input/AndroidSpeechTranscriber.kt` | Implemented (on-device recognizer) |
| Pi gateway client | `gateway/GatewayClient.kt` | Implemented (HTTP + HTTPS, session protocol) |
