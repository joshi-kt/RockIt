package com.example.rockit.player.service

import android.content.Intent
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.rockit.player.notification.AppNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AppAudioService : MediaSessionService() {

    @Inject
    lateinit var mediaSession : MediaSession

    @Inject
    lateinit var notificationManager: AppNotificationManager

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        notificationManager.startNotificationService(
            mediaSession = mediaSession,
            mediaSessionService = this
        )
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession = mediaSession

    override fun onDestroy() {
        super.onDestroy()
        mediaSession.apply {
            release()
        }
    }
}