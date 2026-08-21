package com.tunesync.extension.youtube

import com.tunesync.core.model.PlaybackSource
import com.tunesync.core.model.Song
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/** Provider-specific transport isolated from the TuneSync extension API. */
class YouTubeMusicTransport(
    private val http: OkHttpClient = OkHttpClient()
) {
    private data class WebConfig(val apiKey: String, val clientVersion: String)

    @Volatile private var config: WebConfig? = null

    private fun webConfig(): WebConfig {
        config?.let { return it }
        val request = Request.Builder().url("https://music.youtube.com/")
            .header("User-Agent", USER_AGENT).build()
        val html = http.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("YouTube Music returned HTTP ${response.code}")
            response.body?.string() ?: error("Empty YouTube Music response")
        }
        val key = Regex("\\\"INNERTUBE_API_KEY\\\":\\\"([^\\\"]+)\\\"")
            .find(html)?.groupValues?.get(1)
            ?: error("YouTube Music API key was not found")
        val version = Regex("\\\"INNERTUBE_CLIENT_VERSION\\\":\\\"([^\\\"]+)\\\"")
            .find(html)?.groupValues?.get(1) ?: "1.20250101.01.00"
        return WebConfig(key, version).also { config = it }
    }

    fun search(query: String): List<Song> {
        val cfg = webConfig()
        val body = JSONObject()
            .put("context", JSONObject().put("client", JSONObject()
                .put("clientName", "WEB_REMIX")
                .put("clientVersion", cfg.clientVersion)
                .put("hl", "en").put("gl", "US")))
            .put("query", query).toString().toRequestBody(JSON)
        val request = Request.Builder()
            .url("https://music.youtube.com/youtubei/v1/search?key=${cfg.apiKey}")
            .post(body).header("User-Agent", USER_AGENT)
            .header("Origin", "https://music.youtube.com")
            .header("Referer", "https://music.youtube.com/").build()
        val json = http.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("YouTube Music search failed: HTTP ${response.code}")
            JSONObject(response.body?.string() ?: "{}")
        }
        return parseSongs(json)
    }

    fun resolvePlaybackSource(song: Song): PlaybackSource? = null

    private fun parseSongs(root: JSONObject): List<Song> {
        val result = mutableListOf<Song>()
        walk(root, result)
        return result.distinctBy { it.id }.take(50)
    }

    private fun walk(value: Any?, result: MutableList<Song>) {
        when (value) {
            is JSONObject -> {
                value.optJSONObject("musicResponsiveListItemRenderer")?.let { parseResponsiveItem(it)?.let(result::add) }
                value.optJSONObject("videoRenderer")?.let { parseVideoItem(it)?.let(result::add) }
                val keys = value.keys()
                while (keys.hasNext()) walk(value.opt(keys.next()), result)
            }
            is org.json.JSONArray -> for (i in 0 until value.length()) walk(value.opt(i), result)
        }
    }

    private fun parseVideoItem(item: JSONObject): Song? {
        val id = item.optString("videoId").takeIf { it.isNotBlank() } ?: return null
        val title = simpleText(item.optJSONObject("title")) ?: return null
        val artist = simpleText(item.optJSONObject("ownerText")) ?: "Unknown artist"
        return Song(id, title, artist,
            artworkUrl = thumbnail(item.optJSONObject("thumbnail")),
            durationMs = parseDuration(simpleText(item.optJSONObject("lengthText"))),
            providerId = PROVIDER_ID)
    }

    private fun parseResponsiveItem(item: JSONObject): Song? {
        val id = item.optJSONObject("playlistItemData")?.optString("videoId")
            ?.takeIf { it.isNotBlank() } ?: item.optString("videoId").takeIf { it.isNotBlank() } ?: return null
        val columns = item.optJSONArray("flexColumns")
        val title = textFromRuns(columns, 0) ?: return null
        val artist = textFromRuns(columns, 1) ?: "Unknown artist"
        return Song(id, title, artist,
            artworkUrl = thumbnail(item.optJSONObject("thumbnail")),
            durationMs = parseDuration(textFromRuns(columns, 2)),
            providerId = PROVIDER_ID)
    }

    private fun simpleText(obj: JSONObject?): String? {
        if (obj == null) return null
        obj.optString("simpleText").takeIf { it.isNotBlank() }?.let { return it }
        val runs = obj.optJSONArray("runs") ?: return null
        return buildString { for (i in 0 until runs.length()) append(runs.optJSONObject(i)?.optString("text").orEmpty()) }
            .trim().ifBlank { null }
    }

    private fun textFromRuns(columns: org.json.JSONArray?, index: Int): String? {
        val renderer = columns?.optJSONObject(index)?.optJSONObject("musicResponsiveListItemFlexColumnRenderer") ?: return null
        return simpleText(renderer.optJSONObject("text"))
    }

    private fun thumbnail(obj: JSONObject?): String? {
        val thumbs = obj?.optJSONArray("thumbnails") ?: return null
        return thumbs.optJSONObject(thumbs.length() - 1)?.optString("url")?.takeIf { it.isNotBlank() }
    }

    private fun parseDuration(text: String?): Long {
        if (text.isNullOrBlank()) return 0L
        val parts = text.split(":").mapNotNull { it.toLongOrNull() }
        return when (parts.size) {
            2 -> (parts[0] * 60 + parts[1]) * 1000
            3 -> (parts[0] * 3600 + parts[1] * 60 + parts[2]) * 1000
            else -> 0L
        }
    }

    companion object {
        const val PROVIDER_ID = "youtube-music"
        private const val USER_AGENT = "Mozilla/5.0 (Android 14; Mobile) AppleWebKit/537.36 Chrome/131.0 Mobile Safari/537.36"
        private val JSON = "application/json; charset=utf-8".toMediaType()
    }
}
