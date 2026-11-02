package dev.cr1stalik.phoenixrp

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.cr1stalik.phoenixrp.data.PhoenixViewModel
import dev.cr1stalik.phoenixrp.data.RpCharacterEntity

private val PhoenixBg = Color(0xFF0A0B12)
private val PhoenixPanel = Color(0xFF151724)
private val PhoenixAccent = Color(0xFFB6A0FF)
private val PhoenixMuted = Color(0xFFA3A4B8)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = PhoenixAccent,
                    background = PhoenixBg,
                    surface = PhoenixPanel,
                    onPrimary = Color(0xFF171126),
                    onBackground = Color(0xFFF4F1FF),
                    onSurface = Color(0xFFF4F1FF),
                    secondary = PhoenixMuted
                )
            ) {
                Surface(Modifier.fillMaxSize(), color = PhoenixBg) {
                    PhoenixApp()
                }
            }
        }
    }
}

@Composable
private fun PhoenixApp(vm: PhoenixViewModel = viewModel()) {
    val characters by vm.characters.collectAsStateWithLifecycle()
    var screen by remember { mutableStateOf("home") }
    var activeCharacter by remember { mutableStateOf<RpCharacterEntity?>(null) }

    when (screen) {
        "create" -> CharacterEditor(
            onBack = { screen = "home" },
            onSave = { name, description, opening ->
                vm.createCharacter(name, description, opening)
                screen = "home"
            }
        )
        "chat" -> {
            val character = activeCharacter
            if (character == null) {
                screen = "home"
            } else {
                ChatScreen(
                    character = character,
                    messages = vm.messages(character.id).collectAsState(initial = emptyList()).value,
                    onBack = { screen = "home" },
                    onSend = { vm.sendMessage(character, it) }
                )
            }
        }
        else -> HomeScreen(
            characters = characters,
            onCreate = { screen = "create" },
            onOpen = { activeCharacter = it; screen = "chat" },
            onDelete = vm::deleteCharacter
        )
    }
}

@Composable
private fun HomeScreen(
    characters: List<RpCharacterEntity>,
    onCreate: () -> Unit,
    onOpen: (RpCharacterEntity) -> Unit,
    onDelete: (String) -> Unit
) {
    Column(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF19162B), PhoenixBg)))
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("PHOENIX RP", color = PhoenixAccent, style = MaterialTheme.typography.labelLarge)
        Text("Твоя история.", style = MaterialTheme.typography.headlineLarge)
        Text("Персонажи. Миры. Ролевые сцены.", color = PhoenixMuted)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF28213D)),
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("ТВОЯ ВСЕЛЕННАЯ", color = PhoenixAccent, style = MaterialTheme.typography.labelMedium)
                Text("Персонажи: ${characters.size}", style = MaterialTheme.typography.titleLarge)
                Text("Создавай героев, задавай им характер и начинай сцену.")
                Button(onClick = onCreate, modifier = Modifier.fillMaxWidth()) {
                    Text("＋ Создать персонажа")
                }
            }
        }
        if (characters.isEmpty()) {
            Text("Пока нет персонажей. Создай первого — он сохранится на устройстве.")
        } else {
            Text("Мои персонажи", style = MaterialTheme.typography.titleLarge)
            characters.forEach { character ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PhoenixPanel)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Text("✦ ПЕРСОНАЖ", color = PhoenixAccent, style = MaterialTheme.typography.labelSmall)
                        Text(character.name, style = MaterialTheme.typography.titleMedium)
                        Text(character.description, color = PhoenixMuted)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { onOpen(character) }, modifier = Modifier.weight(1f)) {
                                Text("Продолжить")
                            }
                            OutlinedButton(onClick = { onDelete(character.id) }) {
                                Text("Удалить")
                            }
                        }
                    }
                }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = PhoenixPanel)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Хранение данных", style = MaterialTheme.typography.titleMedium)
                Text("Персонажи и сообщения хранятся в локальной базе Room.", color = PhoenixMuted)
                Text("ИИ: демонстрационные ответы · подключение модели запланировано", color = PhoenixMuted,
                    style = MaterialTheme.typography.labelSmall)
            }
        }
        Text("PHOENIX RP · 0.4.0", color = PhoenixMuted, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun CharacterEditor(
    onBack: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var opening by remember { mutableStateOf("Вы встречаетесь в небольшом кафе поздним вечером.") }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("PHOENIX RP / НОВАЯ ИСТОРИЯ", color = PhoenixAccent)
        Text("Создать персонажа", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(),
            label = { Text("Имя персонажа") }, singleLine = true)
        OutlinedTextField(description, { description = it }, Modifier.fillMaxWidth(),
            label = { Text("Характер и описание") }, minLines = 3)
        OutlinedTextField(opening, { opening = it }, Modifier.fillMaxWidth(),
            label = { Text("Начало сцены") }, minLines = 3)
        Button(
            onClick = { onSave(name, description, opening) },
            enabled = name.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Сохранить персонажа") }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Назад") }
    }
}

@Composable
private fun ChatScreen(
    character: RpCharacterEntity,
    messages: List<dev.cr1stalik.phoenixrp.data.RpMessageEntity>,
    onBack: () -> Unit,
    onSend: (String) -> Unit
) {
    var draft by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text(character.name, style = MaterialTheme.typography.headlineSmall)
                Text(character.description, color = PhoenixMuted, style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onBack) { Text("Готово") }
        }
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (message.fromUser) Color(0xFF29223D) else PhoenixPanel
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(if (message.fromUser) "Ты" else character.name,
                            color = PhoenixAccent, style = MaterialTheme.typography.labelMedium)
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
            onClick = { onSend(draft); draft = "" },
            enabled = draft.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Отправить") }
        Text("ДЕМО-РЕЖИМ · Локальная модель ИИ ещё не подключена",
            color = PhoenixMuted, style = MaterialTheme.typography.labelSmall)
    }
}
