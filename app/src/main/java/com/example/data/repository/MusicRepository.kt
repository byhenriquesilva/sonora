package com.example.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.data.local.AppDatabase
import com.example.data.local.FavoriteEntity
import com.example.data.local.HistoryEntity
import com.example.data.local.PlaylistEntity
import com.example.data.local.PlaylistSongEntity
import com.example.data.model.Album
import com.example.data.model.Artist
import com.example.data.model.Folder
import com.example.data.model.Song
import com.example.data.model.SortOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

class MusicRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val musicDao = db.musicDao()

    private val _deviceSongs = MutableStateFlow<List<Song>>(emptyList())
    val deviceSongs = _deviceSongs.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    val favoriteSongIds: Flow<List<Long>> = musicDao.getFavoriteSongIds()
    val playlists: Flow<List<PlaylistEntity>> = musicDao.getAllPlaylists()
    val history: Flow<List<HistoryEntity>> = musicDao.getRecentHistory()
    val allHistory: Flow<List<HistoryEntity>> = musicDao.getAllHistory()
    val mostPlayedHistory: Flow<List<HistoryEntity>> = musicDao.getMostPlayedHistory()
    val totalPlayCount: Flow<Int?> = musicDao.getTotalPlayCount()
    val totalDurationPlayed: Flow<Long?> = musicDao.getTotalDurationPlayed()

    // Combined songs with favorite flag and play count
    val songsWithFavorites: Flow<List<Song>> = combine(_deviceSongs, favoriteSongIds, allHistory) { songs, favIds, histList ->
        val favSet = favIds.toSet()
        val histMap = histList.associateBy { it.songId }
        songs.map { song ->
            val hist = histMap[song.id]
            song.copy(
                isFavorite = favSet.contains(song.id),
                playCount = hist?.playCount ?: 0
            )
        }
    }

    suspend fun scanDeviceAudio(includeSamplesIfEmpty: Boolean = true): List<Song> = withContext(Dispatchers.IO) {
        _isScanning.value = true
        val songsList = mutableListOf<Song>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DATE_ADDED,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.BUCKET_DISPLAY_NAME
            } else {
                MediaStore.Audio.Media.DATA
            }
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 5000"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            val cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )

            cursor?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val albumIdCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val dataCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val sizeCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val mimeCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
                val dateAddedCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

                while (c.moveToNext()) {
                    val id = c.getLong(idCol)
                    val rawTitle = c.getString(titleCol) ?: "Faixa $id"
                    val rawArtist = c.getString(artistCol)
                    val artist = if (rawArtist.isNullOrBlank() || rawArtist == "<unknown>") "Artista desconhecido" else rawArtist
                    val rawAlbum = c.getString(albumCol)
                    val album = if (rawAlbum.isNullOrBlank() || rawAlbum == "<unknown>") "Álbum desconhecido" else rawAlbum
                    val durationMs = c.getLong(durationCol)
                    val albumId = c.getLong(albumIdCol)
                    val dataPath = c.getString(dataCol) ?: ""
                    val size = c.getLong(sizeCol)
                    val mimeType = c.getString(mimeCol) ?: "audio/mpeg"
                    val dateAdded = c.getLong(dateAddedCol)

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    ).toString()

                    val folderName = try {
                        val file = File(dataPath)
                        file.parentFile?.name ?: "Músicas"
                    } catch (e: Exception) {
                        "Músicas"
                    }

                    songsList.add(
                        Song(
                            id = id,
                            title = rawTitle,
                            artist = artist,
                            album = album,
                            durationMs = durationMs,
                            albumId = albumId,
                            contentUri = contentUri,
                            dataPath = dataPath,
                            size = size,
                            mimeType = mimeType,
                            dateAdded = dateAdded,
                            folder = folderName
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // If no music on device or emulator, add sample offline music so the app is immediately functional
        if (songsList.isEmpty() && includeSamplesIfEmpty) {
            val samples = SampleAudioGenerator.getSampleSongs(context)
            songsList.addAll(samples)
        }

        _deviceSongs.value = songsList
        _isScanning.value = false
        songsList
    }

    suspend fun addDemoTracks(): List<Song> = withContext(Dispatchers.IO) {
        val samples = SampleAudioGenerator.getSampleSongs(context)
        val current = _deviceSongs.value.toMutableList()
        val existingIds = current.map { it.id }.toSet()
        val newSamples = samples.filter { !existingIds.contains(it.id) }
        current.addAll(newSamples)
        _deviceSongs.value = current
        current
    }

    // --- Albums ---
    fun extractAlbums(songs: List<Song>): List<Album> {
        return songs.groupBy { it.album }
            .map { (albumTitle, tracks) ->
                val first = tracks.first()
                Album(
                    id = first.albumId,
                    title = albumTitle,
                    artist = first.artist,
                    albumId = first.albumId,
                    songCount = tracks.size,
                    year = first.year
                )
            }.sortedBy { it.title.lowercase() }
    }

    // --- Artists ---
    fun extractArtists(songs: List<Song>): List<Artist> {
        return songs.groupBy { it.artist }
            .map { (artistName, tracks) ->
                val albumCount = tracks.map { it.album }.distinct().size
                Artist(
                    name = artistName,
                    songCount = tracks.size,
                    albumCount = albumCount
                )
            }.sortedBy { it.name.lowercase() }
    }

    // --- Folders ---
    fun extractFolders(songs: List<Song>): List<Folder> {
        return songs.groupBy { it.folder }
            .map { (folderName, tracks) ->
                val samplePath = try {
                    File(tracks.first().dataPath).parent ?: folderName
                } catch (e: Exception) {
                    folderName
                }
                Folder(
                    path = samplePath,
                    name = folderName,
                    songCount = tracks.size
                )
            }.sortedBy { it.name.lowercase() }
    }

    // --- Favorites ---
    suspend fun toggleFavorite(songId: Long, isFav: Boolean) = withContext(Dispatchers.IO) {
        if (isFav) {
            musicDao.removeFavorite(songId)
        } else {
            musicDao.addFavorite(FavoriteEntity(songId = songId))
        }
    }

    // --- Playlists ---
    suspend fun createPlaylist(name: String, colorHex: String = "#35E06F"): Long = withContext(Dispatchers.IO) {
        musicDao.insertPlaylist(PlaylistEntity(name = name, colorHex = colorHex))
    }

    suspend fun deletePlaylist(playlistId: Long) = withContext(Dispatchers.IO) {
        musicDao.deletePlaylist(playlistId)
        musicDao.deletePlaylistSongs(playlistId)
    }

    suspend fun renamePlaylist(playlistId: Long, newName: String) = withContext(Dispatchers.IO) {
        musicDao.updatePlaylist(PlaylistEntity(id = playlistId, name = newName))
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) = withContext(Dispatchers.IO) {
        musicDao.insertPlaylistSong(PlaylistSongEntity(playlistId = playlistId, songId = songId))
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) = withContext(Dispatchers.IO) {
        musicDao.removeSongFromPlaylist(playlistId, songId)
    }

    fun getPlaylistSongIds(playlistId: Long): Flow<List<Long>> = musicDao.getSongIdsForPlaylist(playlistId)

    // --- History & Stats ---
    suspend fun recordSongPlayed(songId: Long, durationMs: Long = 0L) = withContext(Dispatchers.IO) {
        val existing = musicDao.getHistoryItem(songId)
        val count = (existing?.playCount ?: 0) + 1
        val newDuration = (existing?.durationPlayedMs ?: 0L) + durationMs
        musicDao.recordPlay(
            HistoryEntity(
                songId = songId,
                lastPlayedAt = System.currentTimeMillis(),
                playCount = count,
                durationPlayedMs = newDuration
            )
        )
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        musicDao.clearHistory()
    }

    suspend fun saveQueueAsPlaylist(name: String, songIds: List<Long>): Long = withContext(Dispatchers.IO) {
        val playlistId = musicDao.insertPlaylist(PlaylistEntity(name = name))
        songIds.forEachIndexed { index, songId ->
            musicDao.insertPlaylistSong(
                PlaylistSongEntity(
                    playlistId = playlistId,
                    songId = songId,
                    orderIndex = index
                )
            )
        }
        playlistId
    }

    // --- Settings Persistence ---
    fun getSetting(key: String, defaultValue: String): Flow<String> = musicDao.getSetting(key).map {
        it ?: defaultValue
    }

    suspend fun setSetting(key: String, value: String) = withContext(Dispatchers.IO) {
        musicDao.setSetting(com.example.data.local.AppSettingEntity(key = key, value = value))
    }

    // --- Genre Extraction ---
    fun extractGenres(songs: List<Song>): Map<String, List<Song>> {
        val map = mutableMapOf<String, MutableList<Song>>()
        for (song in songs) {
            val g = if (song.genre.isNotBlank()) song.genre else "Geral"
            map.getOrPut(g) { mutableListOf() }.add(song)
        }
        return map
    }
}
