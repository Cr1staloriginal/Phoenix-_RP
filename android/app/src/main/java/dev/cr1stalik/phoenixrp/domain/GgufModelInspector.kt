package dev.cr1stalik.phoenixrp.domain
import java.io.File
data class GgufInfo(val isGguf: Boolean, val sizeBytes: Long, val warning: String?)
object GgufModelInspector {
 fun inspect(file: File): GgufInfo {
  val header = ByteArray(4)
  val valid = runCatching { file.inputStream().use { it.read(header) == 4 } }.getOrDefault(false) &&
   header.contentEquals(byteArrayOf(0x47, 0x47, 0x55, 0x46))
  val warning = when { !valid -> "Файл не прошёл проверку заголовка GGUF."; file.length() > 2_000_000_000L -> "Большая модель: проверь свободную память устройства."; else -> null }
  return GgufInfo(valid, file.length(), warning)
 }
}
