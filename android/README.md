# Phoenix RP — Android

Native Android client for Phoenix RP. The legacy Python Telegram bot remains in the repository and is not part of the Android application.

## Current status — 0.4.0

- Kotlin + Jetpack Compose interface
- Character creation and deletion
- Local character and message storage with Room
- ViewModel-based state and coroutine operations
- Prototype reply composer (not an AI model)
- Unit tests for prototype reply behavior
- Cloud CI runs unit tests before building the debug APK

## Requirements

- JDK 17
- Android SDK platform 35
- Gradle 8.9
- Android 7.0 (API 24) or newer

## Build

From this directory:

```bash
gradle testDebugUnitTest
gradle assembleDebug
```

The debug APK is produced at `app/build/outputs/apk/debug/app-debug.apk`.

## Signing and upgrades

The workflow supports these GitHub Actions repository secrets:

- `PHOENIX_KEYSTORE_BASE64`
- `PHOENIX_KEY_ALIAS`
- `PHOENIX_KEYSTORE_PASSWORD`
- `PHOENIX_KEY_PASSWORD`

All four must be configured to sign APKs with a persistent key. Do not commit the keystore or passwords. Without these secrets, CI produces a debug-signed APK which may not be upgrade-compatible with APKs signed by a different runner key.

## Data

Characters and messages are stored in the app-private Room database `phoenix-rp.db`. The current schema is version 1. Database migrations must be added before changing the schema in future releases.

## AI roadmap

The current reply composer is a deterministic prototype, not a language model. GGUF model loading and llama.cpp integration are not implemented yet. Large model files must be downloaded separately rather than committed to the repository or bundled into the APK.
