package com.tunesync.core.player

import com.tunesync.core.model.Song
import kotlinx.coroutines.flow.StateFlow

interface PlayerController {
    val state: StateFlow<PlaybackState>

    fun play(song: Song)
    fun pause()
    fun resume()
    fun seekTo(positionMs: Long)
    fun skipNext()
    fun skipPrevious()
    fun setQueue(songs: List<Song>, startIndex: Int = 0)
    fun toggleShuffle()
    fun cycleRepeatMode()
    fun release()
}
