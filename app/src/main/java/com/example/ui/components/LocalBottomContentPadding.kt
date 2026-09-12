package com.example.ui.components

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * CompositionLocal providing dynamic bottom content padding for scrollable screens.
 * 230.dp when MiniPlayer is active, 115.dp (half) when MiniPlayer is inactive,
 * allowing all items to be fully scrolled into view without overlap.
 */
val LocalBottomContentPadding = compositionLocalOf<Dp> { 115.dp }
