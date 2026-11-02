package dev.cr1stalik.phoenixrp.domain
object ReplyComposer {
 fun compose(characterName: String, description: String, userText: String): String {
  val line = userText.trim().trimEnd('.', '!', '?', '…')
  return when {
   line.contains("привет", ignoreCase = true) || line.contains("здравств", ignoreCase = true) -> "$characterName отвечает на приветствие. «Рад(а) тебя видеть. Что привело тебя сюда?»"
   line.contains("кто ты", ignoreCase = true) -> "«Я — $characterName». $description"
   line.contains("почему", ignoreCase = true) -> "$characterName задумывается. «Хороший вопрос. Давай разберёмся вместе»."
   else -> "$characterName внимательно реагирует на твои слова: «${line.take(180)}». Персонаж ждёт продолжения."
  }
 }
}
