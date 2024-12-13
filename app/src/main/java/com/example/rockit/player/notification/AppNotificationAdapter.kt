package com.example.rockit.player.notification

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerNotificationManager
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.target.Target
import coil3.util.CoilUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@UnstableApi
class AppNotificationAdapter(
    private val context: Context,
    private val pendingIntent: PendingIntent?
) : PlayerNotificationManager.MediaDescriptionAdapter {

    override fun getCurrentContentTitle(player: Player): CharSequence = player.mediaMetadata.displayTitle ?: "Unknown"

    override fun createCurrentContentIntent(player: Player): PendingIntent? = pendingIntent

    override fun getCurrentContentText(player: Player): CharSequence = player.mediaMetadata.artist ?: "Unknown"

    override fun getCurrentLargeIcon(
        player: Player,
        callback: PlayerNotificationManager.BitmapCallback
    ): Bitmap? {
        val request = ImageRequest.Builder(context)
            .data(player.mediaMetadata.artworkUri)
            .target( onSuccess = {
                val bitmap = (it as BitmapDrawable).bitmap
                callback.onBitmap(bitmap)
            })
            .build()
        ImageLoader(context).enqueue(request)
        return null
    }

}