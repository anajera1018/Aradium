package com.aradium.aradium

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MusicRepository(private val context: Context) {

    private val dao = MusicDatabase.getDatabase(context).musicDao()
    private val api = RetrofitInstance.api

    val allSongs: Flow<List<Song>> = dao.getAllSongs()
    val allPlaylists: Flow<List<Playlist>> = dao.getAllPlaylists()

    fun getSong(id: Long): Flow<Song?> = dao.getSong(id)
    fun getPlaylist(id: Long): Flow<Playlist?> = dao.getPlaylist(id)
    fun getPlaylistSongs(id: Long): Flow<List<Song>> = dao.getPlaylistSongs(id)

    suspend fun updateSong(song: Song) = dao.updateSong(song)

    suspend fun deleteSong(song: Song) {
        dao.removeSongFromAllPlaylists(song.id)
        dao.deleteSong(song)
    }

    suspend fun markPlayed(id: Long) = dao.markPlayed(id, System.currentTimeMillis())

    suspend fun clearHistory() = dao.clearHistory()

    suspend fun createPlaylist(name: String, description: String): Long {
        return dao.insertPlaylist(Playlist(name = name, description = description))
    }

    suspend fun updatePlaylist(playlist: Playlist) = dao.updatePlaylist(playlist)

    suspend fun deletePlaylist(playlist: Playlist) {
        dao.clearPlaylist(playlist.id)
        dao.deletePlaylist(playlist)
    }

    suspend fun addToPlaylist(playlistId: Long, songId: Long) {
        val count = dao.countPlaylistSongs(playlistId)
        dao.insertPlaylistSong(PlaylistSong(playlistId, songId, count))
    }

    suspend fun removeFromPlaylist(playlistId: Long, songId: Long) {
        dao.removeFromPlaylist(playlistId, songId)
    }

    suspend fun savePlaylistOrder(playlistId: Long, songs: List<Song>) {
        songs.forEachIndexed { index, song ->
            dao.setPosition(playlistId, song.id, index)
        }
    }

    suspend fun importSong(uri: Uri): Song = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (e: SecurityException) {
        }

        val retriever = MediaMetadataRetriever()
        var title: String? = null
        var artist: String? = null
        var album: String? = null
        var genre: String? = null
        var year: String? = null
        var duration = 0L
        var cover: String? = null

        try {
            retriever.setDataSource(context, uri)
            title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
            year = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
            duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            val picture = retriever.embeddedPicture
            if (picture != null) {
                val file = File(context.filesDir, "cover_${System.currentTimeMillis()}.jpg")
                file.writeBytes(picture)
                cover = Uri.fromFile(file).toString()
            }
        } catch (e: Exception) {
        } finally {
            retriever.release()
        }

        val song = Song(
            title = if (title.isNullOrBlank()) getFileName(uri) else title,
            artist = if (artist.isNullOrBlank()) "Unknown Artist" else artist,
            album = album ?: "",
            genre = genre ?: "",
            date = if (year.isNullOrBlank()) today() else year,
            uri = uri.toString(),
            coverUri = cover,
            duration = duration
        )
        val id = dao.insertSong(song)
        song.copy(id = id)
    }

    suspend fun saveCover(uri: Uri): String = withContext(Dispatchers.IO) {
        val file = File(context.filesDir, "cover_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        Uri.fromFile(file).toString()
    }

    suspend fun lookupSong(title: String, artist: String): SongInfo? {
        val term = if (artist == "Unknown Artist") title else "$title $artist"
        val response = api.searchSong(term.trim())
        return response.results.firstOrNull()
    }

    private fun getFileName(uri: Uri): String {
        var name = "Unknown Song"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) {
                name = cursor.getString(index) ?: name
            }
        }
        return name.substringBeforeLast(".")
    }

    private fun today(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
}
