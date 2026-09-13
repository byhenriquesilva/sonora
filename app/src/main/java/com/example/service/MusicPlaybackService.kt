package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media.session.MediaButtonReceiver
import com.example.MainActivity
import com.example.playback.PlaybackManager
import java.io.File

class MusicPlaybackService : Service(), MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    companion object {
        const val CHANNEL_ID = "sonora_playback_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_PLAY_SONG = "com.example.sonora.ACTION_PLAY_SONG"
        const val ACTION_RESUME = "com.example.sonora.ACTION_RESUME"
        const val ACTION_PAUSE = "com.example.sonora.ACTION_PAUSE"
        const val ACTION_TOGGLE = "com.example.sonora.ACTION_TOGGLE"
        const val ACTION_NEXT = "com.example.sonora.ACTION_NEXT"
        const val ACTION_PREV = "com.example.sonora.ACTION_PREV"
        const val ACTION_SEEK = "com.example.sonora.ACTION_SEEK"
        const val ACTION_STOP = "com.example.sonora.ACTION_STOP"

        const val EXTRA_URI = "extra_uri"
        const val EXTRA_DATA_PATH = "extra_data_path"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_ARTIST = "extra_artist"
        const val EXTRA_ALBUM = "extra_album"
        const val EXTRA_DURATION = "extra_duration"
        const val EXTRA_ALBUM_ID = "extra_album_id"
        const val EXTRA_ALBUM_ART_URI = "extra_album_art_uri"
        const val EXTRA_SEEK_MS = "extra_seek_ms"
    }

    private var mediaPlayer: MediaPlayer? = null
    private var audioManager: AudioManager? = null
    private var notificationManager: NotificationManager? = null
    private var mediaSession: MediaSessionCompat? = null
    private var audioFocusRequest: AudioFocusRequest? = null

    private var currentUriStr: String? = null
    private var currentDataPath: String? = null
    private var currentAlbumArtUriStr: String? = null
    private var currentTitle: String = "Sonora"
    private var currentArtist: String = "Música local"
    private var currentAlbum: String = "Sonora"
    private var currentDurationMs: Long = 0L
    private var currentPositionMs: Long = 0L

    private var isPlayingState: Boolean = false
    private var wasPlayingBeforeFocusLoss: Boolean = false
    private var isNoisyReceiverRegistered: Boolean = false
    private var hasAudioFocus: Boolean = false

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                hasAudioFocus = false
                wasPlayingBeforeFocusLoss = false
                PlaybackManager.pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                hasAudioFocus = false
                wasPlayingBeforeFocusLoss = isPlayingState
                PlaybackManager.pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                mediaPlayer?.setVolume(0.2f, 0.2f)
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                hasAudioFocus = true
                mediaPlayer?.setVolume(1.0f, 1.0f)
                if (wasPlayingBeforeFocusLoss) {
                    wasPlayingBeforeFocusLoss = false
                    PlaybackManager.resume()
                }
            }
        }
    }

    private var cachedArtwork: Bitmap? = null
    private var cachedArtworkKey: String? = null

    // Broadcast receiver for headphones disconnected / Bluetooth disconnect
    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                PlaybackManager.pause()
            }
        }
    }

    // MediaSession callbacks to handle lockscreen, notification & Bluetooth hardware buttons
    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {
        override fun onPlay() {
            PlaybackManager.resume()
        }

        override fun onPause() {
            PlaybackManager.pause()
        }

        override fun onSkipToNext() {
            PlaybackManager.playNext()
        }

        override fun onSkipToPrevious() {
            PlaybackManager.playPrevious()
        }

        override fun onSeekTo(pos: Long) {
            PlaybackManager.seekTo(pos)
        }

        override fun onFastForward() {
            PlaybackManager.forward10Seconds()
        }

        override fun onRewind() {
            PlaybackManager.rewind10Seconds()
        }

        override fun onStop() {
            PlaybackManager.clearQueue()
        }

        override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
            val keyEvent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
            } else {
                @Suppress("DEPRECATION")
                mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
            }
            if (keyEvent != null && keyEvent.action == KeyEvent.ACTION_DOWN) {
                when (keyEvent.keyCode) {
                    KeyEvent.KEYCODE_MEDIA_PLAY -> {
                        PlaybackManager.resume()
                        return true
                    }
                    KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                        PlaybackManager.pause()
                        return true
                    }
                    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_HEADSETHOOK -> {
                        PlaybackManager.togglePlayPause()
                        return true
                    }
                    KeyEvent.KEYCODE_MEDIA_NEXT -> {
                        PlaybackManager.playNext()
                        return true
                    }
                    KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                        PlaybackManager.playPrevious()
                        return true
                    }
                    KeyEvent.KEYCODE_MEDIA_STOP -> {
                        PlaybackManager.clearQueue()
                        return true
                    }
                }
            }
            return super.onMediaButtonEvent(mediaButtonIntent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        initMediaSession()
        createNotificationChannel()
        registerNoisyReceiver()
    }

    private fun initMediaSession() {
        val mediaButtonReceiver = ComponentName(this, MediaButtonReceiver::class.java)
        mediaSession = MediaSessionCompat(this, "SonoraSession", mediaButtonReceiver, null).apply {
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )

            val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON, null, this@MusicPlaybackService, MediaButtonReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                this@MusicPlaybackService,
                0,
                mediaButtonIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            setMediaButtonReceiver(pendingIntent)
            setCallback(mediaSessionCallback)
            isActive = true
        }
    }

    private fun registerNoisyReceiver() {
        if (!isNoisyReceiverRegistered) {
            val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.registerReceiver(this, noisyReceiver, filter, ContextCompat.RECEIVER_EXPORTED)
            } else {
                registerReceiver(noisyReceiver, filter)
            }
            isNoisyReceiverRegistered = true
        }
    }

    private fun unregisterNoisyReceiver() {
        if (isNoisyReceiverRegistered) {
            try {
                unregisterReceiver(noisyReceiver)
            } catch (e: Exception) {
                // Ignore
            }
            isNoisyReceiverRegistered = false
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == Intent.ACTION_MEDIA_BUTTON) {
            MediaButtonReceiver.handleIntent(mediaSession, intent)
        }

        val action = intent?.action ?: return START_NOT_STICKY

        // Extract metadata if available
        intent.getStringExtra(EXTRA_TITLE)?.let { currentTitle = it }
        intent.getStringExtra(EXTRA_ARTIST)?.let { currentArtist = it }
        intent.getStringExtra(EXTRA_ALBUM)?.let { currentAlbum = it }
        intent.getStringExtra(EXTRA_DATA_PATH)?.let { currentDataPath = it }
        intent.getStringExtra(EXTRA_ALBUM_ART_URI)?.let { currentAlbumArtUriStr = it }
        val duration = intent.getLongExtra(EXTRA_DURATION, 0L)
        if (duration > 0) currentDurationMs = duration

        when (action) {
            ACTION_PLAY_SONG -> {
                val uriStr = intent.getStringExtra(EXTRA_URI)
                currentUriStr = uriStr
                if (!uriStr.isNullOrBlank()) {
                    playUri(uriStr)
                }
            }
            ACTION_RESUME -> {
                resumePlayback()
            }
            ACTION_PAUSE -> {
                pausePlayback()
            }
            ACTION_TOGGLE -> {
                if (mediaPlayer?.isPlaying == true) {
                    pausePlayback()
                } else {
                    resumePlayback()
                }
            }
            ACTION_NEXT -> {
                PlaybackManager.playNext()
            }
            ACTION_PREV -> {
                PlaybackManager.playPrevious()
            }
            ACTION_SEEK -> {
                val seekMs = intent.getLongExtra(EXTRA_SEEK_MS, 0L)
                currentPositionMs = seekMs
                mediaPlayer?.seekTo(seekMs.toInt())
                updatePlaybackState(isPlayingState, seekMs)
            }
            ACTION_STOP -> {
                stopPlayback()
            }
        }

        return START_NOT_STICKY
    }

    private fun playUri(uriStr: String) {
        requestAudioFocus()
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
                    setOnCompletionListener(this@MusicPlaybackService)
                    setOnErrorListener(this@MusicPlaybackService)
                }
            } else {
                mediaPlayer?.reset()
            }

            val uri = Uri.parse(uriStr)
            if (uri.scheme == "content") {
                mediaPlayer?.setDataSource(applicationContext, uri)
            } else if (uri.scheme == "file" || uriStr.startsWith("/")) {
                val path = if (uri.scheme == "file") uri.path ?: uriStr else uriStr
                val file = File(path)
                if (file.exists()) {
                    mediaPlayer?.setDataSource(file.absolutePath)
                } else {
                    mediaPlayer?.setDataSource(applicationContext, uri)
                }
            } else {
                mediaPlayer?.setDataSource(applicationContext, uri)
            }

            mediaPlayer?.prepareAsync()
            mediaPlayer?.setOnPreparedListener { mp ->
                applyFadeIn(mp)
                mp.start()
                isPlayingState = true
                currentPositionMs = 0L
                val dur = mp.duration.toLong()
                if (dur > 0) currentDurationMs = dur

                PlaybackManager.updatePlaybackState(true)
                PlaybackManager.updatePlaybackPosition(0, currentDurationMs)

                updateMetadata()
                updatePlaybackState(true, 0)
                startForegroundNotification()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            PlaybackManager.updatePlaybackState(false)
        }
    }

    private fun applyFadeIn(mp: MediaPlayer) {
        val crossfadeSecs = PlaybackManager.crossfadeSeconds.value
        if (crossfadeSecs <= 0) {
            mp.setVolume(1.0f, 1.0f)
            return
        }
        val durationMs = (crossfadeSecs.coerceAtMost(3) * 1000L).coerceAtLeast(600L)
        val steps = 15
        val stepDelay = durationMs / steps
        mp.setVolume(0.05f, 0.05f)
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        for (i in 1..steps) {
            handler.postDelayed({
                val vol = (i.toFloat() / steps.toFloat()).coerceIn(0f, 1f)
                try {
                    if (mediaPlayer == mp && isPlayingState) {
                        mp.setVolume(vol, vol)
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }, i * stepDelay)
        }
    }

    private fun resumePlayback() {
        requestAudioFocus()
        mediaPlayer?.let { mp ->
            if (!mp.isPlaying) {
                mp.start()
                isPlayingState = true
                currentPositionMs = mp.currentPosition.toLong()
                PlaybackManager.updatePlaybackState(true)

                updateMetadata()
                updatePlaybackState(true, currentPositionMs)
                startForegroundNotification()
            }
        } ?: run {
            currentUriStr?.let { playUri(it) }
        }
    }

    private fun pausePlayback() {
        mediaPlayer?.let { mp ->
            if (mp.isPlaying) {
                mp.pause()
            }
            isPlayingState = false
            currentPositionMs = mp.currentPosition.toLong()
            PlaybackManager.updatePlaybackState(false)

            updatePlaybackState(false, currentPositionMs)
            updateNotificationOnPause()
        }
    }

    private fun stopPlayback() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaPlayer = null
        isPlayingState = false
        abandonAudioFocus()
        PlaybackManager.updatePlaybackState(false)
        updatePlaybackState(false, 0)
        try {
            com.example.widget.SonoraAppWidgetProvider.updateAllWidgets(applicationContext, false, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
            notificationManager?.cancel(NOTIFICATION_ID)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        stopSelf()
    }

    override fun onCompletion(mp: MediaPlayer?) {
        isPlayingState = false
        updatePlaybackState(false, currentDurationMs)
        PlaybackManager.onPlaybackCompleted()
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        isPlayingState = false
        PlaybackManager.updatePlaybackState(false)
        updatePlaybackState(false, 0)
        return true
    }

    private fun requestAudioFocus(): Boolean {
        if (hasAudioFocus) return true
        val res = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (audioFocusRequest == null) {
                val playbackAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()

                audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(audioFocusChangeListener)
                    .build()
            }
            audioManager?.requestAudioFocus(audioFocusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            audioManager?.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
        hasAudioFocus = (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
        return hasAudioFocus
    }

    private fun abandonAudioFocus() {
        if (!hasAudioFocus) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
            } else {
                @Suppress("DEPRECATION")
                audioManager?.abandonAudioFocus(audioFocusChangeListener)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        hasAudioFocus = false
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Reprodução de Música",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controles de reprodução de música, notificação nativa e tela de bloqueio"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun updateMetadata() {
        val artwork = getOrLoadArtwork()

        val metaBuilder = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentTitle)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currentArtist)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, currentAlbum)
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, currentTitle)
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, currentArtist)
            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, currentAlbum)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, currentDurationMs)

        if (artwork != null) {
            metaBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, artwork)
            metaBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, artwork)
        }

        mediaSession?.setMetadata(metaBuilder.build())
    }

    private fun updatePlaybackState(playing: Boolean, position: Long) {
        val state = if (playing) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
        val speed = if (playing) 1.0f else 0.0f

        val actions = PlaybackStateCompat.ACTION_PLAY or
            PlaybackStateCompat.ACTION_PAUSE or
            PlaybackStateCompat.ACTION_PLAY_PAUSE or
            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
            PlaybackStateCompat.ACTION_SEEK_TO or
            PlaybackStateCompat.ACTION_STOP or
            PlaybackStateCompat.ACTION_FAST_FORWARD or
            PlaybackStateCompat.ACTION_REWIND

        val stateCompat = PlaybackStateCompat.Builder()
            .setActions(actions)
            .setState(state, position, speed, SystemClock.elapsedRealtime())
            .build()

        mediaSession?.setPlaybackState(stateCompat)
    }

    private fun startForegroundNotification() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        com.example.widget.SonoraAppWidgetProvider.updateAllWidgets(
            applicationContext,
            true,
            PlaybackManager.currentSong.value,
            cachedArtwork
        )
    }

    private fun updateNotificationOnPause() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_DETACH)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(false)
        }
        notificationManager?.notify(NOTIFICATION_ID, notification)
        com.example.widget.SonoraAppWidgetProvider.updateAllWidgets(
            applicationContext,
            false,
            PlaybackManager.currentSong.value,
            cachedArtwork
        )
    }

    private fun buildNotification(): Notification {
        // Intent to open directly to Full Player Screen
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_OPEN_FULL_PLAYER, true)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, MusicPlaybackService::class.java).apply { action = ACTION_PREV },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val toggleIntent = PendingIntent.getService(
            this,
            2,
            Intent(this, MusicPlaybackService::class.java).apply { action = ACTION_TOGGLE },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextIntent = PendingIntent.getService(
            this,
            3,
            Intent(this, MusicPlaybackService::class.java).apply { action = ACTION_NEXT },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this,
            4,
            Intent(this, MusicPlaybackService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIcon = if (isPlayingState) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }

        val playPauseTitle = if (isPlayingState) "Pausar" else "Reproduzir"
        val artwork = getOrLoadArtwork()

        val mediaStyle = MediaStyle()
            .setMediaSession(mediaSession?.sessionToken)
            .setShowActionsInCompactView(0, 1, 2)
            .setShowCancelButton(true)
            .setCancelButtonIntent(stopIntent)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setStyle(mediaStyle)
            .setContentTitle(currentTitle)
            .setContentText(currentArtist)
            .setSubText(if (currentAlbum.isNotBlank()) currentAlbum else "Sonora")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setLargeIcon(artwork)
            .setContentIntent(contentPendingIntent)
            .setDeleteIntent(stopIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(isPlayingState)
            .addAction(android.R.drawable.ic_media_previous, "Anterior", prevIntent)
            .addAction(playPauseIcon, playPauseTitle, toggleIntent)
            .addAction(android.R.drawable.ic_media_next, "Próxima", nextIntent)
            .build()
    }

    private fun getOrLoadArtwork(): Bitmap {
        val key = "$currentTitle-$currentArtist-$currentAlbum"
        if (cachedArtwork != null && cachedArtworkKey == key) {
            return cachedArtwork!!
        }

        var bitmap: Bitmap? = null

        // 1. Try ContentResolver albumart Uri
        if (!currentAlbumArtUriStr.isNullOrBlank()) {
            try {
                contentResolver.openInputStream(Uri.parse(currentAlbumArtUriStr))?.use { stream ->
                    val options = BitmapFactory.Options().apply { inSampleSize = 1 }
                    bitmap = BitmapFactory.decodeStream(stream, null, options)
                }
            } catch (e: Exception) {
                // Ignore fallback to retriever
            }
        }

        // 2. Try MediaMetadataRetriever for embedded picture
        if (bitmap == null) {
            try {
                val retriever = MediaMetadataRetriever()
                if (!currentDataPath.isNullOrBlank() && File(currentDataPath!!).exists()) {
                    retriever.setDataSource(currentDataPath)
                } else if (!currentUriStr.isNullOrBlank()) {
                    retriever.setDataSource(this, Uri.parse(currentUriStr))
                }
                val rawBytes = retriever.embeddedPicture
                retriever.release()
                if (rawBytes != null && rawBytes.isNotEmpty()) {
                    bitmap = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size)
                }
            } catch (e: Exception) {
                // Ignore fallback to styled generator
            }
        }

        // 3. Fallback: Generate a stylized local artwork bitmap (512x512)
        if (bitmap == null) {
            bitmap = generateFallbackArtwork(currentTitle, currentArtist)
        }

        cachedArtwork = bitmap
        cachedArtworkKey = key
        return bitmap!!
    }

    private fun generateFallbackArtwork(title: String, artist: String): Bitmap {
        val size = 512
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Seeded gradient colors based on text
        val hash = (title.hashCode() xor artist.hashCode())
        val hue = (hash and 0xFFFF) % 360f

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val startColor = android.graphics.Color.HSVToColor(floatArrayOf(hue, 0.75f, 0.45f))
        val endColor = android.graphics.Color.HSVToColor(floatArrayOf((hue + 45f) % 360f, 0.85f, 0.15f))

        paint.shader = LinearGradient(
            0f, 0f, size.toFloat(), size.toFloat(),
            startColor, endColor,
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)
        paint.shader = null

        // Decorative vinyl record circle
        paint.color = android.graphics.Color.argb(40, 255, 255, 255)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6f
        canvas.drawCircle(size / 2f, size / 2f, size * 0.35f, paint)
        canvas.drawCircle(size / 2f, size / 2f, size * 0.22f, paint)

        // Center vinyl core
        paint.style = Paint.Style.FILL
        paint.color = android.graphics.Color.argb(120, 16, 16, 24)
        canvas.drawCircle(size / 2f, size / 2f, size * 0.14f, paint)

        // Title initial in the center
        paint.color = android.graphics.Color.WHITE
        paint.textSize = size * 0.14f
        paint.textAlign = Paint.Align.CENTER
        paint.isFakeBoldText = true

        val initial = if (title.isNotBlank()) title.take(1).uppercase() else "♪"
        val yPos = (size / 2f) - ((paint.descent() + paint.ascent()) / 2f)
        canvas.drawText(initial, size / 2f, yPos, paint)

        return bitmap
    }

    override fun onDestroy() {
        super.onDestroy()
        abandonAudioFocus()
        unregisterNoisyReceiver()
        mediaPlayer?.release()
        mediaPlayer = null
        mediaSession?.isActive = false
        mediaSession?.release()
        mediaSession = null
        cachedArtwork?.recycle()
        cachedArtwork = null
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
