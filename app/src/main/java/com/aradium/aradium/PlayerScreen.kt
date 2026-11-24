package com.aradium.aradium

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.navigation.NavController

@Composable
fun PlayerScreen(viewModel: MusicViewModel, navController: NavController) {
    val songs by viewModel.songs.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    var dragPosition by remember { mutableStateOf<Float?>(null) }

    val song = songs?.find { it.id == playerState.currentSongId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp), // Layout: space around element
        horizontalAlignment = Alignment.CenterHorizontally // Layout: horizontal alignment
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }

        if (song == null) {
            EmptyState("Nothing is playing right now.")
            return@Column
        }

        Spacer(Modifier.height(16.dp)) // Layout: vertical gap
        SongCover(song.coverUri, 280.dp) // Layout: cover size
        Spacer(Modifier.height(24.dp)) // Layout: vertical gap

        Row(verticalAlignment = Alignment.CenterVertically) { // Layout: vertical alignment
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    song.title,
                    style = MaterialTheme.typography.headlineSmall, // Style: text size (headlineSmall)
                    fontWeight = FontWeight.Bold, // Style: bold text
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(song.artist, style = MaterialTheme.typography.bodyLarge) // Style: text size (bodyLarge)
                if (song.album.isNotBlank()) {
                    Text(song.album, style = MaterialTheme.typography.bodySmall) // Style: text size (bodySmall)
                }
            }
            IconButton(onClick = { viewModel.toggleFavorite(song) }) {
                Icon(
                    if (song.favorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (song.favorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface // Style: icon color
                )
            }
        }

        if (song.description.isNotBlank()) {
            Text(
                song.description,
                style = MaterialTheme.typography.bodySmall, // Style: text size (bodySmall)
                textAlign = TextAlign.Start, // Layout: text alignment
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(16.dp)) // Layout: vertical gap
        val duration = playerState.duration.toFloat().coerceAtLeast(1f)
        Slider( // Style: seek bar (colors from Theme.kt)
            value = (dragPosition ?: playerState.position.toFloat()).coerceIn(0f, duration),
            onValueChange = { dragPosition = it },
            onValueChangeFinished = {
                dragPosition?.let { viewModel.seekTo(it.toLong()) }
                dragPosition = null
            },
            valueRange = 0f..duration
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(formatTime(dragPosition?.toLong() ?: playerState.position), modifier = Modifier.weight(1f))
            Text(formatTime(playerState.duration))
        }

        Spacer(Modifier.height(16.dp)) // Layout: vertical gap
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly, // Layout: horizontal spacing
            verticalAlignment = Alignment.CenterVertically // Layout: vertical alignment
        ) {
            IconButton(onClick = { viewModel.toggleShuffle() }) {
                Icon(
                    Icons.Filled.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (playerState.shuffle) MaterialTheme.colorScheme.primary else Color.Gray // Style: icon color
                )
            }
            IconButton(onClick = { viewModel.previous() }) {
                Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous", modifier = Modifier.size(36.dp)) // Layout: element size
            }
            FilledIconButton(onClick = { viewModel.togglePlay() }, modifier = Modifier.size(72.dp)) { // Style: big round play button; size = button size
                Icon(
                    if (playerState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "Play or pause",
                    modifier = Modifier.size(40.dp) // Layout: element size
                )
            }
            IconButton(onClick = { viewModel.next() }) {
                Icon(Icons.Filled.SkipNext, contentDescription = "Next", modifier = Modifier.size(36.dp)) // Layout: element size
            }
            IconButton(onClick = { viewModel.changeRepeat() }) {
                Icon(
                    if (playerState.repeatMode == Player.REPEAT_MODE_ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                    contentDescription = "Repeat",
                    tint = if (playerState.repeatMode == Player.REPEAT_MODE_OFF) Color.Gray else MaterialTheme.colorScheme.primary // Style: icon color
                )
            }
        }
    }
}
