package dev.cr1stalik.phoenixrp.domain

import dev.cr1stalik.phoenixrp.data.RpCharacterEntity
import dev.cr1stalik.phoenixrp.data.RpMemoryEntity
import dev.cr1stalik.phoenixrp.data.RpMessageEntity
import dev.cr1stalik.phoenixrp.data.RpStoryEntity

/**
 * Builds a bounded, structured context for a local roleplay model.
 *
 * This class is deliberately independent from the inference runtime so the prompt
 * contract can be tested before llama.cpp is integrated.
 */
object RpPromptBuilder {
    data class Context(
        val systemPrompt: String,
        val conversation: List<Pair<Boolean, String>>
    )

    fun build(
        character: RpCharacterEntity,
        story: RpStoryEntity?,
        memories: List<RpMemoryEntity>,
        messages: List<RpMessageEntity>,
        maxHistoryMessages: Int = 24,
        maxMemoryCharacters: Int = 4_500
    ): Context {
        val selectedMemories = selectMemories(
            memories = memories.filter {
                it.storyId == character.storyId &&
                    (it.characterId == null || it.characterId == character.id)
            },
            messages = messages,
            maxCharacters = maxMemoryCharacters
        )

        val prompt = buildString {
            appendLine("Ты — персонаж для текстовой ролевой игры.")
            appendLine("Отыгрывай только указанного персонажа и сохраняй последовательность событий.")
            appendLine("Не управляй персонажем пользователя: не придумывай за него реплики, решения, чувства или действия.")
            appendLine("Считай сведения из профиля, мира и памяти основным каноном этой истории.")
            appendLine("Не выдавай догадки за установленные факты. Если сведений нет, не придумывай прошлое или отношения.")
            appendLine("Не ломай сцену внеигровыми пояснениями; отвечай естественно, в рамках текущего эпизода.")
            appendLine()
            appendLine("[ПЕРСОНАЖ]")
            appendField("Имя", character.name)
            appendField("Характер и личность", character.description)
            appendField("Внешность", character.appearance)
            appendField("Биография и прошлое", character.background)
            appendField("Манера речи", character.speechStyle)
            appendField("Начальная сцена", character.opening)
            appendLine()
            appendLine("[МИР ИСТОРИИ]")
            appendField("Название", story?.title.orEmpty())
            appendField("Описание мира", story?.description.orEmpty())
            appendLine()
            appendLine("[ДОЛГОСРОЧНАЯ ПАМЯТЬ]")
            if (selectedMemories.isEmpty()) {
                appendLine("Сохранённых заметок для этого персонажа пока нет.")
            } else {
                selectedMemories.forEachIndexed { index, memory ->
                    appendLine("${index + 1}. ${memory.content}")
                    if (memory.source == "fandom") {
                        appendLine("   Примечание: импортированный справочный материал; учитывай его как источник лора.")
                    }
                }
            }
            appendLine()
            appendLine("Соблюдай непротиворечивость между профилем, историей, предыдущими сообщениями и памятью.")
        }.trim()

        val conversation = messages
            .sortedBy { it.createdAt }
            .takeLast(maxHistoryMessages.coerceAtLeast(1))
            .map { it.fromUser to it.text }

        return Context(prompt, conversation)
    }

    private fun StringBuilder.appendField(label: String, value: String) {
        if (value.isNotBlank()) {
            appendLine("$label: ${value.trim().take(MAX_FIELD_CHARACTERS)}")
        }
    }

    private fun selectMemories(
        memories: List<RpMemoryEntity>,
        messages: List<RpMessageEntity>,
        maxCharacters: Int
    ): List<RpMemoryEntity> {
        if (maxCharacters <= 0 || memories.isEmpty()) return emptyList()

        val query = messages.asSequence()
            .filter { it.fromUser }
            .takeLast(4)
            .joinToString(" ") { it.text.lowercase() }
        val terms = query.split(Regex("[^\\p{L}\\p{N}]+"))
            .filter { it.length >= 3 }
            .toSet()

        val ranked = memories
            .distinctBy { it.id }
            .mapIndexed { index, memory ->
                val content = memory.content.lowercase()
                val score = terms.count { term -> content.contains(term) }
                Triple(memory, score, index)
            }
            .sortedWith(compareByDescending<Triple<RpMemoryEntity, Int, Int>> { it.second }
                .thenByDescending { it.first.createdAt }
                .thenBy { it.third })

        val selected = mutableListOf<RpMemoryEntity>()
        var used = 0
        for ((memory, _, _) in ranked) {
            val remaining = maxCharacters - used
            if (remaining <= 0) break
            val content = memory.content.trim()
            if (content.isEmpty()) continue
            val bounded = if (content.length > remaining) content.take(remaining) else content
            selected += memory.copy(content = bounded)
            used += bounded.length
        }
        return selected
    }

    private const val MAX_FIELD_CHARACTERS = 3_000
}
