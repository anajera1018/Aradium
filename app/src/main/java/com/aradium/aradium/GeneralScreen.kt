package com.aradium.aradium

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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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

@Composable
fun GeneralScreen(viewModel: MusicViewModel) {
    val settings by viewModel.settings.collectAsState()
    val songs by viewModel.songs.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    var name by rememberSaveable(settings.userName) { mutableStateOf(settings.userName) }
    var showClear by remember { mutableStateOf(false) }

    val songList = songs ?: emptyList()
    val totalTime = songList.sumOf { it.duration }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp) // Layout: space around element
    ) {
        Text("General", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) // Style: text size (headlineMedium)

        SectionTitle("Account")
        Card(modifier = Modifier.fillMaxWidth()) { // Style: grey box (color/shape from Theme.kt)
            Column(modifier = Modifier.padding(16.dp)) { // Layout: space around element
                Row(verticalAlignment = Alignment.CenterVertically) { // Layout: vertical alignment
                    Icon(
                        Icons.Filled.AccountCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary, // Style: icon color
                        modifier = Modifier.size(48.dp) // Layout: element size
                    )
                    Spacer(Modifier.width(12.dp)) // Layout: horizontal gap
                    Text(
                        if (settings.userName.isBlank()) "Guest" else settings.userName,
                        style = MaterialTheme.typography.titleMedium // Style: text size (titleMedium)
                    )
                }
                Spacer(Modifier.height(12.dp)) // Layout: vertical gap
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Your name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp)) // Layout: vertical gap
                Button(onClick = { viewModel.setUserName(name) }) { Text("Save name") } // Style: filled button
            }
        }

        SectionTitle("Settings")
        Card(modifier = Modifier.fillMaxWidth()) { // Style: grey box (color/shape from Theme.kt)
            Column(modifier = Modifier.padding(16.dp)) { // Layout: space around element
                Row(verticalAlignment = Alignment.CenterVertically) { // Layout: vertical alignment
                    Text("Dark mode", modifier = Modifier.weight(1f))
                    Switch(
                        checked = settings.darkMode,
                        onCheckedChange = { viewModel.setDarkMode(it) }
                    )
                }
                Spacer(Modifier.height(8.dp)) // Layout: vertical gap
                Text("Sort songs by", fontWeight = FontWeight.Bold) // Style: bold text
                sortNames.forEach { (key, label) ->
                    Row(verticalAlignment = Alignment.CenterVertically) { // Layout: vertical alignment
                        RadioButton(
                            selected = settings.sortOrder == key,
                            onClick = { viewModel.setSortOrder(key) }
                        )
                        Text(label)
                    }
                }
            }
        }

        SectionTitle("Library")
        Card(modifier = Modifier.fillMaxWidth()) { // Style: grey box (color/shape from Theme.kt)
            Column(modifier = Modifier.padding(16.dp)) { // Layout: space around element
                Text("Songs: ${songList.size}")
                Text("Playlists: ${playlists?.size ?: 0}")
                Text("Favorites: ${songList.count { it.favorite }}")
                Text("Total time: ${formatTime(totalTime)}")
                Spacer(Modifier.height(8.dp)) // Layout: vertical gap
                OutlinedButton(onClick = { showClear = true }) { Text("Clear play history") } // Style: outlined button
            }
        }

        SectionTitle("About")
        Card(modifier = Modifier.fillMaxWidth()) { // Style: grey box (color/shape from Theme.kt)
            Column(modifier = Modifier.padding(16.dp)) { // Layout: space around element
                Text("Aradium Music Player", fontWeight = FontWeight.Bold) // Style: bold text
                Text("Version 1.0")
                Text("CPSC 411A Final Project")
            }
        }
        Spacer(Modifier.height(16.dp)) // Layout: vertical gap
    }

    if (showClear) {
        ConfirmDialog(
            title = "Clear history?",
            message = "This resets Recently Played and Most Played.",
            onConfirm = {
                viewModel.clearHistory()
                showClear = false
            },
            onDismiss = { showClear = false }
        )
    }
}
