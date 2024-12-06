package com.example.rockit.player.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import com.example.rockit.R
import com.example.rockit.Utils.Utils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AppNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val exoPlayer: ExoPlayer
) {

    private val notificationManager : NotificationManagerCompat = NotificationManagerCompat.from(context)

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val notificationChannel = NotificationChannel(
            Utils.NOTIFICATION_CHANNEL_ID,
            Utils.NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(notificationChannel)
    }

    fun startNotificationService(
        mediaSessionService: MediaSessionService,
        mediaSession: MediaSession
    ) {
        buildNotification(mediaSession)
        startForegroundService(mediaSessionService)
    }

    private fun startForegroundService(mediaSessionService: MediaSessionService) {
        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, Utils.NOTIFICATION_CHANNEL_ID)
        } else {
            Notification.Builder(context)
        }
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()

        mediaSessionService.startForeground(Utils.NOTIFICATION_ID, notification)
    }

    @OptIn(UnstableApi::class)
    private fun buildNotification(mediaSession: MediaSession) {
        PlayerNotificationManager.Builder(
            context,
            Utils.NOTIFICATION_ID,
            Utils.NOTIFICATION_CHANNEL_ID
        )
            .setSmallIconResourceId(R.mipmap.ic_launcher)
            .setMediaDescriptionAdapter(
                AppNotificationAdapter(
                    context = context,
                    pendingIntent = mediaSession.sessionActivity
                )
            )
            .build()
            .also {
                it.setMediaSessionToken(mediaSession.platformToken)
                it.setUseRewindActionInCompactView(true)
                it.setUseNextActionInCompactView(true)
                it.setPriority(NotificationCompat.PRIORITY_LOW)
                it.setPlayer(exoPlayer)
            }
    }

}