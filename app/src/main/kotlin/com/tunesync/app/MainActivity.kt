package com.tunesync.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tunesync.app.playback.PlaybackController
import com.tunesync.core.extensions.ExtensionManager
import com.tunesync.core.model.Song
import com.tunesync.extension.youtube.YouTubeMusicExtension
import kotlinx.coroutines.delay

private val sampleSongs = listOf(
    Song("1", "Midnight Drive", "TuneSync Radio", "Afterglow"),
    Song("2", "Golden Hour", "The Open Tones", "Daylight"),
    Song("3", "Neon Skies", "Luna Park", "City Lights"),
    Song("4", "After Rain", "North Avenue", "Blue Hours")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { TuneSyncApp() }
    }
}

@Composable
private fun TuneSyncApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val playbackController = remember { PlaybackController(context) }
    val extensions = remember { ExtensionManager(listOf(YouTubeMusicExtension())) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    var isPlaying by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose { playbackController.release() }
    }

    MaterialTheme {
        Scaffold(
            containerColor = Color(0xFF09090B),
            bottomBar = {
                Column {
                    selectedSong?.let { song ->
                        MiniPlayer(song, isPlaying, onToggle = {
                            playbackController.togglePlayPause()
                            isPlaying = !isPlaying
                        })
                    }
                    NavigationBar(
                        containerColor = Color(0xFF111113),
                        modifier = Modifier.navigationBarsPadding()
                    ) {
                        val items = listOf(
                            "Home" to Icons.Default.Home,
                            "Search" to Icons.Default.Search,
                            "Library" to Icons.Default.LibraryMusic,
                            "Settings" to Icons.Default.Settings
                        )
                        items.forEachIndexed { index, item ->
                            NavigationBarItem(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                icon = { Icon(item.second, item.first) },
                                label = { Text(item.first) }
                            )
                        }
                    }
                }
            }
        ) { padding ->
            when (selectedTab) {
                0 -> HomeScreen(Modifier.padding(padding)) { song ->
                    selectedSong = song
                    isPlaying = true
                    playbackController.play(song)
                }
                1 -> SearchScreen(Modifier.padding(padding), extensions) { song ->
                    selectedSong = song
                    isPlaying = true
                    playbackController.play(song)
                }
                2 -> LibraryScreen(Modifier.padding(padding))
                else -> SettingsScreen(Modifier.padding(padding))
            }
        }
    }
}

@Composable
private fun HomeScreen(modifier: Modifier, onSongClick: (Song) -> Unit) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Spacer(Modifier.height(18.dp))
            Text("Good evening", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Your music, your way", color = Color.LightGray)
        }
        item { SectionTitle("Recently played") }
        items(sampleSongs) { song -> SongRow(song, onClick = { onSongClick(song) }) }
        item { SectionTitle("Quick picks") }
        items(sampleSongs.reversed()) { song -> SongRow(song, onClick = { onSongClick(song) }) }
        item { Spacer(Modifier.height(120.dp)) }
    }
}

@Composable
private fun SearchScreen(
    modifier: Modifier,
    extensions: ExtensionManager,
    onSongClick: (Song) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Song>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(query) {
        if (query.trim().length < 2) {
            results = emptyList()
            error = null
            return@LaunchedEffect
        }
        delay(450)
        loading = true
        error = null
        runCatching {
            extensions.get("youtube-music")?.search(query.trim())?.songs.orEmpty()
        }.onSuccess { results = it }
            .onFailure { error = it.message ?: "Search failed" }
        loading = false
    }

    LazyColumn(modifier.fillMaxSize().padding(20.dp)) {
        item {
            Text("Search", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))
            TextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Songs, artists, albums...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                shape = RoundedCornerShape(18.dp)
            )
            Spacer(Modifier.height(20.dp))
            when {
                loading -> Text("Searching YouTube Music…", color = Color.Gray)
                error != null -> Text(error!!, color = MaterialTheme.colorScheme.error)
                query.length < 2 -> Text("Type at least 2 characters to search", color = Color.Gray)
                results.isEmpty() -> Text("No results", color = Color.Gray)
            }
            if (results.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                SectionTitle("YouTube Music")
            }
        }
        items(results) { song -> SongRow(song, onClick = { onSongClick(song) }) }
        item { Spacer(Modifier.height(120.dp)) }
    }
}

@Composable
private fun LibraryScreen(modifier: Modifier) {
    Column(modifier.fillMaxSize().padding(20.dp)) {
        Text("Library", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))
        listOf("Liked Songs", "Playlists", "Albums", "Artists", "History").forEach {
            Surface(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp), color = Color(0xFF17171A), shape = RoundedCornerShape(16.dp)) {
                Text(it, modifier = Modifier.padding(18.dp))
            }
        }
    }
}

@Composable
private fun SettingsScreen(modifier: Modifier) {
    Column(modifier.fillMaxSize().padding(20.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))
        listOf("Appearance", "Playback", "Audio", "Lyrics", "Cache", "About").forEach {
            Surface(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp), color = Color(0xFF17171A), shape = RoundedCornerShape(16.dp)) {
                Text(it, modifier = Modifier.padding(18.dp))
            }
        }
    }
}

@Composable
private fun SongRow(song: Song, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).clickable(onClick = onClick).padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(58.dp).clip(RoundedCornerShape(14.dp)).background(Brush.linearGradient(listOf(Color(0xFF6B4EFF), Color(0xFFB44CFF)))),
            contentAlignment = Alignment.Center
        ) { Text("♪", style = MaterialTheme.typography.headlineSmall) }
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(song.title, fontWeight = FontWeight.SemiBold)
            Text(song.artist, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        }
        IconButton(onClick = onClick) { Icon(Icons.Default.PlayArrow, "Play") }
    }
}

@Composable
private fun MiniPlayer(song: Song, isPlaying: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color(0xFF1B1B1F)).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(46.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF6B4EFF)), contentAlignment = Alignment.Center) { Text("♪") }
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(song.title, fontWeight = FontWeight.SemiBold)
            Text(song.artist, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        }
        IconButton(onClick = onToggle) {
            Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, if (isPlaying) "Pause" else "Play")
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
}
