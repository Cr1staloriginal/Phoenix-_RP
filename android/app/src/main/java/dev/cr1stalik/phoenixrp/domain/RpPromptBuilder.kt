package dev.cr1stalik.phoenixrp.domain

import dev.cr1stalik.phoenixrp.data.RpCharacterEntity
import dev.cr1stalik.phoenixrp.data.RpMemoryEntity
import dev.cr1stalik.phoenixrp.data.RpMessageEntity

data class RpInferenceRequest(
    val systemPrompt: String,
    /** true = user message, false = character message */
    val conversation: List<Pair<Boolean, String>>
)

/**
 * Builds a bounded, character-grounded prompt from the current story data.
 * Memories are filtered to global story notes plus notes for this character.
 */
object RpPromptBuilder {
    fun build(
        character: RpCharacterEntity,
        memories: List<RpMemoryEntity>,
        history: List<RpMessageEntity>,
        maxHistoryMessages: Int = 24,
        maxMemoryCharacters: Int = 8_000
    ): RpInferenceRequest {
        val relevantMemories = memories
            .filter { it.characterId == null || it.characterId == character.id }
            .sortedBy { it.createdAt }
        val memoryText = buildString {
            var remaining = maxMemoryCharacters
            for (memory in relevantMemories) {
                if (remaining <= 0) break
                val entry = memory.content.trim()
                if (entry.isEmpty()) continue
                val clipped = entry.take(remaining)
                if (isNotEmpty()) append("\n")
                append("- ").append(clipped)
                remaining -= clipped.length + 2
            }
        }.ifBlank { "Пока нет сохранённых воспоминаний." }

        val systemPrompt = buildString {
            append("Ты отыгрываешь персонажа «").append(character.name).append("» в текстовой RP-истории.\n")
            append("Оставайся в роли, соблюдай заданный характер и не выдумывай знания, которых у персонажа нет. ")
            append("Не управляй действиями и мыслями пользователя. Отвечай естественно, сохраняя непрерывность сцены.\n\n")
            append("Характер и личность:\n").append(character.description.ifBlank { "Не указано." })
            append("\n\nВнешность:\n").append(character.appearance.ifBlank { "Не указана." })
            append("\n\nБиография и прошлое:\n").append(character.background.ifBlank { "Не указаны." })
            append("\n\nМанера речи:\n").append(character.speechStyle.ifBlank { "Естественная, соответствующая характеру." })
            append("\n\nНачальная сцена:\n").append(character.opening.ifBlank { "Сцена ещё не задана." })
            append("\n\nВажный контекст истории и воспоминания:\n").append(memoryText)
        }

        val conversation = history
            .sortedBy { it.createdAt }
            .takeLast(maxHistoryMessages.coerceAtLeast(1))
            .map { it.fromUser to it.text }

        return RpInferenceRequest(systemPrompt, conversation)
    }
}
