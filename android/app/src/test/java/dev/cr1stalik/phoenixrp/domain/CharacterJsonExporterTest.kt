package dev.cr1stalik.phoenixrp.domain

import dev.cr1stalik.phoenixrp.data.RpCharacterEntity
import org.junit.Assert.assertTrue
import org.junit.Test

class CharacterJsonExporterTest {
    @Test fun exportsEveryEditableCharacterFieldAsJson() {
        val json = CharacterJsonExporter.toJson(listOf(
            RpCharacterEntity("id", "Mira", "Calm", "Opening", appearance = "Silver fur", background = "From north", speechStyle = "Quiet")
        ))
        assertTrue(json.startsWith("["))
        assertTrue(json.contains("\"name\":\"Mira\""))
        assertTrue(json.contains("\"description\":\"Calm\""))
        assertTrue(json.contains("\"appearance\":\"Silver fur\""))
        assertTrue(json.contains("\"background\":\"From north\""))
        assertTrue(json.contains("\"speechStyle\":\"Quiet\""))
        assertTrue(json.contains("\"opening\":\"Opening\""))
    }

    @Test fun escapesQuotesAndNewlines() {
        val json = CharacterJsonExporter.toJson(listOf(
            RpCharacterEntity("id", "A\"B", "line1\nline2", "")
        ))
        assertTrue(json.contains("A\\\"B"))
        assertTrue(json.contains("line1\\nline2"))
    }
}
