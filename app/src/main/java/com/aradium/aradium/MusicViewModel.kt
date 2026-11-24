package com.aradium.aradium

import android.app.Application
import android.content.ComponentName
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PlayerState(
    val currentSongId: Long? = null,
    val isPlaying: Boolean = false,
    val position: Long = 0,
    val duration: Long = 0,
    val shuffle: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF
)

data class UploadState(
    val isLoading: Boolean = false,
    val uploaded: List<Song> = emptyList(),
    val error: String? = null
)

data class LookupState(
    val isLoading: Boolean = false,
    val result: SongInfo? = null,
    val error: String? = null
)

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MusicRepository(application)
    private val settingsStore = SettingsStore(application)

    val songs: StateFlow<List<Song>?> = repository.allSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val playlists: StateFlow<List<Playlist>?> = repository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val settings: StateFlow<Settings> = settingsStore.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, Settings())

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _uploadState = MutableStateFlow(UploadState())
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()

    private val _lookupState = MutableStateFlow(LookupState())
    val lookupState: StateFlow<LookupState> = _lookupState.asStateFlow()

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    init {
        connectToService()
        viewModelScope.launch {
            while (true) {
                updatePlayerState()
                delay(500)
            }
        }
    }

    private fun connectToService() {
        val app = getApplication<Application>()
        val token = SessionToken(app, ComponentName(app, PlaybackService::class.java))
        val future = MediaController.Builder(app, token).buildAsync()
        controllerFuture = future
        future.addListener({
            try {
                val mediaController = future.get()
                controller = mediaController
                mediaController.addListener(object : Player.Listener {
                    override fun onEvents(player: Player, events: Player.Events) {
                        updatePlayerState()
                    }

                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        val id = mediaItem?.mediaId?.toLongOrNull()
                        if (id != null) {
                            viewModelScope.launch { repository.markPlayed(id) }
                        }
                    }
                })
                updatePlayerState()
            } catch (e: Exception) {
                controller = null
            }
        }, ContextCompat.getMainExecutor(app))
    }

    private fun updatePlayerState() {
        val player = controller ?: return
        _playerState.value = PlayerState(
            currentSongId = player.currentMediaItem?.mediaId?.toLongOrNull(),
            isPlaying = player.isPlaying,
            position = player.currentPosition.coerceAtLeast(0),
            duration = player.duration.coerceAtLeast(0),
            shuffle = player.shuffleModeEnabled,
            repeatMode = player.repeatMode
        )
    }

    fun getSong(id: Long): Flow<Song?> = repository.getSong(id)
    fun getPlaylist(id: Long): Flow<Playlist?> = repository.getPlaylist(id)
    fun getPlaylistSongs(id: Long): Flow<List<Song>> = repository.getPlaylistSongs(id)

    fun playSongs(list: List<Song>, startIndex: Int = 0) {
        val player = controller ?: return
        if (list.isEmpty()) return
        val items = list.map { song ->
            val metadata = MediaMetadata.Builder()
                .setTitle(song.title)
                .setArtist(song.artist)
                .setAlbumTitle(song.album)
                .setArtworkUri(song.coverUri?.let { Uri.parse(it) })
                .build()
            MediaItem.Builder()
                .setMediaId(song.id.toString())
                .setUri(Uri.parse(song.uri))
                .setMediaMetadata(metadata)
                .build()
        }
        player.setMediaItems(items, startIndex.coerceIn(0, items.size - 1), 0)
        player.prepare()
        player.play()
    }

    fun shuffleSongs(list: List<Song>) {
        if (list.isEmpty()) return
        controller?.shuffleModeEnabled = false
        playSongs(list.shuffled())
    }

    fun togglePlay() {
        val player = controller ?: return
        if (player.isPlaying) player.pause() else player.play()
    }

    fun next() {
        controller?.seekToNext()
    }

    fun previous() {
        controller?.seekToPrevious()
    }

    fun seekTo(position: Long) {
        controller?.seekTo(position)
        updatePlayerState()
    }

    fun toggleShuffle() {
        val player = controller ?: return
        player.shuffleModeEnabled = !player.shuffleModeEnabled
    }

    fun changeRepeat() {
        val player = controller ?: return
        player.repeatMode = when (player.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    fun uploadSongs(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            _uploadState.value = _uploadState.value.copy(isLoading = true, error = null)
            val added = mutableListOf<Song>()
            var failed = 0
            for (uri in uris) {
                try {
                    added.add(repository.importSong(uri))
                } catch (e: Exception) {
                    failed++
                }
            }
            _uploadState.value = UploadState(
                isLoading = false,
                uploaded = added + _uploadState.value.uploaded,
                error = if (failed > 0) "$failed file(s) could not be added" else null
            )
        }
    }

    fun clearUploads() {
        _uploadState.value = UploadState()
    }

    fun updateSong(song: Song) {
        viewModelScope.launch { repository.updateSong(song) }
    }

    fun toggleFavorite(song: Song) {
        updateSong(song.copy(favorite = !song.favorite))
    }

    fun deleteSong(song: Song) {
        viewModelScope.launch {
            if (_playerState.value.currentSongId == song.id) {
                controller?.stop()
                controller?.clearMediaItems()
            }
            repository.deleteSong(song)
            _uploadState.value = _uploadState.value.copy(
                uploaded = _uploadState.value.uploaded.filter { it.id != song.id }
            )
        }
    }

    fun setCover(song: Song, uri: Uri?) {
        viewModelScope.launch {
            if (uri == null) {
                repository.updateSong(song.copy(coverUri = null))
            } else {
                try {
                    val path = repository.saveCover(uri)
                    repository.updateSong(song.copy(coverUri = path))
                } catch (e: Exception) {
                }
            }
        }
    }

    fun lookupSong(title: String, artist: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            _lookupState.value = LookupState(isLoading = true)
            try {
                val info = repository.lookupSong(title, artist)
                _lookupState.value = if (info == null) {
                    LookupState(error = "No results found")
                } else {
                    LookupState(result = info)
                }
            } catch (e: Exception) {
                _lookupState.value = LookupState(error = "Could not connect. Check your internet.")
            }
        }
    }

    fun clearLookup() {
        _lookupState.value = LookupState()
    }

    fun createPlaylist(name: String, description: String) {
        viewModelScope.launch { repository.createPlaylist(name.trim(), description.trim()) }
    }

    fun updatePlaylist(playlist: Playlist) {
        viewModelScope.launch { repository.updatePlaylist(playlist) }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch { repository.deletePlaylist(playlist) }
    }

    fun addToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch { repository.addToPlaylist(playlistId, songId) }
    }

    fun removeFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch { repository.removeFromPlaylist(playlistId, songId) }
    }

    fun moveSong(playlistId: Long, songs: List<Song>, from: Int, to: Int) {
        if (to < 0 || to >= songs.size) return
        val list = songs.toMutableList()
        val song = list.removeAt(from)
        list.add(to, song)
        viewModelScope.launch { repository.savePlaylistOrder(playlistId, list) }
    }

    fun setUserName(name: String) {
        viewModelScope.launch { settingsStore.setUserName(name.trim()) }
    }

    fun setDarkMode(dark: Boolean) {
        viewModelScope.launch { settingsStore.setDarkMode(dark) }
    }

    fun setSortOrder(order: String) {
        viewModelScope.launch { settingsStore.setSortOrder(order) }
    }

    fun clearHistory() {
        viewModelScope.launch { repository.clearHistory() }
    }

    override fun onCleared() {
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controller = null
        super.onCleared()
    }
}
