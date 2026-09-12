package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.RepeatMode
import com.example.data.model.SleepTimerMode
import com.example.data.model.Song
import com.example.data.model.VisualizerStyle
import com.example.ui.components.SonoraAlbumArt
import kotlin.math.abs

@Composable
fun FullPlayerScreen(
    song: Song?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    shuffleMode: Boolean,
    repeatMode: RepeatMode,
    sleepTimerMode: SleepTimerMode,
    sleepTimerFormatted: String,
    dynamicColorsEnabled: Boolean,
    visualizerStyle: VisualizerStyle = VisualizerStyle.OFF,
    gesturesEnabled: Boolean,
    onBackClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onRewind10: () -> Unit,
    onForward10: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onOpenQueue: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    onShowDetails: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    if (song == null) return

    var isUserSeeking by remember { mutableStateOf(false) }
    var seekPosition by remember { mutableFloatStateOf(0f) }

    val currentPositionSec = if (isUserSeeking) {
        seekPosition.toLong() / 1000
    } else {
        currentPositionMs / 1000
    }
    val currentFormatted = "%d:%02d".format(currentPositionSec / 60, currentPositionSec % 60)

    val maxSliderValue = durationMs.toFloat().coerceAtLeast(1f)
    val sliderValue = if (isUserSeeking) {
        seekPosition
    } else {
        currentPositionMs.toFloat().coerceIn(0f, maxSliderValue)
    }

    // Dynamic color calculation based on song metadata seed
    val dynamicColor = remember(song.id, dynamicColorsEnabled) {
        if (dynamicColorsEnabled) {
            val hash = abs(song.title.hashCode() + song.artist.hashCode() * 31)
            val hues = listOf(
                Color(0xFF00C9A7), // emerald
                Color(0xFF845EC2), // violet
                Color(0xFF0081CF), // blue
                Color(0xFFFF9671), // coral
                Color(0xFFFF6F91), // magenta
                Color(0xFF00B4D8), // cyan
                Color(0xFFD65DB1)  // orchid
            )
            hues[hash % hues.size]
        } else {
            Color(0xFF00C9A7)
        }
    }

    val animatedAccent by animateColorAsState(targetValue = dynamicColor, label = "dynamic_accent")

    // Gesture swipe tracking
    var totalDragX by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("full_player_screen")
    ) {
        // Solid opaque background layer with dynamic ambient artwork glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D0F17))
        ) {
            if (!song.albumArtUri.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(song.albumArtUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(scaleX = 1.3f, scaleY = 1.3f)
                        .blur(radius = 60.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    animatedAccent.copy(alpha = 0.55f),
                                    Color(0xFF161A29),
                                    Color(0xFF0D0F17)
                                ),
                                radius = 1200f
                            )
                        )
                )
            }

            // Solid high-contrast darkening gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xE608090E),
                                Color(0xF20A0B12),
                                Color(0xFF06070A)
                            )
                        )
                    )
            )
        }

        // Foreground content with insets
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("btn_close_player")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Fechar player",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "REPRODUZINDO AGORA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = animatedAccent
                    )
                    Text(
                        text = song.album,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Sleep Timer button
                    IconButton(
                        onClick = onOpenSleepTimer,
                        modifier = Modifier.testTag("btn_player_sleep_timer")
                    ) {
                        val isTimerActive = sleepTimerMode != SleepTimerMode.OFF
                        if (isTimerActive) {
                            BadgedBox(badge = {
                                Badge(containerColor = animatedAccent) {
                                    Text(sleepTimerFormatted.take(2), fontSize = 9.sp)
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Bedtime,
                                    contentDescription = "Temporizador de sono",
                                    tint = animatedAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Bedtime,
                                contentDescription = "Temporizador de sono",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Details button
                    IconButton(
                        onClick = { onShowDetails(song) },
                        modifier = Modifier.testTag("btn_player_details")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Detalhes",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.12f))

            // Album Art (with gesture swipe support)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .aspectRatio(1f)
                    .shadow(28.dp, RoundedCornerShape(24.dp), ambientColor = animatedAccent.copy(alpha = 0.35f))
                    .then(
                        if (gesturesEnabled) {
                            Modifier.pointerInput(song.id) {
                                detectHorizontalDragGestures(
                                    onDragEnd = {
                                        if (totalDragX < -90f) {
                                            onNextClick()
                                        } else if (totalDragX > 90f) {
                                            onPreviousClick()
                                        }
                                        totalDragX = 0f
                                    },
                                    onDragCancel = { totalDragX = 0f },
                                    onHorizontalDrag = { _, dragAmount ->
                                        totalDragX += dragAmount
                                    }
                                )
                            }
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                SonoraAlbumArt(
                    albumArtUri = song.albumArtUri,
                    seedString = "${song.artist}-${song.title}",
                    size = 340.dp,
                    cornerRadius = 24.dp,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.weight(0.16f))

            // Song Info & Favorite
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (song.isHighQuality) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Card(
                                shape = RoundedCornerShape(4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = animatedAccent.copy(alpha = 0.2f)
                                )
                            ) {
                                Text(
                                    text = "HQ",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = animatedAccent,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = { onToggleFavorite(song) },
                    modifier = Modifier.testTag("btn_player_fav")
                ) {
                    Icon(
                        imageVector = if (song.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (song.isFavorite) "Desfavoritar" else "Favoritar",
                        tint = if (song.isFavorite) animatedAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Slider
            Slider(
                value = sliderValue,
                onValueChange = {
                    isUserSeeking = true
                    seekPosition = it
                },
                onValueChangeFinished = {
                    onSeekTo(seekPosition.toLong())
                    isUserSeeking = false
                },
                valueRange = 0f..maxSliderValue,
                colors = SliderDefaults.colors(
                    thumbColor = animatedAccent,
                    activeTrackColor = animatedAccent,
                    inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("player_progress_slider")
            )

            // Timers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = currentFormatted,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = song.formattedDuration,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Playback Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle
                IconButton(
                    onClick = onToggleShuffle,
                    modifier = Modifier.testTag("btn_player_shuffle")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Embaralhar",
                        tint = if (shuffleMode) animatedAccent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Previous
                IconButton(
                    onClick = onPreviousClick,
                    modifier = Modifier.testTag("btn_player_prev")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Faixa anterior",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Play / Pause FAB
                FloatingActionButton(
                    onClick = onPlayPauseClick,
                    shape = CircleShape,
                    containerColor = animatedAccent,
                    contentColor = Color.Black,
                    modifier = Modifier
                        .size(68.dp)
                        .testTag("btn_player_play_pause")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pausar" else "Reproduzir",
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Next
                IconButton(
                    onClick = onNextClick,
                    modifier = Modifier.testTag("btn_player_next")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Próxima faixa",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Repeat
                IconButton(
                    onClick = onToggleRepeat,
                    modifier = Modifier.testTag("btn_player_repeat")
                ) {
                    val repeatIcon = if (repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat
                    val repeatTint = if (repeatMode != RepeatMode.OFF) animatedAccent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    Icon(
                        imageVector = repeatIcon,
                        contentDescription = "Repetir",
                        tint = repeatTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Secondary Controls (Rewind 10, Fast Forward 10, Playlist, Queue)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onRewind10, modifier = Modifier.testTag("btn_player_rewind10")) {
                    Icon(
                        imageVector = Icons.Default.Replay10,
                        contentDescription = "Retroceder 10 segundos",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(onClick = onForward10, modifier = Modifier.testTag("btn_player_forward10")) {
                    Icon(
                        imageVector = Icons.Default.Forward10,
                        contentDescription = "Avançar 10 segundos",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(onClick = { onAddToPlaylist(song) }, modifier = Modifier.testTag("btn_player_add_playlist")) {
                    Icon(
                        imageVector = Icons.Default.PlaylistAdd,
                        contentDescription = "Adicionar à playlist",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(onClick = onOpenQueue, modifier = Modifier.testTag("btn_player_queue")) {
                    Icon(
                        imageVector = Icons.Default.QueueMusic,
                        contentDescription = "Fila de reprodução",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
