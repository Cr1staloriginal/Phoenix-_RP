package dev.cr1stalik.phoenixrp.domain

import dev.cr1stalik.phoenixrp.data.RpCharacterEntity
import dev.cr1stalik.phoenixrp.data.RpMemoryEntity
import dev.cr1stalik.phoenixrp.data.RpMessageEntity
import dev.cr1stalik.phoenixrp.data.RpStoryEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RpPromptBuilderTest {
    private val character = RpCharacterEntity(
        id = "char-1",
        name = "Мира",
        description = "Осторожная и наблюдательная",
        opening = "Мира стоит у окна.",
        storyId = "story-1",
        appearance = "Серебристые волосы",
        background = "Родилась в северном городе",
        speechStyle = "Говорит спокойно"
    )

    @Test
    fun includesCharacterAndStoryCanon() {
        val context = RpPromptBuilder.build(
            character = character,
            story = RpStoryEntity("story-1", "Север", "Город среди снежных гор"),
            memories = emptyList(),
            messages = emptyList()
        )

        assertTrue(context.systemPrompt.contains("Мира"))
        assertTrue(context.systemPrompt.contains("Серебристые волосы"))
        assertTrue(context.systemPrompt.contains("Город среди снежных гор"))
        assertTrue(context.systemPrompt.contains("Не управляй персонажем пользователя"))
    }

    @Test
    fun excludesMemoriesFromOtherStoriesAndCharacters() {
        val memories = listOf(
            RpMemoryEntity("right", "story-1", null, "В городе начался фестиваль"),
            RpMemoryEntity("wrong-story", "story-2", null, "Секрет из другой истории"),
            RpMemoryEntity("wrong-character", "story-1", "char-2", "Чужое личное воспоминание")
        )

        val context = RpPromptBuilder.build(character, null, memories, emptyList())

        assertTrue(context.systemPrompt.contains("фестиваль"))
        assertFalse(context.systemPrompt.contains("Секрет из другой истории"))
        assertFalse(context.systemPrompt.contains("Чужое личное воспоминание"))
    }

    @Test
    fun prioritizesMemoriesRelatedToRecentConversation() {
        val memories = listOf(
            RpMemoryEntity("old", "story-1", null, "Мира любит читать книги", createdAt = 1),
            RpMemoryEntity("relevant", "story-1", null, "Фестиваль проходит на центральной площади", createdAt = 2)
        )
        val messages = listOf(
            RpMessageEntity("m1", character.id, true, "Расскажи про фестиваль на площади")
        )

        val context = RpPromptBuilder.build(character, null, memories, messages)
        val festivalIndex = context.systemPrompt.indexOf("Фестиваль проходит")
        val booksIndex = context.systemPrompt.indexOf("Мира любит читать")

        assertTrue(festivalIndex >= 0)
        assertTrue(booksIndex > festivalIndex)
    }

    @Test
    fun keepsOnlyTheNewestBoundedConversationMessages() {
        val messages = (1..5).map {
            RpMessageEntity("m$it", character.id, it % 2 == 1, "Сообщение $it", createdAt = it.toLong())
        }

        val context = RpPromptBuilder.build(character, null, emptyList(), messages, maxHistoryMessages = 2)

        assertEquals(2, context.conversation.size)
        assertEquals("Сообщение 4", context.conversation[0].second)
        assertEquals("Сообщение 5", context.conversation[1].second)
    }

    @Test
    fun memoryContextNeverExceedsConfiguredCharacterBudget() {
        val memories = listOf(
            RpMemoryEntity("one", "story-1", null, "А".repeat(100)),
            RpMemoryEntity("two", "story-1", null, "Б".repeat(100))
        )

        val context = RpPromptBuilder.build(character, null, memories, emptyList(), maxMemoryCharacters = 120)

        val section = context.systemPrompt.substringAfter("[ДОЛГОСРОЧНАЯ ПАМЯТЬ]")
            .substringBefore("Соблюдай непротиворечивость")
        assertEquals(120, section.count { it == 'А' || it == 'Б' })
    }
}
