package com.tunesync.core.extensions

import com.tunesync.core.extension.api.MusicExtension

class ExtensionManager(
    extensions: List<MusicExtension> = emptyList()
) {
    private val installed = extensions.associateBy { it.id }.toMutableMap()

    fun register(extension: MusicExtension) {
        installed[extension.id] = extension
    }

    fun unregister(extensionId: String) {
        installed.remove(extensionId)
    }

    fun get(extensionId: String): MusicExtension? = installed[extensionId]

    fun all(): List<MusicExtension> = installed.values.toList()
}
