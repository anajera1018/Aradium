package com.aradium.aradium

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val artist: String = "Unknown Artist",
    val album: String = "",
    val genre: String = "",
    val description: String = "",
    val date: String = "",
    val uri: String,
    val coverUri: String? = null,
    val duration: Long = 0,
    val favorite: Boolean = false,
    val playCount: Int = 0,
    val lastPlayed: Long = 0,
    val dateAdded: Long = System.currentTimeMillis()
)
