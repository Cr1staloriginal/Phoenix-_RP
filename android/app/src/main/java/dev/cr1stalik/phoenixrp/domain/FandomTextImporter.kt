package dev.cr1stalik.phoenixrp.domain

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class ImportedReference(val title: String, val sourceUrl: String, val text: String)

object FandomTextImporter {
    fun fromPastedText(title: String, sourceUrl: String, text: String): Result<ImportedReference> {
        val clean = text.replace(Regex("\\s+"), " ").trim()
        if (clean.length < 80) return Result.failure(IllegalArgumentException("Добавь больше текста статьи (минимум 80 символов)."))
        if (sourceUrl.isNotBlank() && !sourceUrl.startsWith("https://") && !sourceUrl.startsWith("http://")) {
            return Result.failure(IllegalArgumentException("Ссылка должна начинаться с http:// или https://."))
        }
        return Result.success(ImportedReference(title.ifBlank { "Материал Fandom" }, sourceUrl, clean))
    }

    /** Public Fandom article fetch through MediaWiki API. Must be called off the main thread. */
    fun fetchArticle(articleUrl: String): Result<ImportedReference> = runCatching {
        val parsed = URL(articleUrl.trim())
        require(parsed.protocol == "https") { "Для импорта Fandom используй HTTPS-ссылку." }
        val host = parsed.host.lowercase()
        require(host == "fandom.com" || host.endsWith(".fandom.com")) { "Это не ссылка на Fandom." }
        val markerIndex = parsed.path.indexOf("/wiki/")
        require(markerIndex >= 0) { "Нужна ссылка на статью вида /wiki/Название_статьи." }
        val rawTitle = parsed.path.substring(markerIndex + 6).substringBefore('/')
        require(rawTitle.isNotBlank()) { "В ссылке не найдено название статьи." }
        val title = java.net.URLDecoder.decode(rawTitle.replace("+", "%2B"), Charsets.UTF_8.name()).replace('_', ' ')
        val apiUrl = "https://$host/api.php?action=query&prop=extracts&explaintext=1&exsectionformat=plain&format=json&formatversion=2&titles=" +
            URLEncoder.encode(title, Charsets.UTF_8.name())
        val connection = URL(apiUrl).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 12_000
            connection.readTimeout = 20_000
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", "PhoenixRP/1.0 (Android)")
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            if (status !in 200..299) error("Fandom вернул HTTP $status.")
            val pages = JSONObject(body).getJSONObject("query").getJSONArray("pages")
            require(pages.length() > 0) { "Статья не найдена." }
            val page = pages.getJSONObject(0)
            require(!page.has("missing")) { "Статья Fandom не найдена." }
            val extract = page.optString("extract").trim()
            require(extract.length >= 80) { "Fandom не вернул текст статьи. Возможно, страница ограничена или это не статья." }
            ImportedReference(page.optString("title", title), articleUrl.trim(), extract)
        } finally {
            connection.disconnect()
        }
    }
}
