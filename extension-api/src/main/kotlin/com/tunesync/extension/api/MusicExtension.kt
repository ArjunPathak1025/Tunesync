package com.tunesync.extension.api

import com.tunesync.core.model.Album
import com.tunesync.core.model.Artist
import com.tunesync.core.model.Playlist
import com.tunesync.core.model.Song

/** Contract implemented by a TuneSync music-source extension. */
interface MusicExtension {
    val id: String
    val name: String
    val version: Int

    suspend fun search(query: String): SearchResult
    suspend fun getSong(id: String): Song?
    suspend fun getArtist(id: String): Artist?
    suspend fun getAlbum(id: String): Album?
    suspend fun getPlaylist(id: String): Playlist?
    suspend fun getLyrics(song: Song): LyricsResult
    suspend fun getPlaybackSource(song: Song): PlaybackSourceResult
}

data class SearchResult(
    val songs: List<Song> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val albums: List<Album> = emptyList(),
    val playlists: List<Playlist> = emptyList()
)

sealed interface LyricsResult {
    data class Available(val text: String, val synchronized: Boolean = false) : LyricsResult
    data object Unavailable : LyricsResult
}

sealed interface PlaybackSourceResult {
    data class Available(val uri: String, val mimeType: String? = null) : PlaybackSourceResult
    data class Unavailable(val reason: String) : PlaybackSourceResult
}
