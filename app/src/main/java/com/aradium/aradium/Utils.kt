package com.aradium.aradium

fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:" + seconds.toString().padStart(2, '0')
}

fun sortSongs(songs: List<Song>, order: String): List<Song> {
    return when (order) {
        "title" -> songs.sortedBy { it.title.lowercase() }
        "artist" -> songs.sortedBy { it.artist.lowercase() }
        "date" -> songs.sortedByDescending { it.dateAdded }
        else -> songs
    }
}
