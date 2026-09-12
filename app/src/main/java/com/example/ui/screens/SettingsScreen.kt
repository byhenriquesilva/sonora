package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import com.example.ui.components.LocalBottomContentPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SleepTimerMode
import com.example.data.model.VisualizerStyle
import com.example.ui.theme.SonoraAccentCyan
import com.example.ui.theme.SonoraAccentEmerald
import com.example.ui.theme.SonoraAccentOrange
import com.example.ui.theme.SonoraAccentPurple
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    totalSongs: Int,
    currentAccentColor: Color,
    crossfadeSeconds: Int,
    dynamicColorsEnabled: Boolean,
    visualizerStyle: VisualizerStyle,
    gesturesEnabled: Boolean,
    sleepTimerMode: SleepTimerMode,
    sleepTimerFormatted: String,
    onAccentColorSelect: (Color) -> Unit,
    onCrossfadeChange: (Int) -> Unit,
    onDynamicColorsChange: (Boolean) -> Unit,
    onVisualizerStyleChange: (VisualizerStyle) -> Unit,
    onGesturesChange: (Boolean) -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenStats: () -> Unit,
    onRefreshScan: () -> Unit,
    onAddDemoTracks: () -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColors = listOf(
        SonoraAccentEmerald to "Esmeralda",
        SonoraAccentPurple to "Roxo",
        SonoraAccentCyan to "Ciano",
        SonoraAccentOrange to "Laranja"
    )

    var currentCrossfade by remember(crossfadeSeconds) { mutableFloatStateOf(crossfadeSeconds.toFloat()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = LocalBottomContentPadding.current)
    ) {
        // Top Header
        item {
            Text(
                text = "Configurações",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Preferências e controle total offline",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 20.dp)
            )
        }

        // Section: Reprodução e Áudio
        item {
            SettingsSectionHeader(title = "Reprodução e Áudio")
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Temporizador de Sono
                    val timerSubtitle = if (sleepTimerMode != SleepTimerMode.OFF) {
                        "Ativo ($sleepTimerFormatted) • Toque para alterar"
                    } else {
                        "Desativado • Desligar automaticamente por tempo ou fim da música"
                    }
                    SettingsActionRow(
                        icon = Icons.Default.Bedtime,
                        title = "Temporizador de Sono",
                        subtitle = timerSubtitle,
                        tag = "setting_sleep_timer",
                        onClick = onOpenSleepTimer
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    // Crossfade
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "Crossfade",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Transição suave de volume entre faixas",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = "${currentCrossfade.roundToInt()}s",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp
                            )
                        }
                        Slider(
                            value = currentCrossfade,
                            onValueChange = { currentCrossfade = it },
                            onValueChangeFinished = { onCrossfadeChange(currentCrossfade.roundToInt()) },
                            valueRange = 0f..12f,
                            steps = 11,
                            modifier = Modifier.padding(top = 4.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    // Gestos Rápidos no Player
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Swipe,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Gestos Rápidos no Player",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Deslize na capa para trocar de música (anterior/próxima)",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = gesturesEnabled,
                            onCheckedChange = onGesturesChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }

        // Section: Visual e Interface
        item {
            Spacer(modifier = Modifier.height(24.dp))
            SettingsSectionHeader(title = "Visual e Interface")
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Cores Dinâmicas do Álbum
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ColorLens,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Cores Dinâmicas",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Adaptar o gradiente do player à capa da faixa",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = dynamicColorsEnabled,
                            onCheckedChange = onDynamicColorsChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    // Cor de Destaque
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Cor de Destaque Padrão",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Personalize os botões e detalhes do app",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        accentColors.forEach { (color, label) ->
                            val isSelected = color == currentAccentColor
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onAccentColorSelect(color) }
                                    .padding(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .then(
                                            if (isSelected) {
                                                Modifier.border(3.dp, Color.White, CircleShape)
                                            } else Modifier
                                        )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Estatísticas e Biblioteca
        item {
            Spacer(modifier = Modifier.height(24.dp))
            SettingsSectionHeader(title = "Estatísticas e Biblioteca")
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingsActionRow(
                        icon = Icons.Default.BarChart,
                        title = "Estatísticas Locais",
                        subtitle = "Tempo ouvido, mais reproduzidas, artistas e rankings",
                        tag = "setting_stats",
                        onClick = onOpenStats
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    SettingsActionRow(
                        icon = Icons.Default.Refresh,
                        title = "Atualizar biblioteca",
                        subtitle = "Escanear arquivos de áudio no dispositivo ($totalSongs faixas)",
                        tag = "setting_rescan",
                        onClick = onRefreshScan
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    SettingsActionRow(
                        icon = Icons.Default.GraphicEq,
                        title = "Carregar faixas de exemplo locais",
                        subtitle = "Adiciona músicas offline sintetizadas para teste",
                        tag = "setting_add_demo",
                        onClick = onAddDemoTracks
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Formatos suportados",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "MP3, M4A, AAC, FLAC, WAV, OGG, OPUS",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Section: Dados e Privacidade
        item {
            Spacer(modifier = Modifier.height(24.dp))
            SettingsSectionHeader(title = "Dados e Privacidade")
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingsActionRow(
                        icon = Icons.Default.Delete,
                        title = "Limpar histórico de reprodução",
                        subtitle = "Remove a lista de músicas tocadas recentemente",
                        tag = "setting_clear_history",
                        onClick = onClearHistory
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "100% Offline e Privado",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Sem cadastro, sem anúncios, sem conexão externa, sem rastreadores.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Section: Sobre
        item {
            Spacer(modifier = Modifier.height(24.dp))
            SettingsSectionHeader(title = "Sobre o Sonora")
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Sonora Music Player",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Versão 1.0 • Desenvolvido para amantes de música local com alta fidelidade, autonomia total e zero rastreamento.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun SettingsActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    tag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
