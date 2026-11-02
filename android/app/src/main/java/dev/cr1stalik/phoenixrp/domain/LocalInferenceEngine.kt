package dev.cr1stalik.phoenixrp.domain
/** Native inference boundary. The llama.cpp JNI library is a separate follow-up build task. */
interface LocalInferenceEngine {
 val isAvailable: Boolean
 suspend fun generate(modelPath: String, systemPrompt: String, conversation: List<Pair<Boolean, String>>, maxTokens: Int = 256): Result<String>
}
class LlamaCppEngine : LocalInferenceEngine {
 override val isAvailable: Boolean get() = try { System.loadLibrary("phoenix_llama"); true } catch (_: Throwable) { false }
 override suspend fun generate(modelPath: String, systemPrompt: String, conversation: List<Pair<Boolean, String>>, maxTokens: Int): Result<String> =
  Result.failure(UnsupportedOperationException("llama.cpp native runtime is not bundled in this build."))
}
