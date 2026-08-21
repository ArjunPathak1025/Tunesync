package com.tunesync.core.extension.api

import com.tunesync.core.model.Album
import com.tunesync.core.model.Artist
import com.tunesync.core.model.PlaybackSource
import com.tunesync.core.model.Playlist
import com.tunesync.core.model.Song

/** Provider contract inspired by Echo's capability-based Client model. */
interface MusicExtension {
    val id: String
    val name: String
    val version: Int

    suspend fun initialize() {}
    suspend fun search(query: String): SearchPage = SearchPage()
    suspend fun getSong(id: String): Song?
    suspend fun getArtist(id: String): Artist?
    suspend fun getAlbum(id: String): Album?
    suspend fun getPlaylist(id: String): Playlist?
    suspend fun getRelated(song: Song): List<Song> = emptyList()
    suspend fun getLyrics(song: Song): Lyrics? = null
    suspend fun getPlaybackSource(song: Song): PlaybackSource?
}

data class SearchPage(
    val songs: List<Song> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val albums: List<Album> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val continuation: String? = null
)

data class Lyrics(
    val plainText: String? = null,
    val syncedLines: List<SyncedLyricLine> = emptyList()
)

data class SyncedLyricLine(
    val startMs: Long,
    val text: String
)
