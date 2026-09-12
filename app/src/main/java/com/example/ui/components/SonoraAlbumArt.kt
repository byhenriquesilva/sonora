package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlin.math.absoluteValue

@Composable
fun SonoraAlbumArt(
    albumArtUri: String?,
    seedString: String = "",
    size: Dp = 48.dp,
    cornerRadius: Dp = 8.dp,
    modifier: Modifier = Modifier
) {
    val gradient = remember(seedString) {
        val hash = seedString.hashCode().absoluteValue
        val palettes = listOf(
            listOf(Color(0xFF1E3C72), Color(0xFF2A5298)),
            listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364)),
            listOf(Color(0xFF2C3E50), Color(0xFF4CA1AF)),
            listOf(Color(0xFF134E5E), Color(0xFF71B280)),
            listOf(Color(0xFF3A1C71), Color(0xFFD76D77)),
            listOf(Color(0xFF1F1C2C), Color(0xFF928DAB)),
            listOf(Color(0xFF11998E), Color(0xFF38EF7D)),
            listOf(Color(0xFF000428), Color(0xFF004E92))
        )
        val colors = palettes[hash % palettes.size]
        Brush.linearGradient(colors)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        if (!albumArtUri.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(albumArtUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Capa do álbum",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                error = null
            )
        }
        // Fallback icon shown if image fails or is null
        Icon(
            imageVector = Icons.Default.MusicNote,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(size * 0.45f)
        )
    }
}
