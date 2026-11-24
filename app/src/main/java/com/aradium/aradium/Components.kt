package com.aradium.aradium

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage

@Composable
fun SongCover(coverUri: String?, size: Dp) {
    if (coverUri != null) {
        AsyncImage(
            model = coverUri,
            contentDescription = "Cover",
            contentScale = ContentScale.Crop, // Layout: how image fills box
            modifier = Modifier
                .size(size) // Layout: element size
                .clip(RoundedCornerShape(0.dp)) // Style: cover corner roundness (0.dp = square)
        )
    } else {
        Box(
            modifier = Modifier
                .size(size) // Layout: element size
                .clip(RoundedCornerShape(0.dp)) // Style: cover corner roundness (0.dp = square)
                .background(MaterialTheme.colorScheme.surfaceVariant), // Style: box background color
            contentAlignment = Alignment.Center // Layout: content alignment
        ) {
            Icon(
                Icons.Filled.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary, // Style: icon color
                modifier = Modifier.size(size / 2) // Layout: element size
            )
        }
    }
}

@Composable
fun SongRow(
    song: Song,
    isCurrent: Boolean,
    onClick: () -> Unit,
    actions: @Composable () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 6.dp), // Layout: space around element
        verticalAlignment = Alignment.CenterVertically // Layout: vertical alignment
    ) {
        SongCover(song.coverUri, 50.dp) // Layout: cover size
        Spacer(Modifier.width(12.dp)) // Layout: horizontal gap
        Column(modifier = Modifier.weight(1f)) {
            Text(
                song.title,
                fontWeight = FontWeight.Bold, // Style: bold text
                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, // Style: text color
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                song.artist + if (song.duration > 0) " • " + formatTime(song.duration) else "",
                style = MaterialTheme.typography.bodySmall, // Style: text size (bodySmall)
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        actions()
    }
}

@Composable
fun SongCard(song: Song, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(130.dp) // Layout: element width
            .clickable { onClick() }
    ) {
        SongCover(song.coverUri, 130.dp) // Layout: cover size
        Spacer(Modifier.height(4.dp)) // Layout: vertical gap
        Text(song.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis) // Style: bold text
        Text(song.artist, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis) // Style: text size (bodySmall)
    }
}

@Composable
fun SongMenu(song: Song, viewModel: MusicViewModel, navController: NavController) {
    var expanded by remember { mutableStateOf(false) }
    var showPlaylists by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Filled.MoreVert, contentDescription = "More")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Edit") },
                leadingIcon = { Icon(Icons.Filled.Edit, null) },
                onClick = {
                    expanded = false
                    navController.navigate("edit/${song.id}")
                }
            )
            DropdownMenuItem(
                text = { Text(if (song.favorite) "Remove from favorites" else "Add to favorites") },
                leadingIcon = { Icon(if (song.favorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, null) },
                onClick = {
                    expanded = false
                    viewModel.toggleFavorite(song)
                }
            )
            DropdownMenuItem(
                text = { Text("Add to playlist") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, null) },
                onClick = {
                    expanded = false
                    showPlaylists = true
                }
            )
            DropdownMenuItem(
                text = { Text("Delete") },
                leadingIcon = { Icon(Icons.Filled.Delete, null) },
                onClick = {
                    expanded = false
                    showDelete = true
                }
            )
        }
    }

    if (showPlaylists) {
        AddToPlaylistDialog(
            viewModel = viewModel,
            onPick = { playlist ->
                viewModel.addToPlaylist(playlist.id, song.id)
                showPlaylists = false
            },
            onDismiss = { showPlaylists = false }
        )
    }

    if (showDelete) {
        ConfirmDialog(
            title = "Delete song?",
            message = "\"${song.title}\" will be removed from your library and playlists. The file on your phone is not deleted.",
            onConfirm = {
                viewModel.deleteSong(song)
                showDelete = false
            },
            onDismiss = { showDelete = false }
        )
    }
}

@Composable
fun AddToPlaylistDialog(viewModel: MusicViewModel, onPick: (Playlist) -> Unit, onDismiss: () -> Unit) {
    val playlists by viewModel.playlists.collectAsState()
    val list = playlists ?: emptyList()
    var showCreate by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to playlist") },
        text = {
            if (list.isEmpty()) {
                Text("You don't have any playlists yet.")
            } else {
                LazyColumn {
                    items(list) { playlist ->
                        Text(
                            playlist.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPick(playlist) }
                                .padding(vertical = 12.dp) // Layout: space around element
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { showCreate = true }) { Text("New playlist") } // Style: text-only button
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") } // Style: text-only button
        }
    )

    if (showCreate) {
        PlaylistDialog(
            title = "New playlist",
            startName = "",
            startDescription = "",
            onSave = { name, description ->
                viewModel.createPlaylist(name, description)
                showCreate = false
            },
            onDismiss = { showCreate = false }
        )
    }
}

@Composable
fun PlaylistDialog(
    title: String,
    startName: String,
    startDescription: String,
    onSave: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(startName) }
    var description by remember { mutableStateOf(startDescription) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp)) // Layout: vertical gap
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, description) }, enabled = name.isNotBlank()) { // Style: text-only button
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") } // Style: text-only button
        }
    )
}

@Composable
fun ConfirmDialog(title: String, message: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Yes") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { // Layout: content alignment
        CircularProgressIndicator()
    }
}

@Composable
fun EmptyState(message: String, buttonText: String? = null, onClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp), // Layout: space around element
        horizontalAlignment = Alignment.CenterHorizontally, // Layout: horizontal alignment
        verticalArrangement = Arrangement.Center // Layout: vertical spacing
    ) {
        Icon(
            Icons.Filled.MusicNote,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary, // Style: icon color
            modifier = Modifier.size(64.dp) // Layout: element size
        )
        Spacer(Modifier.height(12.dp)) // Layout: vertical gap
        Text(message, textAlign = TextAlign.Center) // Layout: text alignment
        if (buttonText != null) {
            Spacer(Modifier.height(12.dp)) // Layout: vertical gap
            Button(onClick = onClick) { Text(buttonText) } // Style: filled button
        }
    }
}

@Composable
fun MiniPlayer(song: Song, playerState: PlayerState, viewModel: MusicViewModel, onClick: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant) { // Style: mini player background color
        Column {
            val progress = if (playerState.duration > 0) {
                playerState.position.toFloat() / playerState.duration
            } else {
                0f
            }
            LinearProgressIndicator( // Style: thin progress line
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick() }
                    .padding(horizontal = 12.dp, vertical = 8.dp), // Layout: space around element
                verticalAlignment = Alignment.CenterVertically // Layout: vertical alignment
            ) {
                SongCover(song.coverUri, 42.dp) // Layout: cover size
                Spacer(Modifier.width(12.dp)) // Layout: horizontal gap
                Column(modifier = Modifier.weight(1f)) {
                    Text(song.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis) // Style: bold text
                    Text(song.artist, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis) // Style: text size (bodySmall)
                }
                IconButton(onClick = { viewModel.togglePlay() }) {
                    Icon(
                        if (playerState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play or pause"
                    )
                }
                IconButton(onClick = { viewModel.next() }) {
                    Icon(Icons.Filled.SkipNext, contentDescription = "Next")
                }
            }
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleLarge, // Style: text size (titleLarge)
        fontWeight = FontWeight.Bold, // Style: bold text
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp) // Layout: space around element
    )
}
