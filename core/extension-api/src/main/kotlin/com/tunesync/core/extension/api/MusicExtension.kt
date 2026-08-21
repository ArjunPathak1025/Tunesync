package com.tunesync.core.extension.api

import com.tunesync.core.model.Album
import com.tunesync.core.model.Artist
import com.tunesync.core.model.PlaybackSource
import com.tunesync.core.model.Playlist
import com.tunesync.core.model.Song

interface MusicExtension {
    val id: String
    val name: String
    val version: Int

    suspend fun search(query: String): List<Song>
    suspend fun getSong(id: String): Song?
    suspend fun getArtist(id: String): Artist?
    suspend fun getAlbum(id: String): Album?
    suspend fun getPlaylist(id: String): Playlist?
    suspend fun getPlaybackSource(song: Song): PlaybackSource?
}
