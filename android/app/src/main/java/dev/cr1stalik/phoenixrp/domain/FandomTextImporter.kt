package dev.cr1stalik.phoenixrp.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

data class ImportedReference(val title: String, val sourceUrl: String, val text: String)

object FandomTextImporter {
    fun fromPastedText(title: String, sourceUrl: String, text: String): Result<ImportedReference> {
        val clean = text.replace(Regex("\\s+"), " ").trim()
        if (clean.length < 80) return Result.failure(IllegalArgumentException("Добавь больше текста статьи (минимум 80 символов)."))
        if (sourceUrl.isNotBlank() && !sourceUrl.startsWith("https://") && !sourceUrl.startsWith("http://")) {
            return Result.failure(IllegalArgumentException("Ссылка должна начинаться с http:// или https://."))
        }
        return Result.success(ImportedReference(title.ifBlank { "Материал Fandom" }, sourceUrl, clean.take(MAX_ARTICLE_LENGTH)))
    }

    suspend fun fetchFromUrl(sourceUrl: String): Result<ImportedReference> = withContext(Dispatchers.IO) {
        runCatching {
            val articleUrl = URL(sourceUrl.trim())
            val host = articleUrl.host.lowercase()
            require(articleUrl.protocol == "https" && host.endsWith(".fandom.com") && host.length > ".fandom.com".length) {
                "Поддерживаются только HTTPS-ссылки на *.fandom.com."
            }
            val path = articleUrl.path
            require(path.startsWith("/wiki/")) { "Открой страницу статьи Fandom вида /wiki/Название." }
            val pageTitle = path.removePrefix("/wiki/").substringBefore('/').replace('_', ' ')
            require(pageTitle.isNotBlank()) { "Не удалось определить название статьи из ссылки." }

            val encodedTitle = URLEncoder.encode(pageTitle, StandardCharsets.UTF_8.name())
            val apiUrl = URL("https://$host/api.php?action=query&prop=extracts&explaintext=1&redirects=1&titles=$encodedTitle&format=json")
            val connection = (apiUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 12_000
                readTimeout = 20_000
                setRequestProperty("User-Agent", "PhoenixRP/1.0 (Android; Fandom article importer)")
                setRequestProperty("Accept", "application/json")
            }
            try {
                require(connection.responseCode in 200..299) { "Fandom вернул HTTP ${connection.responseCode}." }
                val response = connection.inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
                val root = JSONObject(response)
                val pages = root.getJSONObject("query").getJSONObject("pages")
                val pageKeys = pages.keys()
                require(pageKeys.hasNext()) { "Fandom не вернул страницу статьи." }
                val page = pages.getJSONObject(pageKeys.next())
                require(!page.has("missing")) { "Статья не найдена. Проверь ссылку." }
                val title = page.optString("title", pageTitle)
                val extract = page.optString("extract", "").replace(Regex("\\s+"), " ").trim()
                require(extract.length >= 80) { "Fandom не вернул достаточно текста статьи. Попробуй вставить текст вручную." }
                ImportedReference(title, articleUrl.toString(), extract.take(MAX_ARTICLE_LENGTH))
            } finally {
                connection.disconnect()
            }
        }
    }

    private const val MAX_ARTICLE_LENGTH = 100_000
}
