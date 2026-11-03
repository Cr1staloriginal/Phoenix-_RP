package dev.cr1stalik.phoenixrp.domain

import dev.cr1stalik.phoenixrp.data.RpCharacterEntity
import dev.cr1stalik.phoenixrp.data.RpMemoryEntity
import dev.cr1stalik.phoenixrp.data.RpMessageEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RpPromptBuilderTest {
    private val character = RpCharacterEntity(
        id = "fox", name = "Лис", description = "Осторожный и добрый",
        opening = "Лис сидит у окна.", storyId = "story",
        appearance = "Рыжий мех", background = "Живёт в лесу",
        speechStyle = "Говорит спокойно"
    )

    @Test
    fun includesCharacterProfileAndRelevantMemories() {
        val request = RpPromptBuilder.build(
            character,
            listOf(
                RpMemoryEntity("global", "story", null, "Пользователь любит чай", createdAt = 1),
                RpMemoryEntity("own", "story", "fox", "Лис обещал помочь", createdAt = 2),
                RpMemoryEntity("other", "story", "cat", "Секрет другого героя", createdAt = 3)
            ),
            emptyList()
        )

        assertTrue(request.systemPrompt.contains("Осторожный и добрый"))
        assertTrue(request.systemPrompt.contains("Рыжий мех"))
        assertTrue(request.systemPrompt.contains("Пользователь любит чай"))
        assertTrue(request.systemPrompt.contains("Лис обещал помочь"))
        assertFalse(request.systemPrompt.contains("Секрет другого героя"))
    }

    @Test
    fun keepsOnlyMostRecentMessagesInChronologicalOrder() {
        val history = (1..4).map {
            RpMessageEntity("m$it", "fox", it % 2 == 0, "message-$it", createdAt = it.toLong())
        }
        val request = RpPromptBuilder.build(character, emptyList(), history, maxHistoryMessages = 2)

        assertEquals(listOf(false to "message-3", true to "message-4"), request.conversation)
    }

    @Test
    fun limitsMemoryContextSize() {
        val request = RpPromptBuilder.build(
            character,
            listOf(RpMemoryEntity("m", "story", null, "x".repeat(500))),
            emptyList(),
            maxMemoryCharacters = 20
        )
        assertTrue(request.systemPrompt.length < 1_000)
    }
}
