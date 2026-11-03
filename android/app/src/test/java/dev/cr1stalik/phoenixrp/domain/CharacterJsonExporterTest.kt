package dev.cr1stalik.phoenixrp.domain

import dev.cr1stalik.phoenixrp.data.RpCharacterEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.json.JSONArray

class CharacterJsonExporterTest {
    @Test fun exportsEveryEditableCharacterFieldAsImportableArray() {
        val json = CharacterJsonExporter.toJson(listOf(
            RpCharacterEntity("id", "Mira", "Calm", "Opening", appearance = "Silver fur", background = "From north", speechStyle = "Quiet")
        ))
        val item = JSONArray(json).getJSONObject(0)
        assertEquals("Mira", item.getString("name"))
        assertEquals("Calm", item.getString("description"))
        assertEquals("Silver fur", item.getString("appearance"))
        assertEquals("From north", item.getString("background"))
        assertEquals("Quiet", item.getString("speechStyle"))
        assertEquals("Opening", item.getString("opening"))
        assertTrue(json.startsWith("["))
    }
}
