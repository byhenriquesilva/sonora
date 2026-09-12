package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import com.example.ui.components.LocalBottomContentPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PlaylistEntity
import com.example.data.model.Album
import com.example.data.model.Artist
import com.example.data.model.Folder
import com.example.data.model.Song
import com.example.ui.components.SongListItem
import com.example.ui.components.SonoraAlbumArt

@Composable
fun ArtistDetailScreen(
    artist: Artist,
    songs: List<Song>,
    currentSongId: Long?,
    isPlaying: Boolean,
    onBack: () -> Unit,
    onSongClick: (Song) -> Unit,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
    onFavoriteClick: (Song) -> Unit,
    onMoreClick: (Song) -> Unit
) {
    val artistSongs = songs.filter { it.artist == artist.name }

    Column(modifier = Modifier.fillMaxSize().testTag("artist_detail_screen")) {
        DetailHeader(
            title = artist.name,
            subtitle = "${artistSongs.size} músicas • ${artist.albumCount} álbuns",
            onBack = onBack,
            artComposable = {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    SonoraAlbumArt(albumArtUri = null, seedString = artist.name, size = 100.dp, cornerRadius = 50.dp)
                }
            },
            onPlayAll = onPlayAll,
            onShuffle = onShuffle
        )

        LazyColumn(
            contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = LocalBottomContentPadding.current)
        ) {
            items(artistSongs, key = { it.id }) { song ->
                SongListItem(
                    song = song,
                    isPlaying = isPlaying && currentSongId == song.id,
                    isCurrentTrack = currentSongId == song.id,
                    onSongClick = { onSongClick(song) },
                    onFavoriteClick = { onFavoriteClick(song) },
                    onMoreClick = { onMoreClick(song) }
                )
            }
        }
    }
}

@Composable
fun AlbumDetailScreen(
    album: Album,
    songs: List<Song>,
    currentSongId: Long?,
    isPlaying: Boolean,
    onBack: () -> Unit,
    onSongClick: (Song) -> Unit,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
    onFavoriteClick: (Song) -> Unit,
    onMoreClick: (Song) -> Unit
) {
    val albumSongs = songs.filter { it.album == album.title }

    Column(modifier = Modifier.fillMaxSize().testTag("album_detail_screen")) {
        DetailHeader(
            title = album.title,
            subtitle = "${album.artist} • ${albumSongs.size} faixas",
            onBack = onBack,
            artComposable = {
                SonoraAlbumArt(albumArtUri = album.albumArtUri, seedString = "${album.artist}-${album.title}", size = 110.dp, cornerRadius = 14.dp)
            },
            onPlayAll = onPlayAll,
            onShuffle = onShuffle
        )

        LazyColumn(
            contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = LocalBottomContentPadding.current)
        ) {
            items(albumSongs, key = { it.id }) { song ->
                SongListItem(
                    song = song,
                    isPlaying = isPlaying && currentSongId == song.id,
                    isCurrentTrack = currentSongId == song.id,
                    onSongClick = { onSongClick(song) },
                    onFavoriteClick = { onFavoriteClick(song) },
                    onMoreClick = { onMoreClick(song) }
                )
            }
        }
    }
}

@Composable
fun FolderDetailScreen(
    folder: Folder,
    songs: List<Song>,
    currentSongId: Long?,
    isPlaying: Boolean,
    onBack: () -> Unit,
    onSongClick: (Song) -> Unit,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
    onFavoriteClick: (Song) -> Unit,
    onMoreClick: (Song) -> Unit
) {
    val folderSongs = songs.filter { it.folder == folder.name }

    Column(modifier = Modifier.fillMaxSize().testTag("folder_detail_screen")) {
        DetailHeader(
            title = folder.name,
            subtitle = "${folderSongs.size} músicas na pasta\n${folder.path}",
            onBack = onBack,
            artComposable = {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(72.dp)
                )
            },
            onPlayAll = onPlayAll,
            onShuffle = onShuffle
        )

        LazyColumn(
            contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = LocalBottomContentPadding.current)
        ) {
            items(folderSongs, key = { it.id }) { song ->
                SongListItem(
                    song = song,
                    isPlaying = isPlaying && currentSongId == song.id,
                    isCurrentTrack = currentSongId == song.id,
                    onSongClick = { onSongClick(song) },
                    onFavoriteClick = { onFavoriteClick(song) },
                    onMoreClick = { onMoreClick(song) }
                )
            }
        }
    }
}

@Composable
fun PlaylistDetailScreen(
    playlist: PlaylistEntity,
    songs: List<Song>,
    currentSongId: Long?,
    isPlaying: Boolean,
    onBack: () -> Unit,
    onSongClick: (Song) -> Unit,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
    onFavoriteClick: (Song) -> Unit,
    onMoreClick: (Song) -> Unit,
    onDeletePlaylist: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().testTag("playlist_detail_screen")) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_playlist")) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
            }
            IconButton(onClick = onDeletePlaylist, modifier = Modifier.testTag("btn_delete_playlist")) {
                Icon(Icons.Default.Delete, contentDescription = "Excluir playlist", tint = MaterialTheme.colorScheme.error)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SonoraAlbumArt(albumArtUri = null, seedString = playlist.name, size = 96.dp, cornerRadius = 12.dp)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${songs.size} músicas na playlist",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Action buttons
        if (songs.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onPlayAll,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tocar tudo", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onShuffle,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Embaralhar")
                }
            }
        }

        if (songs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Esta playlist está vazia.\nAdicione músicas tocando no botão '...' de qualquer música e selecionando 'Adicionar à playlist'.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = LocalBottomContentPadding.current)
            ) {
                items(songs, key = { it.id }) { song ->
                    SongListItem(
                        song = song,
                        isPlaying = isPlaying && currentSongId == song.id,
                        isCurrentTrack = currentSongId == song.id,
                        onSongClick = { onSongClick(song) },
                        onFavoriteClick = { onFavoriteClick(song) },
                        onMoreClick = { onMoreClick(song) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    artComposable: @Composable () -> Unit,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit
) {
    Column {
        IconButton(
            onClick = onBack,
            modifier = Modifier.padding(top = 16.dp, start = 8.dp).testTag("btn_detail_back")
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            artComposable()
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onPlayAll,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tocar tudo", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onShuffle,
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Embaralhar")
            }
        }
    }
}
