package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.PlaylistEntity
import com.example.data.model.Album
import com.example.data.model.Artist
import com.example.data.model.Folder
import com.example.data.model.Song
import com.example.data.model.SortOrder
import com.example.data.repository.MusicRepository
import com.example.playback.PlaybackManager
import com.example.ui.theme.SonoraAccentEmerald
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NavigationTab(val label: String) {
    HOME("Explorar"),
    LIBRARY("Biblioteca"),
    PLAYLISTS("Playlists"),
    FAVORITES("Favoritos"),
    SETTINGS("Configurações")
}

enum class LibrarySubTab(val label: String) {
    ALL_SONGS("Todas"),
    ALBUMS("Álbuns"),
    ARTISTS("Artistas"),
    FOLDERS("Pastas"),
    FAVORITES("Favoritos")
}

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MusicRepository(application)

    init {
        PlaybackManager.init(application)
        PlaybackManager.onSongPlayedListener = { song ->
            viewModelScope.launch {
                repository.recordSongPlayed(song.id, song.durationMs)
            }
        }
        // Initial scan
        viewModelScope.launch {
            repository.scanDeviceAudio(includeSamplesIfEmpty = true)
        }
    }

    val isScanning: StateFlow<Boolean> = repository.isScanning

    // Search query
    val searchQuery = MutableStateFlow("")

    // Navigation and screen navigation stack
    val selectedTab = MutableStateFlow(NavigationTab.HOME)
    val selectedLibrarySubTab = MutableStateFlow(LibrarySubTab.ALL_SONGS)
    val sortOrder = MutableStateFlow(SortOrder.NAME)

    // Sub-screens / Detail view states
    val activeArtistDetail = MutableStateFlow<Artist?>(null)
    val activeAlbumDetail = MutableStateFlow<Album?>(null)
    val activeFolderDetail = MutableStateFlow<Folder?>(null)
    val activePlaylistDetail = MutableStateFlow<PlaylistEntity?>(null)
    val activeGenreDetail = MutableStateFlow<String?>(null)
    val activeHistoryDetail = MutableStateFlow(false)
    val activeMostPlayedDetail = MutableStateFlow(false)
    val activeNeverPlayedDetail = MutableStateFlow(false)
    val showStats = MutableStateFlow(false)
    val isSearchOpen = MutableStateFlow(false)

    // Modals and dialogs
    val showFullPlayer = MutableStateFlow(false)
    val showQueueSheet = MutableStateFlow(false)
    val showSleepTimerDialog = MutableStateFlow(false)
    val songForContextMenu = MutableStateFlow<Song?>(null)
    val songForDetails = MutableStateFlow<Song?>(null)
    val songForAddToPlaylist = MutableStateFlow<Song?>(null)
    val showCreatePlaylistDialog = MutableStateFlow(false)

    // Onboarding
    val showOnboarding = MutableStateFlow(false)

    // Appearance
    val isDarkTheme = MutableStateFlow(true)
    val accentColor = MutableStateFlow(SonoraAccentEmerald)

    // Player Preferences delegation
    val crossfadeSeconds = PlaybackManager.crossfadeSeconds
    val dynamicColorsEnabled = PlaybackManager.dynamicColorsEnabled
    val visualizerStyle = PlaybackManager.visualizerStyle
    val libraryViewMode = PlaybackManager.libraryViewMode
    val gesturesEnabled = PlaybackManager.gesturesEnabled
    val sleepTimerMode = PlaybackManager.sleepTimerMode
    val sleepTimerRemainingSeconds = PlaybackManager.sleepTimerRemainingSeconds
    val sleepTimerFormatted = PlaybackManager.sleepTimerFormatted

    // Statistics Flows
    val mostPlayedHistory: StateFlow<List<com.example.data.local.HistoryEntity>> = repository.mostPlayedHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    val totalPlayCount: StateFlow<Int?> = repository.totalPlayCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )
    val totalDurationPlayed: StateFlow<Long?> = repository.totalDurationPlayed.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0L
    )

    // All songs with favorite status
    val allSongs: StateFlow<List<Song>> = repository.songsWithFavorites.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filtered / Sorted Songs based on sort order and search query
    val displaySongs: StateFlow<List<Song>> = combine(allSongs, sortOrder, searchQuery) { songs, sort, query ->
        val filtered = if (query.isBlank()) {
            songs
        } else {
            val q = query.trim().lowercase()
            songs.filter {
                it.title.lowercase().contains(q) ||
                it.artist.lowercase().contains(q) ||
                it.album.lowercase().contains(q) ||
                it.folder.lowercase().contains(q)
            }
        }

        when (sort) {
            SortOrder.NAME -> filtered.sortedBy { it.title.lowercase() }
            SortOrder.NAME_DESC -> filtered.sortedByDescending { it.title.lowercase() }
            SortOrder.ARTIST -> filtered.sortedBy { it.artist.lowercase() }
            SortOrder.ALBUM -> filtered.sortedBy { it.album.lowercase() }
            SortOrder.DATE_ADDED -> filtered.sortedByDescending { it.dateAdded }
            SortOrder.DURATION -> filtered.sortedByDescending { it.durationMs }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Albums
    val albums: StateFlow<List<Album>> = combine(allSongs, searchQuery) { songs, query ->
        val list = repository.extractAlbums(songs)
        if (query.isBlank()) list else {
            val q = query.trim().lowercase()
            list.filter { it.title.lowercase().contains(q) || it.artist.lowercase().contains(q) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Artists
    val artists: StateFlow<List<Artist>> = combine(allSongs, searchQuery) { songs, query ->
        val list = repository.extractArtists(songs)
        if (query.isBlank()) list else {
            val q = query.trim().lowercase()
            list.filter { it.name.lowercase().contains(q) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Folders
    val folders: StateFlow<List<Folder>> = combine(allSongs, searchQuery) { songs, query ->
        val list = repository.extractFolders(songs)
        if (query.isBlank()) list else {
            val q = query.trim().lowercase()
            list.filter { it.name.lowercase().contains(q) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Playlists
    val playlists: StateFlow<List<PlaylistEntity>> = repository.playlists.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Favorites
    val favoriteSongs: StateFlow<List<Song>> = allSongs.combine(repository.favoriteSongIds) { songs, favIds ->
        val favSet = favIds.toSet()
        songs.filter { favSet.contains(it.id) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // History (Recently played)
    val recentlyPlayed: StateFlow<List<Song>> = combine(allSongs, repository.history) { songs, hist ->
        val songMap = songs.associateBy { it.id }
        hist.mapNotNull { songMap[it.songId] }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Recently added songs
    val recentlyAdded: StateFlow<List<Song>> = allSongs.combine(MutableStateFlow(Unit)) { songs, _ ->
        songs.sortedByDescending { it.dateAdded }.take(15)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Active playlist tracks
    val activePlaylistSongs = MutableStateFlow<List<Song>>(emptyList())

    fun loadPlaylistTracks(playlistId: Long) {
        viewModelScope.launch {
            repository.getPlaylistSongIds(playlistId).collect { ids ->
                val set = ids.toSet()
                val currentSongs = allSongs.value
                activePlaylistSongs.value = ids.mapNotNull { id -> currentSongs.find { it.id == id } }
            }
        }
    }

    // Playback state delegation
    val currentSong = PlaybackManager.currentSong
    val isPlaying = PlaybackManager.isPlaying
    val currentPositionMs = PlaybackManager.currentPositionMs
    val durationMs = PlaybackManager.durationMs
    val queue = PlaybackManager.queue
    val queueIndex = PlaybackManager.queueIndex
    val shuffleMode = PlaybackManager.shuffleMode
    val repeatMode = PlaybackManager.repeatMode

    fun refreshLibrary() {
        viewModelScope.launch {
            repository.scanDeviceAudio(includeSamplesIfEmpty = false)
        }
    }

    fun addDemoTracks() {
        viewModelScope.launch {
            repository.addDemoTracks()
        }
    }

    fun playSong(song: Song) {
        PlaybackManager.playSong(song)
    }

    fun playSongList(songs: List<Song>, startIndex: Int = 0) {
        PlaybackManager.playSongList(songs, startIndex, true)
    }

    fun shuffleSongList(songs: List<Song>) {
        if (songs.isEmpty()) return
        val shuffled = songs.shuffled()
        PlaybackManager.playSongList(shuffled, 0, true)
    }

    fun togglePlayPause() = PlaybackManager.togglePlayPause()
    fun playNext() = PlaybackManager.playNext()
    fun playPrevious() = PlaybackManager.playPrevious()
    fun seekTo(ms: Long) = PlaybackManager.seekTo(ms)
    fun rewind10Seconds() = PlaybackManager.rewind10Seconds()
    fun forward10Seconds() = PlaybackManager.forward10Seconds()
    fun toggleShuffle() = PlaybackManager.toggleShuffle()
    fun toggleRepeat() = PlaybackManager.toggleRepeat()

    fun addToQueue(song: Song) = PlaybackManager.addToQueue(song)
    fun playNextInQueue(song: Song) = PlaybackManager.playNextInQueue(song)
    fun removeFromQueue(index: Int) = PlaybackManager.removeFromQueue(index)
    fun clearQueue() = PlaybackManager.clearQueue()
    fun moveQueueItem(from: Int, to: Int) = PlaybackManager.moveQueueItem(from, to)

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            repository.toggleFavorite(song.id, song.isFavorite)
        }
    }

    fun createPlaylist(name: String, colorHex: String = "#35E06F") {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.createPlaylist(name.trim(), colorHex)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
            if (activePlaylistDetail.value?.id == playlistId) {
                activePlaylistDetail.value = null
            }
        }
    }

    fun renamePlaylist(playlistId: Long, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            repository.renamePlaylist(playlistId, newName.trim())
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, songId)
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
            activePlaylistSongs.value = activePlaylistSongs.value.filter { it.id != songId }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun setSortOrder(order: SortOrder) {
        sortOrder.value = order
    }

    fun openArtist(artist: Artist) {
        activeArtistDetail.value = artist
    }

    fun openAlbum(album: Album) {
        activeAlbumDetail.value = album
    }

    fun openFolder(folder: Folder) {
        activeFolderDetail.value = folder
    }

    fun openPlaylist(playlist: PlaylistEntity) {
        activePlaylistDetail.value = playlist
        loadPlaylistTracks(playlist.id)
    }

    fun playGenre(genre: String, shuffle: Boolean = false) {
        val filtered = allSongs.value.filter {
            val g = if (it.genre.isNotBlank()) it.genre else "Geral"
            g.equals(genre, ignoreCase = true)
        }
        if (filtered.isNotEmpty()) {
            val toPlay = if (shuffle) filtered.shuffled() else filtered
            PlaybackManager.playSongList(toPlay, 0, true)
        }
    }

    fun playSongsByGenre(genre: String, shuffle: Boolean = false) = playGenre(genre, shuffle)

    fun playHistory(shuffle: Boolean = false) {
        val songs = recentlyPlayed.value
        if (songs.isNotEmpty()) {
            val toPlay = if (shuffle) songs.shuffled() else songs
            PlaybackManager.playSongList(toPlay, 0, true)
        }
    }

    fun playNeverPlayed(shuffle: Boolean = true) {
        val playedIds = mostPlayedHistory.value.map { it.songId }.toSet()
        val unplayed = allSongs.value.filter { !playedIds.contains(it.id) }
        if (unplayed.isNotEmpty()) {
            val toPlay = if (shuffle) unplayed.shuffled() else unplayed
            PlaybackManager.playSongList(toPlay, 0, true)
        }
    }

    fun playAlbum(album: Album, shuffle: Boolean = false) {
        val tracks = allSongs.value.filter { it.albumId == album.albumId || it.album.equals(album.title, ignoreCase = true) }
        if (tracks.isNotEmpty()) {
            val toPlay = if (shuffle) tracks.shuffled() else tracks
            PlaybackManager.playSongList(toPlay, 0, true)
        }
    }

    fun playMostPlayed(shuffle: Boolean = false) {
        val songMap = allSongs.value.associateBy { it.id }
        val ranked = mostPlayedHistory.value.mapNotNull { songMap[it.songId] }
        if (ranked.isNotEmpty()) {
            val toPlay = if (shuffle) ranked.shuffled() else ranked
            PlaybackManager.playSongList(toPlay, 0, true)
        }
    }

    fun playFavorites(shuffle: Boolean = false) {
        val favs = allSongs.value.filter { it.isFavorite }
        if (favs.isNotEmpty()) {
            val toPlay = if (shuffle) favs.shuffled() else favs
            PlaybackManager.playSongList(toPlay, 0, true)
        }
    }

    fun playAll(shuffle: Boolean = false) {
        val songs = allSongs.value
        if (songs.isNotEmpty()) {
            val toPlay = if (shuffle) songs.shuffled() else songs
            PlaybackManager.playSongList(toPlay, 0, true)
        }
    }

    fun addSongsToQueue(songs: List<Song>) = PlaybackManager.addSongsToQueue(songs)
    fun shuffleUpcomingQueue() = PlaybackManager.shuffleUpcomingQueue()

    fun saveQueueAsPlaylist(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val ids = PlaybackManager.queue.value.map { it.id }
            repository.saveQueueAsPlaylist(name.trim(), ids)
        }
    }

    fun clearStatistics() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun clearPlaybackStats() = clearStatistics()

    fun setSleepTimer(mode: com.example.data.model.SleepTimerMode, minutes: Int = 0) {
        if (mode == com.example.data.model.SleepTimerMode.DURATION) {
            PlaybackManager.startDurationSleepTimer(minutes)
        } else {
            PlaybackManager.setSleepTimerMode(mode)
        }
    }

    fun startSleepTimer(minutes: Int) = PlaybackManager.startDurationSleepTimer(minutes)
    fun setSleepTimerMode(mode: com.example.data.model.SleepTimerMode) = PlaybackManager.setSleepTimerMode(mode)
    fun cancelSleepTimer() = PlaybackManager.cancelSleepTimer()

    fun setCrossfade(seconds: Int) {
        crossfadeSeconds.value = seconds.coerceIn(0, 12)
        viewModelScope.launch {
            repository.setSetting("crossfade_seconds", seconds.toString())
        }
    }

    fun setDynamicColors(enabled: Boolean) {
        dynamicColorsEnabled.value = enabled
        viewModelScope.launch {
            repository.setSetting("dynamic_colors", enabled.toString())
        }
    }

    fun setVisualizerStyle(style: com.example.data.model.VisualizerStyle) {
        visualizerStyle.value = style
        viewModelScope.launch {
            repository.setSetting("visualizer_style", style.name)
        }
    }

    fun setLibraryViewMode(mode: com.example.data.model.LibraryViewMode) {
        libraryViewMode.value = mode
        viewModelScope.launch {
            repository.setSetting("library_view_mode", mode.name)
        }
    }

    fun setGesturesEnabled(enabled: Boolean) {
        gesturesEnabled.value = enabled
        viewModelScope.launch {
            repository.setSetting("gestures_enabled", enabled.toString())
        }
    }

    fun closeDetailViews(): Boolean {
        if (showStats.value) {
            showStats.value = false
            return true
        }
        if (showFullPlayer.value) {
            showFullPlayer.value = false
            return true
        }
        if (showQueueSheet.value) {
            showQueueSheet.value = false
            return true
        }
        if (showSleepTimerDialog.value) {
            showSleepTimerDialog.value = false
            return true
        }
        if (activeArtistDetail.value != null) {
            activeArtistDetail.value = null
            return true
        }
        if (activeAlbumDetail.value != null) {
            activeAlbumDetail.value = null
            return true
        }
        if (activeFolderDetail.value != null) {
            activeFolderDetail.value = null
            return true
        }
        if (activePlaylistDetail.value != null) {
            activePlaylistDetail.value = null
            return true
        }
        if (activeGenreDetail.value != null) {
            activeGenreDetail.value = null
            return true
        }
        if (activeHistoryDetail.value) {
            activeHistoryDetail.value = false
            return true
        }
        if (activeMostPlayedDetail.value) {
            activeMostPlayedDetail.value = false
            return true
        }
        if (activeNeverPlayedDetail.value) {
            activeNeverPlayedDetail.value = false
            return true
        }
        if (isSearchOpen.value) {
            isSearchOpen.value = false
            searchQuery.value = ""
            return true
        }
        return false
    }
}
