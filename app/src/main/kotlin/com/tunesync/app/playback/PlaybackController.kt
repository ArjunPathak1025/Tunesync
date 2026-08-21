package com.tunesync.app.playback

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.tunesync.core.model.Song

/** UI-facing bridge to the background Media3 PlaybackService. */
class PlaybackController(context: Context) {
    private val appContext = context.applicationContext
    private val controllerFuture: ListenableFuture<MediaController>
    private var controller: MediaController? = null

    init {
        val token = SessionToken(appContext, android.content.ComponentName(appContext, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(appContext, token).buildAsync()
        controllerFuture.addListener({
            controller = runCatching { controllerFuture.get() }.getOrNull()
        }, ContextCompat.getMainExecutor(appContext))
    }

    fun play(song: Song) {
        val mediaController = controller ?: return
        val item = MediaItem.Builder()
            .setMediaId(song.id)
            .setUri(song.artworkUrl ?: DEMO_AUDIO_URI)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setAlbumTitle(song.album)
                    .build()
            )
            .build()
        mediaController.setMediaItem(item)
        mediaController.prepare()
        mediaController.play()
    }

    fun togglePlayPause() {
        controller?.let { if (it.isPlaying) it.pause() else it.play() }
    }

    fun release() {
        controllerFuture.addListener({
            controller?.release()
            controller = null
        }, ContextCompat.getMainExecutor(appContext))
    }

    companion object {
        // Public test audio used only to prove the playback pipeline before a provider supplies a real source.
        const val DEMO_AUDIO_URI = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
    }
}
