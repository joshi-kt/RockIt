package com.example.rockit.Utils

import android.util.Log
import androidx.core.text.HtmlCompat
import com.example.rockit.BuildConfig
import com.example.rockit.models.Artist
import java.util.Locale

object Utils {

    const val BASE_URL = "https://saavn.dev/"
    const val NOTIFICATION_ID = 101
    const val NOTIFICATION_CHANNEL_NAME = "CHANNEL_NAME"
    const val NOTIFICATION_CHANNEL_ID = "CHANNEL_ID"
    const val APP_PREFERENCE_NAME = "MUSIC_PREFERENCES"
    const val AUDIO_QUALITY = "AUDIO QUALITY"
    const val KBPS_12 = 0
    const val KBPS_48 = 1
    const val KBPS_96 = 2
    const val KBPS_160 = 3
    const val KBPS_320 = 4
    const val RESTART_APP = "Restart the app to activate audio quality changes"

    fun logger(log : String) {
        if (BuildConfig.DEBUG) {
            Log.d("myapp",log)
        }
    }

    fun parseText(rawText : String) = HtmlCompat.fromHtml(rawText, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()

    fun getArtistName(artists : List<Artist>?) : String {
        if (artists.isNullOrEmpty()) return "Unknown"
        return parseText(artists.joinToString(", ") { artist -> artist.name })
    }

    fun convertTimestampToString(seconds: Float): String {
        val minutes = (seconds / 60).toInt()
        val remainingSeconds = (seconds % 60).toInt()
        return String.format(Locale.US,"%02d:%02d", minutes, remainingSeconds)
    }

}