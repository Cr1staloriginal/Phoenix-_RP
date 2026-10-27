package dev.cr1stalik.phoenixrp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private val PhoenixBg = Color(0xFF0A0B12)
private val PhoenixPanel = Color(0xFF151724)
private val PhoenixAccent = Color(0xFFB6A0FF)
private val PhoenixMuted = Color(0xFFA3A4B8)

private data class RpCharacter(
    val id: String,
    val name: String,
    val description: String,
    val opening: String
)

private data class RpMessage(val fromUser: Boolean, val text: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(background = PhoenixBg, surface = PhoenixPanel, primary = PhoenixAccent, onPrimary = Color(0xFF171126), onBackground = Color(0xFFF4F1FF), onSurface = Color(0xFFF4F1FF), secondary = PhoenixMuted)) {
                Surface(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF19162B), PhoenixBg))), color = PhoenixBg) {
                    PhoenixApp()
                }
            }
        }
    }
}

@Composable
private fun PhoenixApp() {
    val context = LocalContext.current
    val characters = remember {
        mutableStateListOf<RpCharacter>().apply { addAll(loadCharacters(context)) }
    }
    var screen by remember { mutableStateOf("home") }
    var activeId by remember { mutableStateOf<String?>(null) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var opening by remember { mutableStateOf("Вы встречаетесь в небольшом кафе поздним вечером.") }
    var draft by remember { mutableStateOf("") }
    val activeCharacter = characters.firstOrNull { it.id == activeId }
    val messages = remember(activeId) {
        mutableStateListOf<RpMessage>().apply {
            if (activeId != null) addAll(loadMessages(context, activeId!!))
            if (isEmpty() && activeCharacter != null) {
                add(RpMessage(false, activeCharacter.opening))
            }
        }
    }

    fun persistCharacters() = saveCharacters(context, characters)
    fun persistMessages() {
        val id = activeId ?: return
        saveMessages(context, id, messages)
    }

    when (screen) {
        "create" -> Column(
            Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF19162B), PhoenixBg))).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("PHOENIX RP  /  НОВАЯ ИСТОРИЯ", color = PhoenixAccent, style = MaterialTheme.typography.labelMedium)
            Text("Создать персонажа", style = MaterialTheme.typography.headlineMedium)
            Text("Опиши героя и точку, с которой начнётся ваша история.", color = PhoenixMuted)
            OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Имя персонажа") }, singleLine = true, shape = RoundedCornerShape(16.dp))
            OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth(), label = { Text("Характер и описание") }, minLines = 3, shape = RoundedCornerShape(16.dp))
            OutlinedTextField(opening, { opening = it }, Modifier.fillMaxWidth(), label = { Text("Начало сцены") }, minLines = 3, shape = RoundedCornerShape(16.dp))
            Button(
                onClick = {
                    val safeName = name.trim()
                    if (safeName.isNotEmpty()) {
                        characters.add(RpCharacter(UUID.randomUUID().toString(), safeName,
                            description.trim().ifEmpty { "Описание пока не добавлено." },
                            opening.trim().ifEmpty { "Персонаж смотрит на тебя, ожидая первого слова." }))
                        persistCharacters()
                        name = ""; description = ""
                        opening = "Вы встречаетесь в небольшом кафе поздним вечером."
                        screen = "home"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.trim().isNotEmpty()
            ) { Text("Сохранить персонажа") }
            OutlinedButton(onClick = { screen = "home" }, modifier = Modifier.fillMaxWidth()) { Text("Назад") }
        }

        "chat" -> {
            if (activeCharacter == null) {
                Column(Modifier.fillMaxSize().padding(20.dp)) {
                    Text("Персонаж не найден")
                    Button(onClick = { screen = "home" }) { Text("На главную") }
                }
            } else Column(Modifier.fillMaxSize().padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(activeCharacter.name, style = MaterialTheme.typography.headlineSmall)
                        Text(activeCharacter.description, style = MaterialTheme.typography.bodySmall)
                    }
                    TextButton(onClick = { persistMessages(); screen = "home" }) { Text("Готово") }
                }
                Spacer(Modifier.padding(4.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { message ->
                        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = PhoenixPanel)) {
                            Column(Modifier.padding(12.dp)) {
                                Text(if (message.fromUser) "Ты" else activeCharacter.name,
                                    style = MaterialTheme.typography.labelMedium)
                                Text(message.text)
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Твоё сообщение") }
                )
                Button(
                    onClick = {
                        val text = draft.trim()
                        if (text.isNotEmpty()) {
                            messages.add(RpMessage(true, text))
                            messages.add(RpMessage(false, makePrototypeReply(activeCharacter, text)))
                            draft = ""
                            persistMessages()
                        }
                    },
                    enabled = draft.trim().isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Отправить") }
                Text("ДЕМО-РЕЖИМ · ИИ-движок ещё не подключён", color = PhoenixMuted,
                    style = MaterialTheme.typography.labelSmall)
            }
        }

        else -> Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("PHOENIX RP", color = PhoenixAccent, style = MaterialTheme.typography.labelLarge)
            Text("Твоя история.", style = MaterialTheme.typography.headlineLarge)
            Text("Персонажи. Миры. Ролевые сцены.", color = PhoenixMuted)
            
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.background(Brush.linearGradient(listOf(Color(0xFF34264D), PhoenixPanel))).padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("ТВОЯ ВСЕЛЕННАЯ", color = PhoenixAccent, style = MaterialTheme.typography.labelMedium)
                    Text("Персонажи: ${characters.size}", style = MaterialTheme.typography.titleLarge)
                    Text("Создавай героев, задавай им характер и начинай сцену.")
                    Button(onClick = { screen = "create" }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = PhoenixAccent), shape = RoundedCornerShape(14.dp)) {
                        Text("＋ Создать персонажа")
                    }
                }
            }
            if (characters.isEmpty()) {
                Text("Пока нет персонажей. Создай первого — он сохранится на устройстве.")
            } else {
                Text("Мои персонажи", style = MaterialTheme.typography.titleLarge)
                Text("${characters.size} всего", color = PhoenixMuted, style = MaterialTheme.typography.labelSmall)
                characters.toList().forEach { character ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("✦  ПЕРСОНАЖ", color = PhoenixAccent, style = MaterialTheme.typography.labelSmall)
                            Text(character.name, style = MaterialTheme.typography.titleMedium)
                            Text(character.description)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = {
                                    activeId = character.id
                                    screen = "chat"
                                }, modifier = Modifier.weight(1f)) { Text("Продолжить историю") }
                                OutlinedButton(onClick = {
                                    characters.removeAll { it.id == character.id }
                                    persistCharacters()
                                    context.getSharedPreferences("phoenix_rp", Context.MODE_PRIVATE)
                                        .edit().remove("messages_${character.id}").apply()
                                    if (activeId == character.id) activeId = null
                                }) { Text("Удалить") }
                            }
                        }
                    }
                }
            }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Локальное хранение", style = MaterialTheme.typography.titleMedium)
                    Text("Персонажи и переписки сохраняются на телефоне и доступны после перезапуска.")
                    Text("ИИ: ещё не подключён", style = MaterialTheme.typography.labelMedium)
                }
            }
            Text("PHOENIX RP  ·  ПРОТОТИП 0.3.0", color = PhoenixMuted, style = MaterialTheme.typography.labelSmall)
        }
    }
}

private fun makePrototypeReply(character: RpCharacter, userText: String): String {
    val line = userText.trim().trimEnd('.', '!', '?', '…')
    return when {
        line.contains("привет", ignoreCase = true) || line.contains("здравств", ignoreCase = true) ->
            "${character.name} отвечает на приветствие и внимательно смотрит на тебя. «Рад(а) тебя видеть. Что привело тебя сюда?»"
        line.contains("кто ты", ignoreCase = true) ->
            "«Я — ${character.name}». Персонаж делает небольшую паузу. ${character.description}"
        line.contains("почему", ignoreCase = true) ->
            "${character.name} задумывается над твоим вопросом. «Хороший вопрос. Дай мне немного времени, чтобы всё объяснить»."
        else ->
            "${character.name} реагирует на твои слова: «${line.take(180)}». Персонаж ждёт, что ты сделаешь дальше."
    }
}

private fun loadCharacters(context: Context): List<RpCharacter> {
    return try {
        val raw = context.getSharedPreferences("phoenix_rp", Context.MODE_PRIVATE)
            .getString("characters", "[]") ?: "[]"
        val array = JSONArray(raw)
        (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            RpCharacter(obj.getString("id"), obj.getString("name"),
                obj.optString("description"), obj.optString("opening"))
        }
    } catch (_: Exception) { emptyList() }
}

private fun saveCharacters(context: Context, characters: List<RpCharacter>) {
    val array = JSONArray()
    characters.forEach { c ->
        array.put(JSONObject().put("id", c.id).put("name", c.name)
            .put("description", c.description).put("opening", c.opening))
    }
    context.getSharedPreferences("phoenix_rp", Context.MODE_PRIVATE).edit()
        .putString("characters", array.toString()).apply()
}

private fun loadMessages(context: Context, id: String): List<RpMessage> {
    return try {
        val raw = context.getSharedPreferences("phoenix_rp", Context.MODE_PRIVATE)
            .getString("messages_$id", "[]") ?: "[]"
        val array = JSONArray(raw)
        (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            RpMessage(obj.optBoolean("user"), obj.optString("text"))
        }
    } catch (_: Exception) { emptyList() }
}

private fun saveMessages(context: Context, id: String, messages: List<RpMessage>) {
    val array = JSONArray()
    messages.forEach { m -> array.put(JSONObject().put("user", m.fromUser).put("text", m.text)) }
    context.getSharedPreferences("phoenix_rp", Context.MODE_PRIVATE).edit()
        .putString("messages_$id", array.toString()).apply()
}
