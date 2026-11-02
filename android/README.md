# Phoenix RP Android
Native Android client built with Kotlin, Jetpack Compose and Room.

## Implemented foundation
- Independent RP stories with story-scoped character storage.
- Extended character editor: personality, appearance, background, speech style and opening scene.
- JSON character import from a pasted object or array.
- Room v1 → v2 migration preserving existing characters and conversations.
- Persistent local memory/reference notes, separated by story.
- GGUF file import into app-private storage, file-header validation and local model catalogue.
- Persistent light/dark theme preference.
- Responsive Compose layout with safe drawing insets for system bars and display cutouts, plus a readable max content width on tablets/wide screens.
- Fandom article import through the public MediaWiki API, with source URL attribution and a manual-text fallback.

## Important limitations
- llama.cpp is **not yet bundled**. LocalInferenceEngine/LlamaCppEngine currently define an integration boundary only; the chat still uses the deterministic demo ReplyComposer.
- GGUF files can be imported and catalogued, but cannot yet be loaded for inference.
- Memory notes are stored locally but are not yet automatically retrieved and injected into the model prompt.
- Fandom URL import supports public HTTPS article pages through the MediaWiki API. Some wikis/pages may restrict API access; manual text import remains available.
- Character JSON import is available; JSON export and file-picker based JSON import remain future work.

## Build
Use the repository GitHub Actions workflow. Persistent release signing requires PHOENIX_KEYSTORE_BASE64, PHOENIX_KEY_ALIAS, PHOENIX_KEYSTORE_PASSWORD and PHOENIX_KEY_PASSWORD GitHub Actions secrets. Never commit a keystore.
