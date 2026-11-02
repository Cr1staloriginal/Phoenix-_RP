package dev.cr1stalik.phoenixrp.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.cr1stalik.phoenixrp.domain.ReplyComposer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.UUID

class PhoenixViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = PhoenixDatabase.getInstance(application).phoenixDao()

    val characters = dao.observeCharacters().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )

    init {
        importLegacyPreferences()
    }

    fun messages(characterId: String): Flow<List<RpMessageEntity>> =
        dao.observeMessages(characterId)

    fun createCharacter(name: String, description: String, opening: String) {
        viewModelScope.launch {
            val id = UUID.randomUUID().toString()
            val firstLine = opening.trim().ifBlank { "Персонаж ждёт твоего первого слова." }
            dao.insertCharacter(
                RpCharacterEntity(
                    id = id,
                    name = name.trim(),
                    description = description.trim().ifBlank { "Описание пока не добавлено." },
                    opening = firstLine
                )
            )
            dao.insertMessage(
                RpMessageEntity(UUID.randomUUID().toString(), id, false, firstLine)
            )
        }
    }

    fun deleteCharacter(id: String) {
        viewModelScope.launch {
            dao.deleteMessages(id)
            dao.deleteCharacter(id)
        }
    }

    fun sendMessage(character: RpCharacterEntity, text: String) {
        val clean = text.trim()
        if (clean.isBlank()) return
        viewModelScope.launch {
            dao.insertMessage(
                RpMessageEntity(UUID.randomUUID().toString(), character.id, true, clean)
            )
            dao.insertMessage(
                RpMessageEntity(
                    UUID.randomUUID().toString(),
                    character.id,
                    false,
                    ReplyComposer.compose(character.name, character.description, clean)
                )
            )
        }
    }

    private fun importLegacyPreferences() {
        viewModelScope.launch {
            val prefs = getApplication<Application>()
                .getSharedPreferences("phoenix_rp", Application.MODE_PRIVATE)
            if (prefs.getBoolean("room_migration_complete", false)) return@launch

            val rawCharacters = prefs.getString("characters", "[]") ?: "[]"
            val ids = mutableListOf<String>()
            try {
                val array = JSONArray(rawCharacters)
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    val id = item.optString("id").ifBlank { UUID.randomUUID().toString() }
                    ids += id
                    dao.insertCharacter(
                        RpCharacterEntity(
                            id = id,
                            name = item.optString("name", "Персонаж"),
                            description = item.optString("description"),
                            opening = item.optString("opening")
                        )
                    )
                }
            } catch (_: Exception) {
                // Keep the app usable if an old preference value is malformed.
            }

            ids.forEach { id ->
                try {
                    val array = JSONArray(prefs.getString("messages_$id", "[]") ?: "[]")
                    for (index in 0 until array.length()) {
                        val item = array.getJSONObject(index)
                        dao.insertMessage(
                            RpMessageEntity(
                                id = UUID.randomUUID().toString(),
                                characterId = id,
                                fromUser = item.optBoolean("user"),
                                text = item.optString("text")
                            )
                        )
                    }
                } catch (_: Exception) {
                    // Ignore only the malformed legacy conversation.
                }
            }
            prefs.edit().putBoolean("room_migration_complete", true).apply()
        }
    }
}
