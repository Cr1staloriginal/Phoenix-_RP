# Phoenix RP Android
Native Android client built with Kotlin, Jetpack Compose and Room.

## Current foundation
- Independent RP stories and story-scoped character storage.
- Expanded character fields: personality/description, appearance, background, speech style and opening scene.
- Room v1 to v2 migration preserves existing characters and conversations.
- Local memory/reference records and a GGUF model catalogue.
- GGUF header inspection before registering a model.
- Compose safe drawing insets and width constraints for system bars, cutouts and compact screens.
- Light/dark theme support is being introduced in the UI layer.

## AI runtime status
A LocalInferenceEngine boundary and LlamaCppEngine placeholder now define the integration point. **llama.cpp is not bundled and generation is not active.** Chat replies remain deterministic demo text. The native-runtime phase must add NDK/JNI, ABI packaging, context management, cancellation and memory safeguards.

## Imports
Fandom text can be stored as a local reference after pasting article text and its source URL. Automatic URL fetching and site parsing are not enabled yet. Full character JSON transfer is a follow-up task.

## Build
Use the repository GitHub Actions workflow. Persistent release signing requires PHOENIX_KEYSTORE_BASE64, PHOENIX_KEY_ALIAS, PHOENIX_KEYSTORE_PASSWORD and PHOENIX_KEY_PASSWORD GitHub Actions secrets. Never commit a keystore.
