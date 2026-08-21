package com.tunesync.extensions

import com.tunesync.extension.api.MusicExtension
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Keeps provider registration independent from the Android UI. */
class ExtensionManager {
    private val extensions = linkedMapOf<String, MusicExtension>()
    private val _enabledIds = MutableStateFlow<Set<String>>(emptySet())
    val enabledIds: StateFlow<Set<String>> = _enabledIds.asStateFlow()

    fun register(extension: MusicExtension) {
        extensions[extension.id] = extension
    }

    fun unregister(id: String) {
        extensions.remove(id)
        _enabledIds.value = _enabledIds.value - id
    }

    fun enable(id: String) {
        check(extensions.containsKey(id)) { "Unknown extension: $id" }
        _enabledIds.value = _enabledIds.value + id
    }

    fun disable(id: String) {
        _enabledIds.value = _enabledIds.value - id
    }

    fun get(id: String): MusicExtension? = extensions[id]

    fun all(): List<MusicExtension> = extensions.values.toList()

    fun enabled(): List<MusicExtension> = _enabledIds.value.mapNotNull(extensions::get)
}
