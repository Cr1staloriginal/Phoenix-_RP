package dev.cr1stalik.phoenixrp.domain

import dev.cr1stalik.phoenixrp.data.RpCharacterEntity

object CharacterJsonExporter {
    fun toJson(characters: List<RpCharacterEntity>): String =
        characters.joinToString(prefix = "[", postfix = "]") { character ->
            listOf(
                "name" to character.name,
                "description" to character.description,
                "appearance" to character.appearance,
                "background" to character.background,
                "speechStyle" to character.speechStyle,
                "opening" to character.opening
            ).joinToString(prefix = "{", postfix = "}") { (key, value) ->
                "${quote(key)}:${quote(value)}"
            }
        }

    private fun quote(value: String): String = buildString {
        append('"')
        value.forEach { char ->
            when (char) {
                '"' -> append("\\\"")
                '\\' -> append("\\\\")
                '\b' -> append("\\b")
                '\u000C' -> append("\\f")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> if (char.code < 0x20) append("\\u%04x".format(char.code)) else append(char)
            }
        }
        append('"')
    }
}
