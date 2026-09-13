package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.app.Activity
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.Song
import com.example.ui.components.AddToPlaylistDialog
import com.example.ui.components.ConfirmDeleteSongDialog
import com.example.ui.components.ConfirmExcludeFolderDialog
import com.example.ui.components.FloatingAcrylicNavBar
import com.example.ui.components.LocalBottomContentPadding
import com.example.ui.components.ManageExcludedFoldersDialog
import com.example.ui.components.MiniPlayer
import com.example.ui.components.SleepTimerDialog
import com.example.ui.components.SongContextMenuBottomSheet
import com.example.ui.components.SongDetailsDialog
import com.example.ui.screens.AlbumDetailScreen
import com.example.ui.screens.ArtistDetailScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.FolderDetailScreen
import com.example.ui.screens.FullPlayerScreen
import com.example.ui.screens.GenreDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PlaylistDetailScreen
import com.example.ui.screens.PlaylistsScreen
import com.example.ui.screens.QueueBottomSheet
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SongListDetailScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SonoraTheme
import com.example.ui.viewmodel.MusicViewModel
import com.example.ui.viewmodel.NavigationTab

class MainActivity : ComponentActivity() {

  companion object {
    const val EXTRA_OPEN_FULL_PLAYER = "extra_open_full_player"
  }

  private val viewModel: MusicViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    handlePlaybackIntent(intent)
    enableEdgeToEdge()

    setContent {
      val isDarkTheme by viewModel.isDarkTheme.collectAsState()
      val accentColor by viewModel.accentColor.collectAsState()

      SonoraTheme(
        darkTheme = isDarkTheme,
        accentColor = accentColor
      ) {
        SonoraApp(viewModel = viewModel)
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handlePlaybackIntent(intent)
  }

  private fun handlePlaybackIntent(intent: Intent?) {
    if (intent?.getBooleanExtra(EXTRA_OPEN_FULL_PLAYER, false) == true) {
      viewModel.showFullPlayer.value = true
    }
  }
}

@Composable
fun SonoraApp(viewModel: MusicViewModel) {
  val context = LocalContext.current

  // State collectors
  val songs by viewModel.allSongs.collectAsState()
  val displaySongs by viewModel.displaySongs.collectAsState()
  val albums by viewModel.albums.collectAsState()
  val artists by viewModel.artists.collectAsState()
  val folders by viewModel.folders.collectAsState()
  val playlists by viewModel.playlists.collectAsState()
  val favoriteSongs by viewModel.favoriteSongs.collectAsState()
  val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
  val recentlyAdded by viewModel.recentlyAdded.collectAsState()
  val activePlaylistSongs by viewModel.activePlaylistSongs.collectAsState()

  // Navigation and sub-views
  val selectedTab by viewModel.selectedTab.collectAsState()
  val selectedLibrarySubTab by viewModel.selectedLibrarySubTab.collectAsState()
  val currentSort by viewModel.sortOrder.collectAsState()

  val activeArtist by viewModel.activeArtistDetail.collectAsState()
  val activeAlbum by viewModel.activeAlbumDetail.collectAsState()
  val activeFolder by viewModel.activeFolderDetail.collectAsState()
  val activePlaylist by viewModel.activePlaylistDetail.collectAsState()
  val activeGenreDetail by viewModel.activeGenreDetail.collectAsState()
  val activeHistoryDetail by viewModel.activeHistoryDetail.collectAsState()
  val activeMostPlayedDetail by viewModel.activeMostPlayedDetail.collectAsState()
  val activeNeverPlayedDetail by viewModel.activeNeverPlayedDetail.collectAsState()
  val showStats by viewModel.showStats.collectAsState()
  val isSearchOpen by viewModel.isSearchOpen.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()

  // Player state
  val currentSong by viewModel.currentSong.collectAsState()
  val isPlaying by viewModel.isPlaying.collectAsState()
  val currentPositionMs by viewModel.currentPositionMs.collectAsState()
  val durationMs by viewModel.durationMs.collectAsState()
  val queue by viewModel.queue.collectAsState()
  val queueIndex by viewModel.queueIndex.collectAsState()
  val shuffleMode by viewModel.shuffleMode.collectAsState()
  val repeatMode by viewModel.repeatMode.collectAsState()

  // Advanced settings & stats
  val crossfadeSeconds by viewModel.crossfadeSeconds.collectAsState()
  val dynamicColorsEnabled by viewModel.dynamicColorsEnabled.collectAsState()
  val visualizerStyle by viewModel.visualizerStyle.collectAsState()
  val gesturesEnabled by viewModel.gesturesEnabled.collectAsState()
  val sleepTimerMode by viewModel.sleepTimerMode.collectAsState()
  val sleepTimerFormatted by viewModel.sleepTimerFormatted.collectAsState()
  val sleepTimerRemainingSeconds by viewModel.sleepTimerRemainingSeconds.collectAsState()
  val mostPlayedHistory by viewModel.mostPlayedHistory.collectAsState()
  val totalPlayCount by viewModel.totalPlayCount.collectAsState()
  val totalDurationPlayed by viewModel.totalDurationPlayed.collectAsState()

  // Modals
  val showFullPlayer by viewModel.showFullPlayer.collectAsState()
  val showQueueSheet by viewModel.showQueueSheet.collectAsState()
  val showSleepTimerDialog by viewModel.showSleepTimerDialog.collectAsState()
  val songForContextMenu by viewModel.songForContextMenu.collectAsState()
  val songForDetails by viewModel.songForDetails.collectAsState()
  val songForAddToPlaylist by viewModel.songForAddToPlaylist.collectAsState()

  // Folder exclusion & Song deletion state
  val excludedFolders by viewModel.excludedFolders.collectAsState()
  val folderToExclude by viewModel.folderToExclude.collectAsState()
  val showManageExcludedFoldersDialog by viewModel.showManageExcludedFoldersDialog.collectAsState()
  val songToDelete by viewModel.songToDelete.collectAsState()

  val deleteSongLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartIntentSenderForResult()
  ) { result ->
    viewModel.onSongDeleteResultReceived(result.resultCode == Activity.RESULT_OK)
  }

  LaunchedEffect(Unit) {
    viewModel.deleteIntentSenderRequest.collect { request ->
      deleteSongLauncher.launch(request)
    }
  }

  // Permissions & Onboarding check
  var hasStoragePermission by remember {
    val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      Manifest.permission.READ_MEDIA_AUDIO
    } else {
      Manifest.permission.READ_EXTERNAL_STORAGE
    }
    mutableStateOf(ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED)
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { perms ->
    val granted = perms.values.any { it }
    hasStoragePermission = granted
    viewModel.refreshLibrary()
  }

  LaunchedEffect(Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        permissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
      }
    }
  }

  // Handle Android back button
  BackHandler(
    enabled = showFullPlayer || showQueueSheet || showSleepTimerDialog || showStats ||
            activeArtist != null || activeAlbum != null || activeFolder != null ||
            activePlaylist != null || activeGenreDetail != null || activeHistoryDetail ||
            activeMostPlayedDetail || activeNeverPlayedDetail || isSearchOpen ||
            folderToExclude != null || showManageExcludedFoldersDialog || songToDelete != null
  ) {
    viewModel.closeDetailViews()
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val isWideScreen = maxWidth >= 600.dp

    if (!hasStoragePermission && songs.isEmpty()) {
      OnboardingScreen(
        onGrantPermissionAndStart = {
          val toRequest = mutableListOf<String>()
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            toRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
            toRequest.add(Manifest.permission.POST_NOTIFICATIONS)
          } else {
            toRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
          }
          permissionLauncher.launch(toRequest.toTypedArray())
        }
      )
    } else {
      val isMiniPlayerActive = currentSong != null && !showFullPlayer
      val dynamicBottomPadding by animateDpAsState(
        targetValue = when {
          isWideScreen -> 24.dp
          isMiniPlayerActive -> 230.dp
          else -> 115.dp
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "bottom_content_padding"
      )

      Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars
      ) { innerPadding ->
        CompositionLocalProvider(LocalBottomContentPadding provides dynamicBottomPadding) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(top = innerPadding.calculateTopPadding())
          ) {
          // Content layer: flows completely behind floating bars to the bottom of the screen
          Row(
            modifier = Modifier.fillMaxSize()
          ) {
          // Adaptive NavigationRail for Tablets / Foldables
          if (isWideScreen) {
            NavigationRail(
              containerColor = MaterialTheme.colorScheme.surface,
              contentColor = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.fillMaxHeight()
            ) {
              val navItems = listOf(
                NavigationTab.HOME to Pair(Icons.Filled.Home, Icons.Outlined.Home),
                NavigationTab.LIBRARY to Pair(Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic),
                NavigationTab.PLAYLISTS to Pair(Icons.Filled.QueueMusic, Icons.Outlined.QueueMusic),
                NavigationTab.FAVORITES to Pair(Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
                NavigationTab.SETTINGS to Pair(Icons.Filled.Settings, Icons.Outlined.Settings)
              )

              navItems.forEach { (tab, icons) ->
                val isSelected = selectedTab == tab
                NavigationRailItem(
                  selected = isSelected,
                  onClick = {
                    viewModel.closeDetailViews()
                    viewModel.selectedTab.value = tab
                  },
                  icon = {
                    Icon(
                      imageVector = if (isSelected) icons.first else icons.second,
                      contentDescription = tab.label
                    )
                  },
                  label = { Text(tab.label) },
                  colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                  )
                )
              }

              Spacer(modifier = Modifier.weight(1f))

              if (currentSong != null && !showFullPlayer) {
                MiniPlayer(
                  song = currentSong!!,
                  isPlaying = isPlaying,
                  currentPositionMs = currentPositionMs,
                  durationMs = durationMs,
                  onPlayPauseClick = { viewModel.togglePlayPause() },
                  onNextClick = { viewModel.playNext() },
                  onClick = { viewModel.showFullPlayer.value = true },
                  modifier = Modifier.padding(8.dp)
                )
              }
            }
          }

          // Main View Content Container
          Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            when {
              isSearchOpen -> {
                SearchScreen(
                  query = searchQuery,
                  onQueryChange = { viewModel.searchQuery.value = it },
                  onClose = {
                    viewModel.isSearchOpen.value = false
                    viewModel.searchQuery.value = ""
                  },
                  songs = displaySongs,
                  artists = artists,
                  albums = albums,
                  currentSongId = currentSong?.id,
                  isPlaying = isPlaying,
                  onSongClick = { viewModel.playSong(it) },
                  onArtistClick = { viewModel.openArtist(it) },
                  onAlbumClick = { viewModel.openAlbum(it) },
                  onFavoriteClick = { viewModel.toggleFavorite(it) },
                  onMoreClick = { viewModel.songForContextMenu.value = it }
                )
              }

              showStats -> {
                StatsScreen(
                  viewModel = viewModel,
                  onBackClick = { viewModel.showStats.value = false }
                )
              }

              activeGenreDetail != null -> {
                GenreDetailScreen(
                  genre = activeGenreDetail!!,
                  songs = songs,
                  currentSongId = currentSong?.id,
                  isPlaying = isPlaying,
                  onBackClick = { viewModel.activeGenreDetail.value = null },
                  onSongClick = { viewModel.playSong(it) },
                  onPlayAll = { shuffle -> viewModel.playSongsByGenre(activeGenreDetail!!, shuffle) },
                  onFavoriteToggle = { viewModel.toggleFavorite(it) },
                  onMoreClick = { viewModel.songForContextMenu.value = it }
                )
              }

              activeHistoryDetail -> {
                SongListDetailScreen(
                  title = "Histórico de Reprodução",
                  subtitle = "${recentlyPlayed.size} faixas recentes",
                  songs = recentlyPlayed,
                  currentSongId = currentSong?.id,
                  isPlaying = isPlaying,
                  onBackClick = { viewModel.activeHistoryDetail.value = false },
                  onSongClick = { viewModel.playSong(it) },
                  onPlayAll = { shuffle -> viewModel.playHistory(shuffle) },
                  onFavoriteToggle = { viewModel.toggleFavorite(it) },
                  onMoreClick = { viewModel.songForContextMenu.value = it }
                )
              }

              activeMostPlayedDetail -> {
                val mostPlayedSongs = mostPlayedHistory.mapNotNull { hist ->
                  songs.find { it.id == hist.songId }
                }
                SongListDetailScreen(
                  title = "Mais Tocadas",
                  subtitle = "${mostPlayedSongs.size} faixas favoritas",
                  songs = mostPlayedSongs,
                  currentSongId = currentSong?.id,
                  isPlaying = isPlaying,
                  onBackClick = { viewModel.activeMostPlayedDetail.value = false },
                  onSongClick = { viewModel.playSong(it) },
                  onPlayAll = { shuffle -> viewModel.playMostPlayed(shuffle) },
                  onFavoriteToggle = { viewModel.toggleFavorite(it) },
                  onMoreClick = { viewModel.songForContextMenu.value = it }
                )
              }

              activeNeverPlayedDetail -> {
                val playedIds = recentlyPlayed.map { it.id }.toSet()
                val neverPlayed = songs.filter { it.id !in playedIds }
                SongListDetailScreen(
                  title = "Nunca Tocadas",
                  subtitle = "${neverPlayed.size} faixas para descobrir",
                  songs = neverPlayed,
                  currentSongId = currentSong?.id,
                  isPlaying = isPlaying,
                  onBackClick = { viewModel.activeNeverPlayedDetail.value = false },
                  onSongClick = { viewModel.playSong(it) },
                  onPlayAll = { shuffle -> viewModel.playNeverPlayed(shuffle) },
                  onFavoriteToggle = { viewModel.toggleFavorite(it) },
                  onMoreClick = { viewModel.songForContextMenu.value = it }
                )
              }

              activeArtist != null -> {
                ArtistDetailScreen(
                  artist = activeArtist!!,
                  songs = songs,
                  currentSongId = currentSong?.id,
                  isPlaying = isPlaying,
                  onBack = { viewModel.activeArtistDetail.value = null },
                  onSongClick = { viewModel.playSong(it) },
                  onPlayAll = {
                    val aSongs = songs.filter { it.artist == activeArtist!!.name }
                    viewModel.playSongList(aSongs, 0)
                  },
                  onShuffle = {
                    val aSongs = songs.filter { it.artist == activeArtist!!.name }
                    viewModel.shuffleSongList(aSongs)
                  },
                  onFavoriteClick = { viewModel.toggleFavorite(it) },
                  onMoreClick = { viewModel.songForContextMenu.value = it }
                )
              }

              activeAlbum != null -> {
                AlbumDetailScreen(
                  album = activeAlbum!!,
                  songs = songs,
                  currentSongId = currentSong?.id,
                  isPlaying = isPlaying,
                  onBack = { viewModel.activeAlbumDetail.value = null },
                  onSongClick = { viewModel.playSong(it) },
                  onPlayAll = {
                    val aSongs = songs.filter { it.album == activeAlbum!!.title }
                    viewModel.playSongList(aSongs, 0)
                  },
                  onShuffle = {
                    val aSongs = songs.filter { it.album == activeAlbum!!.title }
                    viewModel.shuffleSongList(aSongs)
                  },
                  onFavoriteClick = { viewModel.toggleFavorite(it) },
                  onMoreClick = { viewModel.songForContextMenu.value = it }
                )
              }

              activeFolder != null -> {
                FolderDetailScreen(
                  folder = activeFolder!!,
                  songs = songs,
                  currentSongId = currentSong?.id,
                  isPlaying = isPlaying,
                  onBack = { viewModel.activeFolderDetail.value = null },
                  onSongClick = { viewModel.playSong(it) },
                  onPlayAll = {
                    val fSongs = songs.filter { it.folder == activeFolder!!.name }
                    viewModel.playSongList(fSongs, 0)
                  },
                  onShuffle = {
                    val fSongs = songs.filter { it.folder == activeFolder!!.name }
                    viewModel.shuffleSongList(fSongs)
                  },
                  onFavoriteClick = { viewModel.toggleFavorite(it) },
                  onMoreClick = { viewModel.songForContextMenu.value = it },
                  onExcludeFolder = { viewModel.promptExcludeFolder(activeFolder!!.name, activeFolder!!.path) }
                )
              }

              activePlaylist != null -> {
                PlaylistDetailScreen(
                  playlist = activePlaylist!!,
                  songs = activePlaylistSongs,
                  currentSongId = currentSong?.id,
                  isPlaying = isPlaying,
                  onBack = { viewModel.activePlaylistDetail.value = null },
                  onSongClick = { viewModel.playSong(it) },
                  onPlayAll = { viewModel.playSongList(activePlaylistSongs, 0) },
                  onShuffle = { viewModel.shuffleSongList(activePlaylistSongs) },
                  onFavoriteClick = { viewModel.toggleFavorite(it) },
                  onMoreClick = { viewModel.songForContextMenu.value = it },
                  onDeletePlaylist = { viewModel.deletePlaylist(activePlaylist!!.id) }
                )
              }

              else -> {
                when (selectedTab) {
                  NavigationTab.HOME -> {
                    HomeScreen(
                      songs = songs,
                      recentlyPlayed = recentlyPlayed,
                      recentlyAdded = recentlyAdded,
                      artists = artists,
                      albums = albums,
                      playlists = playlists,
                      onSongClick = { viewModel.playSong(it) },
                      onArtistClick = { viewModel.openArtist(it) },
                      onAlbumClick = { viewModel.openAlbum(it) },
                      onPlaylistClick = { viewModel.openPlaylist(it) },
                      onSearchClick = { viewModel.isSearchOpen.value = true },
                      onScanClick = { viewModel.refreshLibrary() },
                      onAddDemoClick = { viewModel.addDemoTracks() }
                    )
                  }

                  NavigationTab.LIBRARY -> {
                    LibraryScreen(
                      songs = displaySongs,
                      albums = albums,
                      artists = artists,
                      folders = folders,
                      favoriteSongs = favoriteSongs,
                      currentSongId = currentSong?.id,
                      isPlaying = isPlaying,
                      selectedSubTab = selectedLibrarySubTab,
                      currentSort = currentSort,
                      onSubTabSelected = { viewModel.selectedLibrarySubTab.value = it },
                      onSortSelected = { viewModel.setSortOrder(it) },
                      onSongClick = { viewModel.playSong(it) },
                      onPlayAllClick = {
                        val list = if (selectedLibrarySubTab == com.example.ui.viewmodel.LibrarySubTab.FAVORITES) favoriteSongs else displaySongs
                        viewModel.playSongList(list, 0)
                      },
                      onShuffleClick = {
                        val list = if (selectedLibrarySubTab == com.example.ui.viewmodel.LibrarySubTab.FAVORITES) favoriteSongs else displaySongs
                        viewModel.shuffleSongList(list)
                      },
                      onFavoriteClick = { viewModel.toggleFavorite(it) },
                      onMoreClick = { viewModel.songForContextMenu.value = it },
                      onAlbumClick = { viewModel.openAlbum(it) },
                      onArtistClick = { viewModel.openArtist(it) },
                      onFolderClick = { viewModel.openFolder(it) },
                      onRefreshScan = { viewModel.refreshLibrary() },
                      onSearchClick = { viewModel.isSearchOpen.value = true },
                      excludedFoldersCount = excludedFolders.size,
                      onExcludeFolder = { folder -> viewModel.promptExcludeFolder(folder.name, folder.path) },
                      onManageExcludedFolders = { viewModel.showManageExcludedFoldersDialog.value = true }
                    )
                  }

                  NavigationTab.PLAYLISTS -> {
                    PlaylistsScreen(
                      playlists = playlists,
                      onPlaylistClick = { viewModel.openPlaylist(it) },
                      onCreatePlaylist = { name -> viewModel.createPlaylist(name) },
                      onDeletePlaylist = { id -> viewModel.deletePlaylist(id) },
                      onRenamePlaylist = { id, name -> viewModel.renamePlaylist(id, name) }
                    )
                  }

                  NavigationTab.FAVORITES -> {
                    FavoritesScreen(
                      favoriteSongs = favoriteSongs,
                      currentSongId = currentSong?.id,
                      isPlaying = isPlaying,
                      onSongClick = { viewModel.playSong(it) },
                      onPlayAllClick = { viewModel.playSongList(favoriteSongs, 0) },
                      onShuffleClick = { viewModel.shuffleSongList(favoriteSongs) },
                      onFavoriteClick = { viewModel.toggleFavorite(it) },
                      onMoreClick = { viewModel.songForContextMenu.value = it },
                      onExploreClick = { viewModel.selectedTab.value = NavigationTab.LIBRARY }
                    )
                  }

                  NavigationTab.SETTINGS -> {
                    val accent by viewModel.accentColor.collectAsState()

                    SettingsScreen(
                      totalSongs = songs.size,
                      currentAccentColor = accent,
                      crossfadeSeconds = crossfadeSeconds,
                      dynamicColorsEnabled = dynamicColorsEnabled,
                      visualizerStyle = visualizerStyle,
                      gesturesEnabled = gesturesEnabled,
                      sleepTimerMode = sleepTimerMode,
                      sleepTimerFormatted = sleepTimerFormatted,
                      onAccentColorSelect = { viewModel.accentColor.value = it },
                      onCrossfadeChange = { viewModel.setCrossfade(it) },
                      onDynamicColorsChange = { viewModel.setDynamicColors(it) },
                      onVisualizerStyleChange = { viewModel.setVisualizerStyle(it) },
                      onGesturesChange = { viewModel.setGesturesEnabled(it) },
                      onOpenSleepTimer = { viewModel.showSleepTimerDialog.value = true },
                      onOpenStats = { viewModel.showStats.value = true },
                      onRefreshScan = { viewModel.refreshLibrary() },
                      onAddDemoTracks = { viewModel.addDemoTracks() },
                      onClearHistory = { viewModel.clearHistory() },
                      excludedFoldersCount = excludedFolders.size,
                      onManageExcludedFolders = { viewModel.showManageExcludedFoldersDialog.value = true }
                    )
                  }
                }
              }
            }
          }
        }

        // True Floating Overlay for Mini Player & Floating Acrylic Navigation Bar (Mobile)
        if (!isWideScreen) {
          Column(
            modifier = Modifier
              .align(Alignment.BottomCenter)
              .fillMaxWidth()
              .navigationBarsPadding()
              .padding(bottom = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
              // Mini Player
              if (currentSong != null && !showFullPlayer) {
                MiniPlayer(
                  song = currentSong!!,
                  isPlaying = isPlaying,
                  currentPositionMs = currentPositionMs,
                  durationMs = durationMs,
                  onPlayPauseClick = { viewModel.togglePlayPause() },
                  onNextClick = { viewModel.playNext() },
                  onClick = { viewModel.showFullPlayer.value = true }
                )
                Spacer(modifier = Modifier.height(2.dp))
              }

              // Floating Acrylic Navigation Bar
              FloatingAcrylicNavBar(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                  viewModel.closeDetailViews()
                  viewModel.selectedTab.value = tab
                }
              )
            }
          }
        }
        }
      }

      // Full Player Overlay Screen
      AnimatedVisibility(
        visible = showFullPlayer && currentSong != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
      ) {
        FullPlayerScreen(
          song = currentSong,
          isPlaying = isPlaying,
          currentPositionMs = currentPositionMs,
          durationMs = durationMs,
          shuffleMode = shuffleMode,
          repeatMode = repeatMode,
          sleepTimerMode = sleepTimerMode,
          sleepTimerFormatted = sleepTimerFormatted,
          dynamicColorsEnabled = dynamicColorsEnabled,
          visualizerStyle = visualizerStyle,
          gesturesEnabled = gesturesEnabled,
          onBackClick = { viewModel.showFullPlayer.value = false },
          onPlayPauseClick = { viewModel.togglePlayPause() },
          onNextClick = { viewModel.playNext() },
          onPreviousClick = { viewModel.playPrevious() },
          onSeekTo = { viewModel.seekTo(it) },
          onRewind10 = { viewModel.rewind10Seconds() },
          onForward10 = { viewModel.forward10Seconds() },
          onToggleShuffle = { viewModel.toggleShuffle() },
          onToggleRepeat = { viewModel.toggleRepeat() },
          onToggleFavorite = { viewModel.toggleFavorite(it) },
          onOpenQueue = { viewModel.showQueueSheet.value = true },
          onOpenSleepTimer = { viewModel.showSleepTimerDialog.value = true },
          onAddToPlaylist = { viewModel.songForAddToPlaylist.value = it },
          onShowDetails = { viewModel.songForDetails.value = it }
        )
      }

      // Queue Bottom Sheet
      if (showQueueSheet) {
        QueueBottomSheet(
          queue = queue,
          currentIndex = queueIndex,
          onDismiss = { viewModel.showQueueSheet.value = false },
          onSelectTrack = { index -> viewModel.playSongList(queue, index) },
          onRemoveTrack = { index -> viewModel.removeFromQueue(index) },
          onMoveTrack = { from, to -> viewModel.moveQueueItem(from, to) },
          onClearQueue = { viewModel.clearQueue() },
          onShuffleUpcoming = { viewModel.shuffleUpcomingQueue() },
          onSaveQueueAsPlaylist = { name -> viewModel.saveQueueAsPlaylist(name) }
        )
      }

      // Sleep Timer Dialog
      if (showSleepTimerDialog) {
        SleepTimerDialog(
          currentMode = sleepTimerMode,
          remainingFormatted = sleepTimerFormatted,
          onStartDurationTimer = { minutes ->
            viewModel.startSleepTimer(minutes)
            viewModel.showSleepTimerDialog.value = false
          },
          onSetMode = { mode ->
            viewModel.setSleepTimerMode(mode)
            viewModel.showSleepTimerDialog.value = false
          },
          onCancelTimer = {
            viewModel.cancelSleepTimer()
            viewModel.showSleepTimerDialog.value = false
          },
          onDismiss = { viewModel.showSleepTimerDialog.value = false }
        )
      }

      // Context Menu Modal Bottom Sheet
      if (songForContextMenu != null) {
        SongContextMenuBottomSheet(
          song = songForContextMenu!!,
          onDismiss = { viewModel.songForContextMenu.value = null },
          onPlay = { viewModel.playSong(songForContextMenu!!) },
          onPlayNext = { viewModel.playNextInQueue(songForContextMenu!!) },
          onAddToQueue = { viewModel.addToQueue(songForContextMenu!!) },
          onAddToPlaylist = { viewModel.songForAddToPlaylist.value = songForContextMenu },
          onToggleFavorite = { viewModel.toggleFavorite(songForContextMenu!!) },
          onGoToArtist = {
            val artist = artists.find { it.name == songForContextMenu!!.artist }
            if (artist != null) viewModel.openArtist(artist)
          },
          onGoToAlbum = {
            val album = albums.find { it.title == songForContextMenu!!.album }
            if (album != null) viewModel.openAlbum(album)
          },
          onShowDetails = { viewModel.songForDetails.value = songForContextMenu },
          onExcludeFolder = {
            val s = songForContextMenu!!
            val fPath = try {
              java.io.File(s.dataPath).parent ?: s.folder
            } catch (e: Exception) {
              s.folder
            }
            viewModel.promptExcludeFolder(s.folder, fPath)
          },
          onDeleteSong = {
            viewModel.promptDeleteSong(songForContextMenu!!)
          }
        )
      }

      // Song Details Dialog
      if (songForDetails != null) {
        SongDetailsDialog(
          song = songForDetails!!,
          onDismiss = { viewModel.songForDetails.value = null }
        )
      }

      // Add to Playlist Dialog
      if (songForAddToPlaylist != null) {
        AddToPlaylistDialog(
          song = songForAddToPlaylist!!,
          playlists = playlists,
          onDismiss = { viewModel.songForAddToPlaylist.value = null },
          onPlaylistSelected = { playlistId ->
            viewModel.addSongToPlaylist(playlistId, songForAddToPlaylist!!.id)
          },
          onCreateNewPlaylist = {
            viewModel.showCreatePlaylistDialog.value = true
          }
        )
      }

      // Create Playlist Dialog
      if (viewModel.showCreatePlaylistDialog.collectAsState().value) {
        com.example.ui.components.CreatePlaylistDialog(
          onDismiss = { viewModel.showCreatePlaylistDialog.value = false },
          onConfirm = { name ->
            viewModel.createPlaylist(name)
          }
        )
      }

      // Confirm Exclude Folder Dialog
      if (folderToExclude != null) {
        ConfirmExcludeFolderDialog(
          folderName = folderToExclude!!.first,
          folderPath = folderToExclude!!.second,
          onConfirm = { viewModel.confirmExcludeFolder() },
          onDismiss = { viewModel.folderToExclude.value = null }
        )
      }

      // Manage Excluded Folders Dialog
      if (showManageExcludedFoldersDialog) {
        ManageExcludedFoldersDialog(
          excludedFolders = excludedFolders,
          onRestoreFolder = { path -> viewModel.restoreExcludedFolder(path) },
          onClearAll = { viewModel.clearAllExcludedFolders() },
          onDismiss = { viewModel.showManageExcludedFoldersDialog.value = false }
        )
      }

      // Confirm Real Delete Song from Device Dialog
      if (songToDelete != null) {
        ConfirmDeleteSongDialog(
          song = songToDelete!!,
          onConfirm = { viewModel.confirmDeleteSong() },
          onDismiss = { viewModel.songToDelete.value = null }
        )
      }
    }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  MyApplicationTheme { Greeting("Android") }
}
