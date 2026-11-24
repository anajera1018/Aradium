package com.aradium.aradium

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

val playlistSortNames = mapOf(
    "custom" to "Custom order",
    "title" to "Title",
    "artist" to "Artist",
    "date" to "Recently added"
)

@Composable
fun PlaylistScreen(viewModel: MusicViewModel, navController: NavController, playlistId: Long) {
    val playlistFlow = remember(playlistId) { viewModel.getPlaylist(playlistId) }
    val songsFlow = remember(playlistId) { viewModel.getPlaylistSongs(playlistId) }
    val playlist by playlistFlow.collectAsState(initial = null)
    val songs by songsFlow.collectAsState(initial = emptyList())
    val playerState by viewModel.playerState.collectAsState()

    var sortOrder by rememberSaveable { mutableStateOf("custom") }
    var showSortMenu by remember { mutableStateOf(false) }
    var showEdit by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    var showAddSongs by remember { mutableStateOf(false) }

    val current = playlist
    if (current == null) {
        LoadingState()
        return
    }

    val shownSongs = sortSongs(songs, sortOrder)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp) // Layout: space around element
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) { // Layout: vertical alignment
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                current.name,
                style = MaterialTheme.typography.headlineSmall, // Style: text size (headlineSmall)
                fontWeight = FontWeight.Bold, // Style: bold text
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showEdit = true }) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit playlist")
            }
            IconButton(onClick = { showDelete = true }) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete playlist")
            }
        }
        if (current.description.isNotBlank()) {
            Text(current.description, modifier = Modifier.padding(start = 12.dp)) // Layout: space around element
        }
        Text(
            "${songs.size} songs",
            style = MaterialTheme.typography.bodySmall, // Style: text size (bodySmall)
            modifier = Modifier.padding(start = 12.dp) // Layout: space around element
        )

        Row(
            modifier = Modifier.padding(vertical = 8.dp), // Layout: space around element
            verticalAlignment = Alignment.CenterVertically // Layout: vertical alignment
        ) {
            Button(onClick = { viewModel.playSongs(shownSongs) }, enabled = songs.isNotEmpty()) { // Style: filled button
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Text("Play")
            }
            Spacer(Modifier.width(8.dp)) // Layout: horizontal gap
            OutlinedButton(onClick = { viewModel.shuffleSongs(songs) }, enabled = songs.isNotEmpty()) { // Style: outlined button
                Icon(Icons.Filled.Shuffle, contentDescription = null)
            }
            Spacer(Modifier.width(8.dp)) // Layout: horizontal gap
            OutlinedButton(onClick = { showAddSongs = true }) { // Style: outlined button
                Icon(Icons.Filled.Add, contentDescription = null)
                Text("Add")
            }
        }

        Box {
            TextButton(onClick = { showSortMenu = true }) { // Style: text-only button
                Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null)
                Spacer(Modifier.width(4.dp)) // Layout: horizontal gap
                Text(playlistSortNames[sortOrder] ?: "Sort")
            }
            DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                playlistSortNames.forEach { (key, name) ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            sortOrder = key
                            showSortMenu = false
                        }
                    )
                }
            }
        }

        if (songs.isEmpty()) {
            EmptyState("This playlist is empty.", "Add songs") { showAddSongs = true }
        } else {
            LazyColumn {
                itemsIndexed(shownSongs, key = { _, song -> song.id }) { index, song ->
                    SongRow(
                        song = song,
                        isCurrent = song.id == playerState.currentSongId,
                        onClick = { viewModel.playSongs(shownSongs, index) }
                    ) {
                        if (sortOrder == "custom") {
                            IconButton(
                                onClick = { viewModel.moveSong(current.id, songs, index, index - 1) },
                                enabled = index > 0
                            ) {
                                Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Move up")
                            }
                            IconButton(
                                onClick = { viewModel.moveSong(current.id, songs, index, index + 1) },
                                enabled = index < songs.size - 1
                            ) {
                                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Move down")
                            }
                        }
                        IconButton(onClick = { viewModel.removeFromPlaylist(current.id, song.id) }) {
                            Icon(Icons.Filled.Close, contentDescription = "Remove")
                        }
                    }
                }
            }
        }
    }

    if (showEdit) {
        PlaylistDialog(
            title = "Edit playlist",
            startName = current.name,
            startDescription = current.description,
            onSave = { name, description ->
                viewModel.updatePlaylist(current.copy(name = name.trim(), description = description.trim()))
                showEdit = false
            },
            onDismiss = { showEdit = false }
        )
    }

    if (showDelete) {
        ConfirmDialog(
            title = "Delete playlist?",
            message = "\"${current.name}\" will be deleted. Your songs will stay in the library.",
            onConfirm = {
                showDelete = false
                viewModel.deletePlaylist(current)
                navController.popBackStack()
            },
            onDismiss = { showDelete = false }
        )
    }

    if (showAddSongs) {
        AddSongsDialog(viewModel, current.id, songs) { showAddSongs = false }
    }
}

@Composable
fun AddSongsDialog(viewModel: MusicViewModel, playlistId: Long, playlistSongs: List<Song>, onDismiss: () -> Unit) {
    val allSongs by viewModel.songs.collectAsState()
    val ids = playlistSongs.map { it.id }
    val available = (allSongs ?: emptyList()).filter { it.id !in ids }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add songs") },
        text = {
            if (available.isEmpty()) {
                Text("No more songs to add.")
            } else {
                LazyColumn {
                    items(available, key = { it.id }) { song ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.addToPlaylist(playlistId, song.id) }
                                .padding(vertical = 6.dp), // Layout: space around element
                            verticalAlignment = Alignment.CenterVertically // Layout: vertical alignment
                        ) {
                            SongCover(song.coverUri, 40.dp) // Layout: cover size
                            Spacer(Modifier.width(8.dp)) // Layout: horizontal gap
                            Column(modifier = Modifier.weight(1f)) {
                                Text(song.title, maxLines = 1)
                                Text(song.artist, style = MaterialTheme.typography.bodySmall, maxLines = 1) // Style: text size (bodySmall)
                            }
                            Icon(Icons.Filled.Add, contentDescription = "Add")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") } // Style: text-only button
        }
    )
}
