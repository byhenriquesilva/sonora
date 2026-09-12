package com.example.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import com.example.ui.components.LocalBottomContentPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Album
import com.example.data.model.Artist
import com.example.data.model.Song
import com.example.ui.components.SongListItem

@Composable
fun SearchScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    songs: List<Song>,
    artists: List<Artist>,
    albums: List<Album>,
    currentSongId: Long?,
    isPlaying: Boolean,
    onSongClick: (Song) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onFavoriteClick: (Song) -> Unit,
    onMoreClick: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("search_screen")
    ) {
        // Search bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose, modifier = Modifier.testTag("btn_close_search")) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
            }

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Músicas, artistas ou álbuns...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpar busca")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_search")
            )
        }

        if (query.isBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Digite o nome de uma música, artista ou álbum para pesquisar em seus arquivos locais.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else if (songs.isEmpty() && artists.isEmpty() && albums.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhum resultado encontrado para \"$query\".",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = LocalBottomContentPadding.current)
            ) {
                // Matching artists
                if (artists.isNotEmpty()) {
                    item {
                        Text(
                            text = "Artistas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            items(artists) { artist ->
                                ArtistCircularCard(artist = artist, onClick = { onArtistClick(artist) })
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Matching albums
                if (albums.isNotEmpty()) {
                    item {
                        Text(
                            text = "Álbuns",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            items(albums) { album ->
                                AlbumCard(album = album, onClick = { onAlbumClick(album) })
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Matching songs
                if (songs.isNotEmpty()) {
                    item {
                        Text(
                            text = "Músicas (${songs.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                        )
                    }
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
}
