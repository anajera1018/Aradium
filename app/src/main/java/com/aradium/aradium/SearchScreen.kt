package com.aradium.aradium

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun SearchScreen(viewModel: MusicViewModel, navController: NavController) {
    val songs by viewModel.songs.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }

    val text = query.trim()
    val songResults = if (text.isBlank()) emptyList() else (songs ?: emptyList()).filter {
        it.title.contains(text, ignoreCase = true) ||
            it.artist.contains(text, ignoreCase = true) ||
            it.album.contains(text, ignoreCase = true) ||
            it.genre.contains(text, ignoreCase = true)
    }
    val playlistResults = if (text.isBlank()) emptyList() else (playlists ?: emptyList()).filter {
        it.name.contains(text, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp) // Layout: space around element
    ) {
        Text("Search", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) // Style: text size (headlineMedium)
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Songs, artists, albums, playlists") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp) // Layout: space around element
        )

        if (text.isBlank()) {
            EmptyState("Search your music library")
        } else if (songResults.isEmpty() && playlistResults.isEmpty()) {
            EmptyState("No results for \"$text\"")
        } else {
            LazyColumn {
                if (playlistResults.isNotEmpty()) {
                    item { SectionTitle("Playlists") }
                    items(playlistResults, key = { "p" + it.id }) { playlist ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate("playlist/${playlist.id}") }
                                .padding(vertical = 10.dp), // Layout: space around element
                            verticalAlignment = Alignment.CenterVertically // Layout: vertical alignment
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.QueueMusic,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary // Style: icon color
                            )
                            Spacer(Modifier.width(16.dp)) // Layout: horizontal gap
                            Text(playlist.name)
                        }
                    }
                }
                if (songResults.isNotEmpty()) {
                    item { SectionTitle("Songs") }
                    itemsIndexed(songResults, key = { _, song -> "s" + song.id }) { index, song ->
                        SongRow(
                            song = song,
                            isCurrent = song.id == playerState.currentSongId,
                            onClick = { viewModel.playSongs(songResults, index) }
                        ) {
                            SongMenu(song, viewModel, navController)
                        }
                    }
                }
            }
        }
    }
}
