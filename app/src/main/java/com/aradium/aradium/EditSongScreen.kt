package com.aradium.aradium

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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

@Composable
fun EditSongScreen(viewModel: MusicViewModel, navController: NavController, songId: Long) {
    val songFlow = remember(songId) { viewModel.getSong(songId) }
    val song by songFlow.collectAsState(initial = null)
    val lookupState by viewModel.lookupState.collectAsState()

    DisposableEffect(Unit) {
        onDispose { viewModel.clearLookup() }
    }

    val current = song
    if (current == null) {
        LoadingState()
        return
    }

    var title by rememberSaveable(current.id) { mutableStateOf(current.title) }
    var artist by rememberSaveable(current.id) { mutableStateOf(current.artist) }
    var album by rememberSaveable(current.id) { mutableStateOf(current.album) }
    var genre by rememberSaveable(current.id) { mutableStateOf(current.genre) }
    var date by rememberSaveable(current.id) { mutableStateOf(current.date) }
    var description by rememberSaveable(current.id) { mutableStateOf(current.description) }
    var favorite by rememberSaveable(current.id) { mutableStateOf(current.favorite) }
    var showDelete by remember { mutableStateOf(false) }

    val coverLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            viewModel.setCover(current, uri)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp) // Layout: space around element
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) { // Layout: vertical alignment
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Edit Song", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) // Style: text size (headlineSmall)
        }

        Spacer(Modifier.height(12.dp)) // Layout: vertical gap
        Row(verticalAlignment = Alignment.CenterVertically) { // Layout: vertical alignment
            SongCover(current.coverUri, 110.dp) // Layout: cover size
            Spacer(Modifier.width(16.dp)) // Layout: horizontal gap
            Column {
                Button(onClick = { coverLauncher.launch("image/*") }) { Text("Change cover") } // Style: filled button
                if (current.coverUri != null) {
                    TextButton(onClick = { viewModel.setCover(current, null) }) { Text("Remove cover") } // Style: text-only button
                }
            }
        }

        Spacer(Modifier.height(12.dp)) // Layout: vertical gap
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = artist, onValueChange = { artist = it }, label = { Text("Artist") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = album, onValueChange = { album = it }, label = { Text("Album") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = genre, onValueChange = { genre = it }, label = { Text("Genre") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, minLines = 3, modifier = Modifier.fillMaxWidth())

        Row(verticalAlignment = Alignment.CenterVertically) { // Layout: vertical alignment
            Checkbox(checked = favorite, onCheckedChange = { favorite = it })
            Text("Favorite")
        }

        Spacer(Modifier.height(8.dp)) // Layout: vertical gap
        Card(modifier = Modifier.fillMaxWidth()) { // Style: grey box (color/shape from Theme.kt)
            Column(modifier = Modifier.padding(16.dp)) { // Layout: space around element
                Text("Find song info online", fontWeight = FontWeight.Bold) // Style: bold text
                Text("Uses the name and artist above.", style = MaterialTheme.typography.bodySmall) // Style: text size (bodySmall)
                Spacer(Modifier.height(8.dp)) // Layout: vertical gap
                OutlinedButton( // Style: outlined button
                    onClick = { viewModel.lookupSong(title, artist) },
                    enabled = !lookupState.isLoading && title.isNotBlank()
                ) {
                    Text("Search online")
                }
                if (lookupState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp)) // Layout: element size
                }
                val error = lookupState.error
                if (error != null) {
                    Text(error, color = MaterialTheme.colorScheme.error) // Style: text color
                }
                val result = lookupState.result
                if (result != null) {
                    Spacer(Modifier.height(8.dp)) // Layout: vertical gap
                    Text("${result.trackName ?: "?"} - ${result.artistName ?: "?"}")
                    Text(
                        "${result.collectionName ?: ""} ${result.releaseDate?.take(4) ?: ""}",
                        style = MaterialTheme.typography.bodySmall // Style: text size (bodySmall)
                    )
                    TextButton(onClick = { // Style: text-only button
                        title = result.trackName ?: title
                        artist = result.artistName ?: artist
                        album = result.collectionName ?: album
                        genre = result.primaryGenreName ?: genre
                        date = result.releaseDate?.take(10) ?: date
                        val art = result.artworkUrl100
                        if (art != null) {
                            viewModel.updateSong(current.copy(coverUri = art.replace("100x100", "600x600")))
                        }
                        viewModel.clearLookup()
                    }) {
                        Text("Use this info")
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp)) // Layout: vertical gap
        Row {
            Button(onClick = { // Style: filled button
                viewModel.updateSong(
                    current.copy(
                        title = title.trim().ifBlank { current.title },
                        artist = artist.trim().ifBlank { "Unknown Artist" },
                        album = album.trim(),
                        genre = genre.trim(),
                        date = date.trim(),
                        description = description.trim(),
                        favorite = favorite
                    )
                )
                navController.popBackStack()
            }) {
                Text("Save")
            }
            Spacer(Modifier.width(8.dp)) // Layout: horizontal gap
            OutlinedButton(onClick = { showDelete = true }) { Text("Delete") } // Style: outlined button
        }
        Spacer(Modifier.height(16.dp)) // Layout: vertical gap
    }

    if (showDelete) {
        ConfirmDialog(
            title = "Delete song?",
            message = "\"${current.title}\" will be removed from your library and playlists.",
            onConfirm = {
                showDelete = false
                viewModel.deleteSong(current)
                navController.popBackStack()
            },
            onDismiss = { showDelete = false }
        )
    }
}
