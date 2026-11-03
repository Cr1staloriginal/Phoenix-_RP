package dev.cr1stalik.phoenixrp.domain

import dev.cr1stalik.phoenixrp.data.RpCharacterEntity
import org.json.JSONArray
import org.json.JSONObject

object CharacterJsonExporter {
    fun toJson(characters: List<RpCharacterEntity>): String {
        val array = JSONArray()
        characters.forEach { character ->
            array.put(JSONObject()
                .put("name", character.name)
                .put("description", character.description)
                .put("appearance", character.appearance)
                .put("background", character.background)
                .put("speechStyle", character.speechStyle)
                .put("opening", character.opening))
        }
        return array.toString(2)
    }
}
