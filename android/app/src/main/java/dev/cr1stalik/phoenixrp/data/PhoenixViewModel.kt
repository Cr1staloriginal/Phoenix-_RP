package dev.cr1stalik.phoenixrp.data
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.cr1stalik.phoenixrp.domain.ReplyComposer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.util.UUID
class PhoenixViewModel(application: Application) : AndroidViewModel(application) {
 private val dao = PhoenixDatabase.getInstance(application).phoenixDao()
 val stories = dao.observeStories().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
 val characters = dao.observeAllCharacters().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
 val models = dao.observeModels().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
 init { ensureDefaultStory(); importLegacyPreferences() }
 private fun ensureDefaultStory() { viewModelScope.launch { dao.ensureStory(RpStoryEntity("default", "Моя первая история", "Личная RP-вселенная")) } }
 fun createStory(title: String, description: String = "") { if (title.isBlank()) return; viewModelScope.launch { dao.insertStory(RpStoryEntity(UUID.randomUUID().toString(), title.trim(), description.trim())) } }
 fun deleteStory(id: String) { if (id == "default") return; viewModelScope.launch { characters.value.filter { it.storyId == id }.forEach { deleteCharacter(it.id) }; dao.deleteStory(id) } }
 fun characters(storyId: String): Flow<List<RpCharacterEntity>> = dao.observeCharacters(storyId)
 fun messages(characterId: String): Flow<List<RpMessageEntity>> = dao.observeMessages(characterId)
 fun memories(storyId: String): Flow<List<RpMemoryEntity>> = dao.observeMemories(storyId)
 fun createCharacter(storyId: String, name: String, description: String, appearance: String, background: String, speechStyle: String, opening: String) {
  if (name.isBlank()) return
  viewModelScope.launch {
   val id = UUID.randomUUID().toString(); val first = opening.trim().ifBlank { "Персонаж ждёт твоего первого слова." }
   dao.insertCharacter(RpCharacterEntity(id, name.trim(), description.trim(), first, storyId = storyId, appearance = appearance.trim(), background = background.trim(), speechStyle = speechStyle.trim()))
   dao.insertMessage(RpMessageEntity(UUID.randomUUID().toString(), id, false, first))
  }
 }
 fun importCharactersJson(storyId: String, raw: String, onResult: (Boolean, String) -> Unit) {
  viewModelScope.launch {
   val result = runCatching {
    val parsed = JSONTokener(raw).nextValue()
    val items = when (parsed) {
     is JSONArray -> (0 until parsed.length()).map { parsed.getJSONObject(it) }
     is JSONObject -> listOf(parsed)
     else -> error("Ожидается JSON-объект или массив объектов.")
    }
    require(items.isNotEmpty()) { "В JSON нет персонажей." }
    items.forEach { item ->
     val name = item.optString("name").trim()
     require(name.isNotBlank()) { "У каждого персонажа должно быть поле name." }
     val id = UUID.randomUUID().toString()
     val opening = item.optString("opening", "Персонаж ждёт твоего первого слова.")
     dao.insertCharacter(RpCharacterEntity(
      id = id, name = name, description = item.optString("description"),
      opening = opening, storyId = storyId, appearance = item.optString("appearance"),
      background = item.optString("background"), speechStyle = item.optString("speechStyle")
     ))
     if (opening.isNotBlank()) dao.insertMessage(RpMessageEntity(UUID.randomUUID().toString(), id, false, opening))
    }
    items.size
   }
   result.fold(
    { onResult(true, "Импортировано персонажей: $it") },
    { onResult(false, it.message ?: "Ошибка импорта JSON") }
   )
  }
 }
 fun updateCharacter(character: RpCharacterEntity) { viewModelScope.launch { dao.insertCharacter(character) } }
 fun deleteCharacter(id: String) { viewModelScope.launch { dao.deleteMessages(id); dao.deleteCharacter(id) } }
 fun sendMessage(character: RpCharacterEntity, text: String) {
  val clean = text.trim(); if (clean.isBlank()) return
  viewModelScope.launch {
   dao.insertMessage(RpMessageEntity(UUID.randomUUID().toString(), character.id, true, clean))
   dao.insertMessage(RpMessageEntity(UUID.randomUUID().toString(), character.id, false, ReplyComposer.compose(character.name, character.description, clean)))
  }
 }
 fun addMemory(storyId: String, characterId: String?, content: String, source: String = "manual") {
  if (content.isBlank()) return
  viewModelScope.launch { dao.insertMemory(RpMemoryEntity(UUID.randomUUID().toString(), storyId, characterId, content.trim(), source)) }
 }

 fun importFandomUrl(storyId: String, url: String, onResult: (Boolean, String) -> Unit) {
  viewModelScope.launch {
   val result = withContext(Dispatchers.IO) { dev.cr1stalik.phoenixrp.domain.FandomTextImporter.fetchArticle(url) }
   result.fold(
    { reference ->
     dao.insertMemory(RpMemoryEntity(UUID.randomUUID().toString(), storyId, null,
      "${reference.title}\nИсточник: ${reference.sourceUrl}\n${reference.text}", "fandom"))
     onResult(true, "Статья «${reference.title}» импортирована в память истории.")
    },
    { error -> onResult(false, error.message ?: "Не удалось загрузить статью Fandom.") }
   )
  }
 }
 fun addModel(model: LocalModelEntity) { viewModelScope.launch { dao.insertModel(model) } }
 fun deleteModel(model: LocalModelEntity) { viewModelScope.launch { runCatching { java.io.File(model.filePath).delete() }; dao.deleteModel(model.id) } }
 private fun importLegacyPreferences() {
  viewModelScope.launch {
   val prefs = getApplication<Application>().getSharedPreferences("phoenix_rp", Application.MODE_PRIVATE)
   if (prefs.getBoolean("room_migration_complete", false)) return@launch
   val ids = mutableListOf<String>()
   try {
    val array = JSONArray(prefs.getString("characters", "[]") ?: "[]")
    for (i in 0 until array.length()) {
     val item = array.getJSONObject(i); val id = item.optString("id").ifBlank { UUID.randomUUID().toString() }; ids += id
     dao.insertCharacter(RpCharacterEntity(id, item.optString("name", "Персонаж"), item.optString("description"), item.optString("opening")))
    }
   } catch (_: Exception) { }
   ids.forEach { id -> try {
    val array = JSONArray(prefs.getString("messages_$id", "[]") ?: "[]")
    for (i in 0 until array.length()) { val item = array.getJSONObject(i); dao.insertMessage(RpMessageEntity(UUID.randomUUID().toString(), id, item.optBoolean("user"), item.optString("text"))) }
   } catch (_: Exception) { } }
   prefs.edit().putBoolean("room_migration_complete", true).apply()
  }
 }
}
