package com.tunesync.extension.youtube

import com.tunesync.core.model.PlaybackSource
import com.tunesync.core.model.Song
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/**
 * Small transport for the public web surface used by music.youtube.com.
 * The web client configuration is discovered at runtime so a stale API key
 * is not embedded in the application.
 *
 * This is intentionally isolated from the extension API so the transport can
 * be replaced if the upstream web protocol changes.
 */
class YouTubeMusicTransport(
    private val http: OkHttpClient = OkHttpClient()
) {
    private data class WebConfig(val apiKey: String, val clientVersion: String)

    @Volatile
    private var config: WebConfig? = null

    private fun webConfig(): WebConfig {
        config?.let { return it }
        val request = Request.Builder()
            .url("https://music.youtube.com/")
            .header("User-Agent", USER_AGENT)
            .build()
        val html = http.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("YouTube Music returned HTTP ${response.code}")
            response.body?.string() ?: error("Empty YouTube Music response")
        }
        val key = Regex("\\\"INNERTUBE_API_KEY\\\":\\\"([^\\\"]+)\\\"")
            .find(html)?.groupValues?.get(1)
            ?: error("YouTube Music API key was not found")
        val version = Regex("\\\"INNERTUBE_CLIENT_VERSION\\\":\\\"([^\\\"]+)\\\"")
            .find(html)?.groupValues?.get(1)
            ?: "1.20250101.01.00"
        return WebConfig(key, version).also { config = it }
    }

    fun search(query: String): List<Song> {
        val cfg = webConfig()
        val body = JSONObject()
            .put("context", JSONObject()
                .put("client", JSONObject()
                    .put("clientName", "WEB_REMIX")
                    .put("clientVersion", cfg.clientVersion)
                    .put("hl", "en")
                    .put("gl", "US")))
            .put("query", query)
            .toString()
            .toRequestBody(JSON)

        val request = Request.Builder()
            .url("https://music.youtube.com/youtubei/v1/search?key=${cfg.apiKey}")
            .post(body)
            .header("User-Agent", USER_AGENT)
            .header("Origin", "https://music.youtube.com")
            .header("Referer", "https://music.youtube.com/")
            .build()

        val json = http.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("YouTube Music search failed: HTTP ${response.code}")
            JSONObject(response.body?.string() ?: "{}")
        }

        return parseSongs(json)
    }

    fun resolvePlaybackSource(song: Song): PlaybackSource? {
        // Playback resolution is deliberately isolated. The web player can
        // return signed/temporary URLs and the format can change upstream.
        // We only return a direct media URL when the provider supplies one.
        return null
    }

    private fun parseSongs(root: JSONObject): List<Song> {
        val result = mutableListOf<Song>()
        walk(root, result)
        return result.distinctBy { it.id }.take(50)
    }

    private fun walk(value: Any?, result: MutableList<Song>) {
        when (value) {
            is JSONObject -> {
                val renderer = value.optJSONObject("musicResponsiveListItemRenderer")
                if (renderer != null) parseResponsiveItem(renderer)?.let(result::add)

                val video = value.optJSONObject("videoRenderer")
                if (video != null) parseVideoItem(video)?.let(result::add)

                val keys = value.keys()
                while (keys.hasNext()) walk(value.opt(keys.next()), result)
            }
            is org.json.JSONArray -> {
                for (i in 0 until value.length()) walk(value.opt(i), result)
            }
        }
    }

    private fun parseVideoItem(item: JSONObject): Song? {
        val id = item.optString("videoId").takeIf { it.isNotBlank() } ?: return null
        val title = item.optJSONObject("title")?.optString("simpleText")
            ?: item.optJSONObject("title")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text")
            ?: return null
        val artist = item.optJSONObject("ownerText")?.optString("simpleText")
            ?: item.optJSONObject("ownerText")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text")
            ?: "Unknown artist"
        val duration = parseDuration(item.optJSONObject("lengthText")?.optString("simpleText"))
        val artwork = thumbnail(item.optJSONObject("thumbnail"))
        return Song(id, title, artist, artworkUrl = artwork, durationMs = duration, providerId = PROVIDER_ID)
    }

    private fun parseResponsiveItem(item: JSONObject): Song? {
        val id = item.optString("playlistItemData", "").ifBlank {
            item.optString("videoId")
        }.takeIf { it.isNotBlank() } ?: return null

        val title = textFromRuns(item.optJSONArray("flexColumns"), 0) ?: return null
        val artist = textFromRuns(item.optJSONArray("flexColumns"), 1) ?: "Unknown artist"
        val duration = parseDuration(textFromRuns(item.optJSONArray("flexColumns"), 2))
        val artwork = thumbnail(item.optJSONObject("thumbnail"))
        return Song(id, title, artist, artworkUrl = artwork, durationMs = duration, providerId = PROVIDER_ID)
    }

    private fun textFromRuns(columns: org.json.JSONArray?, index: Int): String? {
        val column = columns?.optJSONObject(index) ?: return null
        val renderer = column.optJSONObject("musicResponsiveListItemFlexColumnRenderer") ?: return null
        val runs = renderer.optJSONObject("text")?.optJSONArray("runs") ?: return null
        return buildString {
            for (i in 0 until runs.length()) append(runs.optJSONObject(i)?.optString("text").orEmpty())
        }.trim().ifBlank { null }
    }

    private fun thumbnail(obj: JSONObject?): String? {
        val thumbs = obj?.optJSONArray("thumbnails") ?: return null
        return thumbs.optJSONObject(thumbs.length() - 1)?.optString("url")?.takeIf { it.isNotBlank() }
    }

    private fun parseDuration(text: String?): Long {
        if (text.isNullOrBlank()) return 0L
        val parts = text.split(":").mapNotNull { it.toLongOrNull() }
        if (parts.size == 2) return (parts[0] * 60 + parts[1]) * 1000
        if (parts.size == 3) return (parts[0] * 3600 + parts[1] * 60 + parts[2]) * 1000
        return 0L
    }

    companion object {
        const val PROVIDER_ID = "youtube-music"
        private const val USER_AGENT = "Mozilla/5.0 (Android 14; Mobile) AppleWebKit/537.36 Chrome/131.0 Mobile Safari/537.36"
        private val JSON = "application/json; charset=utf-8".toRequestBody().contentType()!!
    }
}
