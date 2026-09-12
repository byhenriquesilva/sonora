package com.example.data.model

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val albumId: Long,
    val contentUri: String,
    val dataPath: String,
    val size: Long,
    val mimeType: String,
    val year: Int = 0,
    val dateAdded: Long = 0,
    val folder: String = "Músicas",
    val isFavorite: Boolean = false,
    val genre: String = "Geral",
    val playCount: Int = 0
) {
    val formattedDuration: String
        get() {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }

    val albumArtUri: String
        get() = "content://media/external/audio/albumart/$albumId"

    val isHighQuality: Boolean
        get() = mimeType.contains("flac", ignoreCase = true) ||
                mimeType.contains("wav", ignoreCase = true) ||
                size > 15_000_000 ||
                (durationMs > 0 && (size * 8 / (durationMs / 1000.coerceAtLeast(1))) >= 280_000)
}

data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val albumId: Long,
    val songCount: Int,
    val year: Int
) {
    val albumArtUri: String
        get() = "content://media/external/audio/albumart/$albumId"
}

data class Artist(
    val name: String,
    val songCount: Int,
    val albumCount: Int
)

data class Folder(
    val path: String,
    val name: String,
    val songCount: Int
)

enum class SortOrder(val label: String) {
    NAME("Nome (A-Z)"),
    NAME_DESC("Nome (Z-A)"),
    ARTIST("Artista"),
    ALBUM("Álbum"),
    DATE_ADDED("Data adicionada"),
    DURATION("Duração")
}

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

enum class LibraryViewMode(val label: String) {
    LIST("Lista"),
    COMPACT_LIST("Lista compacta"),
    GRID("Grade"),
    COMPACT_GRID("Grade compacta")
}

enum class VisualizerStyle(val label: String) {
    OFF("Desativado"),
    BARS("Barras"),
    WAVE("Onda"),
    PARTICLES("Partículas"),
    AUTO("Automático")
}

enum class SleepTimerMode(val label: String) {
    OFF("Desativado"),
    DURATION("Temporizador"),
    END_OF_TRACK("Fim da música atual"),
    END_OF_ALBUM("Fim do álbum"),
    END_OF_PLAYLIST("Fim da playlist")
}

