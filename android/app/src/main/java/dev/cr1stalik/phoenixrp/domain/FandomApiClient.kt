package dev.cr1stalik.phoenixrp.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

data class FandomSearchResult(val title: String, val pageId: Long, val snippet: String)
data class FandomArticle(val title: String, val sourceUrl: String, val extract: String)

/**
 * Read-only client for public MediaWiki Action APIs (including Fandom).
 * Imports a short introduction and source URL rather than copying an entire article.
 */
object FandomApiClient {
    suspend fun search(wikiUrl: String, query: String, limit: Int = 10): Result<List<FandomSearchResult>> =
        withContext(Dispatchers.IO) {
            runCatching {
                require(query.isNotBlank()) { "Введи имя персонажа для поиска." }
                val json = request(apiEndpoint(wikiUrl), mapOf(
                    "action" to "query", "list" to "search", "srsearch" to query.trim(),
                    "srlimit" to limit.coerceIn(1, 20).toString(), "srnamespace" to "0",
                    "format" to "json", "utf8" to "1"
                ))
                val rows = json.optJSONObject("query")?.optJSONArray("search")
                    ?: error("Вики не вернула результаты поиска.")
                (0 until rows.length()).map { index ->
                    val row = rows.getJSONObject(index)
                    FandomSearchResult(row.optString("title"), row.optLong("pageid"), stripMarkup(row.optString("snippet")))
                }
            }
        }

    suspend fun fetchArticle(wikiUrl: String, title: String): Result<FandomArticle> =
        withContext(Dispatchers.IO) {
            runCatching {
                require(title.isNotBlank()) { "Не выбрана статья." }
                val json = request(apiEndpoint(wikiUrl), mapOf(
                    "action" to "query", "prop" to "extracts|info", "exintro" to "1",
                    "explaintext" to "1", "exchars" to "2400", "inprop" to "url",
                    "redirects" to "1", "titles" to title, "format" to "json", "utf8" to "1"
                ))
                val pages = json.optJSONObject("query")?.optJSONObject("pages")
                    ?: error("Статья не найдена.")
                val page = pages.keys().asSequence().map { pages.getJSONObject(it) }
                    .firstOrNull { !it.has("missing") } ?: error("Статья не найдена.")
                val extract = page.optString("extract").trim()
                require(extract.length >= 40) { "В статье нет доступного краткого описания." }
                FandomArticle(page.optString("title", title),
                    page.optString("fullurl").ifBlank { articleUrl(wikiUrl, title) },
                    extract.take(2_400))
            }
        }

    fun apiEndpoint(input: String): String {
        val uri = URI(input.trim())
        require(uri.scheme.equals("https", true) || uri.scheme.equals("http", true)) {
            "Ссылка должна начинаться с https:// или http://."
        }
        require(!uri.host.isNullOrBlank()) { "Не удалось определить адрес вики." }
        val host = uri.host
        require(!host.equals("localhost", true) && !host.endsWith(".local", true)) {
            "Локальные адреса не поддерживаются."
        }
        val origin = "${uri.scheme.lowercase(Locale.ROOT)}://${uri.rawAuthority}"
        val path = uri.path.orEmpty()
        val apiPath = when {
            path.endsWith("/api.php", true) || path.endsWith("/w/api.php", true) -> path
            path.contains("/wiki/", true) -> "/api.php"
            path.endsWith("/w/index.php", true) -> path.substringBeforeLast('/') + "/api.php"
            else -> path.trimEnd('/') + "/api.php"
        }
        return origin + apiPath
    }

    private fun articleUrl(wikiUrl: String, title: String): String {
        val uri = URI(wikiUrl.trim())
        val origin = "${uri.scheme}://${uri.rawAuthority}"
        val path = uri.path.orEmpty()
        val base = if (path.contains("/wiki/", true)) path.substringBefore("/wiki/") else path.trimEnd('/')
        return origin + base + "/wiki/" + title.replace(' ', '_')
    }

    private fun request(endpoint: String, params: Map<String, String>): JSONObject {
        val query = params.entries.joinToString("&") { "${encode(it.key)}=${encode(it.value)}" }
        val connection = URL("$endpoint?$query").openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 12_000
            connection.readTimeout = 18_000
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", "PhoenixRP/1.0 (Android; MediaWiki API client)")
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            if (status !in 200..299) error("Ошибка вики: HTTP $status")
            val json = JSONObject(body)
            if (json.has("error")) error(json.getJSONObject("error").optString("info", "Ошибка API вики."))
            return json
        } finally {
            connection.disconnect()
        }
    }

    private fun encode(value: String): String = URLEncoder.encode(value, "UTF-8")
    private fun stripMarkup(value: String): String = value
        .replace(Regex("<[^>]*>"), " ")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace(Regex("\\s+"), " ")
        .trim()
}
