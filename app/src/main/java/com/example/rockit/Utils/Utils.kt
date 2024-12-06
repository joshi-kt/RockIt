package com.example.rockit.Utils

import android.os.Debug
import android.util.Log
import androidx.core.text.HtmlCompat
import com.example.rockit.models.Artist
import com.example.rockit.models.Artists
import com.example.rockit.models.DownloadModel
import com.example.rockit.models.Song
import java.util.Locale

object Utils {

    const val BASE_URL = "https://saavn.dev/"
    const val NOTIFICATION_ID = 101
    const val NOTIFICATION_CHANNEL_NAME = "CHANNEL_NAME"
    const val NOTIFICATION_CHANNEL_ID = "CHANNEL_ID"

    fun logger(log : String) = Log.d("myapp",log)

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