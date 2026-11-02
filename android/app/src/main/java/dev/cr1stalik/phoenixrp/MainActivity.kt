package dev.cr1stalik.phoenixrp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.cr1stalik.phoenixrp.data.*
import dev.cr1stalik.phoenixrp.domain.GgufModelInspector
import dev.cr1stalik.phoenixrp.domain.FandomTextImporter
import java.io.File
import java.util.UUID

private val Ink = androidx.compose.ui.graphics.Color(0xFF171923)
private val Violet = androidx.compose.ui.graphics.Color(0xFF7866D8)
private val Muted = androidx.compose.ui.graphics.Color(0xFF777B8B)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent { PhoenixTheme { PhoenixApp() } }
    }
}

@Composable
private fun PhoenixTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences("phoenix_ui", android.content.Context.MODE_PRIVATE) }
    var dark by rememberSaveable { mutableStateOf(preferences.getBoolean("dark_theme", true)) }
    val colors = if (dark) darkColorScheme(
        primary = androidx.compose.ui.graphics.Color(0xFFB5A6FF),
        secondary = androidx.compose.ui.graphics.Color(0xFF9CB9F5),
        background = androidx.compose.ui.graphics.Color(0xFF101116),
        surface = androidx.compose.ui.graphics.Color(0xFF1A1D26),
        onBackground = androidx.compose.ui.graphics.Color(0xFFF1F0F7),
        onSurface = androidx.compose.ui.graphics.Color(0xFFF1F0F7)
    ) else lightColorScheme(
        primary = androidx.compose.ui.graphics.Color(0xFF6250B5),
        secondary = androidx.compose.ui.graphics.Color(0xFF496A9C),
        background = androidx.compose.ui.graphics.Color(0xFFF6F5FA),
        surface = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
        onBackground = androidx.compose.ui.graphics.Color(0xFF20212A),
        onSurface = androidx.compose.ui.graphics.Color(0xFF20212A)
    )
    MaterialTheme(colorScheme = colors) {
        CompositionLocalProvider(LocalThemeToggle provides { dark = !dark; preferences.edit().putBoolean("dark_theme", dark).apply() }) { content() }
    }
}

private val LocalThemeToggle = androidx.compose.runtime.staticCompositionLocalOf<() -> Unit> { {} }

@Composable
private fun PhoenixApp(vm: PhoenixViewModel = viewModel()) {
    val context = LocalContext.current
    val stories by vm.stories.collectAsStateWithLifecycle()
    val models by vm.models.collectAsStateWithLifecycle()
    var storyId by rememberSaveable { mutableStateOf("default") }
    var screen by rememberSaveable { mutableStateOf("stories") }
    var activeCharacter by remember { mutableStateOf<RpCharacterEntity?>(null) }
    var editingCharacter by remember { mutableStateOf<RpCharacterEntity?>(null) }
    val characters by vm.characters(storyId).collectAsStateWithLifecycle(emptyList())
    val openModel = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) runCatching {
            val dir = File(context.filesDir, "models").apply { mkdirs() }
            val name = "model_${System.currentTimeMillis()}.gguf"
            val file = File(dir, name)
            context.contentResolver.openInputStream(uri)?.use { input -> file.outputStream().use(input::copyTo) }
                ?: error("Не удалось прочитать файл")
            val info = GgufModelInspector.inspect(file)
            if (!info.isGguf) { file.delete(); error(info.warning ?: "Неверный GGUF") }
            vm.addModel(LocalModelEntity(UUID.randomUUID().toString(), name, file.absolutePath, info.sizeBytes))
        }.onFailure { android.widget.Toast.makeText(context, it.message ?: "Ошибка импорта", android.widget.Toast.LENGTH_LONG).show() }
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        BoxWithConstraints(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
            val contentWidth = if (maxWidth > 700.dp) 680.dp else maxWidth
            Column(
                Modifier.widthIn(max = contentWidth).fillMaxWidth().align(Alignment.TopCenter)
                    .fillMaxHeight().imePadding().padding(horizontal = if (maxWidth < 360.dp) 12.dp else 18.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (screen == "chat" && activeCharacter != null) {
                    ChatScreen(activeCharacter!!, vm, onBack = { screen = "characters" })
                } else {
                    Header(onTheme = LocalThemeToggle.current)
                    when (screen) {
                        "stories" -> StoriesScreen(stories, storyId, onSelect = { storyId = it; screen = "characters" }, onCreate = vm::createStory)
                        "characters" -> CharactersScreen(
                            storyId, characters,
                            onCreate = { editingCharacter = null; screen = "editor" },
                            onEdit = { editingCharacter = it; screen = "editor" },
                            onChat = { activeCharacter = it; screen = "chat" },
                            onDelete = vm::deleteCharacter,
                            onImport = { raw, done -> vm.importCharactersJson(storyId, raw, done) },
                            onBack = { screen = "stories" }
                        )
                        "editor" -> CharacterEditor(
                            editingCharacter,
                            onSave = { name, desc, appearance, background, speech, opening ->
                                val old = editingCharacter
                                if (old == null) vm.createCharacter(storyId, name, desc, appearance, background, speech, opening)
                                else vm.updateCharacter(old.copy(name = name, description = desc, appearance = appearance, background = background, speechStyle = speech, opening = opening))
                                screen = "characters"
                            },
                            onBack = { screen = "characters" }
                        )
                        "models" -> ModelsScreen(models, onImport = { openModel.launch(arrayOf("application/octet-stream", "*/*")) }, onDelete = vm::deleteModel)
                        "memory" -> MemoryScreen(storyId, vm)
                        "fandom" -> FandomScreen(storyId, vm)
                    }
                    if (screen != "editor") {
                        Spacer(Modifier.weight(1f, fill = false))
                        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                            listOf("stories" to "Истории", "characters" to "Герои", "memory" to "Память", "models" to "Модели", "fandom" to "Fandom").forEach { (key, label) ->
                                NavigationBarItem(selected = screen == key, onClick = { screen = key }, icon = {}, label = { Text(label, maxLines = 1) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(onTheme: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text("PHOENIX RP", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text("Твоя история начинается здесь", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        TextButton(onClick = onTheme) { Text("Тема") }
    }
}

@Composable
private fun StoriesScreen(stories: List<RpStoryEntity>, selected: String, onSelect: (String) -> Unit, onCreate: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Независимые истории", style = MaterialTheme.typography.headlineSmall)
        Text("У каждой истории свои персонажи, диалоги и заметки.", color = Muted)
        OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text("Название новой истории") }, singleLine = true)
        OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth(), label = { Text("Описание мира (необязательно)") }, minLines = 2)
        Button(onClick = { onCreate(title, description); title = ""; description = "" }, enabled = title.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Создать историю") }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f, fill = false)) {
            items(stories, key = { it.id }) { story ->
                Card(modifier = Modifier.fillMaxWidth().clickable { onSelect(story.id) }) {
                    Column(Modifier.padding(16.dp)) {
                        Text(story.title, style = MaterialTheme.typography.titleMedium)
                        if (story.description.isNotBlank()) Text(story.description, color = Muted)
                        Text(if (story.id == selected) "Выбрана" else "Открыть", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun CharactersScreen(storyId: String, characters: List<RpCharacterEntity>, onCreate: () -> Unit, onEdit: (RpCharacterEntity) -> Unit, onChat: (RpCharacterEntity) -> Unit, onDelete: (String) -> Unit, onImport: (String, (Boolean, String) -> Unit) -> Unit, onBack: () -> Unit) {
    var showImport by remember { mutableStateOf(false) }
    var importText by remember { mutableStateOf("") }
    var importError by remember { mutableStateOf<String?>(null) }
    if (showImport) {
        AlertDialog(
            onDismissRequest = { showImport = false },
            title = { Text("Импорт персонажей") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Вставь JSON-объект или массив персонажей. Поддерживаются поля name, description, appearance, background, speechStyle и opening.")
                    OutlinedTextField(importText, { importText = it }, label = { Text("JSON") }, minLines = 5, maxLines = 9)
                    importError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = { TextButton(onClick = {
                onImport(importText) { success, message ->
                    if (success) { showImport = false; importText = ""; importError = null }
                    else importError = message
                }
            }) { Text("Импортировать") } },
            dismissButton = { TextButton(onClick = { showImport = false }) { Text("Отмена") } }
        )
    }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Персонажи", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = onBack) { Text("Истории") }
        }
        Button(onClick = onCreate, modifier = Modifier.fillMaxWidth()) { Text("＋ Новый персонаж") }
        OutlinedButton(onClick = { showImport = true; importError = null }, modifier = Modifier.fillMaxWidth()) { Text("Импортировать из JSON") }
        if (characters.isEmpty()) Text("В этой истории пока нет персонажей.", color = Muted)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(characters, key = { it.id }) { character ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text(character.name, style = MaterialTheme.typography.titleMedium)
                        Text(character.description.ifBlank { "Описание не заполнено" }, color = Muted, maxLines = 2)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { onChat(character) }, modifier = Modifier.weight(1f)) { Text("Открыть RP") }
                            OutlinedButton(onClick = { onEdit(character) }) { Text("Изменить") }
                            TextButton(onClick = { onDelete(character.id) }) { Text("Удалить") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CharacterEditor(character: RpCharacterEntity?, onSave: (String, String, String, String, String, String) -> Unit, onBack: () -> Unit) {
    var name by remember(character?.id) { mutableStateOf(character?.name ?: "") }
    var description by remember(character?.id) { mutableStateOf(character?.description ?: "") }
    var appearance by remember(character?.id) { mutableStateOf(character?.appearance ?: "") }
    var background by remember(character?.id) { mutableStateOf(character?.background ?: "") }
    var speech by remember(character?.id) { mutableStateOf(character?.speechStyle ?: "") }
    var opening by remember(character?.id) { mutableStateOf(character?.opening ?: "") }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(if (character == null) "Новый персонаж" else "Редактирование персонажа", style = MaterialTheme.typography.headlineSmall)
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Имя") }, singleLine = true) }
            item { OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth(), label = { Text("Характер и личность") }, minLines = 3) }
            item { OutlinedTextField(appearance, { appearance = it }, Modifier.fillMaxWidth(), label = { Text("Внешность") }, minLines = 2) }
            item { OutlinedTextField(background, { background = it }, Modifier.fillMaxWidth(), label = { Text("Биография и прошлое") }, minLines = 3) }
            item { OutlinedTextField(speech, { speech = it }, Modifier.fillMaxWidth(), label = { Text("Манера речи") }, minLines = 2) }
            item { OutlinedTextField(opening, { opening = it }, Modifier.fillMaxWidth(), label = { Text("Начало сцены") }, minLines = 3) }
        }
        Button(onClick = { onSave(name.trim(), description, appearance, background, speech, opening) }, enabled = name.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Сохранить") }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Назад") }
    }
}

@Composable
private fun ChatScreen(character: RpCharacterEntity, vm: PhoenixViewModel, onBack: () -> Unit) {
    val messages by vm.messages(character.id).collectAsStateWithLifecycle(emptyList())
    var draft by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text(character.name, style = MaterialTheme.typography.titleLarge)
                Text("Демо-режим: ИИ-модель ещё не подключена", color = Muted, style = MaterialTheme.typography.labelSmall)
            }
            TextButton(onClick = onBack) { Text("Назад") }
        }
        LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(messages, key = { it.id }) { msg ->
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = if (msg.fromUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(if (msg.fromUser) "Ты" else character.name, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Text(msg.text)
                    }
                }
            }
        }
        OutlinedTextField(draft, { draft = it }, Modifier.fillMaxWidth(), label = { Text("Сообщение") }, maxLines = 4)
        Button(onClick = { vm.sendMessage(character, draft); draft = "" }, enabled = draft.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Отправить") }
    }
}

@Composable
private fun ModelsScreen(models: List<LocalModelEntity>, onImport: () -> Unit, onDelete: (LocalModelEntity) -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Локальные GGUF-модели", style = MaterialTheme.typography.headlineSmall)
        Text("Файлы копируются в приватное хранилище приложения. Большие модели могут не поместиться в память устройства.", color = Muted)
        Button(onClick = onImport, modifier = Modifier.fillMaxWidth()) { Text("Импортировать GGUF") }
        if (models.isEmpty()) Text("Модели ещё не добавлены.", color = Muted)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(models, key = { it.id }) { model ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(model.name, style = MaterialTheme.typography.titleSmall)
                            Text(String.format("%.2f GB", model.sizeBytes / 1_000_000_000.0), color = Muted)
                            Text("Импортирована · запуск появится после интеграции llama.cpp", color = Muted, style = MaterialTheme.typography.labelSmall)
                        }
                        TextButton(onClick = { onDelete(model) }) { Text("Удалить") }
                    }
                }
            }
        }
    }
}

@Composable
private fun MemoryScreen(storyId: String, vm: PhoenixViewModel) {
    val memories by vm.memories(storyId).collectAsStateWithLifecycle(emptyList())
    var note by remember { mutableStateOf("") }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Долгосрочная память", style = MaterialTheme.typography.headlineSmall)
        Text("Сохраняй важные факты, события и договорённости этой истории.", color = Muted)
        OutlinedTextField(note, { note = it }, Modifier.fillMaxWidth(), label = { Text("Новая заметка") }, minLines = 3)
        Button(onClick = { vm.addMemory(storyId, null, note); note = "" }, enabled = note.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Сохранить в память") }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(memories, key = { it.id }) { memory ->
                Card(Modifier.fillMaxWidth()) { Text(memory.content, Modifier.padding(14.dp)) }
            }
        }
    }
}

@Composable
private fun FandomScreen(storyId: String, vm: PhoenixViewModel) {
    var title by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var article by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text("Импорт Fandom", style = MaterialTheme.typography.headlineSmall)
        Text("Вставь текст статьи и ссылку на источник. Материал сохранится в локальной памяти выбранной истории.", color = Muted)
        OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text("Название статьи") })
        OutlinedTextField(url, { url = it }, Modifier.fillMaxWidth(), label = { Text("URL Fandom") }, singleLine = true)
        OutlinedTextField(article, { article = it }, Modifier.weight(1f).fillMaxWidth(), label = { Text("Текст статьи") }, minLines = 5)
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = {
            FandomTextImporter.fromPastedText(title, url, article)
                .onSuccess { vm.addMemory(storyId, null, "${it.title}\nИсточник: ${it.sourceUrl}\n${it.text}", "fandom"); title = ""; url = ""; article = ""; error = null }
                .onFailure { error = it.message }
        }, enabled = article.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Импортировать материал") }
    }
}
