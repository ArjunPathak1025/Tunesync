package com.tunesync.extension.youtube

import com.tunesync.core.extension.api.Lyrics
import com.tunesync.core.extension.api.MusicExtension
import com.tunesync.core.extension.api.SearchPage
import com.tunesync.core.model.Album
import com.tunesync.core.model.Artist
import com.tunesync.core.model.PlaybackSource
import com.tunesync.core.model.Playlist
import com.tunesync.core.model.Song

/**
 * TuneSync's first provider. It owns provider-specific protocol details while
 * exposing only normalized TuneSync models to the rest of the application.
 */
class YouTubeMusicExtension(
    private val transport: YouTubeMusicTransport = YouTubeMusicTransport()
) : MusicExtension {
    override val id: String = YouTubeMusicTransport.PROVIDER_ID
    override val name: String = "YouTube Music"
    override val version: Int = 1

    override suspend fun search(query: String): SearchPage {
        if (query.isBlank()) return SearchPage()
        return SearchPage(songs = transport.search(query))
    }

    override suspend fun getSong(id: String): Song? = null

    override suspend fun getArtist(id: String): Artist? = null

    override suspend fun getAlbum(id: String): Album? = null

    override suspend fun getPlaylist(id: String): Playlist? = null

    override suspend fun getLyrics(song: Song): Lyrics? = null

    override suspend fun getPlaybackSource(song: Song): PlaybackSource? =
        transport.resolvePlaybackSource(song)
}
