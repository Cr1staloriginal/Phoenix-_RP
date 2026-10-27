# Phoenix RP — Android

Native Android client for Phoenix RP. The existing Python Telegram bot remains in the repository and is not modified by this app.

## Current status

- Kotlin + Jetpack Compose starter screen
- Minimum Android version: Android 7.0 (API 24)
- Package: `dev.cr1stalik.phoenixrp`
- Cloud debug APK build via GitHub Actions
- No LLM engine or model bundled yet

## Build

The GitHub Actions workflow installs Java, Gradle and Android SDK tools, then runs:

```bash
gradle assembleDebug
```

The debug APK is produced at `app/build/outputs/apk/debug/app-debug.apk` and uploaded as a workflow artifact.

Large GGUF model files will be downloaded separately in a later milestone, not committed to this repository or embedded in the APK.
