package dev.cr1stalik.phoenixrp.domain

/**
 * Curated GGUF presets. Sizes are approximate and must be checked against
 * Content-Length while downloading; hardware recommendations are conservative.
 */
data class LocalModelPreset(
    val id: String,
    val title: String,
    val tier: String,
    val description: String,
    val repository: String,
    val fileName: String,
    val downloadUrl: String,
    val approximateBytes: Long,
    val minimumPhysicalRamBytes: Long,
    val minimumFreeStorageBytes: Long,
    val contextSize: Int = 2_048,
    val license: String = "Apache-2.0"
)

object LocalModelCatalog {
    private const val GB = 1_000_000_000L

    val presets = listOf(
        LocalModelPreset(
            id = "qwen3-0.6b-q4km",
            title = "Qwen3 0.6B · Lite",
            tier = "Лёгкая",
            description = "Для устройств с ограниченной памятью. Самая компактная, но и самая простая по качеству RP.",
            repository = "tensorblock/Qwen_Qwen3-0.6B-GGUF",
            fileName = "Qwen3-0.6B-Q4_K_M.gguf",
            downloadUrl = "https://huggingface.co/tensorblock/Qwen_Qwen3-0.6B-GGUF/resolve/main/Qwen3-0.6B-Q4_K_M.gguf",
            approximateBytes = 484_000_000L,
            minimumPhysicalRamBytes = 3L * GB,
            minimumFreeStorageBytes = 1L * GB
        ),
        LocalModelPreset(
            id = "qwen3-1.7b-q4km",
            title = "Qwen3 1.7B · Balanced",
            tier = "Сбалансированная",
            description = "Основная рекомендация для большинства телефонов с 6–8 ГБ физической RAM.",
            repository = "tensorblock/Qwen_Qwen3-1.7B-GGUF",
            fileName = "Qwen3-1.7B-Q4_K_M.gguf",
            downloadUrl = "https://huggingface.co/tensorblock/Qwen_Qwen3-1.7B-GGUF/resolve/main/Qwen3-1.7B-Q4_K_M.gguf",
            approximateBytes = 1_282_000_000L,
            minimumPhysicalRamBytes = 6L * GB,
            minimumFreeStorageBytes = 3L * GB
        ),
        LocalModelPreset(
            id = "qwen3-4b-q4km",
            title = "Qwen3 4B · Power",
            tier = "Мощная",
            description = "Более требовательная модель. Предлагается только устройствам с большим запасом RAM и свободного места.",
            repository = "Qwen/Qwen3-4B-GGUF",
            fileName = "Qwen3-4B-Q4_K_M.gguf",
            downloadUrl = "https://huggingface.co/Qwen/Qwen3-4B-GGUF/resolve/main/Qwen3-4B-Q4_K_M.gguf",
            approximateBytes = 2_497_000_000L,
            minimumPhysicalRamBytes = 12L * GB,
            minimumFreeStorageBytes = 5L * GB,
            contextSize = 2_048
        )
    )

    fun recommend(totalPhysicalRamBytes: Long, freeStorageBytes: Long): LocalModelPreset {
        return presets.asReversed().firstOrNull {
            totalPhysicalRamBytes >= it.minimumPhysicalRamBytes &&
                freeStorageBytes >= it.minimumFreeStorageBytes
        } ?: presets.first()
    }
}
