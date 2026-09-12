package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val colorHex: String = "#35E06F"
)

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "songId"],
    indices = [Index("playlistId"), Index("songId")]
)
data class PlaylistSongEntity(
    val playlistId: Long,
    val songId: Long,
    val addedAt: Long = System.currentTimeMillis(),
    val orderIndex: Int = 0
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val songId: Long,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey
    val songId: Long,
    val lastPlayedAt: Long = System.currentTimeMillis(),
    val playCount: Int = 1,
    val durationPlayedMs: Long = 0L
)

@Entity(tableName = "settings")
data class AppSettingEntity(
    @PrimaryKey
    val key: String,
    val value: String
)
