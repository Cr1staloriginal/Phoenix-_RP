package dev.cr1stalik.phoenixrp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PhoenixHomeScreen()
                }
            }
        }
    }
}

@Composable
private fun PhoenixHomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Phoenix RP",
            style = MaterialTheme.typography.headlineLarge
        )
        Text(
            text = "Твой мир. Твои персонажи. Твоя история.",
            style = MaterialTheme.typography.bodyLarge
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Персонажи", style = MaterialTheme.typography.titleLarge)
                Text("Здесь появятся твои локально сохранённые персонажи.")
                Button(
                    onClick = { /* Character creation will be added in a later step. */ },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text("Скоро")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Локальная модель", style = MaterialTheme.typography.titleLarge)
                Text("Подготовим запуск ИИ прямо на устройстве — без обязательного облачного API.")
                Text(
                    text = "Статус: движок ещё не подключён",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Text(
            text = "Начальная версия 0.1.0",
            style = MaterialTheme.typography.labelMedium
        )
    }
}
