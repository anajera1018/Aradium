package com.aradium.aradium

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun HomeScreen(viewModel: MusicViewModel, navController: NavController) {
    val songs by viewModel.songs.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val allSongs = songs
    if (allSongs == null) {
        LoadingState()
        return
    }

    val recentlyPlayed = allSongs.filter { it.lastPlayed > 0 }.sortedByDescending { it.lastPlayed }.take(10)
    val mostPlayed = allSongs.filter { it.playCount > 0 }.sortedByDescending { it.playCount }.take(10)
    val recentlyAdded = allSongs.take(10)
    val favorites = allSongs.filter { it.favorite }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp) // Layout: space around element
    ) {
        item {
            Text(
                if (settings.userName.isBlank()) "Welcome!" else "Hi, ${settings.userName}!",
                style = MaterialTheme.typography.headlineMedium, // Style: text size (headlineMedium)
                fontWeight = FontWeight.Bold, // Style: bold text
                modifier = Modifier.padding(top = 16.dp) // Layout: space around element
            )
        }

        if (allSongs.isEmpty()) {
            item {
                EmptyState("Your library is empty.\nUpload some music to get started.", "Upload music") {
                    navController.navigate(Screen.Upload.route)
                }
            }
        } else {
            item {
                Row(modifier = Modifier.padding(top = 12.dp)) { // Layout: space around element
                    Button(onClick = { viewModel.playSongs(allSongs) }) { // Style: filled button
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(4.dp)) // Layout: horizontal gap
                        Text("Play all")
                    }
                    Spacer(Modifier.width(8.dp)) // Layout: horizontal gap
                    OutlinedButton(onClick = { viewModel.shuffleSongs(allSongs) }) { // Style: outlined button
                        Icon(Icons.Filled.Shuffle, contentDescription = null)
                        Spacer(Modifier.width(4.dp)) // Layout: horizontal gap
                        Text("Shuffle")
                    }
                }
            }
            if (recentlyPlayed.isNotEmpty()) {
                item { HomeSection("Recently Played", recentlyPlayed, viewModel) }
            }
            if (mostPlayed.isNotEmpty()) {
                item { HomeSection("Most Played", mostPlayed, viewModel) }
            }
            item { HomeSection("Recently Added", recentlyAdded, viewModel) }
            if (favorites.isNotEmpty()) {
                item { HomeSection("Favorites", favorites, viewModel) }
            }
            item { Spacer(Modifier.height(16.dp)) } // Layout: vertical gap
        }
    }
}

@Composable
fun HomeSection(title: String, songs: List<Song>, viewModel: MusicViewModel) {
    Column {
        SectionTitle(title)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) { // Layout: horizontal spacing
            itemsIndexed(songs) { index, song ->
                SongCard(song) { viewModel.playSongs(songs, index) }
            }
        }
    }
}
