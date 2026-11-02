# Phoenix RP 1.0.0 — Release plan

Target date: 2026-11-08

This is a release target, not a guarantee. The release is considered complete only when every mandatory gate below passes on a real Android device. Do not describe demo behavior or placeholder classes as implemented AI functionality.

## Product requirements

- Canonical roleplay: generation must use the selected character profile, imported lore, story context, and relevant memory.
- Character management: create, edit, delete, and import characters without losing existing data.
- Independent stories: characters, messages, and memories remain scoped to the correct story.
- Fandom import: search by character name and import from a Fandom article URL through supported public APIs; show source attribution and handle missing/blocked pages gracefully.
- Offline AI: a bundled native llama.cpp runtime loads a user-imported compatible GGUF model and generates replies without network access.
- Local persistence: conversations, profiles, lore references, model catalogue, and memory survive app restarts.
- Android delivery: reproducible APK build, stable signing key, install/upgrade verification, and release notes.

## Current verified baseline (2026-11-02)

- Native Kotlin + Jetpack Compose Android app exists.
- Room persistence, story/character/message/memory/model entities exist.
- Character editor, independent story UI, JSON character import, and GGUF file catalogue exist.
- GitHub Actions unit-test and APK build workflow passed on the latest main commit.
- Current chat still uses deterministic demo ReplyComposer.
- LocalInferenceEngine is only an interface/stub; llama.cpp JNI runtime is not bundled.
- Fandom importer accepts pasted article text; automatic API search, URL fetching, and parsing are not implemented.
- Stored memory notes are not automatically retrieved and injected into generation prompts.
- Release signing secrets may be absent; verify persistent signing before distributing updates.

## Work sequence

### 1. Release branch and baseline
- Work on `release/1.0.0`; keep `main` protected from unverified release changes.
- Re-run CI after each coherent implementation.
- Fix data migration, navigation, and persistence defects before adding more features.

### 2. Functional RP engine
- Replace demo ReplyComposer with a provider abstraction.
- Build structured prompts from character identity, personality, appearance, background, speech style, opening scene, story lore, conversation history, and selected memories.
- Ensure model replies are stored as normal messages.
- Provide clear loading, cancellation, and model-error states.

### 3. Offline llama.cpp runtime
- Add a pinned llama.cpp native build for Android ABIs supported by the app.
- Integrate CMake/JNI, model loading, token generation, cancellation, and native resource cleanup.
- Validate GGUF compatibility, model context limits, storage availability, and memory pressure.
- Test actual generation with airplane mode enabled on a physical device.
- Do not claim offline AI until this gate passes.

### 4. Fandom integration
- Implement Fandom/Miraheze-compatible MediaWiki API discovery and article retrieval where permitted.
- Support search by title and URL normalization.
- Parse article sections into structured character data and lore references.
- Preserve source URL and distinguish source facts from inferred summaries.
- Handle disambiguation, redirects, rate limits, missing pages, and network errors.
- Keep already imported lore usable offline.

### 5. Memory and story isolation
- Retrieve relevant memory notes per story/character and include them within a bounded prompt budget.
- Keep story histories isolated.
- Verify edits and deletes do not orphan or mix messages and memories.

### 6. Release hardening
- Test fresh install, upgrade, app restart, offline launch, model import/load/generation, Fandom import, and database migration.
- Verify stable APK signing and artifact availability.
- Publish known limitations and model compatibility guidance.
- Release only if all mandatory acceptance gates pass.

## Daily target checkpoints

- Nov 2: audit baseline, establish release branch, identify blockers.
- Nov 3: RP provider and prompt assembly; regression tests for story isolation.
- Nov 4: Fandom API search/retrieval/import flow.
- Nov 5: llama.cpp Android integration and first on-device inference.
- Nov 6: memory retrieval, model lifecycle, error handling, offline tests.
- Nov 7: regression, signing, upgrade and device testing.
- Nov 8: release candidate approval and APK publication only if all gates pass.

## Release acceptance checklist

- [ ] CI unit tests pass.
- [ ] Fresh APK installs and launches on a physical Android device.
- [ ] Existing user data survives an app update.
- [ ] Characters can be created, edited, deleted, and imported.
- [ ] Multiple stories remain independent.
- [ ] Fandom title search works.
- [ ] Fandom URL import works and records source attribution.
- [ ] Imported character lore is included in generated replies.
- [ ] Real model generation replaces the demo reply composer.
- [ ] A compatible GGUF model loads through the bundled llama.cpp runtime.
- [ ] Roleplay generation works in airplane mode after model import.
- [ ] Memory retrieval affects replies and remains story-scoped.
- [ ] Persistent signing key is used for the distributed APK.
- [ ] No critical crash, data-loss, or cross-story leakage remains.

If any mandatory item is unchecked, do not label the build Phoenix RP 1.0.0 complete.