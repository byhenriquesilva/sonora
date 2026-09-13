package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PlaylistPlay
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Song

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongContextMenuBottomSheet(
    song: Song,
    onDismiss: () -> Unit,
    onPlay: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onToggleFavorite: () -> Unit,
    onGoToArtist: () -> Unit,
    onGoToAlbum: () -> Unit,
    onShowDetails: () -> Unit,
    onExcludeFolder: () -> Unit = {},
    onDeleteSong: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            // Header with song info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SonoraAlbumArt(
                    albumArtUri = song.albumArtUri,
                    seedString = "${song.artist}-${song.title}",
                    size = 54.dp,
                    cornerRadius = 10.dp
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${song.artist} • ${song.album}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                modifier = Modifier.padding(vertical = 6.dp)
            )

            ContextMenuItem(
                icon = Icons.Default.PlayArrow,
                label = "Reproduzir agora",
                tag = "ctx_play",
                onClick = {
                    onPlay()
                    onDismiss()
                }
            )

            ContextMenuItem(
                icon = Icons.Outlined.PlaylistPlay,
                label = "Reproduzir a seguir",
                tag = "ctx_play_next",
                onClick = {
                    onPlayNext()
                    onDismiss()
                }
            )

            ContextMenuItem(
                icon = Icons.Default.QueueMusic,
                label = "Adicionar à fila",
                tag = "ctx_add_queue",
                onClick = {
                    onAddToQueue()
                    onDismiss()
                }
            )

            ContextMenuItem(
                icon = Icons.Default.PlaylistAdd,
                label = "Adicionar à playlist",
                tag = "ctx_add_playlist",
                onClick = {
                    onAddToPlaylist()
                    onDismiss()
                }
            )

            ContextMenuItem(
                icon = if (song.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                label = if (song.isFavorite) "Remover dos favoritos" else "Adicionar aos favoritos",
                tag = "ctx_favorite",
                tint = if (song.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                onClick = {
                    onToggleFavorite()
                    onDismiss()
                }
            )

            ContextMenuItem(
                icon = Icons.Default.Person,
                label = "Ir para artista",
                tag = "ctx_goto_artist",
                onClick = {
                    onGoToArtist()
                    onDismiss()
                }
            )

            ContextMenuItem(
                icon = Icons.Outlined.Album,
                label = "Ir para álbum",
                tag = "ctx_goto_album",
                onClick = {
                    onGoToAlbum()
                    onDismiss()
                }
            )

            ContextMenuItem(
                icon = Icons.Default.Info,
                label = "Detalhes da música",
                tag = "ctx_details",
                onClick = {
                    onShowDetails()
                    onDismiss()
                }
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                modifier = Modifier.padding(vertical = 6.dp)
            )

            ContextMenuItem(
                icon = Icons.Default.FolderOff,
                label = "Ocultar pasta (${song.folder})",
                tag = "ctx_exclude_folder",
                onClick = {
                    onExcludeFolder()
                    onDismiss()
                }
            )

            ContextMenuItem(
                icon = Icons.Default.Delete,
                label = "Excluir do dispositivo",
                tag = "ctx_delete_song",
                tint = MaterialTheme.colorScheme.error,
                onClick = {
                    onDeleteSong()
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun ContextMenuItem(
    icon: ImageVector,
    label: String,
    tag: String,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 15.sp,
            color = tint
        )
    }
}
