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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun UploadScreen(viewModel: MusicViewModel, navController: NavController) {
    val uploadState by viewModel.uploadState.collectAsState()
    val playerState by viewModel.playerState.collectAsState()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        viewModel.uploadSongs(uris)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp) // Layout: space around element
    ) {
        Text("Upload", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) // Style: text size (headlineMedium)
        Spacer(Modifier.height(16.dp)) // Layout: vertical gap

        Card(modifier = Modifier.fillMaxWidth()) { // Style: grey box (color/shape from Theme.kt)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp), // Layout: space around element
                horizontalAlignment = Alignment.CenterHorizontally // Layout: horizontal alignment
            ) {
                Icon(
                    Icons.Filled.CloudUpload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary, // Style: icon color
                    modifier = Modifier.size(64.dp) // Layout: element size
                )
                Spacer(Modifier.height(8.dp)) // Layout: vertical gap
                Text(
                    "Pick audio files from your phone (mp3, m4a, wav, flac, ogg...)",
                    textAlign = TextAlign.Center // Layout: text alignment
                )
                Spacer(Modifier.height(12.dp)) // Layout: vertical gap
                Button( // Style: filled button
                    onClick = { launcher.launch(arrayOf("audio/*")) },
                    enabled = !uploadState.isLoading
                ) {
                    Text("Choose files")
                }
            }
        }

        Spacer(Modifier.height(16.dp)) // Layout: vertical gap

        if (uploadState.isLoading) {
            Row(verticalAlignment = Alignment.CenterVertically) { // Layout: vertical alignment
                CircularProgressIndicator(modifier = Modifier.size(24.dp)) // Layout: element size
                Spacer(Modifier.width(12.dp)) // Layout: horizontal gap
                Text("Adding songs...")
            }
        }

        val error = uploadState.error
        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error) // Style: text color
        }

        if (uploadState.uploaded.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) { // Layout: vertical alignment
                Text(
                    "Just added (${uploadState.uploaded.size})",
                    fontWeight = FontWeight.Bold, // Style: bold text
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { viewModel.clearUploads() }) { Text("Clear") } // Style: text-only button
            }
            LazyColumn {
                items(uploadState.uploaded, key = { it.id }) { song ->
                    SongRow(
                        song = song,
                        isCurrent = song.id == playerState.currentSongId,
                        onClick = { viewModel.playSongs(listOf(song)) }
                    ) {
                        IconButton(onClick = { navController.navigate("edit/${song.id}") }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit")
                        }
                    }
                }
            }
        }
    }
}
