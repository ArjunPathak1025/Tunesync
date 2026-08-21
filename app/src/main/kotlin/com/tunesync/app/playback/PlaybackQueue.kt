package com.tunesync.app.playback

import com.tunesync.core.model.Song

/** Small queue abstraction kept independent from any music provider. */
class PlaybackQueue {
    private val songs = mutableListOf<Song>()

    fun replace(items: List<Song>) {
        songs.clear()
        songs.addAll(items)
    }

    fun add(song: Song) {
        songs += song
    }

    fun remove(songId: String) {
        songs.removeAll { it.id == songId }
    }

    fun move(from: Int, to: Int) {
        if (from !in songs.indices || to !in songs.indices || from == to) return
        val item = songs.removeAt(from)
        songs.add(to, item)
    }

    fun clear() = songs.clear()

    fun snapshot(): List<Song> = songs.toList()
}
