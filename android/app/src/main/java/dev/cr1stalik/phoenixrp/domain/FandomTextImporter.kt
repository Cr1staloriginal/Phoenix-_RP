package dev.cr1stalik.phoenixrp.domain
data class ImportedReference(val title: String, val sourceUrl: String, val text: String)
object FandomTextImporter {
 fun fromPastedText(title: String, sourceUrl: String, text: String): Result<ImportedReference> {
  val clean = text.replace(Regex("\\s+"), " ").trim()
  if (clean.length < 80) return Result.failure(IllegalArgumentException("Добавь больше текста статьи (минимум 80 символов)."))
  if (sourceUrl.isNotBlank() && !sourceUrl.startsWith("https://") && !sourceUrl.startsWith("http://"))
   return Result.failure(IllegalArgumentException("Ссылка должна начинаться с http:// или https://."))
  return Result.success(ImportedReference(title.ifBlank { "Материал Fandom" }, sourceUrl, clean))
 }
}
