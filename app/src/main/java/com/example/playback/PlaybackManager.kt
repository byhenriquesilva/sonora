package com.example.playback

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.model.RepeatMode
import com.example.data.model.Song
import com.example.service.MusicPlaybackService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object PlaybackManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var progressJob: Job? = null
    private var appContext: Context? = null

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs = _durationMs.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue = _queue.asStateFlow()

    private val _queueIndex = MutableStateFlow(0)
    val queueIndex = _queueIndex.asStateFlow()

    private val _shuffleMode = MutableStateFlow(false)
    val shuffleMode = _shuffleMode.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode = _repeatMode.asStateFlow()

    // --- Sleep Timer State ---
    private var sleepTimerJob: Job? = null
    private val _sleepTimerMode = MutableStateFlow(com.example.data.model.SleepTimerMode.OFF)
    val sleepTimerMode = _sleepTimerMode.asStateFlow()

    private val _sleepTimerRemainingSeconds = MutableStateFlow(0L)
    val sleepTimerRemainingSeconds = _sleepTimerRemainingSeconds.asStateFlow()

    private val _sleepTimerFormatted = MutableStateFlow("")
    val sleepTimerFormatted = _sleepTimerFormatted.asStateFlow()

    // --- Preferences & Settings State ---
    val crossfadeSeconds = MutableStateFlow(0)
    val dynamicColorsEnabled = MutableStateFlow(true)
    val visualizerStyle = MutableStateFlow(com.example.data.model.VisualizerStyle.AUTO)
    val libraryViewMode = MutableStateFlow(com.example.data.model.LibraryViewMode.LIST)
    val gesturesEnabled = MutableStateFlow(true)

    private var originalQueue = listOf<Song>()

    var onSongPlayedListener: ((Song) -> Unit)? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun playSongList(songs: List<Song>, startIndex: Int = 0, startPlaying: Boolean = true) {
        if (songs.isEmpty()) return
        originalQueue = songs.toList()
        val index = startIndex.coerceIn(0, songs.size - 1)

        if (_shuffleMode.value) {
            val selected = songs[index]
            val shuffled = songs.toMutableList()
            shuffled.removeAt(index)
            shuffled.shuffle()
            val newQueue = listOf(selected) + shuffled
            _queue.value = newQueue
            _queueIndex.value = 0
            playTrackAtIndex(0, startPlaying)
        } else {
            _queue.value = songs
            _queueIndex.value = index
            playTrackAtIndex(index, startPlaying)
        }
    }

    fun playSong(song: Song) {
        val currentQ = _queue.value.toMutableList()
        val existingIndex = currentQ.indexOfFirst { it.id == song.id }
        if (existingIndex >= 0) {
            playTrackAtIndex(existingIndex, true)
        } else {
            currentQ.add(song)
            _queue.value = currentQ
            originalQueue = currentQ.toList()
            playTrackAtIndex(currentQ.size - 1, true)
        }
    }

    fun playTrackAtIndex(index: Int, startPlaying: Boolean = true) {
        val q = _queue.value
        if (index !in q.indices) return
        val song = q[index]
        _queueIndex.value = index
        _currentSong.value = song
        _durationMs.value = song.durationMs
        _currentPositionMs.value = 0L

        if (startPlaying) {
            _isPlaying.value = true
            startProgressLoop()
            onSongPlayedListener?.invoke(song)
            sendCommand(MusicPlaybackService.ACTION_PLAY_SONG, song)
        } else {
            _isPlaying.value = false
            stopProgressLoop()
        }
    }

    fun togglePlayPause() {
        val current = _currentSong.value ?: return
        if (_isPlaying.value) {
            pause()
        } else {
            resume()
        }
    }

    fun resume() {
        if (_currentSong.value == null) return
        _isPlaying.value = true
        startProgressLoop()
        sendCommand(MusicPlaybackService.ACTION_RESUME)
    }

    fun pause() {
        _isPlaying.value = false
        stopProgressLoop()
        sendCommand(MusicPlaybackService.ACTION_PAUSE)
    }

    fun playNext() {
        val q = _queue.value
        if (q.isEmpty()) return

        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                seekTo(0)
                resume()
            }
            RepeatMode.ALL -> {
                val nextIndex = (_queueIndex.value + 1) % q.size
                playTrackAtIndex(nextIndex, true)
            }
            RepeatMode.OFF -> {
                val nextIndex = _queueIndex.value + 1
                if (nextIndex < q.size) {
                    playTrackAtIndex(nextIndex, true)
                } else {
                    _isPlaying.value = false
                    stopProgressLoop()
                    seekTo(0)
                }
            }
        }
    }

    fun playPrevious() {
        if (_currentPositionMs.value > 3000L) {
            seekTo(0)
            return
        }
        val q = _queue.value
        if (q.isEmpty()) return
        val prevIndex = if (_queueIndex.value > 0) _queueIndex.value - 1 else q.size - 1
        playTrackAtIndex(prevIndex, true)
    }

    fun seekTo(positionMs: Long) {
        val clamped = positionMs.coerceIn(0L, _durationMs.value.coerceAtLeast(1L))
        _currentPositionMs.value = clamped
        sendCommand(MusicPlaybackService.ACTION_SEEK, seekMs = clamped)
    }

    fun rewind10Seconds() {
        seekTo(_currentPositionMs.value - 10000L)
    }

    fun forward10Seconds() {
        seekTo(_currentPositionMs.value + 10000L)
    }

    fun toggleShuffle() {
        val current = _currentSong.value
        val newShuffle = !_shuffleMode.value
        _shuffleMode.value = newShuffle

        if (newShuffle) {
            val list = _queue.value.toMutableList()
            if (current != null) {
                list.remove(current)
                list.shuffle()
                _queue.value = listOf(current) + list
                _queueIndex.value = 0
            } else {
                list.shuffle()
                _queue.value = list
            }
        } else {
            val restored = originalQueue.ifEmpty { _queue.value }
            _queue.value = restored
            val idx = if (current != null) restored.indexOfFirst { it.id == current.id } else 0
            _queueIndex.value = idx.coerceAtLeast(0)
        }
    }

    fun toggleRepeat() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
    }

    fun addToQueue(song: Song) {
        val currentQ = _queue.value.toMutableList()
        currentQ.add(song)
        _queue.value = currentQ
    }

    fun playNextInQueue(song: Song) {
        val currentQ = _queue.value.toMutableList()
        val insertIndex = (_queueIndex.value + 1).coerceAtMost(currentQ.size)
        currentQ.add(insertIndex, song)
        _queue.value = currentQ
    }

    fun removeFromQueue(index: Int) {
        val currentQ = _queue.value.toMutableList()
        if (index in currentQ.indices) {
            currentQ.removeAt(index)
            _queue.value = currentQ
            if (index < _queueIndex.value) {
                _queueIndex.value = _queueIndex.value - 1
            } else if (index == _queueIndex.value && currentQ.isNotEmpty()) {
                val newIdx = index.coerceAtMost(currentQ.size - 1)
                playTrackAtIndex(newIdx, _isPlaying.value)
            }
        }
    }

    fun clearQueue() {
        val wasPlaying = _isPlaying.value
        val hadSong = _currentSong.value != null
        _queue.value = emptyList()
        _currentSong.value = null
        _isPlaying.value = false
        stopProgressLoop()
        if (wasPlaying || hadSong) {
            sendCommand(MusicPlaybackService.ACTION_STOP)
        }
    }

    fun removeSongEverywhere(songId: Long) {
        val currentQ = _queue.value.toMutableList()
        val index = currentQ.indexOfFirst { it.id == songId }
        if (index >= 0) {
            if (currentQ.size <= 1) {
                clearQueue()
            } else {
                removeFromQueue(index)
            }
        }
    }

    fun removeSongsInExcludedFolder(folderPath: String, folderName: String) {
        val currentQ = _queue.value
        if (currentQ.isEmpty()) return

        val pathLower = folderPath.trim().lowercase()
        val nameLower = folderName.trim().lowercase()
        val isMatch: (Song) -> Boolean = { s ->
            s.folder.trim().lowercase() == nameLower ||
            (pathLower.isNotBlank() && (s.dataPath.lowercase().startsWith(pathLower) || s.dataPath.lowercase().contains(pathLower)))
        }

        if (currentQ.none(isMatch)) return

        val current = _currentSong.value
        if (current != null && isMatch(current)) {
            val nextNonExcluded = currentQ.indices.firstOrNull { it != _queueIndex.value && !isMatch(currentQ[it]) }
            if (nextNonExcluded != null) {
                playTrackAtIndex(nextNonExcluded, _isPlaying.value)
            } else {
                clearQueue()
                return
            }
        }

        val filtered = currentQ.filterNot(isMatch)
        if (filtered.isEmpty()) {
            clearQueue()
        } else {
            _queue.value = filtered
            val newIndex = filtered.indexOfFirst { it.id == _currentSong.value?.id }
            _queueIndex.value = newIndex.coerceAtLeast(0)
        }
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        val currentQ = _queue.value.toMutableList()
        if (fromIndex in currentQ.indices && toIndex in currentQ.indices) {
            val item = currentQ.removeAt(fromIndex)
            currentQ.add(toIndex, item)
            _queue.value = currentQ

            if (_queueIndex.value == fromIndex) {
                _queueIndex.value = toIndex
            } else if (fromIndex < _queueIndex.value && toIndex >= _queueIndex.value) {
                _queueIndex.value = _queueIndex.value - 1
            } else if (fromIndex > _queueIndex.value && toIndex <= _queueIndex.value) {
                _queueIndex.value = _queueIndex.value + 1
            }
        }
    }

    fun addSongsToQueue(songs: List<Song>) {
        if (songs.isEmpty()) return
        val currentQ = _queue.value.toMutableList()
        currentQ.addAll(songs)
        _queue.value = currentQ
    }

    fun shuffleUpcomingQueue() {
        val q = _queue.value
        if (q.size <= 2) return
        val currIdx = _queueIndex.value
        val played = q.subList(0, (currIdx + 1).coerceAtMost(q.size))
        val upcoming = if (currIdx + 1 < q.size) q.subList(currIdx + 1, q.size).shuffled() else emptyList()
        _queue.value = played + upcoming
    }

    // --- Sleep Timer Controls ---
    fun startDurationSleepTimer(minutes: Int) {
        cancelSleepTimer()
        val totalSecs = (minutes * 60).toLong()
        _sleepTimerMode.value = com.example.data.model.SleepTimerMode.DURATION
        _sleepTimerRemainingSeconds.value = totalSecs
        updateSleepTimerDisplay(totalSecs)

        sleepTimerJob = scope.launch {
            var remaining = totalSecs
            while (isActive && remaining > 0) {
                delay(1000)
                remaining--
                _sleepTimerRemainingSeconds.value = remaining
                updateSleepTimerDisplay(remaining)
            }
            if (isActive && remaining <= 0) {
                pause()
                cancelSleepTimer()
            }
        }
    }

    fun setSleepTimerMode(mode: com.example.data.model.SleepTimerMode) {
        cancelSleepTimer()
        _sleepTimerMode.value = mode
        when (mode) {
            com.example.data.model.SleepTimerMode.END_OF_TRACK -> {
                _sleepTimerFormatted.value = "Fim da música"
            }
            com.example.data.model.SleepTimerMode.END_OF_ALBUM -> {
                _sleepTimerFormatted.value = "Fim do álbum"
            }
            com.example.data.model.SleepTimerMode.END_OF_PLAYLIST -> {
                _sleepTimerFormatted.value = "Fim da playlist"
            }
            else -> {
                _sleepTimerFormatted.value = ""
            }
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _sleepTimerMode.value = com.example.data.model.SleepTimerMode.OFF
        _sleepTimerRemainingSeconds.value = 0L
        _sleepTimerFormatted.value = ""
    }

    private fun updateSleepTimerDisplay(seconds: Long) {
        val mins = seconds / 60
        val secs = seconds % 60
        _sleepTimerFormatted.value = "%d:%02d".format(mins, secs)
    }

    // Called by MusicPlaybackService when playback completes
    fun onPlaybackCompleted() {
        when (_sleepTimerMode.value) {
            com.example.data.model.SleepTimerMode.END_OF_TRACK -> {
                pause()
                cancelSleepTimer()
                return
            }
            com.example.data.model.SleepTimerMode.END_OF_ALBUM -> {
                val q = _queue.value
                val nextIdx = _queueIndex.value + 1
                val currAlbum = _currentSong.value?.album
                if (nextIdx >= q.size || q[nextIdx].album != currAlbum) {
                    pause()
                    cancelSleepTimer()
                    return
                }
            }
            com.example.data.model.SleepTimerMode.END_OF_PLAYLIST -> {
                val q = _queue.value
                if (_queueIndex.value + 1 >= q.size) {
                    pause()
                    cancelSleepTimer()
                    return
                }
            }
            else -> {
                // Continue standard playback
            }
        }
        playNext()
    }

    // Called by MusicPlaybackService to sync position
    fun updatePlaybackPosition(posMs: Long, durationMs: Long) {
        _currentPositionMs.value = posMs
        if (durationMs > 0) {
            _durationMs.value = durationMs
        }
    }

    fun updatePlaybackState(playing: Boolean) {
        _isPlaying.value = playing
        if (playing) startProgressLoop() else stopProgressLoop()
    }

    private fun startProgressLoop() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                delay(250)
                if (_isPlaying.value) {
                    val nextPos = _currentPositionMs.value + 250
                    if (nextPos <= _durationMs.value) {
                        _currentPositionMs.value = nextPos
                    }
                }
            }
        }
    }

    private fun stopProgressLoop() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun sendCommand(action: String, song: Song? = null, seekMs: Long = 0L) {
        val ctx = appContext ?: return
        val targetSong = song ?: _currentSong.value
        try {
            val intent = Intent(ctx, MusicPlaybackService::class.java).apply {
                this.action = action
                if (targetSong != null) {
                    putExtra(MusicPlaybackService.EXTRA_URI, targetSong.contentUri)
                    putExtra(MusicPlaybackService.EXTRA_DATA_PATH, targetSong.dataPath)
                    putExtra(MusicPlaybackService.EXTRA_TITLE, targetSong.title)
                    putExtra(MusicPlaybackService.EXTRA_ARTIST, targetSong.artist)
                    putExtra(MusicPlaybackService.EXTRA_ALBUM, targetSong.album)
                    putExtra(MusicPlaybackService.EXTRA_DURATION, targetSong.durationMs)
                    putExtra(MusicPlaybackService.EXTRA_ALBUM_ID, targetSong.albumId)
                    putExtra(MusicPlaybackService.EXTRA_ALBUM_ART_URI, targetSong.albumArtUri)
                }
                putExtra(MusicPlaybackService.EXTRA_SEEK_MS, seekMs)
            }
            val isStartForegroundAction = action == MusicPlaybackService.ACTION_PLAY_SONG ||
                    action == MusicPlaybackService.ACTION_RESUME
            if (isStartForegroundAction && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                ctx.startForegroundService(intent)
            } else {
                ctx.startService(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
