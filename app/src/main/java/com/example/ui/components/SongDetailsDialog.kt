package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Song

@Composable
fun SongDetailsDialog(
    song: Song,
    onDismiss: () -> Unit
) {
    val sizeMb = if (song.size > 0) {
        "%.2f MB".format(song.size / (1024.0 * 1024.0))
    } else "Desconhecido"

    val format = song.mimeType.substringAfterLast("/").uppercase()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Detalhes da Música",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                DetailRow(label = "Título", value = song.title)
                DetailRow(label = "Artista", value = song.artist)
                DetailRow(label = "Álbum", value = song.album)
                DetailRow(label = "Duração", value = song.formattedDuration)
                DetailRow(label = "Formato", value = format)
                DetailRow(label = "Tamanho", value = sizeMb)
                DetailRow(label = "Pasta", value = song.folder)
                if (song.dataPath.isNotBlank()) {
                    DetailRow(label = "Caminho", value = song.dataPath)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dialog_close_details")
            ) {
                Text(
                    text = "Fechar",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 18.sp
        )
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
