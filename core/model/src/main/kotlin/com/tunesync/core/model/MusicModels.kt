package com.tunesync.core.model

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String? = null,
    val artworkUrl: String? = null,
    val durationMs: Long = 0L,
    val providerId: String = "unknown"
)

data class Artist(
    val id: String,
    val name: String,
    val artworkUrl: String? = null,
    val providerId: String = "unknown"
)

data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val artworkUrl: String? = null,
    val providerId: String = "unknown"
)

data class Playlist(
    val id: String,
    val title: String,
    val description: String? = null,
    val artworkUrl: String? = null,
    val providerId: String = "local"
)

data class PlaybackSource(
    val uri: String,
    val mimeType: String? = null,
    val headers: Map<String, String> = emptyMap()
)
