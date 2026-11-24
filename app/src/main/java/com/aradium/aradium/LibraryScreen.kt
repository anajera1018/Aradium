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
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

val sortNames = mapOf(
    "date" to "Recently added",
    "title" to "Title",
    "artist" to "Artist"
)

@Composable
fun LibraryScreen(viewModel: MusicViewModel, navController: NavController) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("Songs", "Playlists", "Favorites")

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Library",
            style = MaterialTheme.typography.headlineMedium, // Style: text size (headlineMedium)
            fontWeight = FontWeight.Bold, // Style: bold text
            modifier = Modifier.padding(16.dp) // Layout: space around element
        )
        TabRow(selectedTabIndex = selectedTab) { // Style: tab bar (colors from Theme.kt)
            tabs.forEachIndexed { index, name ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(name) }
                )
            }
        }
        when (selectedTab) {
            0 -> SongsTab(viewModel, navController, false)
            1 -> PlaylistsTab(viewModel, navController)
            else -> SongsTab(viewModel, navController, true)
        }
    }
}

@Composable
fun SongsTab(viewModel: MusicViewModel, navController: NavController, onlyFavorites: Boolean) {
    val songs by viewModel.songs.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }

    val allSongs = songs
    if (allSongs == null) {
        LoadingState()
        return
    }

    val filtered = if (onlyFavorites) allSongs.filter { it.favorite } else allSongs
    val sorted = sortSongs(filtered, settings.sortOrder)

    if (sorted.isEmpty()) {
        if (onlyFavorites) {
            EmptyState("No favorites yet.\nTap the menu on a song to add it.")
        } else {
            EmptyState("No songs yet.", "Upload music") {
                navController.navigate(Screen.Upload.route)
            }
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp), // Layout: space around element
            verticalAlignment = Alignment.CenterVertically // Layout: vertical alignment
        ) {
            Text("${sorted.size} songs", modifier = Modifier.weight(1f))
            Box {
                TextButton(onClick = { showSortMenu = true }) { // Style: text-only button
                    Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null)
                    Spacer(Modifier.width(4.dp)) // Layout: horizontal gap
                    Text(sortNames[settings.sortOrder] ?: "Sort")
                }
                DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                    sortNames.forEach { (key, name) ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = {
                                viewModel.setSortOrder(key)
                                showSortMenu = false
                            }
                        )
                    }
                }
            }
        }
        LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) { // Layout: space around element
            itemsIndexed(sorted, key = { _, song -> song.id }) { index, song ->
                SongRow(
                    song = song,
                    isCurrent = song.id == playerState.currentSongId,
                    onClick = { viewModel.playSongs(sorted, index) }
                ) {
                    SongMenu(song, viewModel, navController)
                }
            }
        }
    }
}

@Composable
fun PlaylistsTab(viewModel: MusicViewModel, navController: NavController) {
    val playlists by viewModel.playlists.collectAsState()
    var showCreate by remember { mutableStateOf(false) }

    val list = playlists
    if (list == null) {
        LoadingState()
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Button( // Style: filled button
            onClick = { showCreate = true },
            modifier = Modifier.padding(16.dp) // Layout: space around element
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(4.dp)) // Layout: horizontal gap
            Text("New playlist")
        }

        if (list.isEmpty()) {
            EmptyState("No playlists yet.\nCreate one to group your songs.")
        } else {
            LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) { // Layout: space around element
                items(list, key = { it.id }) { playlist ->
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate("playlist/${playlist.id}") }
                                .padding(vertical = 12.dp), // Layout: space around element
                            verticalAlignment = Alignment.CenterVertically // Layout: vertical alignment
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.QueueMusic,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary // Style: icon color
                            )
                            Spacer(Modifier.width(16.dp)) // Layout: horizontal gap
                            Column {
                                Text(playlist.name, fontWeight = FontWeight.Bold) // Style: bold text
                                if (playlist.description.isNotBlank()) {
                                    Text(playlist.description, style = MaterialTheme.typography.bodySmall) // Style: text size (bodySmall)
                                }
                            }
                        }
                        HorizontalDivider() // Style: divider line
                    }
                }
            }
        }
    }

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
