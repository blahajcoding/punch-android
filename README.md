# punch-android

Android client for **Punch**. The app name on the phone is **Punch**. Talks to a Pi coding-agent HTTP gateway.

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

Because the application id is now `com.punch.android`, this installs as a **new app** next to any old pincher/launcher build. Uninstall the old one if you no longer need it.

4. Open **Punch** from the app drawer (it is a normal launcher icon, not a Home replacement).
5. Grant microphone permission when you first hold the mic in the Ask bar.

## Pi gateway connection

The Pi TypeScript SDK (`createAgentSession`) cannot run on Android. Punch uses the HTTP shape in front of `pi --mode rpc`:

- `GET /health`
- `POST /session`
- `POST /session/{id}/message` with `{ "parts": [{ "type": "text", "text": "..." }, ...] }`
- `POST /session/{id}/abort`

Auth is HTTP Basic when a username is set; password-only uses Bearer. Both **HTTP** (LAN) and **HTTPS** (Tailscale / TLS) are supported.

### Pair from the phone

1. Run a Pi HTTP gateway on the host (Punch `pi-gateway` on port **4096**, or equivalent).
2. In Punch Settings, enter:
   - LAN: `http://<host-lan-ip>:4096`
   - Tailscale: `https://<tailscale-name-or-ip>:4096` (or `http://` if that is what you expose)
   - Username / password if the gateway requires Basic auth
3. Tap **Save**, then **Test**.
4. Type a prompt and send, or hold the mic in the Ask bar.

Never commit passwords. The app stores them in private app storage and does not log them.

## Bundles

Bundles are Punch’s project folders. Each bundle has a name, shared instructions, and files. Chats inside a bundle, and any other chat that **Use**s that bundle, send that memory to Pi with the prompt.

## Architecture

```
app/src/main/java/com/punch/android/
  MainActivity.kt
  data/
    PunchModels.kt
    PunchStore.kt
    PromptComposer.kt
  input/
    PttController.kt
    PttHub.kt
    AndroidSpeechTranscriber.kt
    PttTypes.kt
  gateway/
    GatewayClient.kt
    GatewayCredentialsStore.kt
    GatewayUrl.kt
    GatewayModels.kt
  ui/
    ChatScreen.kt
    PunchDrawer.kt
    BundleScreen.kt
    SearchChatsScreen.kt
    SettingsScreen.kt
    theme/Theme.kt
```
