package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.QueueMusic
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.NavigationTab

@Composable
fun FloatingAcrylicNavBar(
    selectedTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val navItems = listOf(
        NavigationTab.HOME to Pair(Icons.Filled.Home, Icons.Outlined.Home),
        NavigationTab.LIBRARY to Pair(Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic),
        NavigationTab.PLAYLISTS to Pair(Icons.Filled.QueueMusic, Icons.Outlined.QueueMusic),
        NavigationTab.FAVORITES to Pair(Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
        NavigationTab.SETTINGS to Pair(Icons.Filled.Settings, Icons.Outlined.Settings)
    )

    // Solid opaque styling matching Material 3 surface
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f
    val solidSurfaceColor = if (isDark) {
        Color(0xFF1E212B) // Solid opaque dark surface
    } else {
        Color(0xFFF6F7FB) // Solid opaque light surface
    }
    val solidBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                    ambientColor = Color.Black.copy(alpha = 0.35f)
                )
                .border(
                    border = BorderStroke(1.dp, solidBorderColor),
                    shape = CircleShape
                )
                .clip(CircleShape)
                .testTag("floating_acrylic_navbar"),
            color = solidSurfaceColor,
            shape = CircleShape
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEach { (tab, icons) ->
                    val isSelected = selectedTab == tab

                    FloatingNavItem(
                        tab = tab,
                        isSelected = isSelected,
                        selectedIcon = icons.first,
                        unselectedIcon = icons.second,
                        onClick = { onTabSelected(tab) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingNavItem(
    tab: NavigationTab,
    isSelected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "nav_icon_scale"
    )

    val tintColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
        },
        label = "nav_icon_tint"
    )

    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
        } else {
            Color.Transparent
        },
        label = "nav_item_bg"
    )

    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(containerColor)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = false, radius = 26.dp),
                onClick = onClick
            )
            .testTag("nav_${tab.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isSelected) selectedIcon else unselectedIcon,
                contentDescription = tab.label,
                tint = tintColor,
                modifier = Modifier
                    .size(24.dp)
                    .scale(iconScale)
            )

            // Luminous micro-indicator dot when selected
            Spacer(modifier = Modifier.height(3.dp))
            Box(
                modifier = Modifier
                    .size(if (isSelected) 4.dp else 0.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            )
        }
    }
}
