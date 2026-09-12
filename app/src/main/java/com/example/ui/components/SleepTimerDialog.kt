package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SleepTimerMode
import kotlin.math.roundToInt

@Composable
fun SleepTimerDialog(
    currentMode: SleepTimerMode,
    remainingFormatted: String,
    onStartDurationTimer: (Int) -> Unit,
    onSetMode: (SleepTimerMode) -> Unit,
    onCancelTimer: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMinutes by remember { mutableFloatStateOf(30f) }
    val isTimerActive = currentMode != SleepTimerMode.OFF

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bedtime,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Temporizador de Sono",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isTimerActive) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Temporizador em execução",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = when (currentMode) {
                                        SleepTimerMode.DURATION -> "Desligar em: $remainingFormatted"
                                        SleepTimerMode.END_OF_TRACK -> "Desligar ao fim da música atual"
                                        SleepTimerMode.END_OF_ALBUM -> "Desligar ao fim do álbum"
                                        SleepTimerMode.END_OF_PLAYLIST -> "Desligar ao fim da playlist"
                                        SleepTimerMode.OFF -> ""
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    onCancelTimer()
                                    onDismiss()
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Cancelar", fontSize = 12.sp)
                            }
                        }
                    }
                }

                Text(
                    text = "Por tempo determinado",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Presets chips
                val presets = listOf(5, 10, 15, 30, 45, 60)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.take(3).forEach { mins ->
                        FilterChip(
                            selected = selectedMinutes.roundToInt() == mins,
                            onClick = { selectedMinutes = mins.toFloat() },
                            label = { Text("${mins}m") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.drop(3).forEach { mins ->
                        FilterChip(
                            selected = selectedMinutes.roundToInt() == mins,
                            onClick = { selectedMinutes = mins.toFloat() },
                            label = { Text("${mins}m") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Slider for custom minutes
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Personalizado",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${selectedMinutes.roundToInt()} minutos",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = selectedMinutes,
                        onValueChange = { selectedMinutes = it },
                        valueRange = 1f..120f,
                        steps = 118,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Button(
                    onClick = {
                        onStartDurationTimer(selectedMinutes.roundToInt())
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Iniciar por ${selectedMinutes.roundToInt()} minutos")
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Por conclusão de reprodução",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                OptionItem(
                    icon = Icons.Default.MusicNote,
                    title = "Fim da música atual",
                    subtitle = "Pausa a reprodução quando a música atual terminar",
                    isSelected = currentMode == SleepTimerMode.END_OF_TRACK,
                    onClick = {
                        onSetMode(SleepTimerMode.END_OF_TRACK)
                        onDismiss()
                    }
                )

                OptionItem(
                    icon = Icons.Default.QueueMusic,
                    title = "Fim do álbum atual",
                    subtitle = "Pausa após a última faixa do álbum atual",
                    isSelected = currentMode == SleepTimerMode.END_OF_ALBUM,
                    onClick = {
                        onSetMode(SleepTimerMode.END_OF_ALBUM)
                        onDismiss()
                    }
                )

                OptionItem(
                    icon = Icons.Default.QueueMusic,
                    title = "Fim da playlist / fila",
                    subtitle = "Pausa após reproduzir todas as músicas da fila",
                    isSelected = currentMode == SleepTimerMode.END_OF_PLAYLIST,
                    onClick = {
                        onSetMode(SleepTimerMode.END_OF_PLAYLIST)
                        onDismiss()
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun OptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
