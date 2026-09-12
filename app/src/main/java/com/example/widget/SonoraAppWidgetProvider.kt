package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.model.Song
import com.example.playback.PlaybackManager
import com.example.service.MusicPlaybackService

class SonoraAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        val currentSong = PlaybackManager.currentSong.value
        val isPlaying = PlaybackManager.isPlaying.value
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, isPlaying, currentSong, null)
        }
    }

    companion object {
        fun updateAllWidgets(
            context: Context,
            isPlaying: Boolean,
            song: Song?,
            artwork: Bitmap? = null
        ) {
            val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
            val component = ComponentName(context, SonoraAppWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(component)
            if (appWidgetIds.isEmpty()) return

            for (id in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, id, isPlaying, song, artwork)
            }
        }

        private fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            isPlaying: Boolean,
            song: Song?,
            artwork: Bitmap?
        ) {
            val views = RemoteViews(context.packageName, R.layout.sonora_app_widget)

            // Track metadata
            views.setTextViewText(R.id.widget_title, song?.title ?: "Sonora Player")
            views.setTextViewText(R.id.widget_artist, song?.artist ?: "Música Local Offline")

            // Play/Pause icon
            val playPauseIcon = if (isPlaying) R.drawable.ic_widget_pause else R.drawable.ic_widget_play
            views.setImageViewResource(R.id.widget_btn_play_pause, playPauseIcon)

            // Album artwork
            if (artwork != null) {
                views.setImageViewBitmap(R.id.widget_album_art, artwork)
            } else {
                views.setImageViewResource(R.id.widget_album_art, R.drawable.ic_launcher_foreground)
            }

            // Flags for PendingIntents
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            // Click container -> open MainActivity
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("OPEN_FULL_PLAYER", true)
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                flags
            )
            views.setOnClickPendingIntent(R.id.widget_container, openAppPendingIntent)

            // Click Previous
            val prevIntent = Intent(context, MusicPlaybackService::class.java).apply {
                action = MusicPlaybackService.ACTION_PREV
            }
            val prevPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PendingIntent.getForegroundService(context, 1, prevIntent, flags)
            } else {
                PendingIntent.getService(context, 1, prevIntent, flags)
            }
            views.setOnClickPendingIntent(R.id.widget_btn_prev, prevPendingIntent)

            // Click Play/Pause
            val toggleIntent = Intent(context, MusicPlaybackService::class.java).apply {
                action = MusicPlaybackService.ACTION_TOGGLE
            }
            val togglePendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PendingIntent.getForegroundService(context, 2, toggleIntent, flags)
            } else {
                PendingIntent.getService(context, 2, toggleIntent, flags)
            }
            views.setOnClickPendingIntent(R.id.widget_btn_play_pause, togglePendingIntent)

            // Click Next
            val nextIntent = Intent(context, MusicPlaybackService::class.java).apply {
                action = MusicPlaybackService.ACTION_NEXT
            }
            val nextPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PendingIntent.getForegroundService(context, 3, nextIntent, flags)
            } else {
                PendingIntent.getService(context, 3, nextIntent, flags)
            }
            views.setOnClickPendingIntent(R.id.widget_btn_next, nextPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
